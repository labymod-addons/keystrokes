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

package net.labymod.addons.keystrokes.widgets;

import java.awt.*;
import java.util.function.Consumer;
import net.labymod.addons.keystrokes.KeyStrokeConfig;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidgetConfig;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesWidget;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.lss.property.LssProperty;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.bounds.Rectangle;

@AutoWidget
public class KeyStrokeManageWidget extends KeyStrokesWidget {

  private static final Key CLICK_KEY = MouseButton.LEFT;
  private static final long DRAGGING_DELAY = 100L;

  private final LssProperty<Icon> anchorIcon = new LssProperty<>(null);
  private final Consumer<KeyStrokeConfig> selectConsumer;
  private KeyStrokeWidget editing;
  private float offsetX;
  private float offsetY;
  private long dragStartTime;

  public KeyStrokeManageWidget(KeyStrokesHudWidgetConfig config,
      Consumer<KeyStrokeConfig> selectConsumer) {
    super(config);
    this.selectConsumer = selectConsumer;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
  }

  @Override
  public void tick() {
    super.tick();
    this.checkForNewKeyStrokes();
  }

  @Override
  protected void updateWidgetBounds(Rectangle bounds) {
    super.updateWidgetBounds(bounds);
    Bounds parentBounds = this.parent.bounds();
    this.bounds.setPosition(parentBounds.getCenterX() - bounds.getWidth() / 2,
        parentBounds.getCenterY() - bounds.getHeight() / 2);
  }

  @Override
  public void renderWidget(Stack stack, MutableMouse mouse, float partialTicks) {
    if (this.editing == null) {
      super.renderWidget(stack, mouse, partialTicks);
      return;
    }

    if (this.dragStartTime == -1
        || this.dragStartTime + DRAGGING_DELAY > System.currentTimeMillis()) {
      super.renderWidget(stack, mouse, partialTicks);
      this.highlightEditing(stack);
      return;
    }

    System.out.println("DRAGGING " + this.offsetX + " | " + this.offsetY);
    super.renderDebug(stack);
    for (Widget child : this.children) {
      if (child == this.editing) {
        continue;
      }

      child.render(stack, mouse, partialTicks);
    }

    Bounds parentBounds = this.bounds;
    Bounds bounds = this.editing.bounds();
    float x = mouse.getX() - this.offsetX;
    if (x < parentBounds.getX()) {
      x = parentBounds.getX();
    } else if (x + bounds.getWidth() > parentBounds.getMaxX()) {
      x = parentBounds.getMaxX() - bounds.getWidth();
    }

    float y = mouse.getY() - this.offsetY;
    if (y < parentBounds.getY()) {
      y = parentBounds.getY();
    } else if (y + bounds.getHeight() > parentBounds.getMaxY()) {
      y = parentBounds.getMaxY() - bounds.getHeight();
    }

    bounds.setOuterPosition(x, y);

    this.editing.render(stack, mouse, partialTicks);
    this.highlightEditing(stack);
  }

  private void highlightEditing(Stack stack) {
    Bounds parentBounds = this.bounds;
    Bounds bounds = this.editing.bounds();
    RectangleRenderer rectangleRenderer = this.labyAPI.renderPipeline().rectangleRenderer();
    rectangleRenderer.pos(parentBounds.getX(), bounds.getCenterY() - 0.5F, bounds.getX(),
        bounds.getCenterY() + 0.5F).color(Color.GRAY.getRGB()).render(stack);

    rectangleRenderer.pos(bounds.getCenterX() - 0.5F, parentBounds.getY(),
        bounds.getCenterX() + 0.5F, bounds.getY()).color(Color.GRAY.getRGB()).render(stack);

    rectangleRenderer.pos(bounds.getMaxX(), bounds.getCenterY() - 0.5F, parentBounds.getMaxX(),
        bounds.getCenterY() + 0.5F).color(Color.GRAY.getRGB()).render(stack);

    rectangleRenderer.pos(bounds.getCenterX() - 0.5F, parentBounds.getMaxY(),
        bounds.getCenterX() + 0.5F, bounds.getMaxY()).color(Color.GRAY.getRGB()).render(stack);
  }

  @Override
  public boolean mouseClicked(MutableMouse mouse, MouseButton button) {
    if (button != CLICK_KEY) {
      return false;
    }

    KeyStrokeWidget clicked = null;
    for (Widget child : this.children) {
      if (!(child instanceof KeyStrokeWidget)) {
        continue;
      }

      KeyStrokeWidget keyStrokeWidget = (KeyStrokeWidget) child;
      if (keyStrokeWidget.isHovered()) {
        clicked = keyStrokeWidget;
        break;
      }
    }

    if (this.editing == null && clicked == null) {
      return false;
    }

    this.editing = clicked;
    this.selectConsumer.accept(clicked == null ? null : clicked.getKeyStroke());

    if (clicked == null) {
      return false;
    }

    this.offsetX = mouse.getX() - this.editing.bounds().getX();
    this.offsetY = mouse.getY() - this.editing.bounds().getY();
    this.dragStartTime = System.currentTimeMillis();
    return true;
  }

  @Override
  public boolean mouseReleased(MutableMouse mouse, MouseButton mouseButton) {
    System.out.println("MouseReleased");
    if (this.editing == null || this.dragStartTime == -1) {
      return false;
    }

    this.dragStartTime = -1;
    return true;
  }

  public LssProperty<Icon> anchorIcon() {
    return this.anchorIcon;
  }

  @Override
  protected boolean isEditing() {
    return true;
  }

  public void setFocused(KeyStrokeConfig keyStrokeConfig) {
    if (keyStrokeConfig == null) {
      this.editing = null;
      return;
    }

    for (Widget child : this.children) {
      if (!(child instanceof KeyStrokeWidget)) {
        continue;
      }

      KeyStrokeWidget keyStrokeWidget = (KeyStrokeWidget) child;
      if (keyStrokeWidget.getKeyStroke() == keyStrokeConfig) {
        this.editing = keyStrokeWidget;
        this.dragStartTime = -1;
        return;
      }
    }
  }
}
