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

package net.labymod.addons.keystrokes.hudwidget;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import net.labymod.addons.keystrokes.KeyStroke;
import net.labymod.api.client.gui.hud.config.HudWidgetConfig;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.activities.WorkInProgressActivity;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.AddonActivityWidget.AddonActivitySetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget.ColorPickerSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.util.MethodOrder;

public class KeyStrokesHudWidgetConfig extends HudWidgetConfig {

  @ColorPickerSetting(alpha = true)
  private final ConfigProperty<Integer> backgroundColor = new ConfigProperty<>(
      Color.WHITE.getRGB());

  private final Map<Key, KeyStroke> keyStrokes = new HashMap<>();

  public KeyStrokesHudWidgetConfig() {
    this.keyStrokes.put(Key.W, new KeyStroke(-10, 21, 20, 20));
    this.keyStrokes.put(Key.A, new KeyStroke(-32, -1, 20, 20));
    this.keyStrokes.put(Key.S, new KeyStroke(-10, -1, 20, 20));
    this.keyStrokes.put(Key.D, new KeyStroke(12, -1, 20, 20));
  }

  public ConfigProperty<Integer> backgroundColor() {
    return this.backgroundColor;
  }

  public Map<Key, KeyStroke> getKeyStrokes() {
    return this.keyStrokes;
  }

  @MethodOrder(after = "backgroundColor")
  @AddonActivitySetting
  public Activity edit() {
    return new WorkInProgressActivity("improvement/addon-api");
  }
}
