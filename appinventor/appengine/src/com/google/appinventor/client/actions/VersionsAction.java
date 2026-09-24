// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.dialogs.VersionsDialogBox;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.gwt.user.client.Command;

/**
 * Implements the action to open the Releases & Versions screen for the current project.
 */
public class VersionsAction implements Command {
  @Override
  public void execute() {
    ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
    if (projectRootNode != null) {
      new VersionsDialogBox(projectRootNode.getProjectId(), projectRootNode.getName()).center();
    }
  }
}
