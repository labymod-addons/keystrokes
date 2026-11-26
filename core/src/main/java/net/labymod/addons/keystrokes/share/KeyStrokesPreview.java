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

package net.labymod.addons.keystrokes.share;

import java.util.HashMap;
import java.util.Map;
import net.labymod.addons.keystrokes.share.KeyStrokesLayout.KeyEntry;
import net.labymod.api.labyconnect.share.ChatSharePreview;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Draws a layout as pixel art, one pixel per GUI pixel: the keys in their background color with
 * outline and rounded corners, and short labels in a tiny font of its own. A layout too large for
 * a preview is drawn smaller, then without labels.
 */
final class KeyStrokesPreview {

  private static final int GLYPH_WIDTH = 3;
  private static final int GLYPH_HEIGHT = 5;

  /**
   * A 3x5 font for the labels, row by row from the top, "1" is a lit pixel.
   */
  private static final String[] GLYPHS = {
      "A010101111101101", "B110101110101110", "C011100100100011", "D110101101101110",
      "E111100110100111", "F111100110100100", "G011100101101011", "H101101111101101",
      "I111010010010111", "J001001001101010", "K101101110101101", "L100100100100111",
      "M101111111101101", "N110101101101101", "O010101101101010", "P110101110100100",
      "Q010101101110011", "R110101110101101", "S011100010001110", "T111010010010010",
      "U101101101101111", "V101101101101010", "W101101111111101", "X101101010101101",
      "Y101101010010010", "Z111001010100111", "0111101101101111", "1010110010010111",
      "2110001010100111", "3110001010001110", "4101101111001001", "5111100110001110",
      "6011100111101111", "7111001010010010", "8111101111101111", "9111101111001110",
  };

  private static final Map<Character, String> FONT = new HashMap<>();

  static {
    for (String glyph : GLYPHS) {
      FONT.put(glyph.charAt(0), glyph.substring(1));
    }
  }

  private KeyStrokesPreview() {
  }

  static @NotNull ChatSharePreview render(@NotNull KeyStrokesLayout layout) {
    KeyEntry base = layout.baseKey();
    int count = layout.keys().size();
    int[] lefts = new int[count];
    int[] tops = new int[count];
    int[] widths = new int[count];
    int[] heights = new int[count];

    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (int i = 0; i < count; i++) {
      KeyEntry key = layout.keys().get(i);
      float x = key == base ? base.x() : base.x() + key.x();
      float y = key == base ? base.y() : base.y() + key.y();

      // Truncated to whole pixels, like the widget places its keys
      lefts[i] = (int) x;
      tops[i] = (int) y;
      widths[i] = Math.max(1, (int) key.width());
      heights[i] = (int) layout.height(key);

      minX = Math.min(minX, lefts[i]);
      minY = Math.min(minY, tops[i]);
      maxX = Math.max(maxX, lefts[i] + widths[i]);
      maxY = Math.max(maxY, tops[i] + heights[i]);
    }

    int spanWidth = maxX - minX;
    int spanHeight = maxY - minY;
    int scale = Math.max(1, ceilDiv(Math.max(spanWidth, spanHeight), ChatSharePreview.MAX_SIZE));
    int width = Math.min(ChatSharePreview.MAX_SIZE, Math.max(1, ceilDiv(spanWidth, scale)));
    int height = Math.min(ChatSharePreview.MAX_SIZE, Math.max(1, ceilDiv(spanHeight, scale)));

    int textColor = layout.textColor().argb();
    int backgroundColor = layout.backgroundColor().argb();
    int outlineColor = layout.useTextColorAsOutline() ? textColor : layout.outlineColor().argb();

    Canvas canvas = new Canvas(width, height);
    for (int i = 0; i < count; i++) {
      float radius = 0;
      if (layout.roundedCorners()) {
        radius = Math.min(heights[i] / 2.0F, layout.roundedRadius());
      }

      Shape shape = new Shape(
          (lefts[i] - minX) / (float) scale,
          (tops[i] - minY) / (float) scale,
          widths[i] / (float) scale,
          heights[i] / (float) scale,
          radius / scale
      );
      canvas.fill(shape, backgroundColor, layout.outline() ? outlineColor : null);

      // Labels need every pixel, scaled down they would be noise
      if (scale == 1) {
        KeyEntry key = layout.keys().get(i);
        drawLabel(canvas, key.key(), layout.fancySpace(), lefts[i] - minX, tops[i] - minY,
            widths[i], heights[i], textColor);
      }
    }

    return ChatSharePreview.pixelArt(width, height, canvas.pixels);
  }

