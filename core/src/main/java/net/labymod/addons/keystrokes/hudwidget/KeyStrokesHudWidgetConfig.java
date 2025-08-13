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

import net.labymod.addons.keystrokes.KeyStrokeConfig;
import net.labymod.addons.keystrokes.activities.KeyStrokeManageActivity;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.hud.hudwidget.HudWidgetConfig;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.ActivitySettingWidget.ActivitySetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget.ColorPickerSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingRequires;
import net.labymod.api.util.Color;
import net.labymod.api.util.MethodOrder;

import java.util.HashSet;
import java.util.Set;

public class KeyStrokesHudWidgetConfig extends HudWidgetConfig {

  @ColorPickerSetting(chroma = true, alpha = true)
  private final ConfigProperty<Color> textColor = new ConfigProperty<>(
      Color.ofRGB(255, 255, 255, 255)
  );

  @ColorPickerSetting(chroma = true, alpha = true)
  private final ConfigProperty<Color> backgroundColor = new ConfigProperty<>(
      Color.ofRGB(0, 0, 0, 155)
  );

  @ColorPickerSetting(chroma = true, alpha = true)
  private final ConfigProperty<Color> pressedColor = new ConfigProperty<>(
      Color.ofRGB(255, 255, 255, 104)
  );

  @SwitchSetting
  private final ConfigProperty<Boolean> outline = new ConfigProperty<>(false);

  @SwitchSetting
  @SettingRequires("outline")
  private final ConfigProperty<Boolean> useTextColorAsOutline = new ConfigProperty<>(true);

  @ColorPickerSetting(chroma = true, alpha = true)
  @SettingRequires(value = "useTextColorAsOutline", invert = true)
  private final ConfigProperty<Color> outlineColor = new ConfigProperty<>(
      Color.ofRGB(255, 255, 255, 255)
  );

  @SwitchSetting
  private final ConfigProperty<Boolean> roundedCorners = new ConfigProperty<>(
      Laby.labyAPI().themeService().currentTheme().getId().equals("fancy")
  );

  @SliderSetting(min = 1, max = 10, steps = 1)
  @SettingRequires("roundedCorners")
  private final ConfigProperty<Integer> roundedRadius = new ConfigProperty<>(5);

  @SwitchSetting
  private final ConfigProperty<Boolean> transition = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> fancySpace = new ConfigProperty<>(true)
      .addChangeListener(this::updateFancySpace);

  @SwitchSetting
  private final ConfigProperty<Boolean> bigSpace = new ConfigProperty<>(false)
      .addChangeListener(bigSpace -> this.updateSpace(true));

  @SwitchSetting
  private final ConfigProperty<Boolean> trackMouseCPS = new ConfigProperty<>(true)
      .addChangeListener((property, oldValue, newValue) -> this.refreshCPSTracking());

  private final ConfigProperty<Set<KeyStrokeConfig>> keyStrokes = ConfigProperty.create(
      new HashSet<>());

  private final ConfigProperty<Key> base = new ConfigProperty<>(Key.S);

  public Set<KeyStrokeConfig> getKeyStrokes() {
    return this.keyStrokes.get();
  }

  @MethodOrder(before = "pressedColor")
  @ActivitySetting
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

  public ConfigProperty<Boolean> useTextColorAsOutline() {
    return this.useTextColorAsOutline;
  }

  public ConfigProperty<Color> outlineColor() {
    return this.outlineColor;
  }

  public ConfigProperty<Boolean> roundedCorners() {
    return this.roundedCorners;
  }

  public ConfigProperty<Integer> roundedRadius() {
    return this.roundedRadius;
  }

  public ConfigProperty<Boolean> transition() {
    return this.transition;
  }

  public ConfigProperty<Boolean> fancySpace() {
    return this.fancySpace;
  }

  public ConfigProperty<Boolean> bigSpace() {
    return this.bigSpace;
  }

  public ConfigProperty<Boolean> trackMouseCPS() {
    return this.trackMouseCPS;
  }

  public void setDefaultKeyStrokes() {
    Set<KeyStrokeConfig> keyStrokes = new HashSet<>();
    keyStrokes.add(new KeyStrokeConfig(Key.W, 0, -22));
    keyStrokes.add(new KeyStrokeConfig(Key.A, -22, 0));
    keyStrokes.add(new KeyStrokeConfig(Key.S, 22, 22));
    keyStrokes.add(new KeyStrokeConfig(Key.D, 22, 0));
    KeyStrokeConfig spaceConfig = new KeyStrokeConfig(Key.SPACE, -22, 22);
    keyStrokes.add(spaceConfig);
    keyStrokes.add(new KeyStrokeConfig(MouseButton.LEFT, -22, 44));
    keyStrokes.add(new KeyStrokeConfig(MouseButton.RIGHT, 11, 44));
    if (this.base.get() != Key.S) {
      this.base.reset();
    }

    if (!this.bigSpace.get()) {
      spaceConfig.updateWidth(spaceConfig.key());
      spaceConfig.updatePosition(10 - spaceConfig.getWidth() / 2, spaceConfig.getY());
    }

    this.keyStrokes.set(keyStrokes);
    if (this.fancySpace.get()) {
      this.updateFancySpace(this.fancySpace.get());
    }
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
      if (tracking) {
        keyStrokeConfig.enableKeyTracking();
      } else {
        keyStrokeConfig.disableKeyTracking();
      }
    }
  }

  public void updateSpace(boolean actuallyUpdate) {
    for (KeyStrokeConfig keyStrokeConfig : this.keyStrokes.get()) {
      if (keyStrokeConfig.key() != Key.SPACE) {
        continue;
      }

      if(!actuallyUpdate) {
        keyStrokeConfig.updateBigSpace(this.bigSpace.get());
      } else {
        // update position to center the space key based on the previous width
        final float prevWidth = keyStrokeConfig.getWidth();
        keyStrokeConfig.updateBigSpace(this.bigSpace.get());
        final float newWidth = keyStrokeConfig.getWidth();

        float centerX = keyStrokeConfig.getX() + prevWidth / 2;
        float targetX = centerX - newWidth / 2;
        keyStrokeConfig.updatePosition(targetX, keyStrokeConfig.getY());
      }
      }

  }

  public void updateFancySpace(boolean fancy) {
    KeyStrokeConfig space = this.getKeyStroke(Key.SPACE);
    if (space == null) {
      return;
    }

    float prevHeight = space.getHeight(!fancy);
    float newHeight = space.getHeight(fancy);

    final float bigHeight = fancy ? prevHeight : newHeight;
    final float smallHeight = fancy ? newHeight : prevHeight;

    float targetY = space.getY() + 4 + bigHeight;
    float targetMaxX = space.getX() + space.getWidth();

    Key base = this.base().get();
    // shift up/down all keys intersecting with the space key to adjust for the new space key height
    for (KeyStrokeConfig keyStrokeConfig : this.keyStrokes.get()) {
      final float y = keyStrokeConfig.getY();
      if (keyStrokeConfig.key() == base || keyStrokeConfig.key() == Key.SPACE || y < space.getY()) {
        continue;
      }

      final float x = keyStrokeConfig.getX();
      final float maxX = x + keyStrokeConfig.getWidth();
      boolean inWidth = (x >= space.getX() && x <= targetMaxX)
          || (maxX >= space.getX() && maxX <= targetMaxX);
      if (y < targetY && inWidth) {
        if (fancy) {
          keyStrokeConfig.updatePosition(x, y - bigHeight + smallHeight);
        } else {
          keyStrokeConfig.updatePosition(x, y - smallHeight + bigHeight);
        }
      }
    }
  }






}
