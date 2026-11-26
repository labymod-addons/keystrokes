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

package net.labymod.addons.keystrokes.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KeyTrackerTest {

  @Test
  void clicksOfTheLastSecondAreCounted() {
    KeyTracker tracker = new KeyTracker();
    tracker.press();
    tracker.press();
    tracker.press();

    tracker.update();
    tracker.update();

    assertEquals(3, tracker.getCount());
  }

  @Test
  void clicksExpireAfterOneSecond() throws InterruptedException {
    KeyTracker tracker = new KeyTracker();
    tracker.press();
    Thread.sleep(1_050L);
    tracker.press();

    tracker.update();

    assertEquals(1, tracker.getCount());
  }
}
