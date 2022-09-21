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

package net.labymod.addons.keystrokes.listener;

import com.google.inject.Inject;
import net.labymod.addons.keystrokes.KeyStrokes;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidget;
import net.labymod.api.client.gui.hud.HudWidget;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.gui.hud.HudWidgetRegisterEvent;

public class HudWidgetRegisterListener {

  private final KeyStrokes keyStrokes;

  @Inject
  private HudWidgetRegisterListener(KeyStrokes keyStrokes) {
    this.keyStrokes = keyStrokes;
  }

  @Subscribe
  public void onHudWidgetRegister(HudWidgetRegisterEvent event) {
    HudWidget<?, ?> widget = event.widget();
    if (!(widget instanceof KeyStrokesHudWidget)) {
      return;
    }

    this.keyStrokes.registerHudWidget((KeyStrokesHudWidget) widget);
  }
}
