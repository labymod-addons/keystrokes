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
 * Layout math of the mouse movement widget, in GUI pixels before the HUD scale.
 */
public final class MouseTrackerLayout {

  public static final int MIN_SIZE = 20;
  public static final int MAX_SIZE = 100;
  public static final float MIN_DOT_SIZE = 1.0F;
  public static final float MAX_DOT_SIZE = 12.0F;
  public static final int MAX_CPS_GAP = 10;
  public static final int MIN_TRAIL_MILLIS = 10;

  public static final float CPS_BOX_HEIGHT = 12.0F;
  public static final float CPS_BOX_SPACING = 2.0F;
  public static final int MAX_CPS_LABEL = 99;

  // The circle shader fades the edge out between radius - 1 and radius - 0.5, so a circle looks
  // this much smaller than its radius (in scaled units)
  private static final float CIRCLE_EDGE_INSET = 0.75F;

  private static final String[] CPS_LABELS = new String[MAX_CPS_LABEL + 1];
  private static final String CPS_OVERFLOW_LABEL = MAX_CPS_LABEL + "+";

  static {
    for (int count = 0; count <= MAX_CPS_LABEL; count++) {
      CPS_LABELS[count] = Integer.toString(count);
    }
  }

  private MouseTrackerLayout() {
  }

  public static int size(int configured) {
    return clamp(configured, MIN_SIZE, MAX_SIZE);
  }

  public static float dotSize(float configured) {
    if (!(configured >= MIN_DOT_SIZE)) {
      return MIN_DOT_SIZE;
    }

    return Math.min(configured, MAX_DOT_SIZE);
  }

  public static int cpsGap(int configured) {
    return clamp(configured, 0, MAX_CPS_GAP);
  }

  public static int trailMillis(int configured) {
    return clamp(configured, MIN_TRAIL_MILLIS, TrailSampler.MAX_DURATION_MILLIS);
  }

  /**
   * @return the height of the whole widget: the pad plus, with CPS on, the gap and the boxes
   */
  public static float widgetHeight(int size, boolean showCps, int cpsGap) {
    return showCps ? size + cpsGap + CPS_BOX_HEIGHT : size;
  }

  /**
   * @return the distance between the pad center and the dot center when the dot is at the edge, so
   * the whole dot stays inside the pad and its outline for both shapes
   */
  public static float travel(int size, float dotSize, boolean outline) {
    float travel = size / 2.0F - dotSize / 2.0F - 1.0F - (outline ? 1.0F : 0.0F);
    return Math.max(0.0F, travel);
  }

  public static float trailWidth(float dotSize) {
    return Math.max(1.0F, dotSize / 2.0F);
  }

  public static float cpsBoxWidth(int size) {
    return (size - CPS_BOX_SPACING) / 2.0F;
  }

  public static float cpsBoxY(int size, int cpsGap) {
    return size + cpsGap;
  }

  public static float rightCpsBoxX(int size) {
    return cpsBoxWidth(size) + CPS_BOX_SPACING;
  }

  /**
   * @return the corner radius of a rounded tile, computed the same way as for the key tiles
   */
  public static float cornerRadius(float height, int roundedRadius, float scale) {
    return Math.min(height * scale / 2.0F, roundedRadius * scale);
  }

  /**
   * @return the radius to submit so that the anti-aliased circle looks as big as the given radius
   */
  public static float circleRadius(float visibleRadius, float scale) {
    return visibleRadius + CIRCLE_EDGE_INSET / (scale > 0.0F ? scale : 1.0F);
  }

  public static String cpsLabel(int count) {
    if (count <= 0) {
      return CPS_LABELS[0];
    }

    return count > MAX_CPS_LABEL ? CPS_OVERFLOW_LABEL : CPS_LABELS[count];
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
