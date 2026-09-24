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

import org.junit.jupiter.api.Test;

class MouseTrackerModelTest {

  private static final double EPSILON = 1.0E-6D;
  private static final long MS = 1_000_000L;
  private static final long HALF_LIFE = MouseTrackerModel.DEFAULT_RETURN_TIME_MILLIS * MS;

  @Test
  void noInputKeepsTheDotCentered() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.decayTo(0L);
    model.decayTo(5_000L * MS);

    assertEquals(0.0D, model.x());
    assertEquals(0.0D, model.y());
  }

  @Test
  void twentyDegreesReachTheEdge() {
    MouseTrackerModel full = new MouseTrackerModel();
    full.turn(0L, 20.0D, 0.0D);
    assertEquals(1.0D, full.x());

    MouseTrackerModel half = new MouseTrackerModel();
    half.turn(0L, 10.0D, 0.0D);
    assertEquals(0.5D, half.x());
  }

  @Test
  void positiveDeltasMoveRightAndDown() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.turn(0L, 4.0D, 6.0D);

    assertEquals(0.2D, model.x(), EPSILON);
    assertEquals(0.3D, model.y(), EPSILON);
  }

  @Test
  void theDotGlidesBackWithTheReturnTimeAsHalfLife() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.turn(0L, 20.0D, -20.0D);

    model.decayTo(HALF_LIFE);
    assertEquals(0.5D, model.x(), EPSILON);
    assertEquals(-0.5D, model.y(), EPSILON);

    model.decayTo(3L * HALF_LIFE);
    assertEquals(0.125D, model.x(), EPSILON);
    assertEquals(-0.125D, model.y(), EPSILON);
  }

  @Test
  void theResultDoesNotDependOnTheFrameRate() {
    double[] results = new double[6];
    int[] framesPerSecond = {30, 144, 1000};
    for (int run = 0; run < framesPerSecond.length; run++) {
      MouseTrackerModel model = new MouseTrackerModel();
      long frame = 1_000_000_000L / framesPerSecond[run];
      long nextFrame = 0L;
      // Turns at fixed, frame independent times, frames rendered in between
      for (long time = 0L; time <= 2_000L * MS; time += MS) {
        if (time % (7L * MS) == 0L && time < 1_200L * MS) {
          model.turn(time, 1.5D, -0.75D);
        }

        if (time >= nextFrame) {
          model.decayTo(time);
          nextFrame += frame;
        }
      }

      model.decayTo(2_000L * MS);
      results[run * 2] = model.x();
      results[run * 2 + 1] = model.y();
    }

    assertTrue(results[0] > 0.0D);
    assertEquals(results[0], results[2], EPSILON);
    assertEquals(results[0], results[4], EPSILON);
    assertEquals(results[1], results[3], EPSILON);
    assertEquals(results[1], results[5], EPSILON);
  }

  @Test
  void theSquareClampsEachAxis() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.turn(0L, 60.0D, 10.0D);

    assertEquals(1.0D, model.x());
    assertEquals(0.5D, model.y(), EPSILON);
  }

  @Test
  void theCircleClampsTheLengthAndKeepsTheDirection() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.configure(0L, 1.0D, 150, PadShape.CIRCLE);
    model.turn(0L, 60.0D, 80.0D);

    assertEquals(1.0D, Math.hypot(model.x(), model.y()), EPSILON);
    assertEquals(0.6D, model.x(), EPSILON);
    assertEquals(0.8D, model.y(), EPSILON);
  }

  @Test
  void thePinnedDotReturnsFromTheEdge() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.turn(0L, 500.0D, 0.0D);
    model.turn(MS, 500.0D, 0.0D);
    assertEquals(1.0D, model.x());

    model.decayTo(MS + HALF_LIFE);
    assertEquals(0.5D, model.x(), EPSILON);
  }

  @Test
  void nonFiniteDeltasAreIgnored() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.turn(0L, 4.0D, 2.0D);

    assertFalse(model.turn(0L, Double.NaN, 1.0D));
    assertFalse(model.turn(0L, 1.0D, Double.POSITIVE_INFINITY));
    assertFalse(model.turn(0L, Double.NEGATIVE_INFINITY, 0.0D));

    assertEquals(0.2D, model.x(), EPSILON);
    assertEquals(0.1D, model.y(), EPSILON);
  }

  @Test
  void hugeDeltasClamp() {
    MouseTrackerModel square = new MouseTrackerModel();
    assertTrue(square.turn(0L, 1.0E300D, -Double.MAX_VALUE));
    assertEquals(1.0D, square.x());
    assertEquals(-1.0D, square.y());

    MouseTrackerModel circle = new MouseTrackerModel();
    circle.configure(0L, 5.0D, 150, PadShape.CIRCLE);
    assertTrue(circle.turn(0L, Double.MAX_VALUE, Double.MAX_VALUE));
    assertEquals(Math.sqrt(0.5D), circle.x(), EPSILON);
    assertEquals(Math.sqrt(0.5D), circle.y(), EPSILON);
  }

  @Test
  void changingSettingsNeverMovesTheDot() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.turn(0L, 10.0D, 4.0D);

    model.configure(0L, 5.0D, 1000, PadShape.SQUARE);
    assertEquals(0.5D, model.x(), EPSILON);
    assertEquals(0.2D, model.y(), EPSILON);

    model.configure(0L, 0.1D, 20, PadShape.CIRCLE);
    assertEquals(0.5D, model.x(), EPSILON);
    assertEquals(0.2D, model.y(), EPSILON);
  }

  @Test
  void aNewReturnTimeOnlyAppliesFromTheChange() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.turn(0L, 20.0D, 0.0D);

    // One half-life with the old return time, then one with the new one
    model.configure(HALF_LIFE, 1.0D, 500, PadShape.SQUARE);
    assertEquals(0.5D, model.x(), EPSILON);

    model.decayTo(HALF_LIFE + 500L * MS);
    assertEquals(0.25D, model.x(), EPSILON);
  }

  @Test
  void switchingToTheCircleClampsACorner() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.turn(0L, 40.0D, 40.0D);
    assertEquals(1.0D, model.x());
    assertEquals(1.0D, model.y());

    model.configure(0L, 1.0D, 150, PadShape.CIRCLE);
    assertEquals(Math.sqrt(0.5D), model.x(), EPSILON);
    assertEquals(Math.sqrt(0.5D), model.y(), EPSILON);
  }

  @Test
  void sensitivityScalesTheTurn() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.configure(0L, 2.0D, 150, PadShape.SQUARE);
    model.turn(0L, 5.0D, 0.0D);

    assertEquals(0.5D, model.x(), EPSILON);
  }

  @Test
  void theRestingDotEndsUpExactlyInTheCenter() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.turn(0L, 20.0D, 20.0D);
    model.decayTo(60_000L * MS);

    assertEquals(0.0D, model.x());
    assertEquals(0.0D, model.y());
  }

  @Test
  void positionsInTheFutureAreDecayed() {
    MouseTrackerModel model = new MouseTrackerModel();
    model.turn(0L, 20.0D, 0.0D);

    assertEquals(0.5D, model.xAt(HALF_LIFE), EPSILON);
    assertEquals(1.0D, model.x());
  }

  @Test
  void turningAllocatesNothing() {
    MouseTrackerModel model = new MouseTrackerModel();
    long[] time = {0L};
    Allocations.assertAllocationFree(20_000, () -> {
      time[0] += 3L * MS;
      model.turn(time[0], 0.3D, -0.2D);
      model.decayTo(time[0] + MS);
      model.configure(time[0] + MS, 1.0D, 150, PadShape.SQUARE);
    });
  }
}
