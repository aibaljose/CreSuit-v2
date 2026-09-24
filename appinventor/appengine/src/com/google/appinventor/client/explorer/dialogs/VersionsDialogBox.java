// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.dialogs;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.actions.BarcodeAction;
import com.google.appinventor.client.editor.blocks.BlocklyPanel;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * A dialog box representing the App Release & Version Management screen.
 * Displays the latest release along with previous build versions, allowing
 * direct APK/AAB download, QR code installation, and version management.
 */
public class VersionsDialogBox extends DialogBox {

  private final long projectId;
  private final String projectName;

  private final VerticalPanel mainContainer = new VerticalPanel();
  private final FlowPanel latestReleasePanel = new FlowPanel();
  private final FlowPanel historyContainer = new FlowPanel();
  private final Label statusLabel = new Label();

  public static class BuildRecord {
    public int buildNumber;
    public String versionCode = "1";
    public String versionName = "1.0";
    public String buildType = "APK";
    public String fileName = "";
    public String filePath = "";
    public long fileSize = 0;
    public String aiaPath = "";
    public String aiaFileName = "";
    public long aiaSize = 0;
    public boolean hasProjectSource = false;
    public String date = "";
    public String fullDate = "";
    public String status = "SUCCESS";

    public String getFormattedSize() {
      if (fileSize <= 0) return "";
      if (fileSize < 1024 * 1024) {
        return (fileSize / 1024) + " KB";
      }
      double mb = (double) fileSize / (1024.0 * 1024.0);
      return (Math.round(mb * 10.0) / 10.0) + " MB";
    }

    public String getFormattedAiaSize() {
      if (aiaSize <= 0) return "";
      if (aiaSize < 1024 * 1024) {
        return (aiaSize / 1024) + " KB";
      }
      double mb = (double) aiaSize / (1024.0 * 1024.0);
      return (Math.round(mb * 10.0) / 10.0) + " MB";
    }
  }

  public VersionsDialogBox(long projectId, String projectName) {
    super(false, true);
    this.projectId = projectId;
    this.projectName = projectName;

    setStylePrimaryName("ode-DialogBox");
    addStyleName("cresuit-versions-dialog");

    setText("Releases & Versions - " + projectName);
    setGlassEnabled(true);
    setAnimationEnabled(true);

    buildUI();
    loadVersions();
  }

