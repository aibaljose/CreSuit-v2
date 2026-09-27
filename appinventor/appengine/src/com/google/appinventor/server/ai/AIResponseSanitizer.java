// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024-2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.ai;

import com.google.appinventor.components.common.YaVersion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defensive sanitizer and normalizer for AI-generated UI components (.scm) and
 * Blockly blocks (.bky).
 */
public class AIResponseSanitizer {
  private static final Logger LOG = Logger.getLogger(AIResponseSanitizer.class.getName());

  // Non-visible components that must only be placed directly under the Form
  private static final Set<String> NON_VISIBLE_COMPONENTS = new HashSet<>(Arrays.asList(
      "Notifier", "TinyDB", "Clock", "Sound", "LocationSensor", "AccelerometerSensor",
      "Camera", "Camcorder", "ImagePicker", "Texting", "PhoneCall", "Web", "FirebaseDB",
      "CloudDB", "SoundRecorder", "SpeechRecognizer", "TextToSpeech", "Translator"
  ));

  // Visual layout containers that can hold child components
  private static final Set<String> CONTAINER_COMPONENTS = new HashSet<>(Arrays.asList(
      "HorizontalArrangement", "VerticalArrangement", "TableArrangement",
      "HorizontalScrollArrangement", "VerticalScrollArrangement"
  ));

  // Mapping hallucinated/common alternate names to standard App Inventor component types
  private static final Map<String, String> TYPE_ALIAS_MAP = new HashMap<>();
  static {
    TYPE_ALIAS_MAP.put("InputBox", "TextBox");
    TYPE_ALIAS_MAP.put("TextInput", "TextBox");
    TYPE_ALIAS_MAP.put("EditText", "TextBox");
    TYPE_ALIAS_MAP.put("TextView", "Label");
    TYPE_ALIAS_MAP.put("TextLabel", "Label");
    TYPE_ALIAS_MAP.put("Heading", "Label");
    TYPE_ALIAS_MAP.put("ImageView", "Image");
    TYPE_ALIAS_MAP.put("Picture", "Image");
    TYPE_ALIAS_MAP.put("Container", "VerticalArrangement");
    TYPE_ALIAS_MAP.put("CardView", "VerticalArrangement");
    TYPE_ALIAS_MAP.put("Card", "VerticalArrangement");
    TYPE_ALIAS_MAP.put("Box", "VerticalArrangement");
    TYPE_ALIAS_MAP.put("Row", "HorizontalArrangement");
    TYPE_ALIAS_MAP.put("Column", "VerticalArrangement");
    TYPE_ALIAS_MAP.put("Checkbox", "CheckBox");
    TYPE_ALIAS_MAP.put("Toggle", "Switch");
    TYPE_ALIAS_MAP.put("ToggleSwitch", "Switch");
    TYPE_ALIAS_MAP.put("Alert", "Notifier");
    TYPE_ALIAS_MAP.put("Dialog", "Notifier");
    TYPE_ALIAS_MAP.put("Timer", "Clock");
    TYPE_ALIAS_MAP.put("Database", "TinyDB");
  }

