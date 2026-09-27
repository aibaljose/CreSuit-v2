// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar.View;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidPackageNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.HashSet;
import java.util.Set;

/**
 * A command that creates a new form.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class AddFormCommand extends ChainableCommand {


  private static final int MAX_FORM_COUNT = 10;

  /**
   * Creates a new command for creating a new form
   */
  public AddFormCommand() {
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(ProjectNode node) {
    if (node instanceof YoungAndroidProjectNode) {
      new NewFormDialog((YoungAndroidProjectNode) node).center();
    } else {
      executionFailedOrCanceled();
      throw new IllegalArgumentException("node must be a YoungAndroidProjectNode");
    }
  }

  /**
   * Dialog for getting the name for the new form.
   */
  private class NewFormDialog extends DialogBox {
    // UI elements
    private final LabeledTextBox newNameTextBox;
    private final CheckBox aiCheckBox;
    private final VerticalPanel aiPromptPanel;
    private final TextArea promptArea;
    private final Label statusLabel;
    private final Button okButton;
    private final Button cancelButton;

    private final Set<String> otherFormNames;

    NewFormDialog(final YoungAndroidProjectNode projectRootNode) {
      super(false, true);

      setStylePrimaryName("ode-DialogBox");
      setText(MESSAGES.newFormTitle());
      VerticalPanel contentPanel = new VerticalPanel();

      final String prefix = "Screen";
      final int prefixLength = prefix.length();
      int highIndex = 0;
      // Collect the existing form names so we can prevent duplicate form names.
      otherFormNames = new HashSet<String>();

      for (ProjectNode source : projectRootNode.getAllSourceNodes()) {
        if (source instanceof YoungAndroidFormNode) {
          String formName = ((YoungAndroidFormNode) source).getFormName();
          otherFormNames.add(formName);

          if (formName.startsWith(prefix)) {
            try {
              highIndex = Math.max(highIndex, Integer.parseInt(formName.substring(prefixLength)));
            } catch (NumberFormatException e) {
              continue;
            }
          }
        }
      }

      String defaultFormName = prefix + (highIndex + 1);

      newNameTextBox = new LabeledTextBox(MESSAGES.formNameLabel());
      newNameTextBox.setText(defaultFormName);
      newNameTextBox.getTextBox().addKeyDownHandler(new KeyDownHandler() {
        @Override
        public void onKeyDown(KeyDownEvent event) {
          int keyCode = event.getNativeKeyCode();
          if (keyCode == KeyCodes.KEY_ENTER) {
            handleOkClick(projectRootNode);
          } else if (keyCode == KeyCodes.KEY_ESCAPE) {
            hide();
            executionFailedOrCanceled();
          }
        }
      });
      contentPanel.add(newNameTextBox);

      aiCheckBox = new CheckBox(" ✨ Generate with Gemini AI");
      aiCheckBox.setStyleName("ode-CheckBox");
      aiCheckBox.getElement().getStyle().setProperty("margin", "10px 0 6px 0");
      aiCheckBox.getElement().getStyle().setProperty("fontWeight", "bold");
      contentPanel.add(aiCheckBox);

      aiPromptPanel = new VerticalPanel();
      aiPromptPanel.setWidth("100%");
      aiPromptPanel.setVisible(false);

      Label promptLabel = new Label("Describe screen UI and blocks logic:");
      promptLabel.getElement().getStyle().setProperty("fontSize", "12px");
      promptLabel.getElement().getStyle().setProperty("marginBottom", "4px");
      aiPromptPanel.add(promptLabel);

      promptArea = new TextArea();
      promptArea.setVisibleLines(4);
      promptArea.getElement().setAttribute("placeholder", "e.g., A doctor profile screen with doctor details, specialties, appointment booking button, and notification alerts.");
      promptArea.setWidth("100%");
      promptArea.getElement().getStyle().setProperty("boxSizing", "border-box");
      promptArea.getElement().getStyle().setProperty("resize", "vertical");
      promptArea.getElement().getStyle().setProperty("padding", "6px");
      aiPromptPanel.add(promptArea);

      contentPanel.add(aiPromptPanel);

      aiCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          aiPromptPanel.setVisible(event.getValue());
          if (event.getValue()) {
            promptArea.setFocus(true);
          }
        }
      });

      statusLabel = new Label();
      statusLabel.getElement().getStyle().setProperty("fontSize", "12px");
      statusLabel.getElement().getStyle().setProperty("margin", "6px 0");
      contentPanel.add(statusLabel);

      String cancelText = MESSAGES.cancelButton();
      String okText = MESSAGES.okButton();

      // Keeps track of the total number of screens.
      int formCount = otherFormNames.size() + 1;
      if (formCount > MAX_FORM_COUNT) {
        HorizontalPanel errorPanel = new HorizontalPanel();
        HTML tooManyScreensLabel = new HTML(MESSAGES.formCountErrorLabel());
        errorPanel.add(tooManyScreensLabel);
        errorPanel.setSize("100%", "24px");
        contentPanel.add(errorPanel);

        okText = MESSAGES.addScreenButton();
        cancelText = MESSAGES.cancelScreenButton();
      }

      cancelButton = new Button(cancelText);
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
          executionFailedOrCanceled();
        }
      });
      okButton = new Button(okText);
      okButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handleOkClick(projectRootNode);
        }
      });
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.add(cancelButton);
      buttonPanel.add(okButton);
      buttonPanel.setSize("100%", "24px");
      contentPanel.add(buttonPanel);
      contentPanel.setSize("360px", "100%");

      add(contentPanel);
    }

    private void handleOkClick(YoungAndroidProjectNode projectRootNode) {
      String newFormName = newNameTextBox.getText();
      if (validate(newFormName)) {
        if (aiCheckBox.getValue()) {
          String prompt = promptArea.getText().trim();
          if (prompt.isEmpty()) {
            Window.alert("Please provide a prompt describing your screen and blocks.");
            promptArea.setFocus(true);
            return;
          }
          okButton.setEnabled(false);
          cancelButton.setEnabled(false);
          statusLabel.getElement().getStyle().setProperty("color", "#1a73e8");
          statusLabel.setText("Generating screen and blocks with Gemini AI... Please wait.");
          addFormActionWithAi(projectRootNode, newFormName, prompt, this, okButton, cancelButton, statusLabel);
        } else {
          hide();
          addFormAction(projectRootNode, newFormName);
        }
      } else {
        newNameTextBox.setFocus(true);
      }
    }

    private boolean validate(String newFormName) {
      // Check that it meets the formatting requirements.
      if (!TextValidators.isValidIdentifier(newFormName)) {
        Window.alert(MESSAGES.malformedFormNameError());
        return false;
      }

      // Check for reserved words
      if(TextValidators.isReservedName(newFormName)) {
        Window.alert(MESSAGES.reservedNameError());
        return false;
      }

      // Check that it's unique.
      if (otherFormNames.contains(newFormName)) {
        Window.alert(MESSAGES.duplicateFormNameError());
        return false;
      }

      return true;
    }

    /**
     * Adds a new form to the project.
     *
     * @param formName the new form name
     */
    protected void addFormAction(final YoungAndroidProjectNode projectRootNode, 
        final String formName) {
      final Ode ode = Ode.getInstance();
      final YoungAndroidPackageNode packageNode = projectRootNode.getPackageNode();
      String qualifiedFormName = packageNode.getPackageName() + '.' + formName;
      final String formFileId = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
      final String blocksFileId = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);

      OdeAsyncCallback<Long> callback = new OdeAsyncCallback<Long>(
          // failure message
          MESSAGES.addFormError()) {
        @Override
        public void onSuccess(Long modDate) {
          final Ode ode = Ode.getInstance();
          ode.updateModificationDate(projectRootNode.getProjectId(), modDate);

          // Add the new form and blocks nodes to the project
          final Project project = ode.getProjectManager().getProject(projectRootNode);
          project.addNode(packageNode, new YoungAndroidFormNode(formFileId));
          project.addNode(packageNode, new YoungAndroidBlocksNode(blocksFileId));

          // Add the screen to the DesignToolbar and select the new form editor. 
          // We need to do this once the form editor and blocks editor have been
          // added to the project editor (after the files are completely loaded).
          //
          // TODO(sharon): if we create YaProjectEditor.addScreen() and merge
          // that with the current work done in YaProjectEditor.addFormEditor,
          // consider moving this deferred work to the explicit command for
          // after the form file is loaded.
          Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
              ProjectEditor projectEditor = 
                  ode.getEditorManager().getOpenProjectEditor(project.getProjectId());
              FileEditor formEditor = projectEditor.getFileEditor(formFileId);
              FileEditor blocksEditor = projectEditor.getFileEditor(blocksFileId);
              if (formEditor != null && blocksEditor != null && projectEditor.isEditorInDeck(formEditor) && projectEditor.isEditorInDeck(blocksEditor) && !ode.screensLocked()) {
                DesignToolbar designToolbar = Ode.getInstance().getDesignToolbar();
                long projectId = formEditor.getProjectId();
                designToolbar.addScreen(projectId, formName, formEditor, 
                    blocksEditor);
                designToolbar.switchToScreen(projectId, formName, View.DESIGNER);
                executeNextCommand(projectRootNode);
              } else {
                // The form editor and/or blocks editor is still not there. Try again later.
                Scheduler.get().scheduleDeferred(this);
              }
            }
          });

        }

        @Override
        public void onFailure(Throwable caught) {
          super.onFailure(caught);
          executionFailedOrCanceled();
        }
      };

      // Create the new form on the backend. The backend will create the form (.scm) and blocks
      // (.blk) files.
      ode.getProjectService().addFile(projectRootNode.getProjectId(), formFileId, callback);
    }

    /**
     * Adds a new form to the project using Gemini AI to generate UI and blocks.
     *
     * @param projectRootNode the project root node
     * @param formName the new form name
     * @param prompt natural language description of screen and blocks
     * @param dialog the dialog box
     * @param okBtn the ok button to re-enable on error
     * @param cancelBtn the cancel button to re-enable on error
     * @param statusLbl status label to show progress/errors
     */
    protected void addFormActionWithAi(final YoungAndroidProjectNode projectRootNode,
        final String formName, final String prompt, final DialogBox dialog,
        final Button okBtn, final Button cancelBtn, final Label statusLbl) {
      final Ode ode = Ode.getInstance();
      final YoungAndroidPackageNode packageNode = projectRootNode.getPackageNode();
      String qualifiedFormName = packageNode.getPackageName() + '.' + formName;
      final String formFileId = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
      final String blocksFileId = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);

      OdeAsyncCallback<Long> callback = new OdeAsyncCallback<Long>("Failed to generate screen with Gemini AI") {
        @Override
        public void onSuccess(Long modDate) {
          dialog.hide();
          final Ode ode = Ode.getInstance();
          ode.updateModificationDate(projectRootNode.getProjectId(), modDate);

          // Add the new form and blocks nodes to the project
          final Project project = ode.getProjectManager().getProject(projectRootNode);
          project.addNode(packageNode, new YoungAndroidFormNode(formFileId));
          project.addNode(packageNode, new YoungAndroidBlocksNode(blocksFileId));

          Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
              ProjectEditor projectEditor =
                  ode.getEditorManager().getOpenProjectEditor(project.getProjectId());
              FileEditor formEditor = projectEditor.getFileEditor(formFileId);
              FileEditor blocksEditor = projectEditor.getFileEditor(blocksFileId);
              if (formEditor != null && blocksEditor != null && projectEditor.isEditorInDeck(formEditor) && projectEditor.isEditorInDeck(blocksEditor) && !ode.screensLocked()) {
                DesignToolbar designToolbar = Ode.getInstance().getDesignToolbar();
                long projectId = formEditor.getProjectId();
                designToolbar.addScreen(projectId, formName, formEditor, blocksEditor);
                designToolbar.switchToScreen(projectId, formName, View.DESIGNER);
                executeNextCommand(projectRootNode);
              } else {
                Scheduler.get().scheduleDeferred(this);
              }
            }
          });
        }

        @Override
        public void onFailure(Throwable caught) {
          okBtn.setEnabled(true);
          cancelBtn.setEnabled(true);
          statusLbl.getElement().getStyle().setProperty("color", "#d93025");
          statusLbl.setText("Error: " + caught.getMessage());
          executionFailedOrCanceled();
        }
      };

      ode.getProjectService().createScreenWithAi(projectRootNode.getProjectId(), formFileId, prompt, callback);
    }

    @Override
    public void show() {
      super.show();

      Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          newNameTextBox.setFocus(true);
        }
      });
    }
  }
}
