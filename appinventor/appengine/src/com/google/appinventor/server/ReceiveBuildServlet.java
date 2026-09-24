// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.project.utils.Security;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.io.ByteStreams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for receiving build output files and build error information from a Build Server.
 *
 * <p>This needs to be done from a servlet that does not require login because
 * posts from the Build Server do not contain login information. To ensure
 * safety they contain an encrypted user and project ID as part of
 * their URL.
 *
 * @author markf@google.com (Mark Friedman)
 */
public class ReceiveBuildServlet extends OdeServlet {

  // Logging support
  private static final Logger LOG = Logger.getLogger(ReceiveBuildServlet.class.getName());

  private final OdeAuthFilter odeFilter = new OdeAuthFilter();
  private final transient StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // URIs for receivebuild requests are structured as follows:
    //   /<baseurl>/receivebuild/encryptedUserAndProjectId/fileId
    String uriComponents[] = req.getRequestURI().split("/", 5);

    // TODO(lizlooney,user) If the URI doesn't contain enough components, the following lines
    // will throw an ArrayIndexOutOfBoundsException. We could deal with that outcome more cleanly
    // by returning an HTTP error code. This applies to all of our servlets.

    String userId;
    long projectId;
    try {
      userId = Security.decryptUserId(uriComponents[3]);
      projectId = Security.decryptProjectId(uriComponents[3]);
    } catch (EncryptionException e) {
      throw CrashReport.createAndLogError(LOG, req, null, e);
    }

