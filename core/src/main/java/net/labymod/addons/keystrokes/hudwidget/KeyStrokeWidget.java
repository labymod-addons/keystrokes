package net.labymod.addons.keystrokes.hudwidget;

import net.labymod.addons.keystrokes.KeyStroke;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;

public class KeyStrokeWidget extends SimpleWidget {

  private final Key key;
  private final KeyStroke keyStroke;

  public KeyStrokeWidget(Key key, KeyStroke keyStroke) {
    this.key = key;
    this.keyStroke = keyStroke;
  }


}
