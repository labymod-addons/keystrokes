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
 * Turns the trail samples into quads: one strip from the oldest sample to the dot whose opacity
 * falls from the trail color at the dot to transparent at the tail. The strip shares its corners
 * between neighboring segments, so translucent segments never overlap, and it gets a thin
 * transparent fringe on both sides as anti-aliasing. All buffers are allocated once.
 */
public final class TrailMesh {

  /**
   * Points closer than this (in GUI pixels) to the previous point are dropped, so a resting dot
   * produces no trail.
   */
  public static final float MIN_POINT_DISTANCE = 0.1F;

  public static final int VERTICES_PER_QUAD = 4;
  public static final int VERTICES_PER_SEGMENT = VERTICES_PER_QUAD * 3;

  private static final int MAX_POINTS = TrailSampler.CAPACITY + 1;
  private static final float MIN_POINT_DISTANCE_SQUARED = MIN_POINT_DISTANCE * MIN_POINT_DISTANCE;
  private static final float MAX_MITER = 2.0F;

  private final float[] pointX = new float[MAX_POINTS];
  private final float[] pointY = new float[MAX_POINTS];
  private final int[] pointAlpha = new int[MAX_POINTS];
  private final float[] offsetX = new float[MAX_POINTS];
  private final float[] offsetY = new float[MAX_POINTS];
  private int pointCount;

  private final float[] vertexX = new float[(MAX_POINTS - 1) * VERTICES_PER_SEGMENT];
  private final float[] vertexY = new float[(MAX_POINTS - 1) * VERTICES_PER_SEGMENT];
  private final int[] vertexColor = new int[(MAX_POINTS - 1) * VERTICES_PER_SEGMENT];
  private int vertexCount;

  /**
   * Builds the strip for the samples younger than the trail duration plus the current dot.
   *
   * @param headX   current dot x in normalized pad units
   * @param headY   current dot y in normalized pad units
   * @param travel  distance from the pad center to the dot at the edge, in GUI pixels
   * @param width   width of the opaque part of the trail
   * @param feather width of the transparent fringe on each side
   * @param argb    trail color at the dot
   * @return the number of vertices, a multiple of {@link #VERTICES_PER_QUAD}
   */
  public int build(
      TrailSampler sampler,
      long nanos,
      long durationNanos,
      double headX,
      double headY,
      float centerX,
      float centerY,
      float travel,
      float width,
      float feather,
      int argb
  ) {
    this.pointCount = 0;
    this.vertexCount = 0;

    int alpha = argb >>> 24;
    if (alpha == 0 || durationNanos <= 0L) {
      return 0;
    }

    int size = sampler.size();
    for (int index = sampler.firstIndexWithin(nanos, durationNanos); index < size; index++) {
      float fade = TrailSampler.fade(nanos - sampler.timeNanos(index), durationNanos);
      this.addPoint(
          centerX + sampler.x(index) * travel,
          centerY + sampler.y(index) * travel,
          Math.round(alpha * fade)
      );
    }

    this.addHead(centerX + (float) headX * travel, centerY + (float) headY * travel, alpha);
    if (this.pointCount < 2) {
      return 0;
    }

    this.computeOffsets();

    int rgb = argb & 0xFFFFFF;
    float half = width / 2.0F;
    float outer = half + Math.max(0.0F, feather);
    for (int index = 0; index + 1 < this.pointCount; index++) {
      this.addSegment(index, rgb, half, outer);
    }

    return this.vertexCount;
  }

  public int vertexCount() {
    return this.vertexCount;
  }

  public float vertexX(int index) {
    return this.vertexX[index];
  }

  public float vertexY(int index) {
    return this.vertexY[index];
  }

  public int vertexColor(int index) {
    return this.vertexColor[index];
  }

  /**
   * @return the number of points of the last strip, from the tail to the dot
   */
  public int pointCount() {
    return this.pointCount;
  }

  public int pointAlpha(int index) {
    return this.pointAlpha[index];
  }

  private void addPoint(float x, float y, int alpha) {
    int last = this.pointCount - 1;
    if (last >= 0 && this.isClose(last, x, y)) {
      return;
    }

    this.appendPoint(x, y, alpha);
  }

  private void addHead(float x, float y, int alpha) {
    // The strip always ends in the dot, so older points on top of it make way
    while (this.pointCount > 0 && this.isClose(this.pointCount - 1, x, y)) {
      this.pointCount--;
    }

    this.appendPoint(x, y, alpha);
  }

  private boolean isClose(int index, float x, float y) {
    float dx = x - this.pointX[index];
    float dy = y - this.pointY[index];
    return dx * dx + dy * dy < MIN_POINT_DISTANCE_SQUARED;
  }

