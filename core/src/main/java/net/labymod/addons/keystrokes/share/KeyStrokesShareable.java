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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.labymod.addons.keystrokes.KeyStrokeConfig;
import net.labymod.addons.keystrokes.event.KeyStrokeUpdateEvent;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidget;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidgetConfig;
import net.labymod.addons.keystrokes.share.KeyStrokesLayout.KeyEntry;
import net.labymod.addons.keystrokes.share.KeyStrokesLayout.ShareColor;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.icon.catalog.IconsCommon;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.labyconnect.share.ApplyResult;
import net.labymod.api.labyconnect.share.ChatShareRegistry;
import net.labymod.api.labyconnect.share.ShareKind;
import net.labymod.api.labyconnect.share.ShareUndo;
import net.labymod.api.util.Color;
import net.labymod.api.util.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The keystrokes widget as something to share in the Laby Chat: its colors, looks and keys.
 * Applying one replaces the widget config, Undo brings back the layout before.
 */
public final class KeyStrokesShareable {

  public static final String ID = "keystrokes:layout";

  private static final String LABEL = "Keystrokes";

  private final ChatShareRegistry registry;
  private final KeyStrokesHudWidget widget;
  private final ShareKind<KeyStrokesLayout> kind;

  private KeyStrokesShareable(
      @NotNull ChatShareRegistry registry,
      @NotNull KeyStrokesHudWidget widget
  ) {
    this.registry = registry;
    this.widget = widget;
    this.kind = ShareKind.builder(
            ID,
            new KeyStrokesCodec(KeyStrokesShareable::isKnownKey),
            this::apply
        )
        .label(LABEL)
        .displayName(widget.displayName())
        .icon(IconsCommon.KEYBOARD)
        .source(this::export)
        .preview(KeyStrokesLayout::preview)
        .build();
  }

  /**
   * Creates the layout kind and registers it.
   */
  static @NotNull KeyStrokesShareable register(@NotNull KeyStrokesHudWidget widget) {
    ChatShareRegistry registry = Laby.references().chatShareRegistry();
    KeyStrokesShareable shareable = new KeyStrokesShareable(registry, widget);
    // The registry checks the namespace against this class, which belongs to the addon
    registry.register(shareable.kind);
    return shareable;
  }

  /**
   * Lets the player pick a friend and sends them the layout as it is configured.
   */
  public void share(@NotNull KeyStrokesHudWidgetConfig config) {
    KeyStrokesLayout layout = layout(config);
    if (layout != null) {
      this.registry.share(this.kind, layout);
    }
  }

  public @NotNull ShareKind<KeyStrokesLayout> kind() {
    return this.kind;
  }

  /**
   * The layout the share menu of a chat sends.
   */
  private @Nullable KeyStrokesLayout export() {
    KeyStrokesHudWidgetConfig config = this.widget.getConfig();
    return config == null ? null : layout(config);
  }

  /**
   * Replaces the widget config with the layout. Fails without changing anything if this client
   * cannot build the keys of the layout.
   */
  private @NotNull CompletableFuture<ApplyResult> apply(@NotNull KeyStrokesLayout value) {
    ThreadSafe.ensureRenderThread();
    KeyStrokesHudWidgetConfig config = this.widget.getConfig();
    if (config == null) {
      return CompletableFuture.completedFuture(ApplyResult.failed(null));
    }

    KeyStrokesLayout before = layout(config);
    if (!this.write(config, value)) {
      return CompletableFuture.completedFuture(ApplyResult.failed(null));
    }

    // A config that lost its base key has no layout to go back to
    ShareUndo undo = before == null ? null : ShareUndo.of(() -> {
      KeyStrokesHudWidgetConfig current = this.widget.getConfig();
      if (current != null) {
        this.write(current, before);
      }
    });
    return CompletableFuture.completedFuture(ApplyResult.applied(undo));
  }

  /**
   * Writes a layout into the config, reloads the widget and saves the HUD config.
   *
   * @return {@code false} if nothing was written since the keys cannot be built
   */
  private boolean write(
      @NotNull KeyStrokesHudWidgetConfig config,
      @NotNull KeyStrokesLayout value
  ) {
    Key base = Key.getByName(value.base());
    Set<KeyStrokeConfig> keyStrokes = new HashSet<>();
    for (KeyEntry entry : value.keys()) {
      Key key = Key.getByName(entry.key());
      if (key != null) {
        keyStrokes.add(new KeyStrokeConfig(key, entry.x(), entry.y()));
      }
    }

    if (base == null || keyStrokes.isEmpty()) {
      return false;
    }

    // The switches move the current keys, so they go before the keys are replaced
    config.textColor().set(color(value.textColor()));
    config.backgroundColor().set(color(value.backgroundColor()));
    config.pressedColor().set(color(value.pressedColor()));
    config.outline().set(value.outline());
    config.useTextColorAsOutline().set(value.useTextColorAsOutline());
    config.outlineColor().set(color(value.outlineColor()));
    config.roundedCorners().set(value.roundedCorners());
    config.roundedRadius().set(value.roundedRadius());
    config.transition().set(value.transition());
    config.fancySpace().set(value.fancySpace());
    config.bigSpace().set(value.bigSpace());
    config.trackMouseCPS().set(value.trackMouseCPS());

    config.replaceKeyStrokes(keyStrokes, base);
    config.refreshCPSTracking();
    config.updateSpace(false);

    // A disabled widget refuses updates, it builds from the new config once it is shown
    if (this.widget.isEnabled()) {
      this.widget.reloadConfig();
      Laby.fireEvent(new KeyStrokeUpdateEvent(true));
    }

    Laby.references().hudWidgetRegistry().saveConfig();
    return true;
  }

  /**
   * The layout as the widget is configured.
   *
   * @return the layout, or {@code null} if the config has lost its base key
   */
  static @Nullable KeyStrokesLayout layout(@NotNull KeyStrokesHudWidgetConfig config) {
    if (config.anchorConfig() == null) {
      return null;
    }

    List<KeyEntry> keys = new ArrayList<>();
    for (KeyStrokeConfig keyStroke : config.getKeyStrokes()) {
      keys.add(new KeyEntry(
          keyStroke.key().getActualName(),
          keyStroke.getX(),
          keyStroke.getY(),
          keyStroke.getWidth()
      ));
    }

    return new KeyStrokesLayout(
        shareColor(config.textColor().get()),
        shareColor(config.backgroundColor().get()),
        shareColor(config.pressedColor().get()),
        config.outline().get(),
        config.useTextColorAsOutline().get(),
        shareColor(config.outlineColor().get()),
        config.roundedCorners().get(),
        config.roundedRadius().get(),
        config.transition().get(),
        config.fancySpace().get(),
        config.bigSpace().get(),
        config.trackMouseCPS().get(),
        keys,
        config.base().get().getActualName()
    );
  }

  /**
   * Whether this client has a key by this name. Called on a background thread while reading.
   */
  private static boolean isKnownKey(@NotNull String name) {
    Key key = Key.getByName(name);
    return key != null && key != Key.NONE;
  }

  private static @NotNull ShareColor shareColor(@NotNull Color color) {
    return new ShareColor(color.withoutChroma().get(), color.isChroma(), color.getChromaSpeed());
  }

  private static @NotNull Color color(@NotNull ShareColor color) {
    Color base = Color.of(color.argb());
    return color.chroma()
        ? base.withChroma(color.chromaSpeed())
        : base.withoutChroma(color.chromaSpeed());
  }
}
