// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.appinventor.components.annotations.Asset;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.HorizontalAlignment;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.VerticalAlignment;

import com.google.appinventor.components.runtime.util.AlignmentUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;

// NOTE: Do NOT `import androidx.cardview.widget.CardView;` (or the Material
// equivalent) unqualified — this component class is itself named `CardView`,
// so an unqualified import creates an unresolvable name collision. Every
// reference to the Android widget below uses its fully-qualified name instead.
// If you want true Material theming (stroke color, checked state, etc.) swap
// the FQN below to com.google.android.material.card.MaterialCardView, which
// is a drop-in subclass of androidx.cardview.widget.CardView.

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for components that arranges them in a Material Design card
 * (elevation + rounded corners), backed by an AndroidX CardView.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author kkashi01@gmail.com (Hossein Amerkashi) (added Image and BackgroundColors)
 */
@SuppressWarnings("AbbreviationAsWordInName")
@DesignerComponent(
    version = 1,
    description = "A Material Design CardView container.",
    category = ComponentCategory.LAYOUT,
    nonVisible = false,
    iconName = "images/vertical.png")
@SimpleObject
public class CardView extends AndroidViewComponent implements Component, ComponentContainer {
  private final Activity context;

  // Fully-qualified: avoids collision with this class's own name.
  private final androidx.cardview.widget.CardView cardView;
  private final LinearLayout viewLayout;

  // Translates App Inventor alignment codes to Android gravity.
  private final AlignmentUtil alignmentSetter;

  // The alignment for this component's LinearLayout.
  private HorizontalAlignment horizontalAlignment = HorizontalAlignment.Left;
  private VerticalAlignment verticalAlignment = VerticalAlignment.Top;

  // Backing for background color / card styling.
  private int backgroundColor;
  private float cornerRadius = 8;
  private float cardElevation = 4;

  // Set if a background Image was successfully loaded; null otherwise.
  private Drawable backgroundImageDrawable;
  private String imagePath = "";

  // List of component children.
  private final List<Component> allChildren = new ArrayList<>();

  private final Handler androidUIHandler = new Handler();

  private static final String LOG_TAG = "CardView";

  /**
   * Creates a new CardView component.
   *
   * @param container container the component will be placed in
   */
  public CardView(ComponentContainer container) {
    super(container);
    context = container.$context();

    // Fully-qualified constructor call — resolves to the AndroidX widget,
    // not this class.
    cardView = new androidx.cardview.widget.CardView(context);

    viewLayout = new LinearLayout(
        context,
        ComponentConstants.LAYOUT_ORIENTATION_VERTICAL,
        ComponentConstants.EMPTY_HV_ARRANGEMENT_WIDTH,
        ComponentConstants.EMPTY_HV_ARRANGEMENT_HEIGHT);

    viewLayout.setBaselineAligned(false);

    alignmentSetter = new AlignmentUtil(viewLayout);
    alignmentSetter.setHorizontalAlignment(horizontalAlignment);
    alignmentSetter.setVerticalAlignment(verticalAlignment);

    cardView.setLayoutParams(new ViewGroup.LayoutParams(
        ComponentConstants.EMPTY_HV_ARRANGEMENT_WIDTH,
        ComponentConstants.EMPTY_HV_ARRANGEMENT_HEIGHT));

    cardView.addView(
        viewLayout.getLayoutManager(),
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));



