// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.ai.AIResponseSanitizer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link AIResponseSanitizer}.
 */
public class AIResponseSanitizerTest {

  @Test
  public void testBasicSanitization() throws Exception {
    JSONObject raw = new JSONObject();
    JSONObject form = new JSONObject();
    form.put("$Name", "Screen2");
    form.put("Title", "Test Screen");

    JSONArray components = new JSONArray();
    JSONObject btn = new JSONObject();
    btn.put("$Name", "MyButton");
    btn.put("$Type", "Button");
    btn.put("Text", "Click Me");
    btn.put("Width", "-2");
    components.put(btn);

    form.put("$Components", components);
    raw.put("form", form);

    String blocksXml = "<xml xmlns=\"https://developers.google.com/blockly/xml\">"
        + "<block type=\"component_event\" id=\"b1\" x=\"50\" y=\"50\">"
        + "<mutation component_type=\"Button\" is_generic=\"false\" instance_name=\"MyButton\" event_name=\"Click\"></mutation>"
        + "<field name=\"COMPONENT_SELECTOR\">MyButton</field>"
        + "</block></xml>";
    raw.put("blocks_xml", blocksXml);

    AIResponseSanitizer.SanitizedResult result =
        AIResponseSanitizer.sanitize(raw, "Screen2", "TestApp");

    assertNotNull(result);
    String scm = result.getScmContent();
    assertTrue(scm.startsWith("#|\n$JSON\n"));
    assertTrue(scm.endsWith("\n|#"));
    assertTrue(scm.contains("\"$Name\":\"Screen2\""));
    assertTrue(scm.contains("\"Title\":\"Test Screen\""));
    assertTrue(scm.contains("\"$Name\":\"MyButton\""));

    String bky = result.getBkyContent();
    assertTrue(bky.contains("<yacodeblocks"));
    assertTrue(bky.contains("instance_name=\"MyButton\""));
  }

  @Test
  public void testIdentifierSanitizationAndDeduplication() throws Exception {
    JSONObject raw = new JSONObject();
    JSONObject form = new JSONObject();

    JSONArray components = new JSONArray();

    // Button with spaces
    JSONObject btn1 = new JSONObject();
    btn1.put("$Name", "Submit Button");
    btn1.put("$Type", "Button");
    components.put(btn1);

    // Duplicate button name
    JSONObject btn2 = new JSONObject();
    btn2.put("$Name", "Submit Button");
    btn2.put("$Type", "Button");
    components.put(btn2);

    // Button starting with a number
    JSONObject btn3 = new JSONObject();
    btn3.put("$Name", "123Test");
    btn3.put("$Type", "Button");
    components.put(btn3);

    form.put("$Components", components);
    raw.put("form", form);

    String blocksXml = "<xml xmlns=\"https://developers.google.com/blockly/xml\">"
        + "<block type=\"component_event\" id=\"b1\">"
        + "<mutation component_type=\"Button\" is_generic=\"false\" instance_name=\"Submit Button\" event_name=\"Click\"></mutation>"
        + "<field name=\"COMPONENT_SELECTOR\">Submit Button</field>"
        + "</block></xml>";
    raw.put("blocks_xml", blocksXml);

    AIResponseSanitizer.SanitizedResult result =
        AIResponseSanitizer.sanitize(raw, "Screen2", "TestApp");

    String scm = result.getScmContent();
    // Verify names are clean identifiers and unique
    assertTrue(scm.contains("\"$Name\":\"SubmitButton\""));
    assertTrue(scm.contains("\"$Name\":\"SubmitButton_1\""));
    assertFalse(scm.contains("\"$Name\":\"Submit Button\""));

    // Verify rename map updated the blocks XML
    String bky = result.getBkyContent();
    assertTrue(bky.contains("instance_name=\"SubmitButton\""));
    assertTrue(bky.contains("<field name=\"COMPONENT_SELECTOR\">SubmitButton</field>"));
  }

  @Test
  public void testHallucinatedTypeNormalization() throws Exception {
    JSONObject raw = new JSONObject();
    JSONObject form = new JSONObject();

    JSONArray components = new JSONArray();
    JSONObject input = new JSONObject();
    input.put("$Name", "EmailField");
    input.put("$Type", "InputBox"); // Hallucinated type
    components.put(input);

    JSONObject card = new JSONObject();
    card.put("$Name", "ProfileCard");
    card.put("$Type", "CardView"); // Hallucinated container
    components.put(card);

    form.put("$Components", components);
    raw.put("form", form);

    AIResponseSanitizer.SanitizedResult result =
        AIResponseSanitizer.sanitize(raw, "Screen2", "TestApp");

    String scm = result.getScmContent();
    assertTrue(scm.contains("\"$Type\":\"TextBox\""));
    assertTrue(scm.contains("\"$Type\":\"VerticalArrangement\""));
  }

