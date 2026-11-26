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

/**
 * Records the dot position on a fixed 10 ms grid into a ring buffer, so the trail looks the same at
 * any frame rate. Callers catch the sampler up before they change the source, which makes every
 * sample show the state the source had at the sample's grid time.
 */
public final class TrailSampler {

  public static final long STEP_NANOS = 10_000_000L;
  public static final int MAX_DURATION_MILLIS = 1000;
  public static final int CAPACITY = MAX_DURATION_MILLIS / 10 + 1;

  private final float[] xs = new float[CAPACITY];
  private final float[] ys = new float[CAPACITY];

  // Ring buffer index of the newest sample and the grid tick it was taken at
  private int head = -1;
  private int count;
  private long newestTick;

  /**
   * Records the source at every grid time after the newest sample up to the given time. A gap
   * longer than the whole buffer clears it and starts again at the current grid time.
   */
  public void catchUp(PositionSource source, long nanos) {
    long tick = Math.floorDiv(nanos, STEP_NANOS);
    if (this.count == 0) {
      this.record(source, tick);
      return;
    }

    long pending = tick - this.newestTick;
    if (pending <= 0L) {
      return;
    }

    if (pending > CAPACITY) {
      this.clear();
      this.record(source, tick);
      return;
    }

    for (long next = this.newestTick + 1L; next <= tick; next++) {
      this.record(source, next);
    }
  }

  public void clear() {
    this.head = -1;
    this.count = 0;
    this.newestTick = 0L;
  }

  /**
   * @return the number of stored samples
   */
  public int size() {
    return this.count;
  }

  /**
   * @param index {@code 0} for the oldest sample up to {@code size() - 1} for the newest
   */
  public float x(int index) {
    return this.xs[this.slot(index)];
  }

  /**
   * @param index {@code 0} for the oldest sample up to {@code size() - 1} for the newest
   */
  public float y(int index) {
    return this.ys[this.slot(index)];
  }

  /**
   * @param index {@code 0} for the oldest sample up to {@code size() - 1} for the newest
   * @return the grid time of the sample in nanoseconds
   */
  public long timeNanos(int index) {
    return (this.newestTick - (this.count - 1 - index)) * STEP_NANOS;
  }

  /**
   * @return the index of the oldest sample younger than the given duration, or {@code size()} if
   * there is none
   */
  public int firstIndexWithin(long nanos, long durationNanos) {
    for (int index = 0; index < this.count; index++) {
      if (nanos - this.timeNanos(index) < durationNanos) {
        return index;
      }
    }

    return this.count;
  }

  /**
   * @return the opacity factor of a trail point of the given age: {@code 1} at the dot, falling
   * linearly to {@code 0} at the end of the trail
   */
  public static float fade(long ageNanos, long durationNanos) {
    if (durationNanos <= 0L || ageNanos >= durationNanos) {
      return 0.0F;
    }

    if (ageNanos <= 0L) {
      return 1.0F;
    }

    return 1.0F - (float) ((double) ageNanos / durationNanos);
  }

  private void record(PositionSource source, long tick) {
    long time = tick * STEP_NANOS;
    this.head = (this.head + 1) % CAPACITY;
    this.xs[this.head] = (float) source.xAt(time);
    this.ys[this.head] = (float) source.yAt(time);
    this.newestTick = tick;
    if (this.count < CAPACITY) {
      this.count++;
    }
  }

  private int slot(int index) {
    if (index < 0 || index >= this.count) {
      throw new IndexOutOfBoundsException("Sample " + index + " of " + this.count);
    }

    int slot = this.head - (this.count - 1 - index);
    return slot < 0 ? slot + CAPACITY : slot;
  }
}
