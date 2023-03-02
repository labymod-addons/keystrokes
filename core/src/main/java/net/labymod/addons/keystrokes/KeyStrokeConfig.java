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

import net.labymod.addons.keystrokes.util.KeyTracker;
import net.labymod.addons.keystrokes.widgets.KeyStrokeGridWidget;
import net.labymod.addons.keystrokes.widgets.KeyStrokeWidget;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.render.font.text.TextRenderer;

@SuppressWarnings("FieldMayBeFinal")
public class KeyStrokeConfig {

  private static final TextRenderer TEXT_RENDERER = Laby.labyAPI().renderPipeline().textRenderer();

  private Key key;

  private float x;
  private float y;

  private float width = KeyStrokeGridWidget.DEFAULT_WIDTH;

  private transient boolean pressed;
  private transient long lastPressedUpdate;
  private transient KeyTracker keyTracker;

  public KeyStrokeConfig(Key key, float x, float y) {
    this.key = key;
    this.updateWidth(key);

    this.x = x;
    this.y = y;
  }

  public void updatePressed(boolean pressed) {
    if (this.pressed == pressed) {
      return;
    }

    if (pressed && this.keyTracker != null) {
      this.keyTracker.press();
    }

    this.pressed = pressed;
    this.lastPressedUpdate = System.currentTimeMillis();
  }

  public void updateWidth(Key key) {
    int padding = KeyStrokeWidget.PADDING * 2;
    float actualWidth = TEXT_RENDERER.width(this.getKeyName()) + padding;
    float defaultWidth = KeyStrokeGridWidget.DEFAULT_WIDTH;
    if (key == MouseButton.LEFT || key == MouseButton.MIDDLE
        || key == MouseButton.RIGHT) {
      defaultWidth = defaultWidth * 1.5F + 1;
    }

    this.width = Math.max(actualWidth, defaultWidth);
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

  public String getKeyName() {
    Key key = this.key;
    if (!(key instanceof MouseButton)) {
      return key.getName();
    }

    if (key == MouseButton.LEFT) {
      return "LMB";
    }

    if (key == MouseButton.RIGHT) {
      return "RMB";
    }

    if (key == MouseButton.MIDDLE) {
      return "MMB";
    }

    return key.getName();
  }

  public long getLastPressedUpdate() {
    return this.lastPressedUpdate;
  }

  public void enableKeyTracking() {
    if (this.keyTracker != null) {
      return;
    }

    this.keyTracker = new KeyTracker();
  }

  public void disableKeyTracking() {
    if (this.keyTracker == null) {
      return;
    }

    this.keyTracker = null;
  }

  public KeyTracker getKeyTracker() {
    return this.keyTracker;
  }
}
