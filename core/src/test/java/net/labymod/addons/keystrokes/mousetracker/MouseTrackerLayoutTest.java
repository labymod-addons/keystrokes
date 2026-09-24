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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MouseTrackerLayoutTest {

  private static final float EPSILON = 1.0E-6F;

  @Test
  void theClickBoxesAddTheGapAndTheirHeight() {
    assertEquals(42.0F, MouseTrackerLayout.widgetHeight(42, false, 2));
    assertEquals(56.0F, MouseTrackerLayout.widgetHeight(42, true, 2));
    assertEquals(32.0F, MouseTrackerLayout.widgetHeight(20, true, 0));
    assertEquals(122.0F, MouseTrackerLayout.widgetHeight(100, true, 10));
  }

  @Test
  void theDotStaysInsideThePad() {
    for (int size = MouseTrackerLayout.MIN_SIZE; size <= MouseTrackerLayout.MAX_SIZE; size += 2) {
      for (float dot = 1.0F; dot <= 12.0F; dot += 0.5F) {
        for (int outline = 0; outline <= 1; outline++) {
          float travel = MouseTrackerLayout.travel(size, dot, outline == 1);
          float expected = size / 2.0F - dot / 2.0F - 1.0F - outline;
          assertEquals(Math.max(0.0F, expected), travel, EPSILON);

          // The far edge of the dot at the edge of the pad, for both shapes
          assertTrue(travel + dot / 2.0F <= size / 2.0F - 1.0F - outline + EPSILON);
          assertTrue(travel >= 0.0F);
        }
      }
    }
  }

  @Test
  void theTravelNeverGetsNegative() {
    assertEquals(0.0F, MouseTrackerLayout.travel(4, 12.0F, true));
    assertEquals(15.0F, MouseTrackerLayout.travel(42, 8.0F, true));
  }

  @Test
  void theClickBoxesSplitThePadWidth() {
    assertEquals(9.0F, MouseTrackerLayout.cpsBoxWidth(20));
    assertEquals(11.0F, MouseTrackerLayout.rightCpsBoxX(20));
    assertEquals(20.0F, MouseTrackerLayout.rightCpsBoxX(20) + MouseTrackerLayout.cpsBoxWidth(20));

    assertEquals(49.0F, MouseTrackerLayout.cpsBoxWidth(100));
    assertEquals(51.0F, MouseTrackerLayout.rightCpsBoxX(100));
    assertEquals(44.0F, MouseTrackerLayout.cpsBoxY(42, 2));
  }

  @Test
  void clickCountsAboveNinetyNineShowNinetyNinePlus() {
    assertEquals("0", MouseTrackerLayout.cpsLabel(0));
    assertEquals("7", MouseTrackerLayout.cpsLabel(7));
    assertEquals("99", MouseTrackerLayout.cpsLabel(99));
    assertEquals("99+", MouseTrackerLayout.cpsLabel(100));
    assertEquals("99+", MouseTrackerLayout.cpsLabel(12_345));
    assertEquals("0", MouseTrackerLayout.cpsLabel(-3));

    // Cached, so no string is built per frame
    assertSame(MouseTrackerLayout.cpsLabel(42), MouseTrackerLayout.cpsLabel(42));
    assertSame(MouseTrackerLayout.cpsLabel(100), MouseTrackerLayout.cpsLabel(101));
  }

  @Test
  void theTrailIsHalfAsWideAsTheDotButAtLeastOnePixel() {
    assertEquals(1.0F, MouseTrackerLayout.trailWidth(1.0F));
    assertEquals(1.0F, MouseTrackerLayout.trailWidth(2.0F));
    assertEquals(2.0F, MouseTrackerLayout.trailWidth(4.0F));
    assertEquals(6.0F, MouseTrackerLayout.trailWidth(12.0F));
  }

  @Test
  void cornersAreComputedLikeTheKeyTiles() {
    assertEquals(5.0F, MouseTrackerLayout.cornerRadius(42.0F, 5, 1.0F));
    assertEquals(10.0F, MouseTrackerLayout.cornerRadius(42.0F, 5, 2.0F));
    assertEquals(6.0F, MouseTrackerLayout.cornerRadius(12.0F, 10, 1.0F));
  }

  @Test
  void circlesGrowByTheirSoftEdge() {
    assertEquals(2.75F, MouseTrackerLayout.circleRadius(2.0F, 1.0F), EPSILON);
    assertEquals(2.375F, MouseTrackerLayout.circleRadius(2.0F, 2.0F), EPSILON);
    assertEquals(2.75F, MouseTrackerLayout.circleRadius(2.0F, 0.0F), EPSILON);
  }

  @Test
  void storedValuesOutsideTheSliderRangesAreClamped() {
    assertEquals(20, MouseTrackerLayout.size(0));
    assertEquals(100, MouseTrackerLayout.size(500));
    assertEquals(1.0F, MouseTrackerLayout.dotSize(Float.NaN));
    assertEquals(12.0F, MouseTrackerLayout.dotSize(40.0F));
    assertEquals(0, MouseTrackerLayout.cpsGap(-4));
    assertEquals(10, MouseTrackerLayout.trailMillis(0));
    assertEquals(1000, MouseTrackerLayout.trailMillis(5000));
  }
}
