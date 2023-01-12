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
import net.labymod.addons.keystrokes.KeyStrokes;
import net.labymod.addons.keystrokes.widgets.KeyStrokesWidget;
import net.labymod.api.client.gui.hud.hudwidget.widget.WidgetHudWidget;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.util.bounds.Rectangle;

public class KeyStrokesHudWidget extends WidgetHudWidget<KeyStrokesHudWidgetConfig> {

  private final KeyStrokes keyStrokes;
  private KeyStrokesWidget keyStrokesWidget;

  public KeyStrokesHudWidget(KeyStrokes keyStrokes) {
    super("keyStrokes", KeyStrokesHudWidgetConfig.class);
    this.keyStrokes = keyStrokes;
  }

  @Override
  public void initialize(AbstractWidget<Widget> widget) {
    super.initialize(widget);
    this.keyStrokesWidget = new KeyStrokesWidget(this.config);
    this.config.setWidget(this.keyStrokesWidget);
    widget.addChild(this.keyStrokesWidget);
  }

  @Override
  public void onBoundsChanged(AbstractWidget<Widget> widget, Rectangle newRect) {
    this.keyStrokesWidget.reInitialize();
    super.onBoundsChanged(widget, newRect);
  }

  @Override
  public boolean isVisibleInGame() {
    return true;
  }

  @Subscribe
  public void onKey(KeyEvent event) {
    KeyStrokeConfig keyStroke = this.getConfig().getKeyStroke(event.key());
    if (keyStroke == null) {
      return;
    }

    keyStroke.updatePressed(event.state() != State.UNPRESSED);
  }

  @Subscribe
  public void onMouseButton(MouseButtonEvent event) {
    KeyStrokeConfig keyStroke = this.getConfig().getKeyStroke(event.button());
    if (keyStroke == null) {
      return;
    }

    keyStroke.updatePressed(event.action() == Action.CLICK);
  }
}
