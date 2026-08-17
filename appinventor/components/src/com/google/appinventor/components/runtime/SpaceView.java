// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.View;
import android.widget.Space;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;

@DesignerComponent(
    version = 1,
    description = "A simple spacer component.",
    category = ComponentCategory.LAYOUT,
    nonVisible = false,
    iconName = "images/space.png")
@SimpleObject
public class SpaceView extends AndroidViewComponent {

  private final Space space;

  public SpaceView(ComponentContainer container) {
    super(container);
    space = new Space(container.$context());
    container.$add(this);
  }

  @Override
  public View getView() {
    return space;
  }
}
