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
import net.labymod.addons.keystrokes.widgets.KeyStrokesWidget;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.render.font.text.TextRenderer;

@SuppressWarnings("FieldMayBeFinal")
public class KeyStrokeConfig {

  private static final TextRenderer TEXT_RENDERER = Laby.labyAPI().renderPipeline().textRenderer();

  private Key key;

  private float x;
  private float y;

  private float width = KeyStrokesWidget.HEIGHT;

  private transient boolean pressed;

  public KeyStrokeConfig(Key key, KeyStrokesHudWidgetConfig config, float x, float y) {
    this.key = key;
    this.updateWidth(key);

    this.x = x;
    this.y = y;

  }

  public void updatePressed(boolean pressed) {
    this.pressed = pressed;
  }

  public void updateWidth(Key key) {
    float width = TEXT_RENDERER.width(key.getName());
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

  public float getWidth() {
    return this.width;
  }

  public boolean isPressed() {
    return this.pressed;
  }

  public void updatePosition(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public Key key() {
    return this.key;
  }
}
