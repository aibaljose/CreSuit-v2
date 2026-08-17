// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.cresuit;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.TopToolbar;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

import com.google.gwt.user.client.ui.FlowPanel;

public class TopToolbarCreSuit extends TopToolbar {
  interface TopToolbarUiBinderCreSuit extends UiBinder<FlowPanel, TopToolbarCreSuit> {}
  private static final TopToolbarUiBinderCreSuit uibinder =
      GWT.create(TopToolbarUiBinderCreSuit.class);

  @UiField DropDownButton fileDropDown;
  @UiField DropDownButton connectDropDown;
  @UiField DropDownButton buildDropDown;
  @UiField DropDownButton settingsDropDown;
  @UiField DropDownButton adminDropDown;
  @UiField (provided = true) Boolean hasWriteAccess;
  @UiField (provided = true) Boolean isAvailable;

  @Override
  public void bindUI() {
    readOnly = Ode.getInstance().isReadOnly();
    hasWriteAccess = !readOnly;

    boolean oneProjectMode = Ode.getInstance().getOneProjectMode();
    isAvailable = !oneProjectMode && hasWriteAccess;

    initWidget(uibinder.createAndBindUi(this));
    super.fileDropDown = fileDropDown;
    super.connectDropDown = connectDropDown;
    super.buildDropDown = buildDropDown;
    super.settingsDropDown = settingsDropDown;
    super.adminDropDown = adminDropDown;
    super.isAvailable = isAvailable;
  }
}
