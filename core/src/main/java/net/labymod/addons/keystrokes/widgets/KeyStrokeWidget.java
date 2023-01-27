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
import net.labymod.api.Laby;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.BoundsType;
import net.labymod.api.client.render.RenderPipeline;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.font.text.TextRenderer;
import net.labymod.api.client.render.matrix.Stack;

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
  public void renderWidget(Stack stack, MutableMouse mouse, float partialTicks) {
    super.renderWidget(stack, mouse, partialTicks);

    int textColor = this.getTextColor();

    Bounds bounds = this.bounds();
    RECTANGLE_RENDERER
        .pos(bounds.rectangle(BoundsType.OUTER))
        .color(this.getBackgroundColor());

    boolean roundedCorners = this.defaultConfig.roundedCorners().get();
    boolean outline = this.defaultConfig.outline().get();
    if (roundedCorners) {
      RECTANGLE_RENDERER
          .rounded(5)
          .upperEdgeSoftness(0.2F)
          .lowerEdgeSoftness(-0.5F);

      if (outline) {
        RECTANGLE_RENDERER
            .borderColor(textColor)
            .borderThickness(1);
      }
    }

    RECTANGLE_RENDERER.render(stack);

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
    textRenderer
        .pos(bounds.getCenterX() + 0.4F, bounds.getCenterY() - (textRenderer.height() / 2) + 0.8F)
        .useFloatingPointPosition(true)
        .color(textColor)
        .shadow(true)
        .centered(true)
        .text(this.key.getName())
        .render(stack);
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

  public double getDistanceTo(KeyStrokeWidget widget) {
    Bounds bounds = widget.bounds();
    return this.getDistanceTo(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
  }

  public double getDistanceTo(float x, float y, float width, float height) {
    return Math.pow(
        (x + width / 2) - this.bounds().getCenterX(),
        2
    ) + Math.pow(
        (y + height / 2) - this.bounds().getCenterX(),
        2
    );
  }
}