    // Set the user in the OdeFilter, which is used everywhere as the UserInfoProvider.
    odeFilter.setUserFromUserId(userId, false, false, 0, null, null);
    try {
      String buildFileDirPath = uriComponents[4];
      ZipInputStream zipInputStream = new ZipInputStream(req.getInputStream());
      while (true) {
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        if (zipEntry == null) {
          break;
        }
        String fileName = zipEntry.getName();
        byte[] fileBytes = ByteStreams.toByteArray(zipInputStream);
        if (StorageUtil.ANDROID_KEYSTORE_FILENAME.equals(fileName)) {
          LOG.info("Saving android.keystore for user: " + userId);
          storageIo.addFilesToUser(userId, StorageUtil.ANDROID_KEYSTORE_FILENAME);
          storageIo.uploadRawUserFile(userId, fileName, fileBytes);
        } else if (fileName.equals("build.status")) {
          int progress = Integer.parseInt((new String(fileBytes)).trim());
          LOG.info("Received a build.status file contents = " + progress);
          storageIo.storeBuildStatus(userId, projectId, progress);
        } else {
          String filePath = buildFileDirPath + "/" + fileName;
          LOG.info("Saving build output files: " + filePath);
          storageIo.addOutputFilesToProject(userId, projectId, filePath);
          storageIo.uploadRawFileForce(projectId, filePath, userId, fileBytes);
          storageIo.storeBuildStatus(userId, projectId, 0); // Reset for the next build
          if (fileName.endsWith(".apk") || fileName.endsWith(".aab") || fileName.endsWith(".ipa")) {
            archiveBuildVersion(userId, projectId, fileName, fileBytes);
          }
        }
      }
    } finally {
      odeFilter.removeUser();
    }
  }

  private void archiveBuildVersion(String userId, long projectId, String fileName, byte[] fileBytes) {
    try {
      String settingsStr = storageIo.loadProjectSettings(userId, projectId);
      String versionCode = "1";
      String versionName = "1.0";
      int buildNumber = 1;
      JSONObject settingsObj = null;
      JSONObject simpleSettings = null;
      if (settingsStr != null && !settingsStr.trim().isEmpty()) {
        try {
          settingsObj = new JSONObject(settingsStr);
          simpleSettings = settingsObj.optJSONObject(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS);
          if (simpleSettings != null) {
            if (simpleSettings.has(SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE)) {
              String vc = simpleSettings.optString(SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE);
              if (vc != null && !vc.trim().isEmpty()) {
                versionCode = vc.trim();
              }
            }
            if (simpleSettings.has(SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME)) {
              String vn = simpleSettings.optString(SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME);
              if (vn != null && !vn.trim().isEmpty()) {
                versionName = vn.trim();
              }
            }
            if (simpleSettings.has(SettingsConstants.YOUNG_ANDROID_SETTINGS_BUILDNUMBER)) {
              String bn = simpleSettings.optString(SettingsConstants.YOUNG_ANDROID_SETTINGS_BUILDNUMBER);
              if (bn != null && !bn.trim().isEmpty()) {
                try {
                  buildNumber = Integer.parseInt(bn.trim()) + 1;
                } catch (NumberFormatException ignored) {}
              }
            }
          }
        } catch (Exception e) {
          LOG.log(Level.WARNING, "Error parsing project settings for build versioning", e);
        }
      }

      JSONArray historyList = new JSONArray();
      try {
        String historyStr = storageIo.downloadFile(userId, projectId, "build/history/history.json", "UTF-8");
        if (historyStr != null && !historyStr.trim().isEmpty()) {
          historyList = new JSONArray(historyStr);
        }
      } catch (Exception ignored) {
        // First build, history.json does not exist yet
      }

      if (buildNumber <= historyList.length()) {
        buildNumber = historyList.length() + 1;
      }

      String historyFilePath = "build/history/" + buildNumber + "/" + fileName;
      LOG.info("Archiving build version #" + buildNumber + " to " + historyFilePath);
      storageIo.addOutputFilesToProject(userId, projectId, historyFilePath);
      storageIo.uploadRawFileForce(projectId, historyFilePath, userId, fileBytes);

      String buildType = fileName.endsWith(".apk") ? "APK" : (fileName.endsWith(".aab") ? "AAB" : "IPA");
      String formattedDate = new SimpleDateFormat("MMM d, yyyy", Locale.US).format(new Date());
      String fullDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());

      JSONObject record = new JSONObject();
      record.put("buildNumber", buildNumber);
      record.put("versionCode", versionCode);
      record.put("versionName", versionName);
      record.put("buildType", buildType);
      record.put("fileName", fileName);
      record.put("filePath", historyFilePath);
      record.put("fileSize", fileBytes.length);
      record.put("timestamp", System.currentTimeMillis());
      record.put("date", formattedDate);
      record.put("fullDate", fullDate);
      record.put("status", "SUCCESS");

      // Archive entire project source (.aia) as a version snapshot so it can be restored and edited later
      try {
        String projectName = storageIo.getProjectName(userId, projectId);
        String aiaFileName = projectName + "_v" + versionName + (fileName.endsWith(".aab") ? "_aab" : "") + ".aia";
        String aiaFilePath = "build/history/" + buildNumber + "/" + aiaFileName;
        LOG.info("Archiving full project version snapshot for build #" + buildNumber + " to " + aiaFilePath);
        ProjectSourceZip sourceZip = storageIo.exportProjectSourceZip(
            userId, projectId, /* includeProjectHistory */ false,
            /* includeAndroidKeystore */ false, aiaFileName,
            /* includeYail */ false, /* includeScreenShots */ false,
            /* forGallery */ false, /* fatalError */ false,
            /* forAppStore */ false, /* locallyCachedApp */ false);
        byte[] aiaBytes = sourceZip.getContent();
        if (aiaBytes != null && aiaBytes.length > 0) {
          storageIo.addOutputFilesToProject(userId, projectId, aiaFilePath);
          storageIo.uploadRawFileForce(projectId, aiaFilePath, userId, aiaBytes);
          record.put("aiaPath", aiaFilePath);
          record.put("aiaFileName", aiaFileName);
          record.put("aiaSize", aiaBytes.length);
          record.put("hasProjectSource", true);
        }
      } catch (Exception e) {
        LOG.log(Level.WARNING, "Error archiving project source snapshot for build #" + buildNumber, e);
      }

      historyList.put(record);

      String historyJsonString = historyList.toString();
      storageIo.addOutputFilesToProject(userId, projectId, "build/history/history.json");
      storageIo.uploadFileForce(projectId, "build/history/history.json", userId, historyJsonString, "UTF-8");

      // Update build number in project settings
      if (settingsObj != null && simpleSettings != null) {
        try {
          simpleSettings.put(SettingsConstants.YOUNG_ANDROID_SETTINGS_BUILDNUMBER, Integer.toString(buildNumber));
          storageIo.storeProjectSettings(userId, projectId, settingsObj.toString());
        } catch (Exception e) {
          LOG.log(Level.WARNING, "Error saving incremented build number to project settings", e);
        }
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Failed to archive build version", e);
    }
  }
}
