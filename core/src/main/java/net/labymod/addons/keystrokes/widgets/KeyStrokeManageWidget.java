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

package net.labymod.addons.keystrokes.widgets;

import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidgetConfig;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesWidget;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.lss.property.LssProperty;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.util.bounds.Rectangle;

@AutoWidget
public class KeyStrokeManageWidget extends KeyStrokesWidget {

  private final LssProperty<Icon> anchorIcon = new LssProperty<>(null);

  public KeyStrokeManageWidget(KeyStrokesHudWidgetConfig config) {
    super(config);
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
  }

  @Override
  protected void updateWidgetBounds(Rectangle bounds) {
    super.updateWidgetBounds(bounds);
    Bounds parentBounds = this.parent.bounds();
    this.bounds.setPosition(parentBounds.getCenterX() - bounds.getWidth() / 2,
        parentBounds.getCenterY() - bounds.getHeight() / 2);
  }

  public LssProperty<Icon> anchorIcon() {
    return this.anchorIcon;
  }
}
