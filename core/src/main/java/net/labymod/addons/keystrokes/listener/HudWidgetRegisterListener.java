package net.labymod.addons.keystrokes.listener;

import com.google.inject.Inject;
import net.labymod.addons.keystrokes.KeyStrokes;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidget;
import net.labymod.api.client.gui.hud.HudWidget;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.gui.hud.HudWidgetRegisterEvent;

public class HudWidgetRegisterListener {

  private final KeyStrokes keyStrokes;

  @Inject
  private HudWidgetRegisterListener(KeyStrokes keyStrokes) {
    this.keyStrokes = keyStrokes;
  }

  @Subscribe
  public void onHudWidgetRegister(HudWidgetRegisterEvent event) {
    HudWidget<?, ?> widget = event.widget();
    if (!(widget instanceof KeyStrokesHudWidget)) {
      return;
    }

    this.keyStrokes.registerHudWidget((KeyStrokesHudWidget) widget);
  }
}
