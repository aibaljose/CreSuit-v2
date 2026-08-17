// -*- mode: java; c-basic-offset: 2; -*-


// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectListItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

public class ProjectListItemNeo extends ProjectListItem {

  interface ProjectListItemUiBinderNeo extends UiBinder<FlowPanel, ProjectListItemNeo> {}

  private static final ProjectListItemUiBinderNeo uibinder =
      GWT.create(ProjectListItemUiBinderNeo.class);

  @UiField FlowPanel container;
  @UiField Label nameLabel;
  @UiField Label dateModifiedLabel;
  @UiField Label dateCreatedLabel;
  @UiField Label packageNameLabel;
  @UiField com.google.gwt.user.client.ui.Image appIconImage;
  @UiField com.google.gwt.user.client.ui.HTML defaultIconContainer;
  @UiField CheckBox checkBox;
  @UiField FocusPanel projectnameFocusPanel;
  @UiField FocusPanel exportButton;
  @UiField FocusPanel moveButton;
  @UiField FocusPanel deleteButton;

  public ProjectListItemNeo(Project project) {
    super(project);
    initPackageNameAndIcon(project);
  }

  @Override
  public void bindUI() {
    initWidget(uibinder.createAndBindUi(this));
    super.container = container;
    super.checkBox = checkBox;
    super.dateCreatedLabel = dateCreatedLabel;
    super.dateModifiedLabel = dateModifiedLabel;
    super.nameLabel = nameLabel;
    super.projectnameFocusPanel = projectnameFocusPanel;
  }

  private void initPackageNameAndIcon(final Project project) {
    if (project == null) {
      return;
    }
    if (packageNameLabel != null) {
      String userPrefix = "aibal";
      if (com.google.appinventor.client.Ode.getInstance().getUser() != null) {
        String email = com.google.appinventor.client.Ode.getInstance().getUser().getUserEmail();
        if (email != null && email.contains("@")) {
          userPrefix = email.split("@")[0].toLowerCase().replaceAll("[^a-zA-Z0-9_]", "");
        }
      }
      String cleanProjectName = project.getProjectName().toLowerCase().replaceAll("[^a-zA-Z0-9_]", "");
      packageNameLabel.setText("com." + userPrefix + "." + cleanProjectName);
    }
    project.getSettings().loadSettings().then(settings -> {
      com.google.appinventor.client.settings.Settings yaSettings =
          settings.getSettings(com.google.appinventor.shared.settings.SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS);
      if (yaSettings != null) {
        String customPkg = yaSettings.getPropertyValue(com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_PACKAGE_NAME);
        if (customPkg != null && !customPkg.trim().isEmpty() && packageNameLabel != null) {
          packageNameLabel.setText(customPkg.trim());
        }
        String icon = yaSettings.getPropertyValue(com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON);
        if (icon != null && !icon.trim().isEmpty()) {
          String iconUrl = com.google.appinventor.shared.rpc.ServerLayout.getModuleBaseURL()
              + com.google.appinventor.shared.rpc.ServerLayout.DOWNLOAD_SERVLET_BASE
              + com.google.appinventor.shared.rpc.ServerLayout.DOWNLOAD_FILE + "/"
              + project.getProjectId() + "/assets/" + icon;
          if (appIconImage != null) {
            appIconImage.setUrl(iconUrl);
            appIconImage.setVisible(true);
          }
          if (defaultIconContainer != null) {
            defaultIconContainer.setVisible(false);
          }
        }
      }
      return null;
    });
  }

  @UiHandler("checkBox")
  protected void toggleItemSelection(ClickEvent e) {
    super.toggleItemSelection(e);
  }

  @UiHandler("projectnameFocusPanel")
  @Override
  protected void openProject(KeyDownEvent e) {
    super.openProject(e);
  }

  @UiHandler("projectnameFocusPanel")
  @Override
  protected void itemClicked(ClickEvent e) {
    super.itemClicked(e);
  }

  @UiHandler("deleteButton")
  protected void onDeleteClicked(ClickEvent e) {
    e.stopPropagation();
    if (com.google.gwt.user.client.Window.confirm("Are you sure you want to delete this project?")) {
      com.google.appinventor.client.Ode.getInstance().getFolderManager().moveItemsToFolder(
          java.util.Collections.singletonList(getProject()), java.util.Collections.emptyList(),
          com.google.appinventor.client.Ode.getInstance().getFolderManager().getTrashFolder());
      com.google.appinventor.client.Ode.getInstance().switchToProjectsView();
    }
  }

  @UiHandler("exportButton")
  protected void onExportClicked(ClickEvent e) {
    e.stopPropagation();
    String downloadUrl = com.google.appinventor.shared.rpc.ServerLayout.getModuleBaseURL()
        + com.google.appinventor.shared.rpc.ServerLayout.DOWNLOAD_SERVLET_BASE
        + com.google.appinventor.shared.rpc.ServerLayout.DOWNLOAD_PROJECT_SOURCE + "/" + getProject().getProjectId();
    com.google.gwt.user.client.Window.open(downloadUrl, "_self", "");
  }

  @UiHandler("moveButton")
  protected void onMoveClicked(ClickEvent e) {
    e.stopPropagation();
    setSelected(true);
    new com.google.appinventor.client.actions.MoveProjectsAction().execute();
  }
}
