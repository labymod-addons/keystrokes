package net.labymod.addons.keystrokes.listener;

import com.google.inject.Inject;
import net.labymod.addons.keystrokes.KeyStroke;
import net.labymod.addons.keystrokes.KeyStrokes;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidget;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.input.KeyEvent;

public class KeyListener {

  private final KeyStrokes keyStrokes;

  @Inject
  private KeyListener(KeyStrokes keyStrokes) {
    this.keyStrokes = keyStrokes;
  }

  @Subscribe
  public void onKey(KeyEvent event) {
    Key key = event.key();
    for (KeyStrokesHudWidget hudWidget : this.keyStrokes.getHudWidgets()) {
      KeyStroke keyStroke = hudWidget.getConfig().getKeyStrokes().get(key);
      if (keyStroke == null) {
        return;
      }

      keyStroke.updatePressed(event.state());
    }
  }
}
