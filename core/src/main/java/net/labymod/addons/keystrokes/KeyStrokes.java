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

package net.labymod.addons.keystrokes;

import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidget;
import net.labymod.addons.keystrokes.share.KeyStrokesChatShare;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;

@AddonMain
public class KeyStrokes extends LabyAddon<KeyStrokesConfiguration> {

  @Override
  protected void enable() {
    this.registerSettingCategory();

    KeyStrokesHudWidget hudWidget = new KeyStrokesHudWidget();
    this.labyAPI().hudWidgetRegistry().register(hudWidget);

    // Share the layout with friends through the Laby Chat, on client builds that have it
    try {
      Class.forName(KeyStrokesChatShare.SHARE_API_CLASS);
      KeyStrokesChatShare.register(hudWidget);
    } catch (ClassNotFoundException | LinkageError ignored) {
      this.logger().info(
          "Laby Chat sharing not available in this client build, layouts cannot be shared"
      );
    }
  }

  @Override
  protected Class<? extends KeyStrokesConfiguration> configurationClass() {
    return KeyStrokesConfiguration.class;
  }
}
