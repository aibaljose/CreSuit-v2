// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.ai;

import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.server.OdeAuthFilter;
import com.google.appinventor.server.OdeServlet;
import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;
import com.google.appinventor.server.project.youngandroid.YoungAndroidSettingsBuilder;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidYailNode;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet endpoint for creating an entire App Inventor project with AI,
 * generating both complete UI components (.scm) and functional Blockly blocks (.bky).
 */
public class AiProjectServlet extends OdeServlet {
  private static final Logger LOG = Logger.getLogger(AiProjectServlet.class.getName());
  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("application/json; charset=UTF-8");
    PrintWriter out = resp.getWriter();

    String userId = userInfoProvider.getUserId();
    if (userId == null || userId.isEmpty()) {
      OdeAuthFilter.UserInfo uInfo = OdeAuthFilter.getUserInfo(req);
      if (uInfo != null) {
        userId = uInfo.getUserId();
      }
    }

    if (userId == null || userId.isEmpty()) {
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      JSONObject err = new JSONObject();
      err.put("success", false);
      err.put("error", "User is not logged in");
      out.write(err.toString());
      return;
    }

    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader = req.getReader()) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    }

    String prompt = "";
    String requestedName = "";
    try {
      JSONObject input = new JSONObject(sb.toString());
      prompt = input.optString("prompt", "").trim();
      requestedName = input.optString("projectName", "").trim();
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      JSONObject err = new JSONObject();
      err.put("success", false);
      err.put("error", "Invalid JSON payload: " + e.getMessage());
      out.write(err.toString());
      return;
    }

    if (prompt.isEmpty()) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      JSONObject err = new JSONObject();
      err.put("success", false);
      err.put("error", "Please provide a prompt describing your project idea.");
      out.write(err.toString());
      return;
    }

    // Determine and sanitize project name
    String projectName = requestedName;
    if (projectName.isEmpty()) {
      projectName = deriveProjectName(prompt);
    }

    projectName = projectName.replaceAll("[^A-Za-z0-9_]", "");
    if (projectName.isEmpty() || !Character.isLetter(projectName.charAt(0))) {
      projectName = "AiApp_" + projectName;
    }
    if (projectName.length() > 30) {
      projectName = projectName.substring(0, 30);
    }

    // Ensure project name is unique for this user
    List<String> existingNames = storageIo.getProjectNames(userId);
    String baseName = projectName;
    int suffix = 1;
    while (existingNames.contains(projectName)) {
      projectName = baseName + "_" + suffix++;
    }

    try {
      String userEmail = storageIo.getUser(userId).getUserEmail();
      String qualifiedFormName = StringUtils.getQualifiedFormName(userEmail, projectName);
      final int lastDotPos = qualifiedFormName.lastIndexOf('.');
      String packageName = qualifiedFormName.split("\\.")[2];
      String formName = qualifiedFormName.substring(lastDotPos + 1);

      LOG.info("Creating AI project '" + projectName + "' for user " + userId + " with prompt: " + prompt);

      GeminiClient geminiClient = new GeminiClient();
      JSONObject rawAiResponse = geminiClient.generateScreenAndBlocks(formName, prompt);
      AIResponseSanitizer.SanitizedResult result = AIResponseSanitizer.sanitize(rawAiResponse, formName, packageName);

      String formFileContents = result.getScmContent();
      String blocklyFileContents = result.getBkyContent();
      if (blocklyFileContents == null || blocklyFileContents.trim().isEmpty() || !blocklyFileContents.contains("<block")) {
        blocklyFileContents = "";
      }

      YoungAndroidSettingsBuilder settingsBuilder = new YoungAndroidSettingsBuilder()
          .setProjectName(projectName)
          .setQualifiedFormName(qualifiedFormName);
      String propertiesFileContents = settingsBuilder.toProperties();

      String formFileName = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
      String blocklyFileName = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);
      String yailFileName = YoungAndroidYailNode.getYailFileId(qualifiedFormName);

      Project project = new Project(projectName);
      project.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
      project.addTextFile(new TextFile(YoungAndroidProjectService.PROJECT_PROPERTIES_FILE_NAME, propertiesFileContents));
      project.addTextFile(new TextFile(formFileName, formFileContents));
      project.addTextFile(new TextFile(blocklyFileName, blocklyFileContents));
      project.addTextFile(new TextFile(yailFileName, ""));

      long projectId = storageIo.createProject(userId, project, settingsBuilder.build());
      UserProject userProject = storageIo.getUserProject(userId, projectId);

      UploadResponse uploadResp = new UploadResponse(UploadResponse.Status.SUCCESS, 0, userProject.toString());

      JSONObject successResp = new JSONObject();
      successResp.put("success", true);
      successResp.put("projectId", projectId);
      successResp.put("projectName", projectName);
      successResp.put("userProject", userProject.toString());
      successResp.put("uploadResponseHtml", uploadResp.formatAsHtml());

      LOG.info("Successfully created AI project id=" + projectId + " name=" + projectName);

      resp.setStatus(HttpServletResponse.SC_OK);
      out.write(successResp.toString());

    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Failed to create project with AI for user: " + userId, e);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      JSONObject err = new JSONObject();
      err.put("success", false);
      err.put("error", "AI Project Creation failed: " + e.getMessage());
      out.write(err.toString());
    }
  }

  private String deriveProjectName(String prompt) {
    String lower = prompt.toLowerCase();
    if (lower.contains("calculator") || lower.contains("calculate")) return "SmartCalc";
    if (lower.contains("todo") || lower.contains("task") || lower.contains("notes")) return "TaskManager";
    if (lower.contains("quiz") || lower.contains("trivia") || lower.contains("game")) return "QuizMaster";
    if (lower.contains("market") || lower.contains("store") || lower.contains("shop")) return "MarketplaceApp";
    if (lower.contains("weather")) return "WeatherPulse";
    if (lower.contains("fitness") || lower.contains("workout") || lower.contains("health")) return "FitnessPro";
    if (lower.contains("chat") || lower.contains("message")) return "ChatApp";
    if (lower.contains("finance") || lower.contains("expense") || lower.contains("budget")) return "ExpenseTracker";
    if (lower.contains("saas")) return "SaaSPlatform";
    return "AiApp_" + (System.currentTimeMillis() % 10000);
  }
}
