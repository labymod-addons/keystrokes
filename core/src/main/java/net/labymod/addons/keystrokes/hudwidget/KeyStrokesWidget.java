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
import net.labymod.addons.keystrokes.util.KeyTracker;
import net.labymod.addons.keystrokes.widgets.KeyStrokeGridWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.render.matrix.Stack;

public class KeyStrokesWidget extends KeyStrokeGridWidget {

  public KeyStrokesWidget(KeyStrokesHudWidgetConfig config) {
    super(config);
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    Bounds bounds = this.bounds();
    bounds.setPosition(0, 0, REASON);

    this.updateWidgetBounds(bounds);
  }

  @Override
  public void renderWidget(ScreenContext context) {
    for (KeyStrokeConfig keyStroke : this.config.getKeyStrokes()) {
      KeyTracker keyTracker = keyStroke.getKeyTracker();
      if (keyTracker != null) {
        keyTracker.update();
      }
    }

    context.pushStack();
    context.translate(this.parent.bounds().getX(), this.parent.bounds().getY(), 0);
    super.renderWidget(context);
    context.popStack();
  }
}
