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
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.font.text.TextRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.util.Color;

public class KeyStrokeWidget extends SimpleWidget {

  public static final int PADDING = 4;
  private static final TextRenderer TEXT_RENDERER = Laby.labyAPI().renderPipeline().textRenderer();
  private static final RectangleRenderer RECTANGLE_RENDERER = Laby.labyAPI().renderPipeline()
      .rectangleRenderer();

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

    Bounds bounds = this.bounds();
    RECTANGLE_RENDERER.pos(bounds.rectangle(BoundsType.OUTER)).color(this.getBackgroundColor())
        .render(stack);
    int textColor = this.getTextColor();
    if (this.getDefaultOr(this.defaultConfig.outline(), this.keyStroke.hasOutline())) {
      RECTANGLE_RENDERER.renderOutline(stack, bounds.getX() + 1, bounds.getY() + 1,
          bounds.getMaxX() - 1, bounds.getMaxY() - 1, textColor, 1);
    }

    TEXT_RENDERER
        .pos(bounds.getCenterX() + 0.4F, bounds.getCenterY() - (TEXT_RENDERER.height() / 2) + 0.8F)
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
      return this.getDefaultOr(this.defaultConfig.pressedColor(), this.keyStroke.getPressedColor());
    }

    return this.getDefaultOr(this.defaultConfig.backgroundColor(),
        this.keyStroke.getBackgroundColor());
  }

  private int getTextColor() {
    return this.getDefaultOr(this.defaultConfig.textColor(), this.keyStroke.getTextColor());
  }

  public Key getKey() {
    return this.key;
  }

  public KeyStrokeConfig getKeyStroke() {
    return this.keyStroke;
  }

  private <T> T getDefaultOr(ConfigProperty<T> property, T value) {
    return property.isDefaultValue(value) ? property.get() : value;
  }

  private int getDefaultOr(ConfigProperty<Color> property, Color value) {
    return property.isDefaultValue(value) ? property.get().get() : value.get();
  }
}