  private void appendPoint(float x, float y, int alpha) {
    int index = this.pointCount++;
    this.pointX[index] = x;
    this.pointY[index] = y;
    this.pointAlpha[index] = alpha;
  }

  private void computeOffsets() {
    int last = this.pointCount - 1;
    float previousNormalX = 0.0F;
    float previousNormalY = 0.0F;
    for (int index = 0; index <= last; index++) {
      float normalX = 0.0F;
      float normalY = 0.0F;
      if (index < last) {
        float dx = this.pointX[index + 1] - this.pointX[index];
        float dy = this.pointY[index + 1] - this.pointY[index];
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        normalX = -dy / length;
        normalY = dx / length;
      }

      if (index == 0) {
        this.setOffset(index, normalX, normalY);
      } else if (index == last) {
        this.setOffset(index, previousNormalX, previousNormalY);
      } else {
        this.setMiterOffset(index, previousNormalX, previousNormalY, normalX, normalY);
      }

      previousNormalX = normalX;
      previousNormalY = normalY;
    }
  }

  private void setMiterOffset(
      int index,
      float inNormalX,
      float inNormalY,
      float outNormalX,
      float outNormalY
  ) {
    float sumX = inNormalX + outNormalX;
    float sumY = inNormalY + outNormalY;
    float length = (float) Math.sqrt(sumX * sumX + sumY * sumY);
    if (length < 1.0E-3F) {
      // The path reverses here, there is no meaningful miter
      this.setOffset(index, outNormalX, outNormalY);
      return;
    }

    float miterX = sumX / length;
    float miterY = sumY / length;
    float cosine = miterX * outNormalX + miterY * outNormalY;
    float scale = 1.0F / Math.max(cosine, 1.0F / MAX_MITER);
    this.setOffset(index, miterX * scale, miterY * scale);
  }

  private void setOffset(int index, float x, float y) {
    this.offsetX[index] = x;
    this.offsetY[index] = y;
  }

  private void addSegment(int from, int rgb, float half, float outer) {
    int to = from + 1;
    int fromColor = (this.pointAlpha[from] << 24) | rgb;
    int toColor = (this.pointAlpha[to] << 24) | rgb;

    float fromX = this.pointX[from];
    float fromY = this.pointY[from];
    float fromOffsetX = this.offsetX[from];
    float fromOffsetY = this.offsetY[from];
    float toX = this.pointX[to];
    float toY = this.pointY[to];
    float toOffsetX = this.offsetX[to];
    float toOffsetY = this.offsetY[to];

    // Opaque core
    this.addQuad(
        fromX + fromOffsetX * half, fromY + fromOffsetY * half, fromColor,
        fromX - fromOffsetX * half, fromY - fromOffsetY * half, fromColor,
        toX - toOffsetX * half, toY - toOffsetY * half, toColor,
        toX + toOffsetX * half, toY + toOffsetY * half, toColor
    );

    if (outer <= half) {
      return;
    }

    // Transparent fringe on both sides
    this.addQuad(
        fromX + fromOffsetX * outer, fromY + fromOffsetY * outer, rgb,
        fromX + fromOffsetX * half, fromY + fromOffsetY * half, fromColor,
        toX + toOffsetX * half, toY + toOffsetY * half, toColor,
        toX + toOffsetX * outer, toY + toOffsetY * outer, rgb
    );
    this.addQuad(
        fromX - fromOffsetX * half, fromY - fromOffsetY * half, fromColor,
        fromX - fromOffsetX * outer, fromY - fromOffsetY * outer, rgb,
        toX - toOffsetX * outer, toY - toOffsetY * outer, rgb,
        toX - toOffsetX * half, toY - toOffsetY * half, toColor
    );
  }

  private void addQuad(
      float x0, float y0, int color0,
      float x1, float y1, int color1,
      float x2, float y2, int color2,
      float x3, float y3, int color3
  ) {
    // Emit every quad with the same winding as the GUI rectangles (negative shoelace sum with
    // y pointing down), whatever the direction of the segment
    float shoelace = (x0 * y1 - x1 * y0)
        + (x1 * y2 - x2 * y1)
        + (x2 * y3 - x3 * y2)
        + (x3 * y0 - x0 * y3);

    this.addVertex(x0, y0, color0);
    if (shoelace > 0.0F) {
      this.addVertex(x3, y3, color3);
      this.addVertex(x2, y2, color2);
      this.addVertex(x1, y1, color1);
    } else {
      this.addVertex(x1, y1, color1);
      this.addVertex(x2, y2, color2);
      this.addVertex(x3, y3, color3);
    }
  }

  private void addVertex(float x, float y, int color) {
    int index = this.vertexCount++;
    this.vertexX[index] = x;
    this.vertexY[index] = y;
    this.vertexColor[index] = color;
  }
}
