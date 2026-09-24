// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.blocks.BlocklyPanel;
import com.google.appinventor.client.explorer.dialogs.VersionsDialogBox;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.logging.Logger;

/**
 * Command for displaying a barcode for the target of a project.
 * This command is often chained with SaveAllEditorsCommand and BuildCommand.
 *
 * @author markf@google.com (Mark Friedman)
 */
public class ShowBarcodeCommand extends ChainableCommand {

  private static final Logger LOG = Logger.getLogger(ShowBarcodeCommand.class.getName());

  public enum BuildFormat {
    APK,
    AAB,
    IPA,
    ASC
  }

  // The build target
  private String target;
  private BuildFormat format;
  private String buildResultExtra;

  /**
   * Creates a new command for showing a barcode for the target of a project.
   *
   * @param target the build target
   */
  public ShowBarcodeCommand(String target, BuildFormat format) {
    // Since we don't know when the barcode dialog is finished, we can't
    // support a command after this one.
    super(null); // no next command
    this.target = target;
    this.format = format;
  }

  public void setBuildResultExtra(String extra) {
    this.buildResultExtra = extra;
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return false;
  }

  @Override
  public void execute(final ProjectNode node) {
    if (format == BuildFormat.ASC) {
      new AppStoreDialogBox(node.getName()).center();
      return;
    }

    String versionName = "1.0";
    String versionCode = "1";
    int buildNumber = 1;
    if (buildResultExtra != null && !buildResultExtra.trim().isEmpty()) {
      try {
        JSONValue val = JSONParser.parseStrict(buildResultExtra);
        JSONObject obj = val.isObject();
        if (obj != null) {
          if (obj.containsKey("versionName")) {
            JSONString vn = obj.get("versionName").isString();
            if (vn != null) versionName = vn.stringValue();
          }
          if (obj.containsKey("versionCode")) {
            JSONString vc = obj.get("versionCode").isString();
            if (vc != null) versionCode = vc.stringValue();
          }
          if (obj.containsKey("buildNumber")) {
            JSONNumber bn = obj.get("buildNumber").isNumber();
            if (bn != null) buildNumber = (int) bn.doubleValue();
          }
        }
      } catch (Exception e) {
        LOG.warning("Error parsing buildResultExtra: " + e.getMessage());
      }
    }

    // Display a barcode for an url pointing at our server's download servlet
    String barcodeUrl;
    if (format == BuildFormat.IPA) {
      barcodeUrl = "itms-services://?action=download-manifest&url=" + GWT.getHostPageBaseURL()
          + "b/" + Ode.getInstance().getNonce() + "/manifest.plist";
    } else {
      barcodeUrl = GWT.getHostPageBaseURL()
          + "b/" + Ode.getInstance().getNonce();
      LOG.info("Barcode url is: " + barcodeUrl);
    }
    new BarcodeDialogBox(node.getProjectId(), node.getName(), barcodeUrl, format == BuildFormat.AAB,
        format == BuildFormat.IPA, versionName, versionCode, buildNumber).center();
  }

  static class AppStoreDialogBox extends DialogBox {
    AppStoreDialogBox(String projectName) {
      super(false, true);
      setStylePrimaryName("ode-DialogBox");
      setText(MESSAGES.downloadIpaDialogTitle(projectName));
      setWidth("400px");

      VerticalPanel contentPanel = new VerticalPanel();

      contentPanel.add(new HTML(MESSAGES.continueOnAppStore()));

      ClickHandler buttonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
        }
      };
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
      Button okButton = new Button(MESSAGES.dismissButton());
      okButton.addClickHandler(buttonHandler);
      buttonPanel.add(okButton);
      buttonPanel.setSize("100%", "24px");
      contentPanel.add(buttonPanel);

