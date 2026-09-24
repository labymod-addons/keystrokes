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
 * Position of the dot in normalized pad units. Turning pushes the dot away from the center and it
 * glides back with an exponential decay whose half-life is the return time. The decay is applied
 * in closed form for the elapsed time, so the result does not depend on how often it is updated.
 */
public final class MouseTrackerModel implements PositionSource {

  /**
   * Degrees of turning at which the dot reaches the edge of the pad at sensitivity 1.
   */
  public static final double EDGE_DEGREES = 20.0D;

  public static final double DEFAULT_SENSITIVITY = 1.0D;
  public static final int DEFAULT_RETURN_TIME_MILLIS = 150;

  private static final double LN_2 = Math.log(2.0D);
  private static final double NANOS_PER_MILLI = 1_000_000.0D;

  // Positions below this are treated as the center, so a resting dot ends up exactly there
  private static final double SNAP = 1.0E-9D;
  private static final double CIRCLE_AXIS_LIMIT = 1.0E6D;

  private double x;
  private double y;

  private boolean started;
  private long lastNanos;

  private double sensitivity = DEFAULT_SENSITIVITY;
  private int returnTimeMillis;
  private double decayPerNano;
  private PadShape shape = PadShape.SQUARE;

  public MouseTrackerModel() {
    this.applyReturnTime(DEFAULT_RETURN_TIME_MILLIS);
  }

  /**
   * Applies the settings at the given time. The position is decayed to that time with the previous
   * return time first, so changing a setting never moves the dot by itself. Only a shape change
   * clamps the position to the new shape.
   */
  public void configure(long nanos, double sensitivity, int returnTimeMillis, PadShape shape) {
    this.decayTo(nanos);

    if (Double.isFinite(sensitivity) && sensitivity > 0.0D) {
      this.sensitivity = sensitivity;
    }

    if (returnTimeMillis != this.returnTimeMillis) {
      this.applyReturnTime(returnTimeMillis);
    }

    if (shape != null && shape != this.shape) {
      this.shape = shape;
      this.clamp();
    }
  }

  /**
   * Adds a turn of the given degrees at the given time.
   *
   * @return {@code false} if the turn was ignored because a delta is not finite
   */
  public boolean turn(long nanos, double dxDegrees, double dyDegrees) {
    if (!Double.isFinite(dxDegrees) || !Double.isFinite(dyDegrees)) {
      return false;
    }

    this.decayTo(nanos);

    this.x += dxDegrees * this.sensitivity / EDGE_DEGREES;
    this.y += dyDegrees * this.sensitivity / EDGE_DEGREES;
    this.clamp();
    return true;
  }

  /**
   * Lets the dot glide towards the center until the given time.
   */
  public void decayTo(long nanos) {
    if (!this.started) {
      this.started = true;
      this.lastNanos = nanos;
      return;
    }

    long elapsed = nanos - this.lastNanos;
    if (elapsed <= 0L) {
      return;
    }

    this.lastNanos = nanos;
    if (this.x == 0.0D && this.y == 0.0D) {
      return;
    }

    double factor = Math.exp(-this.decayPerNano * elapsed);
    this.x = snap(this.x * factor);
    this.y = snap(this.y * factor);
  }

  public void reset() {
    this.x = 0.0D;
    this.y = 0.0D;
    this.started = false;
    this.lastNanos = 0L;
  }

  public double x() {
    return this.x;
  }

  public double y() {
    return this.y;
  }

  @Override
  public double xAt(long nanos) {
    return this.x * this.decayFactorAt(nanos);
  }

  @Override
  public double yAt(long nanos) {
    return this.y * this.decayFactorAt(nanos);
  }

  public double sensitivity() {
    return this.sensitivity;
  }

  public int returnTimeMillis() {
    return this.returnTimeMillis;
  }

  public PadShape shape() {
    return this.shape;
  }

  private double decayFactorAt(long nanos) {
    if (!this.started) {
      return 1.0D;
    }

    long elapsed = nanos - this.lastNanos;
    return elapsed <= 0L ? 1.0D : Math.exp(-this.decayPerNano * elapsed);
  }

  private void applyReturnTime(int returnTimeMillis) {
    this.returnTimeMillis = returnTimeMillis;
    double halfLifeNanos = Math.max(1, returnTimeMillis) * NANOS_PER_MILLI;
    this.decayPerNano = LN_2 / halfLifeNanos;
  }

  private void clamp() {
    if (this.shape != PadShape.CIRCLE) {
      this.x = clamp(this.x, 1.0D);
      this.y = clamp(this.y, 1.0D);
      return;
    }

    // Bound the axes and scale down by the larger one first, so a turn that overflowed to
    // infinity keeps its direction and squaring stays finite
    this.x = clamp(this.x, CIRCLE_AXIS_LIMIT);
    this.y = clamp(this.y, CIRCLE_AXIS_LIMIT);
    double largest = Math.max(Math.abs(this.x), Math.abs(this.y));
    if (largest > 1.0D) {
      this.x /= largest;
      this.y /= largest;
    }

    double length = Math.sqrt(this.x * this.x + this.y * this.y);
    if (length > 1.0D) {
      this.x /= length;
      this.y /= length;
    }
  }

  private static double clamp(double value, double limit) {
    return Math.max(-limit, Math.min(limit, value));
  }

  private static double snap(double value) {
    return Math.abs(value) < SNAP ? 0.0D : value;
  }
}
