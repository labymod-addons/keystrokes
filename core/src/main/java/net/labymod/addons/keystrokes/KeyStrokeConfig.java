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

package net.labymod.addons.keystrokes;

import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidgetConfig;
import net.labymod.addons.keystrokes.widgets.KeyStrokeWidget;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.render.font.text.TextRenderer;
import net.labymod.api.util.Color;
import net.labymod.api.util.Lazy;

@SuppressWarnings("FieldMayBeFinal")
public class KeyStrokeConfig {

  private static final Lazy<TextRenderer> TEXT_RENDERER = Lazy.of(
      () -> Laby.labyAPI().renderPipeline()
          .textRenderer());

  private Key key;

  private float x;
  private float y;

  private Color pressedColor;
  private Color textColor;
  private Color backgroundColor;
  private boolean outline;
  //private boolean showCps;

  private float width;

  private transient boolean pressed;

  public KeyStrokeConfig(Key key, KeyStrokesHudWidgetConfig config, float x, float y) {
    this.key = key;
    this.pressedColor = config.pressedColor().get();
    this.textColor = config.textColor().get();
    this.backgroundColor = config.backgroundColor().get();
    this.outline = config.outline().get();
    //this.showCps = config.showCps().get();
    this.width = config.width().get();
    this.updateWidth(key);

    this.x = x;
    this.y = y;

  }

  public void updatePressed(boolean pressed) {
    this.pressed = pressed;
  }

  public void updateWidth(Key key) {
    float width = TEXT_RENDERER.get().width(key.getName());
    int padding = KeyStrokeWidget.PADDING * 2;
    if (this.width < width + padding) {
      this.width = width + padding;
    }
  }

  public float getX() {
    return this.x;
  }

  public float getY() {
    return this.y;
  }

  public Color getPressedColor() {
    return this.pressedColor;
  }

  public Color getTextColor() {
    return this.textColor;
  }

  public Color getBackgroundColor() {
    return this.backgroundColor;
  }

  public boolean hasOutline() {
    return this.outline;
  }

  //public boolean showingCps() {
  //  return this.showCps;
  //}

  public float getWidth() {
    return this.width;
  }

  public boolean isPressed() {
    return this.pressed;
  }

  public Key key() {
    return this.key;
  }
}
