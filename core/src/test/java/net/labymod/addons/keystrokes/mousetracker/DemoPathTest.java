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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DemoPathTest {

  private static final long MS = 1_000_000L;
  private static final DemoPath PATH = DemoPath.INSTANCE;

  @Test
  void staysInsideThePadOnBothAxesAndInsideTheCircle() {
    for (long time = 0L; time < DemoPath.PERIOD_NANOS; time += MS) {
      double x = PATH.xAt(time);
      double y = PATH.yAt(time);
      assertTrue(x >= -1.0D && x <= 1.0D);
      assertTrue(y >= -1.0D && y <= 1.0D);
      assertTrue(Math.hypot(x, y) <= 1.0D);
    }
  }

  @Test
  void repeatsEveryPeriod() {
    long[] starts = {0L, 123L * MS, 7_777L * MS, Long.MAX_VALUE / 4L, -5_000L * MS};
    for (long start : starts) {
      for (long offset = 0L; offset < DemoPath.PERIOD_NANOS; offset += 37L * MS) {
        long time = start + offset;
        assertEquals(PATH.xAt(time), PATH.xAt(time + DemoPath.PERIOD_NANOS), 1.0E-9D);
        assertEquals(PATH.yAt(time), PATH.yAt(time + DemoPath.PERIOD_NANOS), 1.0E-9D);
      }
    }
  }

  @Test
  void drawsAFigureEight() {
    long quarter = DemoPath.PERIOD_NANOS / 4L;
    assertEquals(0.0D, PATH.xAt(0L), 1.0E-9D);
    assertEquals(0.75D, PATH.xAt(quarter), 1.0E-9D);
    assertEquals(0.0D, PATH.yAt(quarter), 1.0E-9D);
    assertEquals(0.5D, PATH.yAt(quarter / 2L), 1.0E-9D);
    assertEquals(-0.75D, PATH.xAt(3L * quarter), 1.0E-9D);
  }
}
