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
import net.labymod.addons.keystrokes.widgets.KeyStrokeWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.util.bounds.Rectangle;

public class KeyStrokesWidget extends SimpleWidget {

  private static final float HEIGHT = 20;

  private final KeyStrokesHudWidgetConfig hudWidgetConfig;
  private final KeyStrokes keyStrokes;

  public KeyStrokesWidget(KeyStrokesHudWidgetConfig hudWidgetConfig, KeyStrokes keyStrokes) {
    this.hudWidgetConfig = hudWidgetConfig;
    this.keyStrokes = keyStrokes;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
    this.bounds.setSize(200, 100);
  }

  @Override
  public void onBoundsChanged(Rectangle previousRect, Rectangle bounds) {
    super.onBoundsChanged(previousRect, bounds);
    this.children.clear();

    float x = bounds.getCenterX();
    float y = bounds.getCenterY();
    for (KeyStrokeConfig keyStroke : this.hudWidgetConfig.getKeyStrokes()) {
      Key key = keyStroke.key();
      keyStroke.updateWidth(key);

      KeyStrokeWidget keyStrokeWidget = new KeyStrokeWidget(key, keyStroke);
      Bounds widgetBounds = keyStrokeWidget.bounds();
      widgetBounds.setX(x + keyStroke.getX() - keyStroke.getWidth() / 2);
      widgetBounds.setY(y + keyStroke.getY() - HEIGHT / 2);
      widgetBounds.setHeight(HEIGHT);
      widgetBounds.setWidth(keyStroke.getWidth());

      this.addChild(keyStrokeWidget);
    }
  }
}