  // Exact system component versions matching simple_components.json
  // Ensures Blockly versioning never throws "no upgrade map / upgrader" errors
  private static final Map<String, String> COMPONENT_VERSIONS = new HashMap<>();
  static {
    COMPONENT_VERSIONS.put("AbsoluteArrangement", "1");
    COMPONENT_VERSIONS.put("AccelerometerSensor", "5");
    COMPONENT_VERSIONS.put("ActivityStarter", "6");
    COMPONENT_VERSIONS.put("AnomalyDetection", "2");
    COMPONENT_VERSIONS.put("Ball", "8");
    COMPONENT_VERSIONS.put("BarcodeScanner", "2");
    COMPONENT_VERSIONS.put("Barometer", "1");
    COMPONENT_VERSIONS.put("BluetoothClient", "8");
    COMPONENT_VERSIONS.put("BluetoothServer", "5");
    COMPONENT_VERSIONS.put("Button", "7");
    COMPONENT_VERSIONS.put("Camcorder", "1");
    COMPONENT_VERSIONS.put("Camera", "3");
    COMPONENT_VERSIONS.put("Canvas", "15");
    COMPONENT_VERSIONS.put("CardView", "1");
    COMPONENT_VERSIONS.put("Chart", "4");
    COMPONENT_VERSIONS.put("ChartData2D", "2");
    COMPONENT_VERSIONS.put("ChatBot", "5");
    COMPONENT_VERSIONS.put("CheckBox", "2");
    COMPONENT_VERSIONS.put("Circle", "2");
    COMPONENT_VERSIONS.put("CircularProgress", "1");
    COMPONENT_VERSIONS.put("Clock", "4");
    COMPONENT_VERSIONS.put("CloudDB", "2");
    COMPONENT_VERSIONS.put("ContactPicker", "6");
    COMPONENT_VERSIONS.put("DataFile", "1");
    COMPONENT_VERSIONS.put("DatePicker", "4");
    COMPONENT_VERSIONS.put("EmailPicker", "7");
    COMPONENT_VERSIONS.put("Ev3ColorSensor", "2");
    COMPONENT_VERSIONS.put("Ev3Commands", "1");
    COMPONENT_VERSIONS.put("Ev3GyroSensor", "2");
    COMPONENT_VERSIONS.put("Ev3Motors", "1");
    COMPONENT_VERSIONS.put("Ev3Sound", "1");
    COMPONENT_VERSIONS.put("Ev3TouchSensor", "1");
    COMPONENT_VERSIONS.put("Ev3UI", "1");
    COMPONENT_VERSIONS.put("Ev3UltrasonicSensor", "2");
    COMPONENT_VERSIONS.put("FeatureCollection", "2");
    COMPONENT_VERSIONS.put("File", "4");
    COMPONENT_VERSIONS.put("FilePicker", "1");
    COMPONENT_VERSIONS.put("FirebaseDB", "3");
    COMPONENT_VERSIONS.put("Form", Integer.toString(YaVersion.FORM_COMPONENT_VERSION));
    COMPONENT_VERSIONS.put("FusiontablesControl", "4");
    COMPONENT_VERSIONS.put("GameClient", "1");
    COMPONENT_VERSIONS.put("GyroscopeSensor", "1");
    COMPONENT_VERSIONS.put("HorizontalArrangement", "4");
    COMPONENT_VERSIONS.put("HorizontalScrollArrangement", "2");
    COMPONENT_VERSIONS.put("Hygrometer", "1");
    COMPONENT_VERSIONS.put("Image", "6");
    COMPONENT_VERSIONS.put("ImageBot", "2");
    COMPONENT_VERSIONS.put("ImagePicker", "5");
    COMPONENT_VERSIONS.put("ImageSprite", "10");
    COMPONENT_VERSIONS.put("Label", "5");
    COMPONENT_VERSIONS.put("LightSensor", "1");
    COMPONENT_VERSIONS.put("LineString", "2");
    COMPONENT_VERSIONS.put("LinearProgress", "1");
    COMPONENT_VERSIONS.put("ListPicker", "9");
    COMPONENT_VERSIONS.put("ListView", "10");
    COMPONENT_VERSIONS.put("LocationSensor", "4");
    COMPONENT_VERSIONS.put("MagneticFieldSensor", "1");
    COMPONENT_VERSIONS.put("Map", "7");
    COMPONENT_VERSIONS.put("Marker", "4");
    COMPONENT_VERSIONS.put("MediaStore", "1");
    COMPONENT_VERSIONS.put("Navigation", "2");
    COMPONENT_VERSIONS.put("NearField", "1");
    COMPONENT_VERSIONS.put("Notifier", "6");
    COMPONENT_VERSIONS.put("NxtColorSensor", "1");
    COMPONENT_VERSIONS.put("NxtDirectCommands", "2");
    COMPONENT_VERSIONS.put("NxtDrive", "1");
    COMPONENT_VERSIONS.put("NxtLightSensor", "1");
    COMPONENT_VERSIONS.put("NxtSoundSensor", "1");
    COMPONENT_VERSIONS.put("NxtTouchSensor", "1");
    COMPONENT_VERSIONS.put("NxtUltrasonicSensor", "1");
    COMPONENT_VERSIONS.put("OrientationSensor", "2");
    COMPONENT_VERSIONS.put("PasswordTextBox", "7");
    COMPONENT_VERSIONS.put("Pedometer", "3");
    COMPONENT_VERSIONS.put("PhoneCall", "3");
    COMPONENT_VERSIONS.put("PhoneNumberPicker", "4");
    COMPONENT_VERSIONS.put("PhoneStatus", "1");
    COMPONENT_VERSIONS.put("Player", "7");
    COMPONENT_VERSIONS.put("Polygon", "2");
    COMPONENT_VERSIONS.put("ProximitySensor", "1");
    COMPONENT_VERSIONS.put("Rectangle", "2");
    COMPONENT_VERSIONS.put("Regression", "2");
    COMPONENT_VERSIONS.put("Serial", "1");
    COMPONENT_VERSIONS.put("Sharing", "1");
    COMPONENT_VERSIONS.put("Slider", "3");
    COMPONENT_VERSIONS.put("Sound", "4");
    COMPONENT_VERSIONS.put("SoundRecorder", "2");
    COMPONENT_VERSIONS.put("SpaceView", "1");
    COMPONENT_VERSIONS.put("SpeechRecognizer", "3");
    COMPONENT_VERSIONS.put("Spinner", "2");
    COMPONENT_VERSIONS.put("Spreadsheet", "4");
    COMPONENT_VERSIONS.put("Switch", "1");
    COMPONENT_VERSIONS.put("TableArrangement", "1");
    COMPONENT_VERSIONS.put("TextBox", "14");
    COMPONENT_VERSIONS.put("TextToSpeech", "6");
    COMPONENT_VERSIONS.put("Texting", "5");
    COMPONENT_VERSIONS.put("Thermometer", "1");
    COMPONENT_VERSIONS.put("TimePicker", "4");
    COMPONENT_VERSIONS.put("TinyDB", "3");
    COMPONENT_VERSIONS.put("TinyWebDB", "2");
    COMPONENT_VERSIONS.put("Translator", "1");
    COMPONENT_VERSIONS.put("Trendline", "1");
    COMPONENT_VERSIONS.put("Twitter", "4");
    COMPONENT_VERSIONS.put("VerticalArrangement", "4");
    COMPONENT_VERSIONS.put("VerticalScrollArrangement", "2");
    COMPONENT_VERSIONS.put("VideoPlayer", "7");
    COMPONENT_VERSIONS.put("Voting", "1");
    COMPONENT_VERSIONS.put("Web", "9");
    COMPONENT_VERSIONS.put("WebViewer", "11");
    COMPONENT_VERSIONS.put("YandexTranslate", "3");
  }

