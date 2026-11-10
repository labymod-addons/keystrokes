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

package net.labymod.addons.keystrokes.share;

import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidget;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidgetConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Entry point to Laby Chat sharing. Nothing here touches the share API before
 * {@link #register(KeyStrokesHudWidget)} ran, so the rest of the addon can ask
 * {@link #isAvailable()} on any client.
 */
public final class KeyStrokesChatShare {

  /**
   * The registry of the share API, present from Laby Client 50 on.
   */
  public static final String SHARE_API_CLASS =
      "net.labymod.api.labyconnect.share.ChatShareRegistry";

  private static @Nullable KeyStrokesShareable shareable;

  private KeyStrokesChatShare() {
  }

  /**
   * Registers the layout kind. Throws a {@link LinkageError} on a client without the share API.
   */
  public static void register(@NotNull KeyStrokesHudWidget widget) {
    if (shareable != null) {
      return;
    }

    // Every call into the share API sits in KeyStrokesShareable: verifying this class must not
    // need any share API type, it is loaded on clients without it too
    shareable = KeyStrokesShareable.register(widget);
  }

  public static boolean isAvailable() {
    return shareable != null;
  }

  /**
   * Lets the player pick a friend and sends them the layout. Called on the render thread.
   */
  public static void share(@NotNull KeyStrokesHudWidgetConfig config) {
    KeyStrokesShareable kind = shareable;
    if (kind != null) {
      kind.share(config);
    }
  }
}
