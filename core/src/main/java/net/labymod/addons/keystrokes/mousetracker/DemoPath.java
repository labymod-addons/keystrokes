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
 * Figure-eight motion shown in the HUD editor instead of live input.
 */
public final class DemoPath implements PositionSource {

  public static final DemoPath INSTANCE = new DemoPath();

  public static final long PERIOD_NANOS = 2_400_000_000L;

  private static final double TWO_PI = Math.PI * 2.0D;

  private DemoPath() {
  }

  @Override
  public double xAt(long nanos) {
    return 0.75D * Math.sin(TWO_PI * phase(nanos));
  }

  @Override
  public double yAt(long nanos) {
    return 0.5D * Math.sin(2.0D * TWO_PI * phase(nanos));
  }

  private static double phase(long nanos) {
    return (double) Math.floorMod(nanos, PERIOD_NANOS) / PERIOD_NANOS;
  }
}
