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

import java.util.Set;
import net.labymod.addons.keystrokes.KeyStrokeConfig;
import net.labymod.addons.keystrokes.event.KeyStrokeUpdateEvent;
import net.labymod.api.client.gui.hud.hudwidget.widget.WidgetHudWidget;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;

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
  }

  @Override
  public void initialize(AbstractWidget<Widget> widget) {
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
    if (this.config == null) {
      return;
    }

    KeyStrokeConfig keyStroke = this.config.getKeyStroke(event.key());
    if (keyStroke == null) {
      return;
    }

    keyStroke.updatePressed(event.state() != State.UNPRESSED);
  }

  @Subscribe
  public void onMouseButton(MouseButtonEvent event) {
    if (this.config == null) {
      return;
    }

    KeyStrokeConfig keyStroke = this.config.getKeyStroke(event.button());
    if (keyStroke == null) {
      return;
    }

    keyStroke.updatePressed(event.action() == Action.CLICK);
  }

  @Subscribe
  public void onKeyStrokeUpdate(KeyStrokeUpdateEvent event) {
    this.requestUpdate();
  }
}
