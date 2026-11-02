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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.labymod.addons.keystrokes.share.KeyStrokesLayout.KeyEntry;
import net.labymod.addons.keystrokes.share.KeyStrokesLayout.ShareColor;
import net.labymod.api.labyconnect.share.ChatSharePreview;
import org.junit.jupiter.api.Test;

class KeyStrokesLayoutTest {

  private static final Predicate<String> ANY_KEY = name -> true;

  /**
   * The widget config as the client saves it, with the HUD placement around it.
   */
  private static final String SAVED_CONFIG = """
      {
        "textColor": -1,
        "backgroundColor": -1694498816,
        "pressedColor": 1761607679,
        "outline": false,
        "useTextColorAsOutline": true,
        "outlineColor": -1,
        "roundedCorners": false,
        "roundedRadius": 5,
        "transition": true,
        "fancySpace": true,
        "bigSpace": false,
        "trackMouseCPS": true,
        "keyStrokes": [
          {"key": "A", "x": -22.0, "y": 0.0, "width": 20.0, "bigSpace": false},
          {"key": "W", "x": 0.0, "y": -22.0, "width": 20.0, "bigSpace": false},
          {"key": "D", "x": 22.0, "y": 0.0, "width": 20.0, "bigSpace": false},
          {"key": "LEFT", "x": -22.0, "y": 38.0, "width": 31.0, "bigSpace": false},
          {"key": "S", "x": 22.0, "y": 22.0, "width": 20.0, "bigSpace": false},
          {"key": "SPACE", "x": -9.0, "y": 22.0, "width": 20.0, "bigSpace": false},
          {"key": "RIGHT", "x": 11.0, "y": 38.0, "width": 31.0, "bigSpace": false}
        ],
        "base": "S",
        "scale": 1.0,
        "areaIdentifier": "TOP_LEFT",
        "x": 0.0,
        "y": 0.0,
        "enabled": false,
        "configVersion": 1
      }
      """;

  private static JsonObject payload() {
    JsonObject data = new JsonObject();
    data.add(KeyStrokesLayout.VALUES, JsonParser.parseString(SAVED_CONFIG));
    return data;
  }

  private static JsonObject payload(Consumer<JsonObject> change) {
    JsonObject data = payload();
    change.accept(data.getAsJsonObject(KeyStrokesLayout.VALUES));
    return data;
  }

  private static JsonObject key(JsonObject values, int index) {
    return values.getAsJsonArray("keyStrokes").get(index).getAsJsonObject();
  }

  private static KeyStrokesLayout layout() {
    KeyStrokesLayout layout = KeyStrokesLayout.read(payload(), ANY_KEY);
    assertNotNull(layout);
    return layout;
  }

  @Test
  void readsTheSavedConfig() {
    KeyStrokesLayout layout = layout();

    assertEquals(new ShareColor(-1, false, 1.0F), layout.textColor());
    assertEquals(-1694498816, layout.backgroundColor().argb());
    assertEquals(5, layout.roundedRadius());
    assertTrue(layout.fancySpace());
    assertEquals("S", layout.base());
    assertEquals(7, layout.keys().size());
    assertEquals(new KeyEntry("LEFT", -22, 38, 31), layout.keys().get(3));
  }

  @Test
  void writesWhatItReads() {
    KeyStrokesLayout layout = layout();

    assertEquals(layout, KeyStrokesLayout.read(layout.write(), ANY_KEY));
  }

  @Test
  void writesOnlyTheLayoutNotThePlacement() {
    JsonObject values = layout().write().getAsJsonObject(KeyStrokesLayout.VALUES);

    assertNull(values.get("scale"));
    assertNull(values.get("x"));
    assertNull(values.get("enabled"));
    assertEquals(new JsonPrimitive(-1), values.get("textColor"));
  }

  @Test
  void keepsRainbowColors() {
    JsonObject data = payload(values -> {
      JsonObject color = new JsonObject();
      color.addProperty("value", 0xFF00FF00);
      color.addProperty("chroma", true);
      color.addProperty("chromaSpeed", 2.5F);
      values.add("textColor", color);
    });

    KeyStrokesLayout layout = KeyStrokesLayout.read(data, ANY_KEY);
    assertNotNull(layout);
    assertEquals(new ShareColor(0xFF00FF00, true, 2.5F), layout.textColor());
    assertEquals(layout, KeyStrokesLayout.read(layout.write(), ANY_KEY));
  }

