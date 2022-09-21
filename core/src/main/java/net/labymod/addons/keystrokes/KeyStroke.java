package net.labymod.addons.keystrokes;

import net.labymod.api.event.client.input.KeyEvent.State;

public class KeyStroke {

  private final float x;
  private final float y;
  private final float width;
  private final float height;

  private transient boolean pressed;

  public KeyStroke(float x, float y, float width, float height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public void updatePressed(State state) {
    if (state == State.HOLDING && !this.pressed) {
      this.pressed = true;
      return;
    }

    this.pressed = state == State.PRESS;
  }
}