      add(contentPanel);
    }
  }

  static class BarcodeDialogBox extends DialogBox {

    BarcodeDialogBox(final long projectId, final String projectName, final String appInstallUrl,
        boolean isAab, boolean isIos, String versionName, String versionCode, int buildNumber) {
      super(false, true);
      setStylePrimaryName("ode-DialogBox");
      addStyleName("cresuit-barcode-dialog");
      setText("Build Successful - " + projectName);

      final String downloadUrl;
      if (isIos) {
        downloadUrl = appInstallUrl.split("&url=")[1].replace("manifest.plist", "app.ipa");
      } else {
        downloadUrl = appInstallUrl;
      }

      // Main layout panel
      VerticalPanel contentPanel = new VerticalPanel();
      contentPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

      // Header summary
      VerticalPanel headerPanel = new VerticalPanel();
      headerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
      headerPanel.setStyleName("cresuit-barcode-header");

      Label successBadge = new Label("BUILD SUCCESSFUL");
      successBadge.setStyleName("cresuit-barcode-success-badge");

      Label appLabel = new Label(projectName);
      appLabel.setStyleName("cresuit-barcode-appname");

      Label versionLabel = new Label("Version " + versionName + " · Build #" + buildNumber);
      versionLabel.setStyleName("cresuit-barcode-versioninfo");

      headerPanel.add(successBadge);
      headerPanel.add(appLabel);
      headerPanel.add(versionLabel);
      contentPanel.add(headerPanel);

      // Container
      HorizontalPanel container = new HorizontalPanel();
      container.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

      // Container > Left
      VerticalPanel left = new VerticalPanel();
      left.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

      // Container > Left > Download Button
      ClickHandler downloadHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Window.open(downloadUrl, "_self", "enabled");
        }
      };
      HorizontalPanel downloadPanel = new HorizontalPanel();
      downloadPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
      Anchor downloadButton = new Anchor();
      downloadButton.setHref(downloadUrl);
      downloadButton.addStyleName("gwt-Button");
      downloadButton.addStyleName("download-button");
      // Container > Left > Download Button > Image
      Image downloadIcon = new Image(Ode.getImageBundle().GetApp());
      downloadIcon.setSize("110px", "100px");
      downloadIcon.addStyleName("download-icon");
      downloadButton.getElement().appendChild(downloadIcon.getElement());
      // Container > Left > Download Button > Inner Text
      SpanElement text = Document.get().createSpanElement();
      text.setInnerHTML(isIos ? MESSAGES.barcodeDownloadIpa() : isAab ? MESSAGES.barcodeDownloadAab() : MESSAGES.barcodeDownloadApk());
      downloadButton.getElement().appendChild(text);
      downloadButton.addClickHandler(downloadHandler);
      downloadPanel.add(downloadButton);
      downloadPanel.setSize("100%", "30px");
      left.add(downloadPanel);

      // Container > Left
      container.add(left);

      // The Android App Bundle should only be used to publish the app through Google Play Store. Thus,
      // it does not make sense to provide a QR code which the user might think they can scan and directly
      // install in the phone directly.
      if (!isAab) {
        // Container > Right
        VerticalPanel right = new VerticalPanel();

        // Container > Right > Barcode
        HTML barcodeQrcode = new HTML("<center>" + BlocklyPanel.getQRCode(appInstallUrl) + "</center>");
        barcodeQrcode.addStyleName("download-barcode");
        right.add(barcodeQrcode);

        // Container > Right
        container.add(right);
      }

      // Container
      contentPanel.add(container);

      // Warning
      // The warning label is added only in APK files, as there is no QR code for the AAB. It is supposed that
      // users download the bundle just when they get it.
      if (!isAab) {
        HorizontalPanel warningPanel = new HorizontalPanel();
        warningPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        HTML warningLabel = new HTML(MESSAGES.barcodeWarning2(
            "<a href=\"" + "http://appinventor.mit.edu/explore/ai2/share.html" +
                "\" target=\"_blank\">",
            "</a>"));
        warningLabel.setWordWrap(true);
        warningLabel.setWidth("400px");  // set width to get the text to wrap
        warningLabel.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
        warningPanel.add(warningLabel);
        contentPanel.add(warningPanel);
      } else {
        HorizontalPanel aabInfoPanel = new HorizontalPanel();
        aabInfoPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        Label aabInfo = new Label("📦 A complete snapshot of this project has been archived as a version. You can restore and edit it anytime from Version History.");
        aabInfo.setStyleName("cresuit-aab-info-note");
        aabInfo.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
        aabInfoPanel.add(aabInfo);
        contentPanel.add(aabInfoPanel);
      }

      // Bottom Button panel
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
      buttonPanel.setStyleName("cresuit-barcode-bottom-panel");
      buttonPanel.setWidth("100%");

      Button historyButton = new Button("Version History");
      historyButton.setStyleName("cresuit-btn-version-history");
      historyButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
          new VersionsDialogBox(projectId, projectName).center();
        }
      });
      buttonPanel.add(historyButton);

      Button okButton = new Button(MESSAGES.dismissButton());
      okButton.setStyleName("cresuit-btn-dismiss");
      okButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
        }
      });
      buttonPanel.add(okButton);

      contentPanel.add(buttonPanel);

      add(contentPanel);
    }
  }
}
