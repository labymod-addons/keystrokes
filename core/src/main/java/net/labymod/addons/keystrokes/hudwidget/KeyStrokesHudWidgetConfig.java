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
import java.util.HashSet;
import java.util.Set;
import net.labymod.addons.keystrokes.KeyStrokeConfig;
import net.labymod.addons.keystrokes.activities.HudWidgetEditActivity;
import net.labymod.api.client.gui.hud.config.HudWidgetConfig;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.AddonActivityWidget.AddonActivitySetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget.ColorPickerSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.util.MethodOrder;

@SuppressWarnings("FieldMayBeFinal")
public class KeyStrokesHudWidgetConfig extends HudWidgetConfig {

  @ColorPickerSetting
  private final ConfigProperty<Integer> pressedColor = new ConfigProperty<>(
      new Color(255, 255, 255, 255).getRGB()
  );

  @ColorPickerSetting
  private final ConfigProperty<Integer> textColor = new ConfigProperty<>(
      new Color(255, 255, 255, 255).getRGB()
  );

  @ColorPickerSetting(alpha = true)
  private final ConfigProperty<Integer> backgroundColor = new ConfigProperty<>(
      new Color(0, 0, 0, 155).getRGB()
  );

  @SwitchSetting
  private final ConfigProperty<Boolean> outline = new ConfigProperty<>(false);

  @SwitchSetting
  private final ConfigProperty<Boolean> showCps = new ConfigProperty<>(false);

  @SliderSetting(min = 20, max = 100)
  private final ConfigProperty<Integer> width = new ConfigProperty<>(20);

  private final ConfigProperty<Set<KeyStrokeConfig>> keyStrokes = ConfigProperty.create(
      new HashSet<>(),
      set -> {
        set.add(new KeyStrokeConfig(Key.W, this, 0, -22));
        set.add(new KeyStrokeConfig(Key.A, this, -22, 0));
        set.add(new KeyStrokeConfig(Key.S, this, 22, 22));
        set.add(new KeyStrokeConfig(Key.D, this, 22, 0));
      });

  private final ConfigProperty<Key> base = new ConfigProperty<>(Key.S);

  private transient KeyStrokesWidget widget;

  public Set<KeyStrokeConfig> getKeyStrokes() {
    return this.keyStrokes.get();
  }

  @MethodOrder(before = "pressedColor")
  @AddonActivitySetting
  public Activity edit() {
    return new HudWidgetEditActivity(this);
  }

  public ConfigProperty<Integer> pressedColor() {
    return this.pressedColor;
  }

  public ConfigProperty<Integer> textColor() {
    return this.textColor;
  }

  public ConfigProperty<Integer> backgroundColor() {
    return this.backgroundColor;
  }

  public ConfigProperty<Boolean> outline() {
    return this.outline;
  }

  public ConfigProperty<Boolean> showCps() {
    return this.showCps;
  }

  public ConfigProperty<Integer> width() {
    return this.width;
  }

  public KeyStrokeConfig getKeyStroke(Key key) {
    for (KeyStrokeConfig keyStroke : this.getKeyStrokes()) {
      if (keyStroke.key() == key) {
        return keyStroke;
      }
    }

    return null;
  }

  public ConfigProperty<Key> base() {
    return this.base;
  }

  public boolean addKeyStroke(KeyStrokeConfig keyStrokeConfig) {
    if (this.getKeyStroke(keyStrokeConfig.key()) != null) {
      return false;
    }

    this.keyStrokes.get().add(keyStrokeConfig);
    return true;
  }

  public void setWidget(KeyStrokesWidget widget) {
    this.widget = widget;
  }

  public KeyStrokesWidget widget() {
    return this.widget;
  }
}
