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

import net.labymod.addons.keystrokes.mousetracker.PadShape;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.hud.hudwidget.HudWidgetConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget.ColorPickerSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownEntryTranslationPrefix;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingRequires;
import net.labymod.api.configuration.settings.annotation.SettingSection;
import net.labymod.api.util.Color;

public class MouseTrackerHudWidgetConfig extends HudWidgetConfig {

  @SettingSection("motion")
  @SliderSetting(min = 0.1F, max = 5.0F, steps = 0.1F)
  private final ConfigProperty<Float> sensitivity = new ConfigProperty<>(1.0F);

  @SliderSetting(min = 20, max = 1000, steps = 10)
  private final ConfigProperty<Integer> returnTime = new ConfigProperty<>(150);

  @SettingSection("appearance")
  @DropdownEntryTranslationPrefix("keystrokes.hudWidget.mouseTracker.padShape.entries")
  @DropdownSetting
  private final ConfigProperty<PadShape> padShape = ConfigProperty.createEnum(PadShape.SQUARE);

  @SliderSetting(min = 20, max = 100, steps = 2)
  private final ConfigProperty<Integer> size = new ConfigProperty<>(42);

  @SliderSetting(min = 1.0F, max = 12.0F, steps = 0.5F)
  private final ConfigProperty<Float> dotSize = new ConfigProperty<>(4.0F);

  @ColorPickerSetting(chroma = true, alpha = true)
  private final ConfigProperty<Color> dotColor = new ConfigProperty<>(
      Color.ofRGB(255, 255, 255, 255)
  );

  @ColorPickerSetting(chroma = true, alpha = true)
  private final ConfigProperty<Color> backgroundColor = new ConfigProperty<>(
      Color.ofRGB(0, 0, 0, 155)
  );

  @SwitchSetting
  private final ConfigProperty<Boolean> outline = new ConfigProperty<>(false);

  @ColorPickerSetting(chroma = true, alpha = true)
  @SettingRequires("outline")
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
  private final ConfigProperty<Boolean> showCenterCross = new ConfigProperty<>(true);

  @ColorPickerSetting(chroma = true, alpha = true)
  @SettingRequires("showCenterCross")
  private final ConfigProperty<Color> guideColor = new ConfigProperty<>(
      Color.ofRGB(255, 255, 255, 64)
  );

  @SwitchSetting
  private final ConfigProperty<Boolean> showTrail = new ConfigProperty<>(true);

  @SliderSetting(min = 50, max = 1000, steps = 50)
  @SettingRequires("showTrail")
  private final ConfigProperty<Integer> trailDuration = new ConfigProperty<>(300);

  @ColorPickerSetting(chroma = true, alpha = true)
  @SettingRequires("showTrail")
  private final ConfigProperty<Color> trailColor = new ConfigProperty<>(
      Color.ofRGB(255, 255, 255, 153)
  );

  @SettingSection("clicks")
  @SwitchSetting
  private final ConfigProperty<Boolean> showCps = new ConfigProperty<>(false);

  @SliderSetting(min = 0, max = 10, steps = 1)
  @SettingRequires("showCps")
  private final ConfigProperty<Integer> cpsGap = new ConfigProperty<>(2);

  @ColorPickerSetting(chroma = true, alpha = true)
  @SettingRequires("showCps")
  private final ConfigProperty<Color> textColor = new ConfigProperty<>(
      Color.ofRGB(255, 255, 255, 255)
  );

  public ConfigProperty<Float> sensitivity() {
    return this.sensitivity;
  }

  public ConfigProperty<Integer> returnTime() {
    return this.returnTime;
  }

  public ConfigProperty<PadShape> padShape() {
    return this.padShape;
  }

  public ConfigProperty<Integer> size() {
    return this.size;
  }

  public ConfigProperty<Float> dotSize() {
    return this.dotSize;
  }

  public ConfigProperty<Color> dotColor() {
    return this.dotColor;
  }

  public ConfigProperty<Color> backgroundColor() {
    return this.backgroundColor;
  }

  public ConfigProperty<Boolean> outline() {
    return this.outline;
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

  public ConfigProperty<Boolean> showCenterCross() {
    return this.showCenterCross;
  }

  public ConfigProperty<Color> guideColor() {
    return this.guideColor;
  }

  public ConfigProperty<Boolean> showTrail() {
    return this.showTrail;
  }

  public ConfigProperty<Integer> trailDuration() {
    return this.trailDuration;
  }

  public ConfigProperty<Color> trailColor() {
    return this.trailColor;
  }

  public ConfigProperty<Boolean> showCps() {
    return this.showCps;
  }

  public ConfigProperty<Integer> cpsGap() {
    return this.cpsGap;
  }

  public ConfigProperty<Color> textColor() {
    return this.textColor;
  }
}
