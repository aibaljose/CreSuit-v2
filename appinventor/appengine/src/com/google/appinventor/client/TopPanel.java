// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.client.Ode.getSystemConfig;

import com.google.appinventor.client.actions.SelectLanguage;

import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.appinventor.client.widgets.TextButton;

import com.google.appinventor.shared.rpc.user.Config;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Logger;

/**
 * The top panel, which contains the main menu, various links plus ads.
 *
 */
public class TopPanel extends Composite {
  // Strings for links and dropdown menus:

  interface TopPanelUiBinder extends UiBinder<FlowPanel, TopPanel> {}

  private static final String WIDGET_NAME_LANGUAGE = "Language";
  private static final String WIDGET_NAME_DELETE_ACCOUNT = "DeleteAccount";
  public static final String WINDOW_OPEN_FEATURES = "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes";
  public static final String WINDOW_OPEN_LOCATION = "_ai2";

  @UiField public TopToolbar topToolbar;
  @UiField public ImageElement logo;
  @UiField public Label readOnly;
  @UiField public FlowPanel rightPanel;
  @UiField public DropDownButton languageDropDown;
  @UiField public DropDownButton accountButton;
  @UiField public DropDownItem deleteAccountItem;
  @UiField public FlowPanel links;
  @UiField public TextButton myProjects;
  @UiField public TextButton viewTrash;
  @UiField(provided = false) public FlowPanel companionIndicator;
  @UiField(provided = false) public HTML companionStatusHtml;

  final Ode ode = Ode.getInstance();

  interface Translations extends ClientBundle {
    Translations INSTANCE = GWT.create(Translations.class);

    @Source("languages.json")
    TextResource languages();
  }

  static {
    loadLanguages(Translations.INSTANCE.languages().getText());
    LANGUAGES = Dictionary.getDictionary("LANGUAGES");
  }

  private static native void loadLanguages(String resource)/*-{
    $wnd['LANGUAGES'] = JSON.parse(resource);
  }-*/;

  private static final Dictionary LANGUAGES;
  private static final Logger LOG = Logger.getLogger(TopPanel.class.getName());