  public static String getComponentVersion(String componentType) {
    String ver = COMPONENT_VERSIONS.get(componentType);
    return ver != null ? ver : "1";
  }

  public static class SanitizedResult {
    private final String scmContent;
    private final String bkyContent;

    public SanitizedResult(String scmContent, String bkyContent) {
      this.scmContent = scmContent;
      this.bkyContent = bkyContent;
    }

    public String getScmContent() {
      return scmContent;
    }

    public String getBkyContent() {
      return bkyContent;
    }
  }

  /**
   * Sanitizes and normalizes the raw Gemini response into valid App Inventor .scm and .bky files.
   *
   * @param rawResponse JSON object containing "form" and "blocks_xml"
   * @param screenName verified screen identifier
   * @param appName application name
   * @return SanitizedResult containing completed .scm and .bky strings
   */
  public static SanitizedResult sanitize(JSONObject rawResponse, String screenName, String appName) {
    Set<String> usedNames = new HashSet<>();
    usedNames.add(screenName);
    Map<String, String> renameMap = new HashMap<>();
    AtomicInteger uuidSeq = new AtomicInteger(1000);

    // 1. Sanitize Form UI
    JSONObject formObj = rawResponse.optJSONObject("form");
    if (formObj == null) {
      formObj = new JSONObject();
    }

    JSONObject properties = new JSONObject();
    try {
      properties.put("$Name", screenName);
      properties.put("$Type", "Form");
      properties.put("$Version", Integer.toString(YaVersion.FORM_COMPONENT_VERSION));
      properties.put("Uuid", "0");
      properties.put("Title", formObj.optString("Title", screenName));
      properties.put("AppName", appName);

      JSONArray rawComponents = formObj.optJSONArray("$Components");
      JSONArray sanitizedComponents = new JSONArray();
      JSONArray nonVisibleHoisted = new JSONArray();

      if (rawComponents != null) {
        for (int i = 0; i < rawComponents.length(); i++) {
          JSONObject comp = rawComponents.optJSONObject(i);
          if (comp != null) {
            JSONObject clean = sanitizeComponent(comp, usedNames, renameMap, nonVisibleHoisted, uuidSeq);
            if (clean != null) {
              sanitizedComponents.put(clean);
            }
          }
        }
      }

      // Append any hoisted non-visible components to root Form
      for (int i = 0; i < nonVisibleHoisted.length(); i++) {
        sanitizedComponents.put(nonVisibleHoisted.getJSONObject(i));
      }

      properties.put("$Components", sanitizedComponents);

      JSONObject rootScm = new JSONObject();
      rootScm.put("authURL", new JSONArray());
      rootScm.put("YaVersion", Integer.toString(YaVersion.YOUNG_ANDROID_VERSION));
      rootScm.put("Source", "Form");
      rootScm.put("Properties", properties);

      String scmContent = "#|\n$JSON\n" + rootScm.toString() + "\n|#";

      // 2. Sanitize Blocks XML
      String rawBlocksXml = rawResponse.optString("blocks_xml", "");
      String bkyContent = sanitizeBlocksXml(rawBlocksXml, renameMap);

      return new SanitizedResult(scmContent, bkyContent);

    } catch (JSONException e) {
      LOG.log(Level.SEVERE, "Error building sanitized SCM json", e);
      throw new RuntimeException("Failed to sanitize generated screen structure", e);
    }
  }

