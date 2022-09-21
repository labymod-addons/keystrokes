package net.labymod.addons.keystrokes.hudwidget;

import net.labymod.addons.keystrokes.KeyStrokes;
import net.labymod.api.client.gui.hud.HudWidget;
import net.labymod.api.client.gui.hud.annotation.HudWidgetTypeProvider;
import net.labymod.api.client.gui.hud.category.HudWidgetMiscellaneousCategory;
import net.labymod.api.client.gui.hud.info.HudWidgetInfo;
import net.labymod.api.inject.LabyGuice;
import org.jetbrains.annotations.NotNull;

@HudWidgetTypeProvider(
    typeId = "keyStrokes",
    categoryClass = HudWidgetMiscellaneousCategory.class,
    configClass = KeyStrokesHudWidgetConfig.class
)
public class KeyStrokesHudWidget extends HudWidget<KeyStrokesWidget, KeyStrokesHudWidgetConfig> {

  private static final KeyStrokes KEY_STROKES = LabyGuice.getInstance(KeyStrokes.class);

  protected KeyStrokesHudWidget(
      @NotNull HudWidgetInfo info,
      @NotNull KeyStrokesHudWidgetConfig config
  ) {
    super(info, config, () -> {
      return new KeyStrokesWidget(config, KEY_STROKES);
    });
  }
}
