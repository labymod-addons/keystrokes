package net.labymod.addons.keystrokes.hudwidget;

import net.labymod.addons.keystrokes.KeyStrokes;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.render.matrix.Stack;

public class KeyStrokesWidget extends SimpleWidget {

  private final KeyStrokesHudWidgetConfig hudWidgetConfig;
  private final KeyStrokes keyStrokes;

  public KeyStrokesWidget(KeyStrokesHudWidgetConfig hudWidgetConfig, KeyStrokes keyStrokes) {
    this.hudWidgetConfig = hudWidgetConfig;
    this.keyStrokes = keyStrokes;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
    this.bounds.setSize(50, 20);
    this.backgroundColor().set(this.hudWidgetConfig.backgroundColor().get());
  }

  @Override
  public void render(Stack stack, MutableMouse mouse, float partialTicks) {
    this.backgroundColor().set(this.hudWidgetConfig.backgroundColor().get());
    super.render(stack, mouse, partialTicks);
  }
}
