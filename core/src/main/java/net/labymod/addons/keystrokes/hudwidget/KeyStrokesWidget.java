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
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.render.matrix.Stack;

public class KeyStrokesWidget extends SimpleWidget {

  private final KeyStrokesHudWidgetConfig hudWidgetConfig;
  private final KeyStrokes keyStrokes;

  public KeyStrokesWidget(KeyStrokesHudWidgetConfig hudWidgetConfig, KeyStrokes keyStrokes) {
    this.hudWidgetConfig = hudWidgetConfig;
    this.keyStrokes = keyStrokes;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
    this.bounds.setSize(50, 20);
    this.backgroundColor().set(this.hudWidgetConfig.backgroundColor().get());
  }

  @Override
  public void render(Stack stack, MutableMouse mouse, float partialTicks) {
    this.backgroundColor().set(this.hudWidgetConfig.backgroundColor().get());
    super.render(stack, mouse, partialTicks);
  }
}