  private void buildUI() {
    mainContainer.setWidth("640px");
    mainContainer.setStyleName("cresuit-versions-container");

    // Top Header Bar
    HorizontalPanel headerBar = new HorizontalPanel();
    headerBar.setStyleName("cresuit-versions-header");
    headerBar.setWidth("100%");

    VerticalPanel titleGroup = new VerticalPanel();
    Label appTitle = new Label(projectName);
    appTitle.setStyleName("cresuit-versions-apptitle");
    Label appSubtitle = new Label("App Release Management & Build History");
    appSubtitle.setStyleName("cresuit-versions-appsubtitle");
    titleGroup.add(appTitle);
    titleGroup.add(appSubtitle);
    headerBar.add(titleGroup);

    HorizontalPanel actionsGroup = new HorizontalPanel();
    actionsGroup.setStyleName("cresuit-versions-headeractions");

    Button buildBtn = new Button("+ Build APK");
    buildBtn.setStyleName("cresuit-btn-build");
    buildBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
        new BarcodeAction(false, false).execute();
      }
    });

    Button closeBtn = new Button("✕");
    closeBtn.setStyleName("cresuit-btn-close");
    closeBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
      }
    });

    actionsGroup.add(buildBtn);
    actionsGroup.add(closeBtn);
    headerBar.add(actionsGroup);
    headerBar.setCellHorizontalAlignment(actionsGroup, HorizontalPanel.ALIGN_RIGHT);

    mainContainer.add(headerBar);

    // Status / Loading label
    statusLabel.setStyleName("cresuit-versions-status");
    mainContainer.add(statusLabel);

    // Latest Release Section
    latestReleasePanel.setStyleName("cresuit-latest-release-container");
    mainContainer.add(latestReleasePanel);

    // History Section
    FlowPanel historyHeader = new FlowPanel();
    historyHeader.setStyleName("cresuit-history-header");
    Label historyTitle = new Label("Version History");
    historyTitle.setStyleName("cresuit-history-title");
    historyHeader.add(historyTitle);

    Button refreshBtn = new Button("↻ Refresh");
    refreshBtn.setStyleName("cresuit-btn-refresh");
    refreshBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        loadVersions();
      }
    });
    historyHeader.add(refreshBtn);
    mainContainer.add(historyHeader);

    ScrollPanel scrollHistory = new ScrollPanel();
    scrollHistory.setStyleName("cresuit-history-scroll");
    historyContainer.setStyleName("cresuit-history-list");
    scrollHistory.add(historyContainer);
    mainContainer.add(scrollHistory);

    add(mainContainer);
  }

  public void loadVersions() {
    statusLabel.setText("Loading versions...");
    statusLabel.setVisible(true);

    Ode.getInstance().getProjectService().load(projectId, "build/history/history.json",
        new OdeAsyncCallback<String>("Error loading version history") {
          @Override
          public void onSuccess(String jsonStr) {
            statusLabel.setVisible(false);
            List<BuildRecord> records = parseHistory(jsonStr);
            renderVersions(records);
          }

          @Override
          public void onFailure(Throwable caught) {
            statusLabel.setVisible(false);
            renderVersions(new ArrayList<BuildRecord>());
          }
        });
  }

  private List<BuildRecord> parseHistory(String jsonStr) {
    List<BuildRecord> list = new ArrayList<>();
    if (jsonStr == null || jsonStr.trim().isEmpty()) {
      return list;
    }
    try {
      JSONValue parsed = JSONParser.parseStrict(jsonStr);
      JSONArray arr = parsed.isArray();
      if (arr != null) {
        for (int i = 0; i < arr.size(); i++) {
          JSONObject obj = arr.get(i).isObject();
          if (obj != null) {
            BuildRecord rec = new BuildRecord();
            if (obj.containsKey("buildNumber")) {
              JSONNumber bn = obj.get("buildNumber").isNumber();
              if (bn != null) rec.buildNumber = (int) bn.doubleValue();
            }
            if (obj.containsKey("versionCode")) {
              JSONString vc = obj.get("versionCode").isString();
              if (vc != null) rec.versionCode = vc.stringValue();
            }
            if (obj.containsKey("versionName")) {
              JSONString vn = obj.get("versionName").isString();
              if (vn != null) rec.versionName = vn.stringValue();
            }
            if (obj.containsKey("buildType")) {
              JSONString bt = obj.get("buildType").isString();
              if (bt != null) rec.buildType = bt.stringValue();
            }
            if (obj.containsKey("fileName")) {
              JSONString fn = obj.get("fileName").isString();
              if (fn != null) rec.fileName = fn.stringValue();
            }
            if (obj.containsKey("filePath")) {
              JSONString fp = obj.get("filePath").isString();
              if (fp != null) rec.filePath = fp.stringValue();
            }
            if (obj.containsKey("fileSize")) {
              JSONNumber fs = obj.get("fileSize").isNumber();
              if (fs != null) rec.fileSize = (long) fs.doubleValue();
            }
            if (obj.containsKey("aiaPath")) {
              JSONString ap = obj.get("aiaPath").isString();
              if (ap != null) rec.aiaPath = ap.stringValue();
            }
            if (obj.containsKey("aiaFileName")) {
              JSONString afn = obj.get("aiaFileName").isString();
              if (afn != null) rec.aiaFileName = afn.stringValue();
            }
            if (obj.containsKey("aiaSize")) {
              JSONNumber as = obj.get("aiaSize").isNumber();
              if (as != null) rec.aiaSize = (long) as.doubleValue();
            }
            if (obj.containsKey("hasProjectSource")) {
              JSONBoolean hps = obj.get("hasProjectSource").isBoolean();
              if (hps != null) rec.hasProjectSource = hps.booleanValue();
            }
            if (!rec.aiaPath.isEmpty()) {
              rec.hasProjectSource = true;
            }
            if (obj.containsKey("date")) {
              JSONString d = obj.get("date").isString();
              if (d != null) rec.date = d.stringValue();
            }
            if (obj.containsKey("fullDate")) {
              JSONString fd = obj.get("fullDate").isString();
              if (fd != null) rec.fullDate = fd.stringValue();
            }
            list.add(rec);
          }
        }
      }
    } catch (Exception e) {
      // Return whatever parsed so far
    }
    return list;
  }

  private void renderVersions(final List<BuildRecord> records) {
    latestReleasePanel.clear();
    historyContainer.clear();

    if (records.isEmpty()) {
      FlowPanel emptyPanel = new FlowPanel();
      emptyPanel.setStyleName("cresuit-empty-state");
      Label emptyTitle = new Label("No release versions yet");
      emptyTitle.setStyleName("cresuit-empty-title");
      Label emptyDesc = new Label("Each time you build your app (APK or AAB), it will be archived here automatically with version details and download links.");
      emptyDesc.setStyleName("cresuit-empty-desc");
      emptyPanel.add(emptyTitle);
      emptyPanel.add(emptyDesc);
      latestReleasePanel.add(emptyPanel);
      return;
    }

    // Latest Release is the last item in the list
    final BuildRecord latest = records.get(records.size() - 1);
    final String latestDownloadUrl = ServerLayout.DOWNLOAD_SERVLET_BASE
        + ServerLayout.DOWNLOAD_FILE + "/" + projectId + "/" + latest.filePath;
    final String latestQrUrl = GWT.getHostPageBaseURL() + ServerLayout.DOWNLOAD_SERVLET_BASE
        + ServerLayout.DOWNLOAD_FILE + "/" + projectId + "/" + latest.filePath;

    FlowPanel heroCard = new FlowPanel();
    heroCard.setStyleName("cresuit-release-card-hero");

    Label heroTag = new Label("LATEST RELEASE");
    heroTag.setStyleName("cresuit-hero-tag");
    heroCard.add(heroTag);

    HorizontalPanel heroContent = new HorizontalPanel();
    heroContent.setWidth("100%");

    VerticalPanel heroDetails = new VerticalPanel();
    Label heroVersion = new Label("v" + latest.versionName);
    heroVersion.setStyleName("cresuit-hero-version");

    String heroMetaText = "Build " + latest.buildNumber + " · " + latest.date;
    if (!latest.getFormattedSize().isEmpty()) {
      heroMetaText += " · " + latest.getFormattedSize();
    }
    heroMetaText += " · " + latest.buildType;
    Label heroMeta = new Label(heroMetaText);
    heroMeta.setStyleName("cresuit-hero-meta");

    heroDetails.add(heroVersion);
    heroDetails.add(heroMeta);
    heroContent.add(heroDetails);

    HorizontalPanel heroButtons = new HorizontalPanel();
    heroButtons.setStyleName("cresuit-hero-buttons");

    Button downloadBtn = new Button("Download " + latest.buildType);
    downloadBtn.setStyleName("cresuit-btn-download-hero");
    downloadBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Downloader.getInstance().download(latestDownloadUrl);
      }
    });

    Button qrBtn = new Button("QR Code");
    qrBtn.setStyleName("cresuit-btn-qr-hero");
    qrBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showQrModal("v" + latest.versionName + " (Build #" + latest.buildNumber + ")", latestQrUrl);
      }
    });

    heroButtons.add(downloadBtn);
    if (!"AAB".equalsIgnoreCase(latest.buildType)) {
      heroButtons.add(qrBtn);
    }

    Button editBtn = new Button("✏️ Edit Version");
    editBtn.setStyleName("cresuit-btn-edit-hero");
    editBtn.setTitle("Restore and edit this older version as a new project");
    editBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showEditVersionDialog(latest);
      }
    });
    heroButtons.add(editBtn);

    if (latest.hasProjectSource || !latest.aiaPath.isEmpty()) {
      final String latestAiaUrl = ServerLayout.DOWNLOAD_SERVLET_BASE
          + ServerLayout.DOWNLOAD_FILE + "/" + projectId + "/" + latest.aiaPath;
      Button aiaBtn = new Button("Download AIA");
      aiaBtn.setStyleName("cresuit-btn-aia-hero");
      aiaBtn.setTitle("Download project source archive (.aia)");
      aiaBtn.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Downloader.getInstance().download(latestAiaUrl);
        }
      });
      heroButtons.add(aiaBtn);
    }

    heroContent.add(heroButtons);
    heroContent.setCellHorizontalAlignment(heroButtons, HorizontalPanel.ALIGN_RIGHT);
    heroCard.add(heroContent);
    latestReleasePanel.add(heroCard);

    // Version History List (rendered in reverse order, newest first)
    for (int i = records.size() - 1; i >= 0; i--) {
      final BuildRecord record = records.get(i);
      final String downloadUrl = ServerLayout.DOWNLOAD_SERVLET_BASE
          + ServerLayout.DOWNLOAD_FILE + "/" + projectId + "/" + record.filePath;
      final String qrUrl = GWT.getHostPageBaseURL() + ServerLayout.DOWNLOAD_SERVLET_BASE
          + ServerLayout.DOWNLOAD_FILE + "/" + projectId + "/" + record.filePath;

      HorizontalPanel row = new HorizontalPanel();
      row.setStyleName("cresuit-history-row");
      row.setWidth("100%");

      // Left info: Version, Build Number, Date, Size, Type badge, Project Saved badge
      HorizontalPanel infoCol = new HorizontalPanel();
      infoCol.setStyleName("cresuit-history-info");

      Label vLabel = new Label("v" + record.versionName);
      vLabel.setStyleName("cresuit-history-vname");

      Label bLabel = new Label("Build " + record.buildNumber);
      bLabel.setStyleName("cresuit-history-bnum");

      Label typeBadge = new Label(record.buildType);
      typeBadge.setStyleName("cresuit-type-badge " + ("AAB".equalsIgnoreCase(record.buildType) ? "badge-aab" : "badge-apk"));

      infoCol.add(vLabel);
      infoCol.add(bLabel);
      infoCol.add(typeBadge);

      if (record.hasProjectSource || !record.aiaPath.isEmpty()) {
        Label srcBadge = new Label("📦 Project Saved");
        srcBadge.setStyleName("cresuit-source-badge");
        srcBadge.setTitle("Full project source archived on export");
        infoCol.add(srcBadge);
      }

      Label dateLabel = new Label(record.date);
      dateLabel.setStyleName("cresuit-history-date");

      Label sizeLabel = new Label(record.getFormattedSize());
      sizeLabel.setStyleName("cresuit-history-size");

      infoCol.add(dateLabel);
      infoCol.add(sizeLabel);
      row.add(infoCol);

      // Right actions: Edit Version, AIA, Download, QR, Delete
      HorizontalPanel actionsCol = new HorizontalPanel();
      actionsCol.setStyleName("cresuit-history-actions");

      Button rowEditBtn = new Button("✏️ Edit");
      rowEditBtn.setStyleName("cresuit-btn-edit-sm");
      rowEditBtn.setTitle("Restore and edit this older version as a new project");
      rowEditBtn.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showEditVersionDialog(record);
        }
      });
      actionsCol.add(rowEditBtn);

      if (record.hasProjectSource || !record.aiaPath.isEmpty()) {
        final String aiaDownloadUrl = ServerLayout.DOWNLOAD_SERVLET_BASE
            + ServerLayout.DOWNLOAD_FILE + "/" + projectId + "/" + record.aiaPath;
        Button rowAiaBtn = new Button("AIA");
        rowAiaBtn.setStyleName("cresuit-btn-download-sm");
        rowAiaBtn.setTitle("Download project source archive (.aia)");
        rowAiaBtn.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Downloader.getInstance().download(aiaDownloadUrl);
          }
        });
        actionsCol.add(rowAiaBtn);
      }

      Button rowDownloadBtn = new Button("Download");
      rowDownloadBtn.setStyleName("cresuit-btn-download-sm");
      rowDownloadBtn.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Downloader.getInstance().download(downloadUrl);
        }
      });
      actionsCol.add(rowDownloadBtn);

      if (!"AAB".equalsIgnoreCase(record.buildType)) {
        Button rowQrBtn = new Button("QR");
        rowQrBtn.setStyleName("cresuit-btn-qr-sm");
        rowQrBtn.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            showQrModal("v" + record.versionName + " (Build #" + record.buildNumber + ")", qrUrl);
          }
        });
        actionsCol.add(rowQrBtn);
      }

      Button rowDeleteBtn = new Button("🗑");
      rowDeleteBtn.setStyleName("cresuit-btn-delete-sm");
      rowDeleteBtn.setTitle("Delete this build version");
      rowDeleteBtn.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          deleteVersion(record, records);
        }
      });
      actionsCol.add(rowDeleteBtn);

      row.add(actionsCol);
      row.setCellHorizontalAlignment(actionsCol, HorizontalPanel.ALIGN_RIGHT);
      historyContainer.add(row);
    }
  }

  private void deleteVersion(final BuildRecord toDelete, final List<BuildRecord> records) {
    if (!Window.confirm("Are you sure you want to delete Build #" + toDelete.buildNumber + " (" + toDelete.fileName + ")?")) {
      return;
    }

    statusLabel.setText("Deleting version #" + toDelete.buildNumber + "...");
    statusLabel.setVisible(true);

    final String aiaPathToDelete = toDelete.aiaPath;
    Ode.getInstance().getProjectService().deleteFile(
        Ode.getInstance().getSessionId(),
        projectId,
        toDelete.filePath,
        new OdeAsyncCallback<Long>("Error deleting build artifact") {
          @Override
          public void onSuccess(Long result) {
            if (aiaPathToDelete != null && !aiaPathToDelete.isEmpty()) {
              Ode.getInstance().getProjectService().deleteFile(
                  Ode.getInstance().getSessionId(),
                  projectId,
                  aiaPathToDelete,
                  new OdeAsyncCallback<Long>("Error deleting build source archive") {
                    @Override
                    public void onSuccess(Long res) {}
                  });
            }
            records.remove(toDelete);
            saveHistoryJson(records);
          }

          @Override
          public void onFailure(Throwable caught) {
            if (aiaPathToDelete != null && !aiaPathToDelete.isEmpty()) {
              Ode.getInstance().getProjectService().deleteFile(
                  Ode.getInstance().getSessionId(),
                  projectId,
                  aiaPathToDelete,
                  new OdeAsyncCallback<Long>("Error deleting build source archive") {
                    @Override
                    public void onSuccess(Long res) {}
                  });
            }
            records.remove(toDelete);
            saveHistoryJson(records);
          }
        });
  }

  private void saveHistoryJson(final List<BuildRecord> records) {
    JSONArray arr = new JSONArray();
    for (int i = 0; i < records.size(); i++) {
      BuildRecord r = records.get(i);
      JSONObject o = new JSONObject();
      o.put("buildNumber", new JSONNumber(r.buildNumber));
      o.put("versionCode", new JSONString(r.versionCode));
      o.put("versionName", new JSONString(r.versionName));
      o.put("buildType", new JSONString(r.buildType));
      o.put("fileName", new JSONString(r.fileName));
      o.put("filePath", new JSONString(r.filePath));
      o.put("fileSize", new JSONNumber(r.fileSize));
      if (!r.aiaPath.isEmpty()) {
        o.put("aiaPath", new JSONString(r.aiaPath));
      }
      if (!r.aiaFileName.isEmpty()) {
        o.put("aiaFileName", new JSONString(r.aiaFileName));
      }
      if (r.aiaSize > 0) {
        o.put("aiaSize", new JSONNumber(r.aiaSize));
      }
      if (r.hasProjectSource) {
        o.put("hasProjectSource", JSONBoolean.getInstance(true));
      }
      o.put("date", new JSONString(r.date));
      o.put("fullDate", new JSONString(r.fullDate));
      o.put("status", new JSONString(r.status));
      arr.set(i, o);
    }

    Ode.getInstance().getProjectService().save(
        Ode.getInstance().getSessionId(),
        projectId,
        "build/history/history.json",
        arr.toString(),
        new OdeAsyncCallback<Long>("Error updating version history") {
          @Override
          public void onSuccess(Long result) {
            statusLabel.setVisible(false);
            renderVersions(records);
          }

          @Override
          public void onFailure(Throwable caught) {
            statusLabel.setVisible(false);
            renderVersions(records);
          }
        });
  }

  private void showEditVersionDialog(final BuildRecord record) {
    final DialogBox editBox = new DialogBox(false, true);
    editBox.setStylePrimaryName("ode-DialogBox");
    editBox.addStyleName("cresuit-restore-dialog");
    editBox.setText("Edit Older Version - v" + record.versionName);
    editBox.setGlassEnabled(true);
    editBox.setAnimationEnabled(true);

    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("440px");
    panel.setStyleName("cresuit-restore-panel");

    Label title = new Label("Edit Version " + record.versionName + " (Build #" + record.buildNumber + ")");
    title.setStyleName("cresuit-restore-title");
    panel.add(title);

    Label desc = new Label("This will restore the complete project snapshot from when this "
        + record.buildType + " was released, allowing you to edit the older version as an independent project.");
    desc.setStyleName("cresuit-restore-desc");
    panel.add(desc);

    Label nameLabel = new Label("New Project Name:");
    nameLabel.setStyleName("cresuit-restore-field-label");
    panel.add(nameLabel);

    String cleanVersion = record.versionName.replaceAll("[^a-zA-Z0-9_]", "_");
    String baseName = projectName + "_v" + cleanVersion;
    String suggestedName = baseName;
    int counter = 1;
    while (Ode.getInstance().getProjectManager().getProject(suggestedName) != null) {
      suggestedName = baseName + "_" + counter;
      counter++;
    }

    final TextBox nameBox = new TextBox();
    nameBox.setText(suggestedName);
    nameBox.setStyleName("cresuit-restore-input");
    nameBox.setWidth("100%");
    panel.add(nameBox);

    final Label errorLabel = new Label();
    errorLabel.setStyleName("cresuit-restore-error");
    errorLabel.setVisible(false);
    panel.add(errorLabel);

    HorizontalPanel btnPanel = new HorizontalPanel();
    btnPanel.setWidth("100%");
    btnPanel.setStyleName("cresuit-restore-btn-panel");

    Button cancelBtn = new Button("Cancel");
    cancelBtn.setStyleName("cresuit-btn-cancel");
    cancelBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        editBox.hide();
      }
    });

    Button restoreBtn = new Button("Restore & Edit Project");
    restoreBtn.setStyleName("cresuit-btn-restore-confirm");
    restoreBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final String newProjectName = nameBox.getText().trim();
        if (newProjectName.isEmpty()) {
          errorLabel.setText("Project name cannot be empty");
          errorLabel.setVisible(true);
          return;
        }
        if (TextValidators.checkNewProjectName(newProjectName, true) != TextValidators.ProjectNameStatus.SUCCESS) {
          errorLabel.setText("Invalid project name. Names must start with a letter and contain only letters, numbers, and underscores.");
          errorLabel.setVisible(true);
          return;
        }
        if (Ode.getInstance().getProjectManager().getProject(newProjectName) != null) {
          errorLabel.setText("A project with this name already exists");
          errorLabel.setVisible(true);
          return;
        }

        editBox.hide();
        statusLabel.setText("Restoring project version " + record.versionName + "...");
        statusLabel.setVisible(true);

        if (record.aiaPath != null && !record.aiaPath.isEmpty()) {
          Ode.getInstance().getProjectService().restoreProjectVersion(
              projectId,
              record.aiaPath,
              newProjectName,
              new OdeAsyncCallback<UserProject>("Error restoring project version") {
                @Override
                public void onSuccess(UserProject restoredProject) {
                  statusLabel.setVisible(false);
                  Project newProject = Ode.getInstance().getProjectManager().addProject(restoredProject);
                  hide();
                  Ode.getInstance().openYoungAndroidProjectInDesigner(newProject);
                }

                @Override
                public void onFailure(Throwable caught) {
                  statusLabel.setText("Failed to restore version: " + caught.getMessage());
                  statusLabel.setVisible(true);
                }
              });
        } else {
          // Fallback for legacy builds created before automatic .aia archival
          Ode.getInstance().getProjectService().copyProject(
              projectId,
              newProjectName,
              new OdeAsyncCallback<UserProject>("Error creating project version") {
                @Override
                public void onSuccess(UserProject copiedProject) {
                  statusLabel.setVisible(false);
                  Project newProject = Ode.getInstance().getProjectManager().addProject(copiedProject);
                  hide();
                  Ode.getInstance().openYoungAndroidProjectInDesigner(newProject);
                }

                @Override
                public void onFailure(Throwable caught) {
                  statusLabel.setText("Failed to create version: " + caught.getMessage());
                  statusLabel.setVisible(true);
                }
              });
        }
      }
    });

    btnPanel.add(cancelBtn);
    btnPanel.add(restoreBtn);
    btnPanel.setCellHorizontalAlignment(restoreBtn, HorizontalPanel.ALIGN_RIGHT);
    panel.add(btnPanel);

    editBox.add(panel);
    editBox.center();
    nameBox.selectAll();
    nameBox.setFocus(true);
  }

  private void showQrModal(String label, String url) {
    final DialogBox qrBox = new DialogBox(true, true);
    qrBox.setStylePrimaryName("ode-DialogBox");
    qrBox.setText("Scan QR Code - " + label);
    qrBox.setGlassEnabled(true);

    VerticalPanel vp = new VerticalPanel();
    vp.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
    vp.setStyleName("cresuit-qr-modal");

    HTML qrHtml = new HTML("<center>" + BlocklyPanel.getQRCode(url) + "</center>");
    qrHtml.setStyleName("cresuit-qr-image");
    vp.add(qrHtml);

    Label hint = new Label("Scan with your phone's camera to install");
    hint.setStyleName("cresuit-qr-hint");
    vp.add(hint);

    Button close = new Button("Close");
    close.setStyleName("cresuit-btn-close-modal");
    close.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        qrBox.hide();
      }
    });
    vp.add(close);

    qrBox.add(vp);
    qrBox.center();
  }
}
