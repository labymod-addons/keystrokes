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

package net.labymod.addons.keystrokes.mousetracker;

import net.labymod.api.event.client.entity.player.ClientPlayerTurnEvent;

/**
 * Feeds turn events into the motion. It only reads the event: the turn is never changed or
 * cancelled, so the dot also shows turns that other listeners cancel or redirect.
 */
public final class TurnInput {

  private TurnInput() {
  }

  /**
   * @return {@code false} if the turn was ignored because a delta is not finite
   */
  public static boolean accept(ClientPlayerTurnEvent event, MouseMotion motion, long nanos) {
    return motion.turn(nanos, event.getX(), event.getY());
  }
}
