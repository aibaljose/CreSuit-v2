// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.ai;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client for interacting with the Google Gemini API to generate App Inventor
 * UI component hierarchies (.scm) and Blockly blocks (.bky).
 */
public class GeminiClient {
  private static final Logger LOG = Logger.getLogger(GeminiClient.class.getName());

  private static final String DEFAULT_MODEL = "gemini-2.5-flash";
  private static final String GEMINI_API_ENDPOINT =
      "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
  private static final String HARDCODED_API_KEY = "AQ.Ab8RN6Kg6TQ1iCi0CjSOBC2Bh4vh0hRaLXlWEI3Kq1Uv5zGdjQ";

  private final String apiKey;
  private final String model;

  public GeminiClient() {
    this(getApiKeyFromEnvOrSystem(), getModelFromEnvOrSystem());
  }

  public GeminiClient(String apiKey, String model) {
    this.apiKey = apiKey;
    this.model = (model != null && !model.trim().isEmpty()) ? model : DEFAULT_MODEL;
  }

  public static String getApiKeyFromEnvOrSystem() {
    String key = System.getenv("GEMINI_API_KEY");
    if (key != null && !key.trim().isEmpty()) {
      return key.trim();
    }
    key = System.getProperty("gemini.api.key");
    if (key != null && !key.trim().isEmpty()) {
      return key.trim();
    }

    // Try ~/.appinventor/gemini.key
    String userHome = System.getProperty("user.home");
    if (userHome != null) {
      File f = new File(userHome, ".appinventor/gemini.key");
      if (f.exists() && f.isFile()) {
        try {
          return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8).trim();
        } catch (IOException ignored) {}
      }
    }

    // Try local gemini.key file
    File localKey = new File("gemini.key");
    if (localKey.exists() && localKey.isFile()) {
      try {
        return new String(Files.readAllBytes(localKey.toPath()), StandardCharsets.UTF_8).trim();
      } catch (IOException ignored) {}
    }

    File webInfKey = new File("WEB-INF/gemini.key");
    if (webInfKey.exists() && webInfKey.isFile()) {
      try {
        return new String(Files.readAllBytes(webInfKey.toPath()), StandardCharsets.UTF_8).trim();
      } catch (IOException ignored) {}
    }

