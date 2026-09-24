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

class TrailMeshTest {

  private static final long MS = 1_000_000L;
  private static final long DURATION = 300L * MS;
  private static final int COLOR = 0x99FFFFFF;
  private static final float CENTER = 21.0F;
  private static final float TRAVEL = 18.0F;
  private static final long SWEEP_END = 400L * MS;

  @Test
  void aRestingDotHasNoTrail() {
    MouseMotion motion = new MouseMotion();
    for (long time = 0L; time <= 1_000L * MS; time += 16L * MS) {
      motion.update(time, 1.0D, 150, PadShape.SQUARE);
    }

    TrailMesh mesh = new TrailMesh();
    assertEquals(0, this.build(mesh, motion, 1_000L * MS));
  }

  @Test
  void theTrailDisappearsOnceTheDotHasSettled() {
    MouseMotion motion = new MouseMotion();
    motion.update(0L, 1.0D, 150, PadShape.SQUARE);
    motion.turn(MS, 10.0D, 5.0D);

    long time = MS;
    TrailMesh mesh = new TrailMesh();
    for (; time <= 400L * MS; time += 16L * MS) {
      motion.update(time, 1.0D, 150, PadShape.SQUARE);
    }
    assertTrue(this.build(mesh, motion, time) > 0);

    for (; time <= 8_000L * MS; time += 16L * MS) {
      motion.update(time, 1.0D, 150, PadShape.SQUARE);
    }
    assertEquals(0, this.build(mesh, motion, time));
  }

  @Test
  void theOpacityFallsFromTheDotToTheTail() {
    MouseMotion motion = this.sweepingMotion(PadShape.SQUARE);
    TrailMesh mesh = new TrailMesh();
    int vertices = this.build(mesh, motion, SWEEP_END);

    assertTrue(vertices > 0);
    assertEquals(0, vertices % TrailMesh.VERTICES_PER_SEGMENT);

    int last = mesh.pointCount() - 1;
    assertEquals(COLOR >>> 24, mesh.pointAlpha(last));
    assertTrue(mesh.pointAlpha(0) < 0x99 / 5);
    for (int index = 1; index <= last; index++) {
      assertTrue(mesh.pointAlpha(index) >= mesh.pointAlpha(index - 1));
    }

    for (int index = 0; index < vertices; index++) {
      int alpha = mesh.vertexColor(index) >>> 24;
      assertTrue(alpha <= 0x99);
      assertEquals(0xFFFFFF, mesh.vertexColor(index) & 0xFFFFFF);
    }
  }

  @Test
  void theFringeIsTransparent() {
    MouseMotion motion = this.sweepingMotion(PadShape.SQUARE);
    TrailMesh mesh = new TrailMesh();
    int vertices = this.build(mesh, motion, SWEEP_END);

    int transparent = 0;
    for (int index = 0; index < vertices; index++) {
      if ((mesh.vertexColor(index) >>> 24) == 0) {
        transparent++;
      }
    }

    // Each fringe quad has two transparent corners, the core quads have none at the dot end
    assertTrue(transparent >= vertices / 3);
  }

  @Test
  void everyQuadHasTheSameWinding() {
    MouseMotion motion = new MouseMotion();
    long time = 0L;
    // Zig-zag in all directions
    for (int step = 0; step < 40; step++) {
      motion.update(time, 1.0D, 150, PadShape.SQUARE);
      double angle = step * 2.4D;
      motion.turn(time, Math.cos(angle) * 4.0D, Math.sin(angle) * 4.0D);
      time += 8L * MS;
    }

    TrailMesh mesh = new TrailMesh();
    int vertices = this.build(mesh, motion, time);
    assertTrue(vertices > 0);
    for (int quad = 0; quad < vertices; quad += TrailMesh.VERTICES_PER_QUAD) {
      assertTrue(shoelace(mesh, quad) <= 1.0E-4F, "Quad " + quad + " is wound the other way");
    }
  }

  @Test
  void theTrailStaysInsideTheCircle() {
    MouseMotion motion = new MouseMotion();
    long time = 0L;
    for (int step = 0; step < 60; step++) {
      motion.update(time, 5.0D, 150, PadShape.CIRCLE);
      double angle = step * 0.4D;
      motion.turn(time, Math.cos(angle) * 30.0D, Math.sin(angle) * 30.0D);
      time += 9L * MS;
    }
    motion.update(time, 5.0D, 150, PadShape.CIRCLE);

    // Pad size 42, dot size 4, no outline: travel 18, trail width 2, fringe 0.5
    TrailMesh mesh = new TrailMesh();
    int vertices = mesh.build(
        motion.sampler(), time, 1_000L * MS,
        motion.model().x(), motion.model().y(),
        CENTER, CENTER, TRAVEL, 2.0F, 0.5F, COLOR
    );

    assertTrue(vertices > 0);
    for (int index = 0; index < vertices; index++) {
      float dx = mesh.vertexX(index) - CENTER;
      float dy = mesh.vertexY(index) - CENTER;
      assertTrue(Math.sqrt(dx * dx + dy * dy) <= CENTER + 1.0E-3F,
          "Vertex " + index + " is outside");
    }
  }

  @Test
  void aTransparentTrailColorDrawsNothing() {
    MouseMotion motion = this.sweepingMotion(PadShape.SQUARE);
    TrailMesh mesh = new TrailMesh();
    int vertices = mesh.build(
        motion.sampler(), SWEEP_END, DURATION,
        motion.model().x(), motion.model().y(),
        CENTER, CENTER, TRAVEL, 2.0F, 0.5F, 0x00FFFFFF
    );

    assertEquals(0, vertices);
  }

  @Test
  void buildingAllocatesNothing() {
    MouseMotion motion = this.sweepingMotion(PadShape.CIRCLE);
    TrailMesh mesh = new TrailMesh();
    Allocations.assertAllocationFree(5_000, () -> this.build(mesh, motion, SWEEP_END));
  }

  private MouseMotion sweepingMotion(PadShape shape) {
    MouseMotion motion = new MouseMotion();
    for (long time = 0L; time <= SWEEP_END; time += 5L * MS) {
      motion.update(time, 1.0D, 150, shape);
      if (time < SWEEP_END - 50L * MS) {
        motion.turn(time, 0.3D, 0.1D);
      }
    }
    return motion;
  }

  private int build(TrailMesh mesh, MouseMotion motion, long time) {
    return mesh.build(
        motion.sampler(), time, DURATION,
        motion.model().x(), motion.model().y(),
        CENTER, CENTER, TRAVEL, 2.0F, 0.5F, COLOR
    );
  }

  private static float shoelace(TrailMesh mesh, int first) {
    float sum = 0.0F;
    for (int corner = 0; corner < TrailMesh.VERTICES_PER_QUAD; corner++) {
      int index = first + corner;
      int next = first + (corner + 1) % TrailMesh.VERTICES_PER_QUAD;
      sum += mesh.vertexX(index) * mesh.vertexY(next) - mesh.vertexX(next) * mesh.vertexY(index);
    }
    return sum;
  }
}