  @Test
  public void testNonVisibleComponentHoisting() throws Exception {
    JSONObject raw = new JSONObject();
    JSONObject form = new JSONObject();

    JSONArray components = new JSONArray();
    JSONObject layout = new JSONObject();
    layout.put("$Name", "VerticalArrangement1");
    layout.put("$Type", "VerticalArrangement");

    JSONArray layoutChildren = new JSONArray();
    JSONObject label = new JSONObject();
    label.put("$Name", "Label1");
    label.put("$Type", "Label");
    layoutChildren.put(label);

    // Notifier improperly nested inside arrangement
    JSONObject notifier = new JSONObject();
    notifier.put("$Name", "Notifier1");
    notifier.put("$Type", "Notifier");
    layoutChildren.put(notifier);

    layout.put("$Components", layoutChildren);
    components.put(layout);

    form.put("$Components", components);
    raw.put("form", form);

    AIResponseSanitizer.SanitizedResult result =
        AIResponseSanitizer.sanitize(raw, "Screen2", "TestApp");

    String scm = result.getScmContent();
    // Parse the generated scm JSON to verify structure
    int start = scm.indexOf("$JSON\n") + 6;
    int end = scm.lastIndexOf("\n|#");
    JSONObject parsed = new JSONObject(scm.substring(start, end));
    JSONObject props = parsed.getJSONObject("Properties");
    JSONArray rootChildren = props.getJSONArray("$Components");

    // Root children should contain both VerticalArrangement1 and hoisted Notifier1
    assertEquals(2, rootChildren.length());

    JSONObject rootArrangement = rootChildren.getJSONObject(0);
    assertEquals("VerticalArrangement1", rootArrangement.getString("$Name"));
    // The arrangement should only contain Label1 now
    JSONArray arrChildren = rootArrangement.getJSONArray("$Components");
    assertEquals(1, arrChildren.length());
    assertEquals("Label1", arrChildren.getJSONObject(0).getString("$Name"));

    // Notifier1 must be hoisted to root
    JSONObject hoistedNotifier = rootChildren.getJSONObject(1);
    assertEquals("Notifier1", hoistedNotifier.getString("$Name"));
    assertEquals("Notifier", hoistedNotifier.getString("$Type"));
  }

  @Test
  public void testDimensionNormalization() throws Exception {
    JSONObject raw = new JSONObject();
    JSONObject form = new JSONObject();

    JSONArray components = new JSONArray();
    JSONObject btn = new JSONObject();
    btn.put("$Name", "Btn");
    btn.put("$Type", "Button");
    btn.put("Width", "fill parent");
    btn.put("Height", "100px");
    components.put(btn);

    form.put("$Components", components);
    raw.put("form", form);

    AIResponseSanitizer.SanitizedResult result =
        AIResponseSanitizer.sanitize(raw, "Screen2", "TestApp");

    String scm = result.getScmContent();
    assertTrue(scm.contains("\"Width\":\"-2\""));
    assertTrue(scm.contains("\"Height\":\"100\""));
  }

  @Test
  public void testMalformedBlocksXmlRecovery() throws Exception {
    JSONObject raw = new JSONObject();
    JSONObject form = new JSONObject();
    raw.put("form", form);
    // Incomplete / unclosed XML tag
    raw.put("blocks_xml", "<xml><block type=\"unclosed\">");

    AIResponseSanitizer.SanitizedResult result =
        AIResponseSanitizer.sanitize(raw, "Screen2", "TestApp");

    String bky = result.getBkyContent();
    assertNotNull(bky);
    // Should safely fallback to empty string matching standard App Inventor initial blocks
    assertEquals("", bky);
  }

  @Test
  public void testFixJsonEscapes() throws Exception {
    // String with illegal escapes: \d, \., \', and unescaped newline
    String badJson = "{\"blocks_xml\": \"<tag regex=\\\"\\d+\\\" val=\\'test\\'>hello \\. world</tag>\"}";
    String fixed = com.google.appinventor.server.ai.GeminiClient.fixJsonEscapes(badJson);
    JSONObject obj = new JSONObject(fixed);
    assertNotNull(obj);
    assertTrue(obj.has("blocks_xml"));
    assertTrue(obj.getString("blocks_xml").contains("hello"));
  }
}