  @Test
  void readsUnsignedColors() {
    KeyStrokesLayout layout = KeyStrokesLayout.read(
        payload(values -> values.addProperty("textColor", 0xFFFFFFFFL)),
        ANY_KEY
    );

    assertNotNull(layout);
    assertEquals(-1, layout.textColor().argb());
  }

  @Test
  void leavesOutKeysThisClientDoesNotKnow() {
    JsonObject data = payload(values -> {
      JsonObject future = new JsonObject();
      future.addProperty("key", "F_99");
      future.addProperty("x", 44);
      future.addProperty("y", 0);
      future.addProperty("width", 20);
      values.getAsJsonArray("keyStrokes").add(future);
    });

    KeyStrokesLayout layout = KeyStrokesLayout.read(data, name -> !name.equals("F_99"));

    assertNotNull(layout);
    assertEquals(7, layout.keys().size());
  }

  @Test
  void ignoresUnknownValues() {
    assertNotNull(KeyStrokesLayout.read(
        payload(values -> values.addProperty("somethingNew", "x")),
        ANY_KEY
    ));
  }

  @Test
  void rejectsAMissingBaseKey() {
    assertNull(KeyStrokesLayout.read(payload(), name -> !name.equals("S")));
    assertNull(KeyStrokesLayout.read(payload(values -> values.addProperty("base", "Q")), ANY_KEY));
  }

  @Test
  void rejectsMalformedPayloads() {
    List<Consumer<JsonObject>> changes = List.of(
        values -> values.remove("textColor"),
        values -> values.addProperty("outline", "true"),
        values -> values.addProperty("textColor", 1.5),
        values -> values.addProperty("textColor", 0x1FFFFFFFFL),
        values -> values.add("textColor", new JsonObject()),
        values -> values.add("textColor", chroma("chroma", new JsonPrimitive(1))),
        values -> values.add("textColor", chroma("chromaSpeed", new JsonPrimitive(50))),
        values -> values.addProperty("roundedRadius", 0),
        values -> values.addProperty("roundedRadius", 11),
        values -> values.addProperty("roundedRadius", 2.5),
        values -> values.addProperty("base", "s"),
        values -> values.add("keyStrokes", new JsonArray()),
        values -> values.add("keyStrokes", new JsonObject()),
        values -> values.getAsJsonArray("keyStrokes").add(new JsonPrimitive("W")),
        values -> key(values, 0).addProperty("key", "W"),
        values -> key(values, 0).addProperty("key", "LEFT SHIFT"),
        values -> key(values, 0).addProperty("key", "A".repeat(33)),
        values -> key(values, 0).remove("width"),
        values -> key(values, 0).addProperty("x", 4096),
        values -> key(values, 0).addProperty("y", Double.NaN),
        values -> key(values, 0).addProperty("y", Double.POSITIVE_INFINITY),
        values -> key(values, 0).addProperty("width", 0),
        values -> key(values, 0).addProperty("width", 1024)
    );

    for (int i = 0; i < changes.size(); i++) {
      assertNull(KeyStrokesLayout.read(payload(changes.get(i)), ANY_KEY), "change " + i);
    }

    assertNull(KeyStrokesLayout.read(new JsonObject(), ANY_KEY));
  }

  private static JsonObject chroma(String key, JsonElement value) {
    JsonObject color = new JsonObject();
    color.addProperty("value", -1);
    color.add(key, value);
    return color;
  }

  @Test
  void capsTheNumberOfKeys() {
    JsonArray keys = new JsonArray();
    for (int i = 0; i <= KeyStrokesLayout.MAX_KEYS; i++) {
      JsonObject key = new JsonObject();
      key.addProperty("key", "K" + i);
      key.addProperty("x", i);
      key.addProperty("y", 0);
      key.addProperty("width", 20);
      keys.add(key);
    }

    JsonObject tooMany = payload(values -> {
      values.add("keyStrokes", keys);
      values.addProperty("base", "K0");
    });
    assertNull(KeyStrokesLayout.read(tooMany, ANY_KEY));

    keys.remove(KeyStrokesLayout.MAX_KEYS);
    assertNotNull(KeyStrokesLayout.read(tooMany, ANY_KEY));
  }

