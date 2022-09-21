package net.labymod.addons.keystrokes.listener;

import com.google.inject.Inject;
import net.labymod.addons.keystrokes.KeyStrokes;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidget;
import net.labymod.api.client.gui.hud.HudWidget;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.gui.hud.HudWidgetUnregisterEvent;

public class HudWidgetUnregisterListener {

  private final KeyStrokes keyStrokes;

  @Inject
  private HudWidgetUnregisterListener(KeyStrokes keyStrokes) {
    this.keyStrokes = keyStrokes;
  }

  @Subscribe
  public void onHudWidgetUnregister(HudWidgetUnregisterEvent event) {
    HudWidget<?, ?> widget = event.widget();
    if (!(widget instanceof KeyStrokesHudWidget)) {
      return;
    }

    this.keyStrokes.unregisterHudWidget((KeyStrokesHudWidget) widget);
  }
}