    container.$add(this);
    BackgroundColor(Component.COLOR_DEFAULT);
    CornerRadius(8);
    Elevation(4);
  }

  // ComponentContainer implementation

  @Override
  public Activity $context() {
    return context;
  }

  @Override
  public Form $form() {
    return container.$form();
  }

  @Override
  public void $add(AndroidViewComponent component) {
    viewLayout.add(component);
    allChildren.add(component);
  }

  @Override
  public List<? extends Component> getChildren() {
    return allChildren;
  }

  @Override
  public void setChildWidth(final AndroidViewComponent component, int width) {
    setChildWidth(component, width, 0);
  }

  public void setChildWidth(final AndroidViewComponent component, int width, final int trycount) {
    int cWidth = container.$form().Width();
    if (cWidth == 0 && trycount < 2) {     // We're not really ready yet...
      final int fWidth = width;            // but give up after two tries...
      androidUIHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          Log.d(LOG_TAG, "(CardView) Width not stable yet... trying again");
          setChildWidth(component, fWidth, trycount + 1);
        }
      }, 100);
      return;
    }
    if (width <= LENGTH_PERCENT_TAG) {
      width = cWidth * (-(width - LENGTH_PERCENT_TAG)) / 100;
    }
    component.setLastWidth(width);
    ViewUtil.setChildWidthForVerticalLayout(component.getView(), width);
  }

  @Override
  public void setChildHeight(final AndroidViewComponent component, int height) {
    int cHeight = container.$form().Height();
    if (cHeight == 0) {         // Not ready yet...
      final int fHeight = height;
      androidUIHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          Log.d(LOG_TAG, "(CardView) Height not stable yet... trying again");
          setChildHeight(component, fHeight);
        }
      }, 100);
      return;
    }
    if (height <= LENGTH_PERCENT_TAG) {
      height = cHeight * (-(height - LENGTH_PERCENT_TAG)) / 100;
    }
    component.setLastHeight(height);
    ViewUtil.setChildHeightForVerticalLayout(component.getView(), height);
  }

  @Override
  public void setChildNeedsLayout(AndroidViewComponent component) {
    // not needed for linear layout
  }

  // AndroidViewComponent implementation

  @Override
  public View getView() {
    return cardView;
  }

  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "A number that encodes how contents of the %type% are aligned "
          + "horizontally. The choices are: 1 = left aligned, 2 = right aligned, "
          + "3 = horizontally centered.  Alignment has no effect if the arrangement's width is "
          + "automatic.")
  public @Options(HorizontalAlignment.class) int AlignHorizontal() {
    return AlignHorizontalAbstract().toUnderlyingValue();
  }

  @SuppressWarnings("RegularMethodName")
  public HorizontalAlignment AlignHorizontalAbstract() {
    return horizontalAlignment;
  }

  @SuppressWarnings("RegularMethodName")
  public void AlignHorizontalAbstract(HorizontalAlignment alignment) {
    alignmentSetter.setHorizontalAlignment(alignment);
    horizontalAlignment = alignment;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT,
      defaultValue = ComponentConstants.HORIZONTAL_ALIGNMENT_DEFAULT + "")
  @SimpleProperty
  public void AlignHorizontal(@Options(HorizontalAlignment.class) int alignment) {
    HorizontalAlignment align = HorizontalAlignment.fromUnderlyingValue(alignment);
    if (align == null) {
      container.$form().dispatchErrorOccurredEvent(this, "HorizontalAlignment",
          ErrorMessages.ERROR_BAD_VALUE_FOR_HORIZONTAL_ALIGNMENT, alignment);
      return;
    }
    AlignHorizontalAbstract(align);
  }

  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "A number that encodes how the contents of the %type% are aligned "
          + "vertically. The choices are: 1 = aligned at the top, 2 = vertically centered, "
          + "3 = aligned at the bottom.  Alignment has no effect if the arrangement's height "
          + "is automatic.")
  public @Options(VerticalAlignment.class) int AlignVertical() {
    return AlignVerticalAbstract().toUnderlyingValue();
  }

  @SuppressWarnings("RegularMethodName")
  public VerticalAlignment AlignVerticalAbstract() {
    return verticalAlignment;
  }

  @SuppressWarnings("RegularMethodName")
  public void AlignVerticalAbstract(VerticalAlignment alignment) {
    alignmentSetter.setVerticalAlignment(alignment);
    verticalAlignment = alignment;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VERTICAL_ALIGNMENT,
      defaultValue = ComponentConstants.VERTICAL_ALIGNMENT_DEFAULT + "")
  @SimpleProperty
  public void AlignVertical(@Options(VerticalAlignment.class) int alignment) {
    VerticalAlignment align = VerticalAlignment.fromUnderlyingValue(alignment);
    if (align == null) {
      container.$form().dispatchErrorOccurredEvent(this, "VerticalAlignment",
          ErrorMessages.ERROR_BAD_VALUE_FOR_VERTICAL_ALIGNMENT, alignment);
      return;
    }
    AlignVerticalAbstract(align);
  }

  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the background color of the %type%")
  public int BackgroundColor() {
    return backgroundColor;
  }

  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Returns the card corner radius.")
  public float CornerRadius() {
    return cornerRadius;
  }

  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Returns the card elevation.")
  public float Elevation() {
    return cardElevation;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
  @SimpleProperty(description = "Specifies the background color of the %type%. "
      + "The background color will not be visible if an Image is being displayed.")
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    updateAppearance();
  }

  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "8")
  @SimpleProperty(description = "Sets the card corner radius.")
  public void CornerRadius(float radius) {
    cornerRadius = radius;
    cardView.setRadius(radius);
  }

  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "4")
  @SimpleProperty(description = "Sets the card elevation.")
  public void Elevation(float elevation) {
    cardElevation = elevation;
    cardView.setCardElevation(elevation);
  }

  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public String Image() {
    return imagePath;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
  @SimpleProperty(description = "Specifies the path of the background image for the %type%.  "
      + "If there is both an Image and a BackgroundColor, only the Image will be visible.")
  public void Image(@Asset String path) {
    if (path.equals(imagePath) && backgroundImageDrawable != null) {
      return;
    }

    imagePath = (path == null) ? "" : path;
    backgroundImageDrawable = null;

    if (imagePath.length() > 0) {
      try {
        backgroundImageDrawable = MediaUtil.getBitmapDrawable(container.$form(), imagePath);
      } catch (IOException ioe) {
        // Fall through with a value of null for backgroundImageDrawable.
      }
    }

    updateAppearance();
  }

  // Update appearance based on values of backgroundImageDrawable, backgroundColor and shape.
  // Images take precedence over background colors.
  private void updateAppearance() {
    if (backgroundImageDrawable == null) {
      if (backgroundColor == Component.COLOR_DEFAULT) {
        // Material Design default card color is white.
        cardView.setCardBackgroundColor(Color.WHITE);
      } else {
        cardView.setCardBackgroundColor(backgroundColor);
      }
    } else {
      ViewUtil.setBackgroundImage(viewLayout.getLayoutManager(), backgroundImageDrawable);
    }
  }
}