  private static JSONObject sanitizeComponent(
      JSONObject comp,
      Set<String> usedNames,
      Map<String, String> renameMap,
      JSONArray nonVisibleHoisted,
      AtomicInteger uuidSeq) throws JSONException {

    String originalType = comp.optString("$Type", "Label").trim();
    String normalizedType = normalizeComponentType(originalType);

    String originalName = comp.optString("$Name", "").trim();
    if (originalName.isEmpty()) {
      originalName = normalizedType;
    }
    String sanitizedName = sanitizeIdentifier(originalName, normalizedType, usedNames);
    if (!sanitizedName.equals(originalName)) {
      if (!renameMap.containsKey(originalName)) {
        renameMap.put(originalName, sanitizedName);
      }
    }
    usedNames.add(sanitizedName);

    JSONObject cleanComp = new JSONObject();
    cleanComp.put("$Name", sanitizedName);
    cleanComp.put("$Type", normalizedType);
    cleanComp.put("$Version", getComponentVersion(normalizedType));
    cleanComp.put("Uuid", Integer.toString(uuidSeq.incrementAndGet()));

    // Copy properties while strictly ensuring all values are Strings (crucial for DesignerEditor parsing)
    String[] propNames = JSONObject.getNames(comp);
    if (propNames != null) {
      for (String key : propNames) {
        if (key.equals("$Name") || key.equals("$Type") || key.equals("$Version") || key.equals("Uuid") || key.equals("$Components")) {
          continue;
        }
        Object val = comp.opt(key);
        if (val != null) {
          cleanComp.put(key, normalizePropertyValue(key, val));
        }
      }
    }

    // Process nested children
    if (CONTAINER_COMPONENTS.contains(normalizedType)) {
      JSONArray rawChildren = comp.optJSONArray("$Components");
      JSONArray cleanChildren = new JSONArray();
      if (rawChildren != null) {
        for (int i = 0; i < rawChildren.length(); i++) {
          JSONObject child = rawChildren.optJSONObject(i);
          if (child != null) {
            String childType = normalizeComponentType(child.optString("$Type", "Label").trim());
            if (NON_VISIBLE_COMPONENTS.contains(childType)) {
              // Hoist out of arrangement to root Form
              JSONObject cleanChild = sanitizeComponent(child, usedNames, renameMap, nonVisibleHoisted, uuidSeq);
              if (cleanChild != null) {
                nonVisibleHoisted.put(cleanChild);
              }
            } else {
              JSONObject cleanChild = sanitizeComponent(child, usedNames, renameMap, nonVisibleHoisted, uuidSeq);
              if (cleanChild != null) {
                cleanChildren.put(cleanChild);
              }
            }
          }
        }
      }
      cleanComp.put("$Components", cleanChildren);
    }

    return cleanComp;
  }

  private static String normalizeComponentType(String type) {
    if (TYPE_ALIAS_MAP.containsKey(type)) {
      return TYPE_ALIAS_MAP.get(type);
    }
    return type;
  }

  private static String sanitizeIdentifier(String name, String fallbackPrefix, Set<String> usedNames) {
    // Remove non-alphanumeric and spaces
    String cleaned = name.replaceAll("[^A-Za-z0-9_]", "");
    if (cleaned.isEmpty() || Character.isDigit(cleaned.charAt(0))) {
      cleaned = fallbackPrefix + (cleaned.isEmpty() ? "1" : ("_" + cleaned));
    }

    // Ensure unique
    String candidate = cleaned;
    int suffix = 1;
    while (usedNames.contains(candidate)) {
      candidate = cleaned + "_" + suffix++;
    }
    return candidate;
  }

