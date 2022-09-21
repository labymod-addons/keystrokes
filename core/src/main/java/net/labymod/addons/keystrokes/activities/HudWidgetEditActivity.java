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

package net.labymod.addons.keystrokes.activities;

import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidgetConfig;
import net.labymod.api.client.gui.screen.LabyScreen;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import org.jetbrains.annotations.Nullable;

@Link("edit.lss")
@AutoActivity
public class HudWidgetEditActivity extends Activity {

  private final KeyStrokesHudWidgetConfig hudWidgetConfig;

  public HudWidgetEditActivity(KeyStrokesHudWidgetConfig hudWidgetConfig) {
    this.hudWidgetConfig = hudWidgetConfig;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
  }

  @Override
  public <T extends LabyScreen> @Nullable T renew() {
    return new HudWidgetEditActivity(this.hudWidgetConfig).generic();
  }
}
