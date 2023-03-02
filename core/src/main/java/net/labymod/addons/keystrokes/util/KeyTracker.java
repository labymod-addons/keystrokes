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

package net.labymod.addons.keystrokes.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongListIterator;

public class KeyTracker {

  private final LongList clicks = new LongArrayList();

  public void press() {
    this.clicks.add(System.currentTimeMillis() + 1000);
  }

  public void update() {
    long currentTime = System.currentTimeMillis();
    LongListIterator iterator = this.clicks.iterator();
    while (iterator.hasNext()) {
      if (iterator.nextLong() < currentTime) {
        iterator.remove();
      }
    }
  }

  public int getCount() {
    return this.clicks.size();
  }
}