  private static String normalizePropertyValue(String key, Object val) {
    if (val == null) {
      return "";
    }
    if (key.equalsIgnoreCase("Width") || key.equalsIgnoreCase("Height")) {
      return normalizeDimension(val);
    }
    if (val instanceof Boolean) {
      return ((Boolean) val) ? "True" : "False";
    }
    return String.valueOf(val);
  }

  private static String normalizeDimension(Object val) {
    if (val == null) {
      return "-1";
    }
    String s = val.toString().trim().toLowerCase();
    if (s.contains("fill") || s.equals("-2") || s.equals("100%")) {
      return "-2";
    }
    if (s.contains("auto") || s.equals("-1") || s.isEmpty()) {
      return "-1";
    }
    try {
      // If it's a numeric pixel value
      int px = Integer.parseInt(s.replaceAll("[^0-9-]", ""));
      return Integer.toString(px);
    } catch (NumberFormatException e) {
      return "-1";
    }
  }

  private static String sanitizeBlocksXml(String rawXml, Map<String, String> renameMap) {
    if (rawXml == null || rawXml.trim().isEmpty() || !rawXml.contains("<block")) {
      return "";
    }

    String xml = rawXml.trim();

    // Normalize any standard math_arithmetic blocks to App Inventor math_add
    if (xml.contains("math_arithmetic")) {
      xml = xml.replace("type=\"math_arithmetic\"", "type=\"math_add\"")
               .replace("name=\"A\"", "name=\"NUM0\"")
               .replace("name=\"B\"", "name=\"NUM1\"");
      xml = xml.replaceAll("<field name=\"OP\">[^<]*</field>", "");
    }

    if (xml.contains("math_on_text") || xml.contains("math_is_a_number")) {
      LOG.warning("Generated blocks XML contains unsupported block types; defaulting to empty workspace");
      return "";
    }

    // Strip markdown code fences if present
    if (xml.startsWith("```xml")) {
      xml = xml.substring(6);
    } else if (xml.startsWith("```")) {
      xml = xml.substring(3);
    }
    if (xml.endsWith("```")) {
      xml = xml.substring(0, xml.length() - 3);
    }
    xml = xml.trim();

    // Strip any leading XML processing instruction (e.g. <?xml version="1.0" encoding="UTF-8"?>)
    xml = xml.replaceFirst("^<\\?xml[^>]*\\?>\\s*", "").trim();

    // Apply renameMap replacements
    for (Map.Entry<String, String> entry : renameMap.entrySet()) {
      xml = xml.replace("instance_name=\"" + entry.getKey() + "\"", "instance_name=\"" + entry.getValue() + "\"");
      xml = xml.replace("<field name=\"COMPONENT_SELECTOR\">" + entry.getKey() + "</field>",
          "<field name=\"COMPONENT_SELECTOR\">" + entry.getValue() + "</field>");
    }

    // If blocks were mistakenly wrapped inside <yacodeblocks>, unwrap them
    if (xml.contains("<yacodeblocks") && xml.indexOf("<block") > xml.indexOf("<yacodeblocks")) {
      xml = xml.replaceAll("<yacodeblocks[^>]*>", "").replace("</yacodeblocks>", "");
    }

    String yaTag = String.format(
        "<yacodeblocks xmlns=\"https://appinventor.mit.edu/ns/project/\" ya-version=\"%d\" language-version=\"%d\"></yacodeblocks>",
        YaVersion.YOUNG_ANDROID_VERSION, YaVersion.BLOCKS_LANGUAGE_VERSION);

    // Ensure it starts with <xml>
    if (!xml.startsWith("<xml")) {
      xml = "<xml xmlns=\"https://developers.google.com/blockly/xml\">" + xml + "</xml>";
    }

    // Ensure yacodeblocks metadata is present at the end
    if (!xml.contains("<yacodeblocks")) {
      if (xml.contains("</xml>")) {
        xml = xml.replace("</xml>", yaTag + "</xml>");
      } else {
        xml = xml + yaTag;
      }
    }

    // Verify XML well-formedness
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new InputSource(new StringReader(xml)));
      if (doc != null) {
        return xml;
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Generated Blockly XML was malformed; falling back to empty blocks", e);
      return "";
    }

    return "";
  }
}
