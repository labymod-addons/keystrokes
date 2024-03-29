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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.labymod.addons.keystrokes.KeyStrokeConfig;
import net.labymod.addons.keystrokes.activities.KeyStrokeManageActivity;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.hud.hudwidget.HudWidgetConfig;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.AddonActivityWidget.AddonActivitySetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget.ColorPickerSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.util.Color;
import net.labymod.api.util.MethodOrder;

@SuppressWarnings("FieldMayBeFinal")
public class KeyStrokesHudWidgetConfig extends HudWidgetConfig {

  @ColorPickerSetting(chroma = true, alpha = true)
  private final ConfigProperty<Color> pressedColor = new ConfigProperty<>(
      Color.ofRGB(255, 255, 255, 104)
  );

  @ColorPickerSetting(chroma = true, alpha = true)
  private final ConfigProperty<Color> textColor = new ConfigProperty<>(
      Color.ofRGB(255, 255, 255, 255)
  );

  @ColorPickerSetting(chroma = true, alpha = true)
  private final ConfigProperty<Color> backgroundColor = new ConfigProperty<>(
      Color.ofRGB(0, 0, 0, 155)
  );

  @SwitchSetting
  private final ConfigProperty<Boolean> outline = new ConfigProperty<>(false);

  @SwitchSetting
  private final ConfigProperty<Boolean> roundedCorners = new ConfigProperty<>(
      Laby.labyAPI().themeService().currentTheme().getId().equals("fancy")
  );

  @SwitchSetting
  private final ConfigProperty<Boolean> transition = new ConfigProperty<>(true);

  public ConfigProperty<Boolean> trackMouseCPS() {
    return this.trackMouseCPS;
  }  @SwitchSetting
  private final ConfigProperty<Boolean> trackMouseCPS = new ConfigProperty<>(true)
      .addChangeListener((property, oldValue, newValue) -> this.refreshCPSTracking());

  private final ConfigProperty<Set<KeyStrokeConfig>> keyStrokes = ConfigProperty.create(
      new HashSet<>());

  private final ConfigProperty<Key> base = new ConfigProperty<>(Key.S);

  private transient List<KeyStrokesWidget> widget = new ArrayList<>();

  public Set<KeyStrokeConfig> getKeyStrokes() {
    return this.keyStrokes.get();
  }

  @MethodOrder(before = "pressedColor")
  @AddonActivitySetting
  public Activity edit() {
    return new KeyStrokeManageActivity(this);
  }

  public ConfigProperty<Color> pressedColor() {
    return this.pressedColor;
  }

  public ConfigProperty<Color> textColor() {
    return this.textColor;
  }

  public ConfigProperty<Color> backgroundColor() {
    return this.backgroundColor;
  }

  public ConfigProperty<Boolean> outline() {
    return this.outline;
  }

  public ConfigProperty<Boolean> roundedCorners() {
    return this.roundedCorners;
  }

  public ConfigProperty<Boolean> transition() {
    return this.transition;
  }

  public void setDefaultKeyStrokes() {
    Set<KeyStrokeConfig> keyStrokes = new HashSet<>();
    keyStrokes.add(new KeyStrokeConfig(Key.W, 0, -22));
    keyStrokes.add(new KeyStrokeConfig(Key.A, -22, 0));
    keyStrokes.add(new KeyStrokeConfig(Key.S, 22, 22));
    keyStrokes.add(new KeyStrokeConfig(Key.D, 22, 0));
    keyStrokes.add(new KeyStrokeConfig(MouseButton.LEFT, -22, 22));
    keyStrokes.add(new KeyStrokeConfig(MouseButton.RIGHT, 11, 22));
    if (this.anchorConfig() == null) {
      this.base.reset();
    }

    this.keyStrokes.set(keyStrokes);
  }

  public KeyStrokeConfig anchorConfig() {
    return this.getKeyStroke(this.base.get());
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

  public boolean removeKeyStroke(KeyStrokeConfig keyStrokeConfig) {
    return this.keyStrokes.get().remove(keyStrokeConfig);
  }

  public void refreshCPSTracking() {
    boolean tracking = this.trackMouseCPS.get();
    for (KeyStrokeConfig keyStrokeConfig : this.keyStrokes.get()) {
      Key key = keyStrokeConfig.key();
      if (key != MouseButton.LEFT && key != MouseButton.RIGHT) {
        continue;
      }

      if (tracking) {
        keyStrokeConfig.enableKeyTracking();
      } else {
        keyStrokeConfig.disableKeyTracking();
      }
    }
  }


}
