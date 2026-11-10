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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Measures the heap allocations of the current thread, to prove that hot paths allocate nothing.
 */
final class Allocations {

  // Far below one allocation per iteration of the loops that use it
  private static final long TOLERATED_BYTES = 1024L;

  private Allocations() {
  }

  static void assertAllocationFree(int iterations, Runnable action) {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    assumeTrue(bean instanceof com.sun.management.ThreadMXBean,
        "Thread allocation counters are not available");
    com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean) bean;
    assumeTrue(threads.isThreadAllocatedMemorySupported(),
        "Thread allocation counters are not supported");
    threads.setThreadAllocatedMemoryEnabled(true);

    // Warm up, so class loading and first-call work is not counted
    for (int i = 0; i < iterations; i++) {
      action.run();
    }

    long before = threads.getCurrentThreadAllocatedBytes();
    for (int i = 0; i < iterations; i++) {
      action.run();
    }
    long allocated = threads.getCurrentThreadAllocatedBytes() - before;

    assertTrue(allocated < TOLERATED_BYTES,
        "Allocated " + allocated + " bytes in " + iterations + " iterations");
  }
}
