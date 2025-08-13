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
import net.labymod.addons.keystrokes.event.KeyStrokeUpdateEvent;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.hud.hudwidget.widget.WidgetHudWidget;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.hud.HudWidgetWidget;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.gui.screen.ScreenDisplayEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;

import java.util.Set;

public class KeyStrokesHudWidget extends WidgetHudWidget<KeyStrokesHudWidgetConfig> {

  public KeyStrokesHudWidget() {
    super("keyStrokes", KeyStrokesHudWidgetConfig.class);
  }

  @Override
  public void load(KeyStrokesHudWidgetConfig config) {
    super.load(config);
    Set<KeyStrokeConfig> keyStrokes = config.getKeyStrokes();
    if (keyStrokes == null || keyStrokes.isEmpty()) {
      config.setDefaultKeyStrokes();
    }

    config.refreshCPSTracking();
    config.updateSpace(false);
  }

  @Override
  public void initialize(HudWidgetWidget widget) {
    super.initialize(widget);
    widget.setStencil(false);
    KeyStrokesWidget keyStrokesWidget = new KeyStrokesWidget(this.config);
    widget.addChild(keyStrokesWidget);
  }

  @Override
  public boolean isVisibleInGame() {
    return true;
  }

  @Subscribe
  public void onKey(KeyEvent event) {
    this.press(event.key(), event.state() != State.UNPRESSED);
  }

  @Subscribe
  public void onMouseButton(MouseButtonEvent event) {
    this.press(event.button(), event.action() == Action.CLICK);
  }

  @Subscribe
  public void onKeyStrokeUpdate(KeyStrokeUpdateEvent event) {
    this.requestUpdate("key_stroke_update");
  }

  @Subscribe
  public void onScreenOpen(ScreenDisplayEvent event) {
    if (event.getPreviousScreen() != null) {
      return;
    }

    for (KeyStrokeConfig keyStroke : this.config.getKeyStrokes()) {
      keyStroke.updatePressed(false);
    }
  }

  private void press(Key key, boolean pressed) {
    if (this.config == null) {
      return;
    }

    if (pressed && !Laby.labyAPI().minecraft().isMouseLocked()) {
      return;
    }

    KeyStrokeConfig keyStroke = this.config.getKeyStroke(key);
    if (keyStroke == null) {
      return;
    }

    keyStroke.updatePressed(pressed);
  }
}