  /**
   * Initializes and assembles all UI elements shown in the top panel.
   */
  public TopPanel() {
    /*
     * The layout of the top panel is as follows:
     *
     *  +-- topPanel ------------------------------------+
     *  |+-- logo --++-----tools-----++--links/account--+|
     *  ||          ||               ||                 ||
     *  |+----------++---------------++-----------------+|
     *  +------------------------------------------------+
     */
    bindUI();
    Config config = getSystemConfig();
    String logoUrl = config.getLogoUrl();


    if (!Strings.isNullOrEmpty(logoUrl)) {
      try {
        Image wpr = Image.wrap(logo);
        wpr.addClickHandler(new WindowOpenClickHandler(logoUrl));
        logo.setAttribute("alt", "MIT App Inventor");
      } catch (AssertionError e) {
        LOG.warning("assertion error in getting Image from logo url");
      }
    }

    if (Ode.getInstance().isReadOnly()) {
      accountButton.setItemVisible(WIDGET_NAME_DELETE_ACCOUNT, false);
    } else {
      readOnly.removeFromParent();
    }

    if (Ode.getInstance().getOneProjectMode()) {
      myProjects.removeFromParent();
      viewTrash.removeFromParent();
    }

    // Language
    List<DropDownItem> languageItems = Lists.newArrayList();
    for (String localeName : LocaleInfo.getAvailableLocaleNames()) {
      if (!localeName.equals("default")) {
        languageItems.add(new DropDownItem(WIDGET_NAME_LANGUAGE, getDisplayName(localeName),
            new SelectLanguage(localeName)));
      }
    }
    languageDropDown.setItems(languageItems);
    languageDropDown.setCaption(getDisplayName(LocaleInfo.getCurrentLocale().getLocaleName()));

    // Companion Connection Indicator setup
    if (companionIndicator != null) {
      companionIndicator.addDomHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          int state = getReplState();
          if (state == 2 || state == 4 || state == 5) {
            TopToolbar toolbar = Ode.getInstance().getTopToolbar();
            if (toolbar != null) {
              toolbar.replUpdate();
            }
          }
        }
      }, ClickEvent.getType());
    }

    Timer companionTimer = new Timer() {
      @Override
      public void run() {
        updateCompanionIndicator();
      }
    };
    companionTimer.scheduleRepeating(1000);
    updateCompanionIndicator();
  }

  public void bindUI() {
    TopPanelUiBinder uibinder = GWT.create(TopPanelUiBinder.class);
    initWidget(uibinder.createAndBindUi(this));
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  public TopToolbar getTopToolbar() {
    return topToolbar;
  }

  public void updateCompanionIndicator() {
    if (companionIndicator == null) {
      return;
    }
    int state = getReplState();
    boolean isConnected = (state == 2 || state == 4 || state == 5);
    boolean isConnecting = (state == 1 || state == 3);

    if (isConnected) {
      companionIndicator.setVisible(true);
      companionIndicator.getElement().getStyle().setProperty("display", "inline-flex");
      companionIndicator.setStyleName("ode-CompanionIndicator flex items-center gap-1.5 px-3 py-1 bg-emerald-50 text-emerald-700 border border-emerald-200/90 rounded-full text-xs font-semibold shadow-2xs transition duration-200 cursor-pointer");
      if (companionStatusHtml != null) {
        companionStatusHtml.setHTML(
          "<span class=\"flex items-center gap-1.5\">" +
            "<span class=\"relative flex h-2 w-2\">" +
              "<span class=\"animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75\"></span>" +
              "<span class=\"relative inline-flex rounded-full h-2 w-2 bg-emerald-500\"></span>" +
            "</span>" +
            "<svg class=\"w-3.5 h-3.5 text-emerald-600 shrink-0\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\" stroke-width=\"2.2\">" +
              "<path stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M10.5 1.5H8.25A2.25 2.25 0 006 3.75v16.5a2.25 2.25 0 002.25 2.25h7.5A2.25 2.25 0 0018 20.25V3.75a2.25 2.25 0 00-2.25-2.25H13.5m-3 0V3h3V1.5m-3 0h3m-3 18.75h3\" />" +
            "</svg>" +
            "<span class=\"font-bold text-emerald-800 tracking-tight\">Companion Live</span>" +
          "</span>"
        );
      }
      companionIndicator.setTitle("CreSuit Companion is connected (click to refresh screen)");
    } else if (isConnecting) {
      companionIndicator.setVisible(true);
      companionIndicator.getElement().getStyle().setProperty("display", "inline-flex");
      companionIndicator.setStyleName("ode-CompanionIndicator flex items-center gap-1.5 px-3 py-1 bg-amber-50 text-amber-700 border border-amber-200/90 rounded-full text-xs font-medium shadow-2xs transition duration-200 cursor-pointer");
      if (companionStatusHtml != null) {
        companionStatusHtml.setHTML(
          "<span class=\"flex items-center gap-1.5\">" +
            "<span class=\"relative flex h-2 w-2\">" +
              "<span class=\"animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75\"></span>" +
              "<span class=\"relative inline-flex rounded-full h-2 w-2 bg-amber-500\"></span>" +
            "</span>" +
            "<svg class=\"w-3.5 h-3.5 text-amber-600 shrink-0 animate-spin\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\" stroke-width=\"2\">" +
              "<path stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15\" />" +
            "</svg>" +
            "<span class=\"font-medium text-amber-700 tracking-tight\">Connecting...</span>" +
          "</span>"
        );
      }
      companionIndicator.setTitle("Connecting to Companion...");
    } else {
      companionIndicator.setVisible(false);
      companionIndicator.getElement().getStyle().setProperty("display", "none");
    }
  }

  private static native int getReplState() /*-{
    if (top && top.ReplState) {
      return top.ReplState.state || 0;
    }
    return 0;
  }-*/;

  private String getDisplayName(String localeName){
    String nativeName=LocaleInfo.getLocaleNativeDisplayName(localeName);
    try {
      return LANGUAGES.get(localeName);
    } catch (MissingResourceException e) {
      return nativeName;
    }
  }

  /**
   * Updates the UI to show the user's email address.
   *
   * @param email the email address
   */
  public void showUserEmail(String email) {
    accountButton.setCaption(email);
  }

  private static class WindowOpenClickHandler implements ClickHandler {
    private final String url;

    WindowOpenClickHandler(String url) {
      this.url = url;
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
      Window.open(url, WINDOW_OPEN_LOCATION, WINDOW_OPEN_FEATURES);
    }
  }
}

