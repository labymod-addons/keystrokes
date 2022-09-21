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

import net.labymod.addons.keystrokes.KeyStrokes;
import net.labymod.api.client.gui.hud.HudWidget;
import net.labymod.api.client.gui.hud.annotation.HudWidgetTypeProvider;
import net.labymod.api.client.gui.hud.category.HudWidgetMiscellaneousCategory;
import net.labymod.api.client.gui.hud.info.HudWidgetInfo;
import net.labymod.api.inject.LabyGuice;
import org.jetbrains.annotations.NotNull;

@HudWidgetTypeProvider(
    typeId = "keyStrokes",
    categoryClass = HudWidgetMiscellaneousCategory.class,
    configClass = KeyStrokesHudWidgetConfig.class
)
public class KeyStrokesHudWidget extends HudWidget<KeyStrokesWidget, KeyStrokesHudWidgetConfig> {

  private static final KeyStrokes KEY_STROKES = LabyGuice.getInstance(KeyStrokes.class);

  protected KeyStrokesHudWidget(
      @NotNull HudWidgetInfo info,
      @NotNull KeyStrokesHudWidgetConfig config
  ) {
    super(info, config, () -> {
      return new KeyStrokesWidget(config, KEY_STROKES);
    });
  }
}
