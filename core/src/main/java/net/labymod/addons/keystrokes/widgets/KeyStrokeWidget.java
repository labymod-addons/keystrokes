/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.labymod.addons.keystrokes.widgets;

import net.labymod.addons.keystrokes.KeyStrokeConfig;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidgetConfig;
import net.labymod.addons.keystrokes.util.KeyTracker;
import net.labymod.api.Laby;
import net.labymod.api.client.gfx.pipeline.RenderAttributes;
import net.labymod.api.client.gfx.pipeline.RenderAttributesStack;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.state.RoundedData;
import net.labymod.api.client.gui.screen.state.ScreenCanvas;
import net.labymod.api.client.gui.screen.state.TextFlags;
import net.labymod.api.client.gui.screen.state.states.GuiRectangleRenderState.RectConfig;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.BoundsType;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.Color;
import net.labymod.api.util.color.format.ColorFormat;

public class KeyStrokeWidget extends SimpleWidget {

  public static final int PADDING = 4;

  private final Key key;
  private final KeyStrokeConfig keyStroke;
  private final KeyStrokesHudWidgetConfig defaultConfig;

  public KeyStrokeWidget(Key key, KeyStrokeConfig keyStroke,
      KeyStrokesHudWidgetConfig defaultConfig) {
    this.key = key;
    this.keyStroke = keyStroke;
    this.defaultConfig = defaultConfig;
  }

  @Override
  public void renderWidget(ScreenContext context) {
    super.renderWidget(context);

    Bounds bounds = this.bounds();
    boolean transition = this.defaultConfig.transition().get();
    float transitionProgress = !transition ? 1.0F : this.getTransitionProgress();
    boolean renderPressedSeparate = transitionProgress != 1.0F && transitionProgress != 0.0F;

    int backgroundColor;
    if (renderPressedSeparate) {
      backgroundColor = this.defaultConfig.backgroundColor().get().get();
    } else {
      backgroundColor = this.keyStroke.isPressed() ? this.defaultConfig.pressedColor().get().get()
          : this.defaultConfig.backgroundColor().get().get();
    }

    ScreenCanvas canvas = context.canvas();

    int textColor = this.getTextColor();
    boolean roundedCorners = this.defaultConfig.roundedCorners().get();
    boolean outline = this.defaultConfig.outline().get();

    float borderRadius = 0;

    if (roundedCorners) {
      Float scale = this.defaultConfig.scale().get();
      float height = this.bounds().getHeight() * scale;
      float radius = this.defaultConfig.roundedRadius().get() * scale;
      borderRadius = Math.min(height / 2, radius);
    }

    int outlineColor = !outline || this.defaultConfig.useTextColorAsOutline().get()
        ? textColor
        : this.defaultConfig.outlineColor().get().get();

    Stack stack = context.stack();
    canvas.submitGuiRect(
        bounds.rectangle(BoundsType.OUTER),
        RectConfig.builder()
            .setArgb(backgroundColor)
            .setRoundedData(RoundedData.builder()
                .setRadius(borderRadius)
                .setUpperEdgeSoftness(0.2F)
                .setLowerEdgeSoftness(-0.5F)
                .setBorderColor(outlineColor)
                .setBorderThickness(outline ? 1 : 0)
                .build())
            .build()
    );

    if (renderPressedSeparate) {
      float width = bounds.getWidth() * transitionProgress;
      float height = bounds.getHeight() * transitionProgress;

      float x = bounds.getX() + (bounds.getWidth() - width) / 2;
      float y = bounds.getY() + (bounds.getHeight() - height) / 2;
      Color color = this.defaultConfig.pressedColor().get();

      canvas.submitGuiRect(
          x, y, width, height,
          RectConfig.builder()
              .setArgb(ColorFormat.ARGB32.pack(
                  color.get(),
                  (int) (color.getAlpha() * transitionProgress)
              ))
              .setRoundedData(RoundedData.builder()
                  .setRadius(roundedCorners ? borderRadius : 0)
                  .setUpperEdgeSoftness(0.2F)
                  .setLowerEdgeSoftness(-0.5F)
                  .setBorderColor(outlineColor)
                  .setBorderThickness(outline ? 1 : 0)
                  .build())
              .build()
      );
    }

    if (this.key == Key.SPACE && this.defaultConfig.fancySpace().get()) {
      float spaceWidth = bounds.getWidth() / 2 / 2;
      RenderAttributesStack renderAttributesStack = Laby.references()
          .renderEnvironmentContext()
          .renderAttributesStack();
      RenderAttributes attributes = renderAttributesStack.last();
      canvas.submitGuiRect(
          bounds.getCenterX() - spaceWidth,
          bounds.getY() + 5,
          spaceWidth * 2,
          attributes.isForceVanillaFont() ? 1.0F : 1.5F,
          RectConfig.builder()
              .setArgb(textColor)
              .setRoundedData(RoundedData.builder()
                  .setRadius(attributes.isForceVanillaFont() ? 0 : 1f)
                  .setUpperEdgeSoftness(0.2F)
                  .setLowerEdgeSoftness(-0.5F)
                  .build())
              .build()
      );
      return;
    }

    KeyTracker keyTracker = this.keyStroke.getKeyTracker();
    float fontHeight = canvas.getLineHeight();
    float y = bounds.getCenterY() - (fontHeight / 2) + 0.8F;
    if (keyTracker != null) {
      y -= fontHeight * 0.25F;
    }

    float x = bounds.getCenterX() + 0.4F;
    canvas.submitText(
        this.keyStroke.getKeyName(),
        x, y,
        textColor,
        1.0F,
        TextFlags.SHADOW | TextFlags.CENTERED | TextFlags.USE_FLOATING_POINT_VALUES
    );

    if (keyTracker != null) {
      stack.push();
      stack.translate(x, y, 0);
      stack.scale(0.5F);
      canvas.submitText(
          keyTracker.getCount() + " CPS",
          0, fontHeight * 1.5F + 3,
          textColor,
          1.0F,
          TextFlags.SHADOW | TextFlags.CENTERED | TextFlags.USE_FLOATING_POINT_VALUES
      );
      stack.pop();
    }
  }

  private int getBackgroundColor() {
    boolean pressed = this.keyStroke.isPressed();
    if (pressed) {
      return this.defaultConfig.pressedColor().get().get();
    }

    return this.defaultConfig.backgroundColor().get().get();
  }

  private int getTextColor() {
    return this.defaultConfig.textColor().get().get();
  }

  public Key key() {
    return this.key;
  }

  public KeyStrokeConfig config() {
    return this.keyStroke;
  }

  private float getTransitionProgress() {
    long timeElapsed = System.currentTimeMillis() - this.keyStroke.getLastPressedUpdate();
    if (timeElapsed > 100) {
      return 1.0F;
    }

    if (!this.keyStroke.isPressed()) {
      return 1.0F - timeElapsed / 100.0F;
    }

    return timeElapsed / 100.0F;
  }
}
