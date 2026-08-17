// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.SimplePanel;

public final class MockSpaceView extends MockVisibleComponent {
  public static final String TYPE = "SpaceView";

  private final SimplePanel spaceWidget;

  public MockSpaceView(SimpleEditor editor) {
    super(editor, TYPE, images.rectangle());
    spaceWidget = new SimplePanel();
    initComponent(spaceWidget);
  }

  @Override
  public int getPreferredWidth() {
    return 10;
  }

  @Override
  public int getPreferredHeight() {
    return 10;
  }
}
