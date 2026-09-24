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
 * Live dot state of one widget: the motion model plus the trail sampled from it.
 */
public final class MouseMotion {

  /**
   * The turn event carries the game's turn delta divided by 8 on every version, and the game turns
   * the view by 0.15 degrees per unit of that delta.
   */
  public static final double DEGREES_PER_EVENT_UNIT = 8.0D * 0.15D;

  private final MouseTrackerModel model = new MouseTrackerModel();
  private final TrailSampler sampler = new TrailSampler();

  /**
   * Adds a camera turn with the deltas of the turn event.
   *
   * @return {@code false} if the turn was ignored because a delta is not finite
   */
  public boolean turn(long nanos, double eventX, double eventY) {
    double dxDegrees = eventX * DEGREES_PER_EVENT_UNIT;
    double dyDegrees = eventY * DEGREES_PER_EVENT_UNIT;
    if (!Double.isFinite(dxDegrees) || !Double.isFinite(dyDegrees)) {
      return false;
    }

    this.sampler.catchUp(this.model, nanos);
    return this.model.turn(nanos, dxDegrees, dyDegrees);
  }

  /**
   * Brings the trail and the dot up to the given time and applies the current settings.
   */
  public void update(long nanos, double sensitivity, int returnTimeMillis, PadShape shape) {
    this.sampler.catchUp(this.model, nanos);
    this.model.configure(nanos, sensitivity, returnTimeMillis, shape);
  }

  public void reset() {
    this.model.reset();
    this.sampler.clear();
  }

  public MouseTrackerModel model() {
    return this.model;
  }

  public TrailSampler sampler() {
    return this.sampler;
  }
}