  @Test
  void needsTheBaseKeyAmongTheKeys() {
    KeyStrokesLayout layout = layout();

    assertThrows(IllegalArgumentException.class, () -> new KeyStrokesLayout(
        layout.textColor(), layout.backgroundColor(), layout.pressedColor(), false, true,
        layout.outlineColor(), false, 5, true, true, false, true,
        List.of(new KeyEntry("W", 0, 0, 20)), "S"
    ));
  }

  @Test
  void drawsTheKeysAtTheirPlaces() {
    KeyStrokesLayout layout = layout();
    ChatSharePreview preview = layout.preview();

    // A, S and D in a row, W above, the mouse buttons below the space bar
    assertEquals(64, preview.width());
    assertEquals(80, preview.height());

    int background = layout.backgroundColor().argb();
    int text = layout.textColor().argb();
    // The top left corner of W, the gap between W and A, and the corner of A
    assertEquals(background, preview.pixel(22, 0));
    assertEquals(0, preview.pixel(10, 10));
    assertEquals(background, preview.pixel(0, 22));

    assertTrue(colors(preview, 22, 0, 20, 20).contains(text), "W has a label");
    // The fancy space bar is drawn, not the word
    assertEquals(text, preview.pixel(13 + 10, 44 + 5));
  }

  @Test
  void drawsOutlineAndRoundedCorners() {
    KeyStrokesLayout layout = KeyStrokesLayout.read(payload(values -> {
      values.addProperty("outline", true);
      values.addProperty("useTextColorAsOutline", false);
      values.addProperty("outlineColor", 0xFFFF0000);
      values.addProperty("roundedCorners", true);
      values.addProperty("roundedRadius", 4);
    }), ANY_KEY);
    assertNotNull(layout);

    ChatSharePreview preview = layout.preview();
    // W spans 22..41 by 0..19
    assertEquals(0, preview.pixel(22, 0), "the corner is cut off");
    assertEquals(0xFFFF0000, preview.pixel(32, 0), "the top edge is outlined");
    assertEquals(layout.backgroundColor().argb(), preview.pixel(25, 15));
    assertNotEquals(0xFFFF0000, preview.pixel(32, 1));
  }

  @Test
  void scalesLargeLayoutsDown() {
    KeyStrokesLayout layout = KeyStrokesLayout.read(payload(values -> {
      key(values, 0).addProperty("x", -KeyStrokesLayout.MAX_OFFSET);
      key(values, 1).addProperty("y", KeyStrokesLayout.MAX_OFFSET);
    }), ANY_KEY);
    assertNotNull(layout);

    ChatSharePreview preview = layout.preview();
    assertTrue(preview.width() <= ChatSharePreview.MAX_SIZE);
    assertTrue(preview.height() <= ChatSharePreview.MAX_SIZE);
    assertTrue(preview.width() > 1 && preview.height() > 1);
  }

  @Test
  void labelsKeysLikeTheWidget() {
    assertEquals("LMB", KeyStrokesPreview.label("LEFT"));
    assertEquals("1", KeyStrokesPreview.label("NUM_1"));
    assertEquals("7", KeyStrokesPreview.label("NUMPAD_7"));
    assertEquals("F12", KeyStrokesPreview.label("F_12"));
    assertEquals("W", KeyStrokesPreview.label("W"));
    assertNull(KeyStrokesPreview.label("LEFT_SHIFT"));
  }

  private static Set<Integer> colors(ChatSharePreview preview, int x, int y, int width,
      int height) {
    List<Integer> colors = new ArrayList<>();
    for (int row = y; row < y + height; row++) {
      for (int column = x; column < x + width; column++) {
        colors.add(preview.pixel(column, row));
      }
    }

    return Set.copyOf(colors);
  }
}
