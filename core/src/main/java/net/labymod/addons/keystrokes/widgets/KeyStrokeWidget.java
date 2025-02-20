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
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.BoundsType;
import net.labymod.api.client.render.RenderPipeline;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.font.text.TextRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.Color;
import net.labymod.api.util.color.format.ColorFormat;

public class KeyStrokeWidget extends SimpleWidget {

  public static final int PADDING = 4;
  private static final RenderPipeline RENDER_PIPELINE = Laby.labyAPI().renderPipeline();
  private static final RectangleRenderer RECTANGLE_RENDERER = RENDER_PIPELINE.rectangleRenderer();

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

    RECTANGLE_RENDERER
        .pos(bounds.rectangle(BoundsType.OUTER))
        .color(backgroundColor);

    int textColor = this.getTextColor();
    boolean roundedCorners = this.defaultConfig.roundedCorners().get();
    boolean outline = this.defaultConfig.outline().get();
    if (roundedCorners) {
      RECTANGLE_RENDERER
          .rounded(5)
          .upperEdgeSoftness(0.2F);

      if (outline) {
        RECTANGLE_RENDERER
            .borderColor(textColor)
            .borderThickness(1);
      } else {
        RECTANGLE_RENDERER
            .lowerEdgeSoftness(-0.5F);
      }
    }

    Stack stack = context.stack();
    RECTANGLE_RENDERER.render(stack);

    if (renderPressedSeparate) {
      float width = bounds.getWidth() * transitionProgress;
      float height = bounds.getHeight() * transitionProgress;

      float x = bounds.getX() + (bounds.getWidth() - width) / 2;
      float y = bounds.getY() + (bounds.getHeight() - height) / 2;
      Color color = this.defaultConfig.pressedColor().get();

      RECTANGLE_RENDERER
          .pos(x, y)
          .size(width, height)
          .color(ColorFormat.ARGB32.pack(color.get(), (int) (color.getAlpha() * transitionProgress)));

      if (roundedCorners) {
        RECTANGLE_RENDERER
            .rounded(5)
            .upperEdgeSoftness(0.2F)
            .lowerEdgeSoftness(-0.5F);
      }

      RECTANGLE_RENDERER.render(stack);
    }

    if (!roundedCorners && outline) {
      RECTANGLE_RENDERER
          .renderOutline(
              stack,
              bounds.getX() + 1,
              bounds.getY() + 1,
              bounds.getMaxX() - 1,
              bounds.getMaxY() - 1,
              textColor,
              1
          );
    }

    TextRenderer textRenderer = RENDER_PIPELINE.textRenderer();
    KeyTracker keyTracker = this.keyStroke.getKeyTracker();
    float fontHeight = textRenderer.height();
    float y = bounds.getCenterY() - (textRenderer.height() / 2) + 0.8F;
    if (keyTracker != null) {
      y -= fontHeight * 0.25F;
    }

    float x = bounds.getCenterX() + 0.4F;
    textRenderer
        .pos(
            x,
            y
        )
        .useFloatingPointPosition(true)
        .color(textColor)
        .shadow(true)
        .centered(true)
        .text(this.keyStroke.getKeyName())
        .render(stack);

    if (keyTracker != null) {
      stack.push();
      stack.translate(x, y, 0);
      stack.scale(0.5F);
      textRenderer
          .pos(
              0,
              fontHeight * 1.5F + 3
          )
          .useFloatingPointPosition(true)
          .color(textColor)
          .shadow(true)
          .centered(true)
          .text(keyTracker.getCount() + " CPS")
          .render(stack);
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
