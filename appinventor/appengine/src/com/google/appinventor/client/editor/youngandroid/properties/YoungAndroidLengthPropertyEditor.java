// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.components.MockVisibleComponent;
import com.google.appinventor.client.widgets.properties.AdditionalChoicePropertyEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Property editor for length properties (i.e. width and height).
 *
 */
public class YoungAndroidLengthPropertyEditor extends AdditionalChoicePropertyEditor {
  private static final Logger LOG = Logger.getLogger(YoungAndroidLengthPropertyEditor.class.getName());

  public static final String CONST_AUTOMATIC = "" + MockVisibleComponent.LENGTH_PREFERRED;
  public static final String CONST_FILL_PARENT = "" + MockVisibleComponent.LENGTH_FILL_PARENT;

  private static int uniqueIdSeed = 0;

  private final RadioButton automaticRadioButton;
  private final RadioButton fillParentRadioButton;
  private final RadioButton customLengthRadioButton;
  private final RadioButton percentfillRadioButton;
  private final TextBox customLengthField;
  private final TextBox percentLengthField;


  public YoungAndroidLengthPropertyEditor() {
    this(true);
  }

  /**
   * Creates a new length property editor.
   *
   * @param includePercent  whether to include percent of screen option
   */
  public YoungAndroidLengthPropertyEditor(boolean includePercent) {
    super(true);

    // The radio button group cannot be shared across all instances, so we append a unique id.
    int uniqueId = ++uniqueIdSeed;
    String radioButtonGroup = "LengthType-" + uniqueId;
    automaticRadioButton = new RadioButton(radioButtonGroup, MESSAGES.automaticCaption());
    fillParentRadioButton = new RadioButton(radioButtonGroup, MESSAGES.fillParentCaption());
    percentfillRadioButton = new RadioButton(radioButtonGroup);
    customLengthRadioButton = new RadioButton(radioButtonGroup);
    customLengthField = new TextBox();
    customLengthField.setVisibleLength(4);
    customLengthField.setMaxLength(4);
    percentLengthField = new TextBox();
    percentLengthField.setVisibleLength(4);
    percentLengthField.setMaxLength(4);

    Panel customRow = new HorizontalPanel();
    customRow.add(customLengthRadioButton);
    customRow.add(customLengthField);
    Label pixels = new Label(MESSAGES.pixelsCaption());
    pixels.setStylePrimaryName("ode-PixelsLabel");
    customRow.add(pixels);

    Panel percentRow = new HorizontalPanel();
    percentRow.add(percentfillRadioButton);
    percentRow.add(percentLengthField);
    Label percent = new Label(MESSAGES.percentCaption());
    percent.setStylePrimaryName("ode-PixelsLabel"); // recycle css definition
    percentRow.add(percent);

    Panel panel = new VerticalPanel();
    panel.add(automaticRadioButton);
    panel.add(fillParentRadioButton);
    panel.add(customRow);

    if ( includePercent ) {
      panel.add(percentRow);
    }

    automaticRadioButton.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent event) {
        // Clear the custom and percent length fields.
        customLengthField.setText("");
        percentLengthField.setText("");
        setOkButtonEnabled(true);
      }
    });
    fillParentRadioButton.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent event) {
        // Clear the custom and percent length fields.
        customLengthField.setText("");
        percentLengthField.setText("");
        setOkButtonEnabled(true);
      }
    });
    customLengthField.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // If the user clicks on the custom length field, but the radio button for a custom length
        // is not checked, check it.
        if (!customLengthRadioButton.isChecked()) {
          customLengthRadioButton.setChecked(true);
          percentLengthField.setText("");
          setOkButtonEnabled(true);
        }
      }
    });

    percentLengthField.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // If the user clicks on the percent length field, but the radio button for a custom length
        // is not checked, check it.
        if (!percentfillRadioButton.isChecked()) {
          percentfillRadioButton.setChecked(true);
          customLengthField.setText("");
          setOkButtonEnabled(true);
        }
      }
    });

    initAdditionalChoicePanel(panel);

    // Build compound widget: [ Text value (80px) | (Fit content icon) (Fill parent icon) ]
    com.google.gwt.user.client.ui.FlowPanel lengthContainer = new com.google.gwt.user.client.ui.FlowPanel();
    lengthContainer.setStylePrimaryName("ode-LengthPropertyEditor");

    summary.setStyleName("ode-LengthSummary");
    lengthContainer.add(summary);

    com.google.gwt.user.client.ui.FlowPanel buttonsGroup = new com.google.gwt.user.client.ui.FlowPanel();
    buttonsGroup.setStylePrimaryName("ode-LengthActionButtons");

    // Button 1: Fit Content / Automatic
    com.google.gwt.user.client.ui.Button autoButton = new com.google.gwt.user.client.ui.Button(
        "<svg viewBox=\"0 0 24 24\" width=\"15\" height=\"15\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M7 4H4v3M17 4h3v3M4 17v3h3M20 17v3h-3\"/><circle cx=\"12\" cy=\"12\" r=\"2.5\" fill=\"currentColor\" stroke=\"none\"/></svg>");
    autoButton.setTitle(MESSAGES.automaticCaption());
    autoButton.setStylePrimaryName("ode-LengthActionButton");
    autoButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        property.setValue(CONST_AUTOMATIC);
        updateValue();
      }
    });

    // Button 2: Fill Parent / Match Parent
    com.google.gwt.user.client.ui.Button fillButton = new com.google.gwt.user.client.ui.Button(
        "<svg viewBox=\"0 0 24 24\" width=\"15\" height=\"15\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M6 3H3v3M18 3h3v3M3 18v3h3M21 18v3h-3\"/></svg>");
    fillButton.setTitle(MESSAGES.fillParentCaption());
    fillButton.setStylePrimaryName("ode-LengthActionButton");
    fillButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        property.setValue(CONST_FILL_PARENT);
        updateValue();
      }
    });

    buttonsGroup.add(autoButton);
    buttonsGroup.add(fillButton);
    lengthContainer.add(buttonsGroup);

    initWidget(lengthContainer);
  }

  @Override
  protected void openAdditionalChoiceDialog() {
    super.openAdditionalChoiceDialog();
    automaticRadioButton.setFocus(true);
  }

  @Override
  protected void updateValue() {
    super.updateValue();

    String propertyValue = property.getValue();
    if (propertyValue.isEmpty() && isMultipleValues()) {  // Multiselect collision
      automaticRadioButton.setValue(false);
      fillParentRadioButton.setValue(false);
      percentfillRadioButton.setValue(false);
      percentLengthField.setValue("");
      customLengthRadioButton.setValue(false);
      customLengthField.setValue("");
      setOkButtonEnabled(false);
    } else if (propertyValue.equals(CONST_AUTOMATIC)) {
      automaticRadioButton.setChecked(true);
    } else if (propertyValue.equals(CONST_FILL_PARENT)) {
      fillParentRadioButton.setChecked(true);
    } else {
      int v = Integer.parseInt(propertyValue);
      if (v <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
        v = (-v) + MockVisibleComponent.LENGTH_PERCENT_TAG;
        percentfillRadioButton.setChecked(true);
        percentLengthField.setText("" + v);
      } else {
        customLengthRadioButton.setChecked(true);
        customLengthField.setText(propertyValue);
      }
    }
  }

  @Override
  protected String getPropertyValueSummary() {
    String lengthHint = property.getValue();
    if (lengthHint.isEmpty() && isMultipleValues()) {
      return MESSAGES.multipleValues();
    } else if (lengthHint.equals(CONST_AUTOMATIC)) {
      return MESSAGES.automaticCaption();
    } else if (lengthHint.equals(CONST_FILL_PARENT)) {
      return MESSAGES.fillParentCaption();
    } else {
      try {
        int v = Integer.parseInt(lengthHint);
        if (v <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
          v = (-v) + MockVisibleComponent.LENGTH_PERCENT_TAG;
          return v + "%";
        } else {
          return lengthHint + "px";
        }
      } catch (NumberFormatException e) {
        return lengthHint;
      }
    }
  }

  @Override
  protected boolean okAction() {
    setMultipleValues(false);
    if (automaticRadioButton.isChecked()) {
      property.setValue(CONST_AUTOMATIC);
    } else if (fillParentRadioButton.isChecked()) {
      property.setValue(CONST_FILL_PARENT);
    } else if (customLengthRadioButton.isChecked()) {
      // Custom length
      String text = customLengthField.getText();
      // Make sure it's a non-negative number.  It is important
      // that this check stay within the custom length case because
      // CONST_AUTOMATIC and CONST_FILL_PARENT are deliberately negative.
      boolean success = false;
      try {
        if (Integer.parseInt(text) >= 0) {
          success = true;
        }
      } catch (NumberFormatException e) {
        // fall through with success == false
      }
      if (!success) {
        Window.alert(MESSAGES.nonnumericInputError());
        return false;
      }
      property.setValue(text);
    } else if (percentfillRadioButton.getValue()) {         // Percent field!
      String text = percentLengthField.getText();
      boolean success = false;
      try {
        int v = Integer.parseInt(text);
        if (v >= 0 && v <= 100) {
          success = true;
          property.setValue("" + (-v + MockVisibleComponent.LENGTH_PERCENT_TAG));
        }
      } catch (NumberFormatException e) {
        // fall through with success == false
      }
      if (!success) {
        Window.alert(MESSAGES.nonvalidPercentValue());
        return false;
      }
    }
    return true;
  }
}