  private static void drawLabel(Canvas canvas, String name, boolean fancySpace, int left,
      int top, int width, int height, int color) {
    if (fancySpace && KeyStrokesLayout.SPACE.equals(name)) {
      // The bar the widget draws in place of the word
      int barWidth = width / 2;
      canvas.fillRect(left + (width - barWidth) / 2, top + 5, barWidth, 1, color);
      return;
    }

    String label = label(name);
    if (label == null) {
      return;
    }

    int labelWidth = label.length() * (GLYPH_WIDTH + 1) - 1;
    if (labelWidth > width - 2 || GLYPH_HEIGHT > height - 2) {
      return;
    }

    int x = left + (width - labelWidth) / 2;
    int y = top + (height - GLYPH_HEIGHT) / 2;
    for (int i = 0; i < label.length(); i++) {
      String glyph = FONT.get(label.charAt(i));
      for (int row = 0; row < GLYPH_HEIGHT; row++) {
        for (int column = 0; column < GLYPH_WIDTH; column++) {
          if (glyph.charAt(row * GLYPH_WIDTH + column) == '1') {
            canvas.set(x + column, y + row, color);
          }
        }
      }

      x += GLYPH_WIDTH + 1;
    }
  }

  /**
   * A short label for a key, close to the one the widget shows, or {@code null} for none.
   */
  static @Nullable String label(@NotNull String name) {
    String label = switch (name) {
      case "LEFT" -> "LMB";
      case "RIGHT" -> "RMB";
      case "MIDDLE" -> "MMB";
      default -> {
        if (name.startsWith("NUMPAD_")) {
          yield name.substring("NUMPAD_".length());
        }

        if (name.startsWith("NUM_")) {
          yield name.substring("NUM_".length());
        }

        if (name.startsWith("F_")) {
          yield "F" + name.substring("F_".length());
        }

        yield name;
      }
    };

    for (int i = 0; i < label.length(); i++) {
      if (!FONT.containsKey(label.charAt(i))) {
        return null;
      }
    }

    return label;
  }

  private static int ceilDiv(int value, int divisor) {
    return (value + divisor - 1) / divisor;
  }

  /**
   * A key in preview pixels, a rectangle with rounded corners.
   */
  private record Shape(float x, float y, float width, float height, float radius) {

    /**
     * Whether the center of a pixel lies inside.
     */
    boolean contains(int pixelX, int pixelY) {
      float x = pixelX + 0.5F;
      float y = pixelY + 0.5F;
      if (x < this.x || y < this.y || x >= this.x + this.width || y >= this.y + this.height) {
        return false;
      }

      // Distance into a corner, zero along the straight edges
      float right = this.x + this.width - this.radius;
      float bottom = this.y + this.height - this.radius;
      float dx = Math.max(0, Math.max(this.x + this.radius - x, x - right));
      float dy = Math.max(0, Math.max(this.y + this.radius - y, y - bottom));
      return dx * dx + dy * dy <= this.radius * this.radius;
    }
  }

  private static final class Canvas {

    private final int width;
    private final int height;
    private final int[] pixels;

    private Canvas(int width, int height) {
      this.width = width;
      this.height = height;
      this.pixels = new int[width * height];
    }

    /**
     * Fills a shape, with a one pixel outline along its edge if a color is given for it.
     */
    void fill(Shape shape, int color, @Nullable Integer outlineColor) {
      int fromX = Math.max(0, (int) Math.floor(shape.x()));
      int fromY = Math.max(0, (int) Math.floor(shape.y()));
      int toX = Math.min(this.width, (int) Math.ceil(shape.x() + shape.width()));
      int toY = Math.min(this.height, (int) Math.ceil(shape.y() + shape.height()));
      for (int y = fromY; y < toY; y++) {
        for (int x = fromX; x < toX; x++) {
          if (!shape.contains(x, y)) {
            continue;
          }

          boolean edge = !shape.contains(x - 1, y) || !shape.contains(x + 1, y)
              || !shape.contains(x, y - 1) || !shape.contains(x, y + 1);
          this.set(x, y, edge && outlineColor != null ? outlineColor : color);
        }
      }
    }

    void fillRect(int x, int y, int width, int height, int color) {
      for (int row = y; row < y + height; row++) {
        for (int column = x; column < x + width; column++) {
          this.set(column, row, color);
        }
      }
    }

    void set(int x, int y, int color) {
      if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
        this.pixels[y * this.width + x] = color;
      }
    }
  }
}
