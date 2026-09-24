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

class TrailSamplerTest {

  private static final long MS = 1_000_000L;
  private static final long STEP = TrailSampler.STEP_NANOS;

  @Test
  void samplesLandOnTheGrid() {
    TrailSampler sampler = new TrailSampler();
    RecordingSource source = new RecordingSource();

    // Uneven frame times, like a real game
    long time = 3L * MS;
    while (time <= 300L * MS) {
      sampler.catchUp(source, time);
      time += (time / MS) % 3L == 0L ? 7L * MS : 13L * MS;
    }
    sampler.catchUp(source, 300L * MS);

    for (int index = 0; index < sampler.size(); index++) {
      long sampleTime = sampler.timeNanos(index);
      assertEquals(0L, sampleTime % STEP);
      assertEquals(sampleTime, source.timeOf(sampler.x(index)));
    }

    int first = sampler.firstIndexWithin(300L * MS, 300L * MS);
    assertEquals(30, sampler.size() - first);
    assertEquals(10L * MS, sampler.timeNanos(first));
    assertEquals(300L * MS, sampler.timeNanos(sampler.size() - 1));
  }

  @Test
  void samplesBeforeAnInputShowTheStateBeforeIt() {
    MouseMotion motion = new MouseMotion();
    motion.update(0L, 1.0D, 150, PadShape.SQUARE);
    motion.turn(25L * MS, 5.0D, 0.0D);
    motion.update(40L * MS, 1.0D, 150, PadShape.SQUARE);

    TrailSampler sampler = motion.sampler();
    assertEquals(5, sampler.size());
    assertEquals(0.0F, sampler.x(0));
    assertEquals(0.0F, sampler.x(1));
    assertEquals(0.0F, sampler.x(2));
    assertTrue(sampler.x(3) > 0.0F);
    assertTrue(sampler.x(4) > 0.0F);
    assertTrue(sampler.x(4) < sampler.x(3));
  }

  @Test
  void aLongGapClearsTheBufferWithoutLoopingMoreThanItsCapacity() {
    TrailSampler sampler = new TrailSampler();
    RecordingSource source = new RecordingSource();
    for (long time = 0L; time <= 500L * MS; time += 16L * MS) {
      sampler.catchUp(source, time);
    }
    int callsBefore = source.calls;

    sampler.catchUp(source, 5_500L * MS);

    assertTrue(source.calls - callsBefore <= TrailSampler.CAPACITY);
    assertEquals(1, sampler.size());
    assertEquals(5_500L * MS, sampler.timeNanos(0));
  }

  @Test
  void aShortGapIsFilledFromTheSource() {
    TrailSampler sampler = new TrailSampler();
    RecordingSource source = new RecordingSource();
    sampler.catchUp(source, 0L);
    sampler.catchUp(source, 200L * MS);

    assertEquals(21, sampler.size());
  }

  @Test
  void theBufferKeepsTheNewestSamples() {
    TrailSampler sampler = new TrailSampler();
    RecordingSource source = new RecordingSource();
    for (long time = 0L; time <= 3_000L * MS; time += 5L * MS) {
      sampler.catchUp(source, time);
    }

    assertEquals(TrailSampler.CAPACITY, sampler.size());
    assertEquals(3_000L * MS, sampler.timeNanos(TrailSampler.CAPACITY - 1));
    assertEquals(2_000L * MS, sampler.timeNanos(0));
  }

  @Test
  void iterationRunsFromOldestToNewest() {
    TrailSampler sampler = new TrailSampler();
    RecordingSource source = new RecordingSource();
    for (long time = 0L; time <= 1_500L * MS; time += 9L * MS) {
      sampler.catchUp(source, time);
    }

    for (int index = 1; index < sampler.size(); index++) {
      assertTrue(sampler.x(index) > sampler.x(index - 1));
      assertEquals(STEP, sampler.timeNanos(index) - sampler.timeNanos(index - 1));
    }
  }

  @Test
  void theOpacityRampsFromTheTailToTheDot() {
    long duration = 300L * MS;
    assertEquals(1.0F, TrailSampler.fade(0L, duration));
    assertEquals(0.5F, TrailSampler.fade(150L * MS, duration), 1.0E-6F);
    assertEquals(0.0F, TrailSampler.fade(duration, duration));
    assertEquals(0.0F, TrailSampler.fade(2L * duration, duration));

    float previous = 0.0F;
    for (long age = duration; age >= 0L; age -= STEP) {
      float fade = TrailSampler.fade(age, duration);
      assertTrue(fade >= previous);
      previous = fade;
    }
  }

  @Test
  void samplingAllocatesNothing() {
    TrailSampler sampler = new TrailSampler();
    RecordingSource source = new RecordingSource();
    long[] time = {0L};
    Allocations.assertAllocationFree(20_000, () -> {
      time[0] += 7L * MS;
      sampler.catchUp(source, time[0]);
      sampler.firstIndexWithin(time[0], 300L * MS);
    });
  }

  /**
   * Returns the sample time in milliseconds as position, so tests can tell samples apart.
   */
  private static final class RecordingSource implements PositionSource {

    private int calls;

    @Override
    public double xAt(long nanos) {
      this.calls++;
      return nanos / (double) MS;
    }

    @Override
    public double yAt(long nanos) {
      return 0.0D;
    }

    long timeOf(float x) {
      return Math.round(x) * MS;
    }
  }
}
