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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.labymod.api.labyconnect.share.ChatSharePreview;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A keystrokes layout as it is shared: the colors and looks of the widget and its keys. Where the
 * widget sits on the HUD and its scale stay the receiver's own.
 *
 * <p>The payload is {@code {"values": {...}}} with the keys of the widget config. The key set
 * holds the base key at its own offset and every other key relative to it, like the config.
 *
 * @param keys the keys, the base key among them
 * @param base the name of the base key
 * @throws IllegalArgumentException if the keys do not hold the base key
 */
public record KeyStrokesLayout(
    @NotNull ShareColor textColor,
    @NotNull ShareColor backgroundColor,
    @NotNull ShareColor pressedColor,
    boolean outline,
    boolean useTextColorAsOutline,
    @NotNull ShareColor outlineColor,
    boolean roundedCorners,
    int roundedRadius,
    boolean transition,
    boolean fancySpace,
    boolean bigSpace,
    boolean trackMouseCPS,
    @NotNull List<KeyEntry> keys,
    @NotNull String base
) {

  /**
   * The most keys a received layout may have, a full keyboard and every mouse button fit.
   */
  public static final int MAX_KEYS = 128;

  /**
   * The farthest a key may sit from the base key, in GUI pixels.
   */
  public static final float MAX_OFFSET = 2048;

  /**
   * The widest a key may be, in GUI pixels.
   */
  public static final float MAX_WIDTH = 512;

  public static final int MIN_RADIUS = 1;
  public static final int MAX_RADIUS = 10;

  public static final float MIN_CHROMA_SPEED = 0.25F;
  public static final float MAX_CHROMA_SPEED = 5.0F;

  static final String VALUES = "values";

  private static final Pattern KEY_NAME = Pattern.compile("[A-Z0-9_]{1,32}");

  private static final String TEXT_COLOR = "textColor";
  private static final String BACKGROUND_COLOR = "backgroundColor";
  private static final String PRESSED_COLOR = "pressedColor";
  private static final String OUTLINE = "outline";
  private static final String USE_TEXT_COLOR_AS_OUTLINE = "useTextColorAsOutline";
  private static final String OUTLINE_COLOR = "outlineColor";
  private static final String ROUNDED_CORNERS = "roundedCorners";
  private static final String ROUNDED_RADIUS = "roundedRadius";
  private static final String TRANSITION = "transition";
  private static final String FANCY_SPACE = "fancySpace";
  private static final String BIG_SPACE = "bigSpace";
  private static final String TRACK_MOUSE_CPS = "trackMouseCPS";
  private static final String KEY_STROKES = "keyStrokes";
  private static final String BASE = "base";

  private static final String KEY = "key";
  private static final String X = "x";
  private static final String Y = "y";
  private static final String WIDTH = "width";

  private static final String COLOR_VALUE = "value";
  private static final String COLOR_CHROMA = "chroma";
  private static final String COLOR_CHROMA_SPEED = "chromaSpeed";

  static final String SPACE = "SPACE";
  static final float KEY_HEIGHT = 20;
  static final float FANCY_SPACE_HEIGHT = 14;

  public KeyStrokesLayout {
    Objects.requireNonNull(textColor, "textColor");
    Objects.requireNonNull(backgroundColor, "backgroundColor");
    Objects.requireNonNull(pressedColor, "pressedColor");
    Objects.requireNonNull(outlineColor, "outlineColor");
    Objects.requireNonNull(base, "base");
    keys = List.copyOf(keys);
    if (keys.stream().noneMatch(key -> key.key().equals(base))) {
      throw new IllegalArgumentException("The keys do not hold the base key " + base);
    }
  }

  /**
   * Reads a received layout. Keys this client does not know are left out, the rest of the layout
   * still applies; anything else that is missing, of the wrong type or out of range rejects it.
   *
   * @param knownKey whether this client knows a key by its name
   * @return the layout, or {@code null} if the payload is not a valid one
   */
  public static @Nullable KeyStrokesLayout read(
      @NotNull JsonObject data,
      @NotNull Predicate<String> knownKey
  ) {
    if (!(data.get(VALUES) instanceof JsonObject values)) {
      return null;
    }

    ShareColor textColor = color(values.get(TEXT_COLOR));
    ShareColor backgroundColor = color(values.get(BACKGROUND_COLOR));
    ShareColor pressedColor = color(values.get(PRESSED_COLOR));
    ShareColor outlineColor = color(values.get(OUTLINE_COLOR));
    Boolean outline = bool(values.get(OUTLINE));
    Boolean useTextColorAsOutline = bool(values.get(USE_TEXT_COLOR_AS_OUTLINE));
    Boolean roundedCorners = bool(values.get(ROUNDED_CORNERS));
    Integer roundedRadius = integer(values.get(ROUNDED_RADIUS), MIN_RADIUS, MAX_RADIUS);
    Boolean transition = bool(values.get(TRANSITION));
    Boolean fancySpace = bool(values.get(FANCY_SPACE));
    Boolean bigSpace = bool(values.get(BIG_SPACE));
    Boolean trackMouseCPS = bool(values.get(TRACK_MOUSE_CPS));
    String base = keyName(values.get(BASE));
    if (textColor == null || backgroundColor == null || pressedColor == null
        || outlineColor == null || outline == null || useTextColorAsOutline == null
        || roundedCorners == null || roundedRadius == null || transition == null
        || fancySpace == null || bigSpace == null || trackMouseCPS == null || base == null) {
      return null;
    }

    if (!(values.get(KEY_STROKES) instanceof JsonArray keyStrokes)
        || keyStrokes.isEmpty() || keyStrokes.size() > MAX_KEYS) {
      return null;
    }

    List<KeyEntry> keys = new ArrayList<>(keyStrokes.size());
    Set<String> names = new HashSet<>();
    boolean hasBase = false;
    for (JsonElement element : keyStrokes) {
      KeyEntry entry = keyEntry(element);
      if (entry == null || !names.add(entry.key())) {
        return null;
      }

      // A key from a newer client or another keyboard layout is left out, the others still fit
      if (!knownKey.test(entry.key())) {
        continue;
      }

      hasBase |= entry.key().equals(base);
      keys.add(entry);
    }

    // Every other key is placed relative to the base key, without it the layout falls apart
    if (!hasBase) {
      return null;
    }

    return new KeyStrokesLayout(
        textColor, backgroundColor, pressedColor, outline, useTextColorAsOutline, outlineColor,
        roundedCorners, roundedRadius, transition, fancySpace, bigSpace, trackMouseCPS,
        keys, base
    );
  }

  private static @Nullable KeyEntry keyEntry(@Nullable JsonElement element) {
    if (!(element instanceof JsonObject object)) {
      return null;
    }

    String key = keyName(object.get(KEY));
    Float x = number(object.get(X), -MAX_OFFSET, MAX_OFFSET);
    Float y = number(object.get(Y), -MAX_OFFSET, MAX_OFFSET);
    Float width = number(object.get(WIDTH), 1, MAX_WIDTH);
    if (key == null || x == null || y == null || width == null) {
      return null;
    }

    return new KeyEntry(key, x, y, width);
  }

  private static @Nullable String keyName(@Nullable JsonElement element) {
    if (!(element instanceof JsonPrimitive primitive) || !primitive.isString()) {
      return null;
    }

    String name = primitive.getAsString();
    return KEY_NAME.matcher(name).matches() ? name : null;
  }

  private static @Nullable ShareColor color(@Nullable JsonElement element) {
    if (element instanceof JsonPrimitive primitive) {
      Integer value = argb(primitive);
      return value == null ? null : new ShareColor(value, false, ShareColor.DEFAULT_CHROMA_SPEED);
    }

    if (!(element instanceof JsonObject object)
        || !(object.get(COLOR_VALUE) instanceof JsonPrimitive valuePrimitive)) {
      return null;
    }

    Integer value = argb(valuePrimitive);
    if (value == null) {
      return null;
    }

    boolean chroma = false;
    if (object.has(COLOR_CHROMA)) {
      Boolean chromaValue = bool(object.get(COLOR_CHROMA));
      if (chromaValue == null) {
        return null;
      }

      chroma = chromaValue;
    }

    float speed = ShareColor.DEFAULT_CHROMA_SPEED;
    if (object.has(COLOR_CHROMA_SPEED)) {
      Float speedValue = number(
          object.get(COLOR_CHROMA_SPEED),
          MIN_CHROMA_SPEED,
          MAX_CHROMA_SPEED
      );
      if (speedValue == null) {
        return null;
      }

      speed = speedValue;
    }

    return new ShareColor(value, chroma, speed);
  }

  /**
   * An ARGB color, signed like the config writes it or unsigned.
   */
  private static @Nullable Integer argb(@NotNull JsonPrimitive primitive) {
    if (!primitive.isNumber()) {
      return null;
    }

    double value = primitive.getAsDouble();
    if (value != Math.rint(value) || value < Integer.MIN_VALUE || value > 0xFFFFFFFFL) {
      return null;
    }

    return (int) (long) value;
  }

  private static @Nullable Boolean bool(@Nullable JsonElement element) {
    return element instanceof JsonPrimitive primitive && primitive.isBoolean()
        ? primitive.getAsBoolean()
        : null;
  }

  private static @Nullable Integer integer(@Nullable JsonElement element, int min, int max) {
    if (!(element instanceof JsonPrimitive primitive) || !primitive.isNumber()) {
      return null;
    }

    double value = primitive.getAsDouble();
    return value == Math.rint(value) && value >= min && value <= max ? (int) value : null;
  }

  private static @Nullable Float number(@Nullable JsonElement element, float min, float max) {
    if (!(element instanceof JsonPrimitive primitive) || !primitive.isNumber()) {
      return null;
    }

    // NaN fails both comparisons, infinities fail the range
    double value = primitive.getAsDouble();
    return value >= min && value <= max ? (float) value : null;
  }

  /**
   * The payload, in the shape the widget config is saved in.
   */
  public @NotNull JsonObject write() {
    JsonObject values = new JsonObject();
    values.add(TEXT_COLOR, this.textColor.write());
    values.add(BACKGROUND_COLOR, this.backgroundColor.write());
    values.add(PRESSED_COLOR, this.pressedColor.write());
    values.addProperty(OUTLINE, this.outline);
    values.addProperty(USE_TEXT_COLOR_AS_OUTLINE, this.useTextColorAsOutline);
    values.add(OUTLINE_COLOR, this.outlineColor.write());
    values.addProperty(ROUNDED_CORNERS, this.roundedCorners);
    values.addProperty(ROUNDED_RADIUS, this.roundedRadius);
    values.addProperty(TRANSITION, this.transition);
    values.addProperty(FANCY_SPACE, this.fancySpace);
    values.addProperty(BIG_SPACE, this.bigSpace);
    values.addProperty(TRACK_MOUSE_CPS, this.trackMouseCPS);

    JsonArray keyStrokes = new JsonArray();
    for (KeyEntry key : this.keys) {
      JsonObject keyObject = new JsonObject();
      keyObject.addProperty(KEY, key.key());
      keyObject.addProperty(X, key.x());
      keyObject.addProperty(Y, key.y());
      keyObject.addProperty(WIDTH, key.width());
      keyObject.addProperty(BIG_SPACE, this.bigSpace && SPACE.equals(key.key()));
      keyStrokes.add(keyObject);
    }

    values.add(KEY_STROKES, keyStrokes);
    values.addProperty(BASE, this.base);

    JsonObject data = new JsonObject();
    data.add(VALUES, values);
    return data;
  }

  /**
   * The keys as drawn, in their background color with outline, rounded corners and labels.
   */
  public @NotNull ChatSharePreview preview() {
    return KeyStrokesPreview.render(this);
  }

  /**
   * The height of a key as drawn.
   */
  float height(@NotNull KeyEntry key) {
    return this.fancySpace && SPACE.equals(key.key()) ? FANCY_SPACE_HEIGHT : KEY_HEIGHT;
  }

  /**
   * The base key, a layout always holds it.
   */
  @NotNull KeyEntry baseKey() {
    for (KeyEntry key : this.keys) {
      if (key.key().equals(this.base)) {
        return key;
      }
    }

    throw new IllegalStateException("The layout has no base key " + this.base);
  }

  /**
   * One key of the layout. The base key is at its offset in the widget, any other key at its
   * offset from the base key, in GUI pixels.
   */
  public record KeyEntry(@NotNull String key, float x, float y, float width) {

    public KeyEntry {
      Objects.requireNonNull(key, "key");
    }
  }

  /**
   * A color like the color picker saves it: ARGB, and whether it cycles through the rainbow.
   */
  public record ShareColor(int argb, boolean chroma, float chromaSpeed) {

    public static final float DEFAULT_CHROMA_SPEED = 1.0F;

    @NotNull JsonElement write() {
      boolean defaultSpeed = this.chromaSpeed == DEFAULT_CHROMA_SPEED;
      if (!this.chroma && defaultSpeed) {
        return new JsonPrimitive(this.argb);
      }

      JsonObject object = new JsonObject();
      object.addProperty(COLOR_VALUE, this.argb);
      if (this.chroma) {
        object.addProperty(COLOR_CHROMA, true);
      }

      if (!defaultSpeed) {
        object.addProperty(COLOR_CHROMA_SPEED, this.chromaSpeed);
      }

      return object;
    }
  }
}