    return HARDCODED_API_KEY;
  }

  public static String getModelFromEnvOrSystem() {
    String m = System.getenv("GEMINI_MODEL");
    if (m == null || m.trim().isEmpty()) {
      m = System.getProperty("gemini.model");
    }
    return (m != null && !m.trim().isEmpty()) ? m : DEFAULT_MODEL;
  }

  public boolean isConfigured() {
    return apiKey != null && !apiKey.trim().isEmpty();
  }

  /**
   * Requests Gemini to generate UI components and starter blocks for a given screen.
   *
   * @param screenName name of the screen (e.g. "Screen2", "DoctorDetail")
   * @param userPrompt description of what the screen should contain and do
   * @return JSONObject containing "form" and "blocks_xml"
   * @throws IOException on connection or network error
   * @throws JSONException on invalid JSON response
   */
  public JSONObject generateScreenAndBlocks(String screenName, String userPrompt)
      throws IOException, JSONException {
    if (!isConfigured()) {
      throw new IllegalStateException(
          "Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable or gemini.api.key system property.");
    }

    String urlStr = String.format(GEMINI_API_ENDPOINT, model, apiKey);
    URL url = new URL(urlStr);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    try {
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      conn.setDoOutput(true);
      conn.setConnectTimeout(25000);
      conn.setReadTimeout(45000);

      JSONObject requestBody = buildRequestBody(screenName, userPrompt);
      byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);

      try (OutputStream os = conn.getOutputStream()) {
        os.write(input, 0, input.length);
      }

      int status = conn.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        String errorResponse = readStream(conn.getErrorStream());
        LOG.log(Level.SEVERE, "Gemini API error (status " + status + "): " + errorResponse);
        throw new IOException("Gemini API call failed with status " + status + ": " + errorResponse);
      }

      String responseStr = readStream(conn.getInputStream());
      return extractGeneratedJson(responseStr);

    } finally {
      conn.disconnect();
    }
  }

  private JSONObject buildRequestBody(String screenName, String userPrompt) throws JSONException {
    JSONObject root = new JSONObject();

    // System instructions
    JSONObject systemInstruction = new JSONObject();
    JSONObject systemPart = new JSONObject();
    systemPart.put("text", getSystemInstructions(screenName));
    systemInstruction.put("parts", new JSONArray().put(systemPart));
    root.put("systemInstruction", systemInstruction);

    // User prompt content
    JSONObject content = new JSONObject();
    JSONObject userPart = new JSONObject();
    userPart.put("text", "Screen Name: " + screenName + "\nUser Description: " + userPrompt);
    content.put("parts", new JSONArray().put(userPart));
    root.put("contents", new JSONArray().put(content));

    // Generation config (force JSON mode)
    JSONObject genConfig = new JSONObject();
    genConfig.put("response_mime_type", "application/json");
    genConfig.put("temperature", 0.2);
    genConfig.put("max_output_tokens", 8192);
    root.put("generationConfig", genConfig);

    return root;
  }

  private String getSystemInstructions(String screenName) {
    return "You are an expert MIT App Inventor engineer and UI/UX designer.\n"
        + "Your task is to generate both the UI Component hierarchy (for .scm) and functional Blockly blocks (for .bky) for an App Inventor screen named '" + screenName + "'.\n"
        + "You MUST return a JSON object with two fields:\n"
        + "1. \"form\": A JSON object containing the UI hierarchy.\n"
        + "   - \"$Name\": \"" + screenName + "\"\n"
        + "   - \"$Type\": \"Form\"\n"
        + "   - \"Title\": A user-friendly title string.\n"
        + "   - \"$Components\": An array of child components creating a modern, complete layout.\n"
        + "2. \"blocks_xml\": An XML string representing functional App Inventor Blockly blocks.\n\n"
        + "APP INVENTOR UI RULES:\n"
        + "- Allowed Components:\n"
        + "  * Visual: Button, Label, TextBox, PasswordTextBox, CheckBox, Switch, Image, ListView, Spinner, DatePicker, TimePicker.\n"
        + "  * Layout: HorizontalArrangement, VerticalArrangement, TableArrangement.\n"
        + "  * Non-visible: Notifier, TinyDB, Clock, Sound.\n"
        + "- Non-visible components MUST be direct children of the root Form (NOT inside any Arrangement).\n"
        + "- Visual components inside Arrangements must be in their \"$Components\" array.\n"
        + "- Component property values:\n"
        + "  * ALL property values must be STRING literals (e.g. \"Text\": \"Submit\", \"FontSize\": \"14.0\", \"Enabled\": \"True\"). Do NOT use raw boolean (true/false) or numeric literals.\n"
        + "  * \"Width\": \"-1\" for Automatic, \"-2\" for Fill Parent, or a pixel number as string like \"100\".\n"
        + "  * \"Height\": \"-1\" for Automatic, \"-2\" for Fill Parent, or a pixel number as string like \"50\".\n"
        + "  * \"$Type\": Must be an exact component type name.\n"
        + "  * \"$Name\": Must be a unique, valid Java identifier without spaces (e.g. SubmitButton, EmailInput, Notifier1).\n\n"
        + "APP INVENTOR BLOCKLY RULES (for \"blocks_xml\"):\n"
        + "- Format: Valid XML wrapped in <xml xmlns=\"https://developers.google.com/blockly/xml\"> ... </xml>.\n"
        + "- Create functional event handlers and logic for the interactive components (Buttons, Switches, CheckBoxes, etc.) defined in \"form\".\n"
        + "- Event Handler syntax:\n"
        + "  <block type=\"component_event\" id=\"b_event1\" x=\"20\" y=\"20\">\n"
        + "    <mutation component_type=\"Button\" is_generic=\"false\" instance_name=\"SubmitButton\" event_name=\"Click\"></mutation>\n"
        + "    <field name=\"COMPONENT_SELECTOR\">SubmitButton</field>\n"
        + "    <statement name=\"DO\">\n"
        + "      ... statements ...\n"
        + "    </statement>\n"
        + "  </block>\n"
        + "- Set Property syntax:\n"
        + "  <block type=\"component_set_get\" id=\"b_set1\">\n"
        + "    <mutation component_type=\"Label\" set_or_get=\"set\" property_name=\"Text\" is_generic=\"false\" instance_name=\"ResultLabel\"></mutation>\n"
        + "    <field name=\"COMPONENT_SELECTOR\">ResultLabel</field>\n"
        + "    <field name=\"PROP\">Text</field>\n"
        + "    <value name=\"VALUE\">... value block ...</value>\n"
        + "  </block>\n"
        + "- Get Property syntax:\n"
        + "  <block type=\"component_set_get\" id=\"b_get1\">\n"
        + "    <mutation component_type=\"TextBox\" set_or_get=\"get\" property_name=\"Text\" is_generic=\"false\" instance_name=\"InputBox\"></mutation>\n"
        + "    <field name=\"COMPONENT_SELECTOR\">InputBox</field>\n"
        + "    <field name=\"PROP\">Text</field>\n"
        + "  </block>\n"
        + "- Method Call (e.g. Notifier.ShowAlert):\n"
        + "  <block type=\"component_method\" id=\"b_notify1\">\n"
        + "    <mutation component_type=\"Notifier\" method_name=\"ShowAlert\" is_generic=\"false\" instance_name=\"Notifier1\">\n"
        + "      <arg name=\"notice\"></arg>\n"
        + "    </mutation>\n"
        + "    <field name=\"COMPONENT_SELECTOR\">Notifier1</field>\n"
        + "    <value name=\"ARG0\"><block type=\"text\" id=\"b_msg\"><field name=\"TEXT\">Action Completed!</field></block></value>\n"
        + "  </block>\n"
        + "- Text literal: <block type=\"text\" id=\"...\"><field name=\"TEXT\">Hello</field></block>\n"
        + "- Math number: <block type=\"math_number\" id=\"...\"><field name=\"NUM\">0</field></block>\n"
        + "- Math operations: use type=\"math_add\", \"math_subtract\", \"math_multiply\", or \"math_division\" with <mutation items=\"2\"></mutation><value name=\"NUM0\">...</value><value name=\"NUM1\">...</value>. NEVER use math_arithmetic.\n"
        + "- Boolean: <block type=\"logic_boolean\" id=\"...\"><field name=\"BOOL\">TRUE</field></block>\n"
        + "- Statement chaining inside <statement name=\"DO\">: chain with <next><block ...>...</block></next>.\n"
        + "- Every block MUST have an id (e.g. id=\"b1\", id=\"b2\", etc.).\n"
        + "- Component instance_name and COMPONENT_SELECTOR in blocks MUST exactly match the $Name in \"form\".\n\n"
        + "- Return ONLY valid JSON adhering to this schema.";
  }

  private JSONObject extractGeneratedJson(String responseStr) throws JSONException {
    JSONObject resp = new JSONObject(responseStr);
    JSONArray candidates = resp.optJSONArray("candidates");
    if (candidates == null || candidates.length() == 0) {
      throw new JSONException("No candidates returned from Gemini API");
    }

    JSONObject candidate = candidates.getJSONObject(0);
    JSONObject content = candidate.optJSONObject("content");
    if (content == null) {
      throw new JSONException("No content in Gemini response candidate");
    }

    JSONArray parts = content.optJSONArray("parts");
    if (parts == null || parts.length() == 0) {
      throw new JSONException("No parts in Gemini response content");
    }

    String text = parts.getJSONObject(0).optString("text", "");
    if (text.trim().isEmpty()) {
      throw new JSONException("Empty text in Gemini response part");
    }

    // Strip markdown formatting if any was returned (e.g. ```json ... ```)
    String cleaned = text.trim();
    if (cleaned.startsWith("```json")) {
      cleaned = cleaned.substring(7);
    } else if (cleaned.startsWith("```")) {
      cleaned = cleaned.substring(3);
    }
    if (cleaned.endsWith("```")) {
      cleaned = cleaned.substring(0, cleaned.length() - 3);
    }
    cleaned = cleaned.trim();

    // Extract outermost JSON object if there is leading/trailing text
    int firstBrace = cleaned.indexOf('{');
    int lastBrace = cleaned.lastIndexOf('}');
    if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
      cleaned = cleaned.substring(firstBrace, lastBrace + 1);
    }

    try {
      return new JSONObject(cleaned);
    } catch (JSONException e) {
      LOG.log(Level.WARNING, "Direct JSON parse failed (" + e.getMessage() + "), attempting escape sequence normalization");
      String fixed = fixJsonEscapes(cleaned);
      try {
        return new JSONObject(fixed);
      } catch (JSONException e2) {
        LOG.log(Level.SEVERE, "Failed to parse JSON even after escape normalization. Sample: "
            + (fixed.length() > 500 ? fixed.substring(0, 500) + "..." : fixed), e2);
        throw e2;
      }
    }
  }

  /**
   * Fixes illegal escape sequences in a JSON string (such as \', \d, \w, \., unescaped backslashes,
   * or literal unescaped newlines inside strings) so that standard JSON parsers can parse it.
   */
  public static String fixJsonEscapes(String json) {
    if (json == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder(json.length() + 64);
    boolean inString = false;
    for (int i = 0; i < json.length(); i++) {
      char c = json.charAt(i);
      if (!inString) {
        if (c == '"') {
          inString = true;
        }
        sb.append(c);
      } else {
        if (c == '"') {
          inString = false;
          sb.append(c);
        } else if (c == '\\') {
          if (i + 1 < json.length()) {
            char next = json.charAt(i + 1);
            if (next == '"' || next == '\\' || next == '/' || next == 'b'
                || next == 'f' || next == 'n' || next == 'r' || next == 't') {
              // Valid single-character escape
              sb.append(c).append(next);
              i++;
            } else if (next == 'u') {
              if (i + 5 < json.length()
                  && isHex(json.charAt(i + 2)) && isHex(json.charAt(i + 3))
                  && isHex(json.charAt(i + 4)) && isHex(json.charAt(i + 5))) {
                sb.append(c).append(next);
                i++;
              } else {
                sb.append("\\\\");
              }
            } else {
              // Illegal escape like \', \d, \w, \., \+, \(, \space, etc.
              sb.append("\\\\");
            }
          } else {
            // Trailing backslash
            sb.append("\\\\");
          }
        } else if (c == '\n') {
          sb.append("\\n");
        } else if (c == '\r') {
          sb.append("\\r");
        } else {
          sb.append(c);
        }
      }
    }
    return sb.toString();
  }

  private static boolean isHex(char c) {
    return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }

  private static String readStream(InputStream is) throws IOException {
    if (is == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append('\n');
      }
    }
    return sb.toString();
  }
}
