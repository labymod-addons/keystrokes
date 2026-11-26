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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.labymod.api.event.client.entity.player.ClientPlayerTurnEvent;
import org.junit.jupiter.api.Test;

class TurnInputTest {

  private static final double EPSILON = 1.0E-9D;

  @Test
  void theEventIsOnlyRead() {
    ClientPlayerTurnEvent event = new ClientPlayerTurnEvent(null, 2.5D, -1.0D);
    MouseMotion motion = new MouseMotion();

    assertTrue(TurnInput.accept(event, motion, 0L));

    assertEquals(2.5D, event.getX());
    assertEquals(-1.0D, event.getY());
    assertFalse(event.isCancelled());
  }

  @Test
  void eventUnitsAreConvertedToDegrees() {
    // 2.5 event units are 2.5 * 8 * 0.15 = 3 degrees, a 20th of the way per degree
    ClientPlayerTurnEvent event = new ClientPlayerTurnEvent(null, 2.5D, -1.0D);
    MouseMotion motion = new MouseMotion();
    TurnInput.accept(event, motion, 0L);

    assertEquals(0.15D, motion.model().x(), EPSILON);
    assertEquals(-0.06D, motion.model().y(), EPSILON);
  }

  @Test
  void cancelledTurnsStillMoveTheDot() {
    ClientPlayerTurnEvent event = new ClientPlayerTurnEvent(null, 5.0D, 0.0D);
    event.setCancelled(true);
    MouseMotion motion = new MouseMotion();

    assertTrue(TurnInput.accept(event, motion, 0L));

    assertTrue(motion.model().x() > 0.0D);
    assertTrue(event.isCancelled());
    assertEquals(5.0D, event.getX());
  }

  @Test
  void nonFiniteTurnsAreReported() {
    ClientPlayerTurnEvent event = new ClientPlayerTurnEvent(null, Double.NaN, 0.0D);
    MouseMotion motion = new MouseMotion();

    assertFalse(TurnInput.accept(event, motion, 0L));
    assertEquals(0.0D, motion.model().x());
  }
}
