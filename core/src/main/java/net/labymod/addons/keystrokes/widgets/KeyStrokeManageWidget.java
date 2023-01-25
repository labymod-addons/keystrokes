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
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.lss.property.LssProperty;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.bounds.ModifyReason;
import net.labymod.api.util.bounds.Point;
import net.labymod.api.util.bounds.Rectangle;

@AutoWidget
public class KeyStrokeManageWidget extends KeyStrokesWidget {

  private static final ModifyReason REASON = ModifyReason.of("keyStrokeUpdate");

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
  public void tick() {
    super.tick();
    this.checkForNewKeyStrokes();
  }

  @Override
  protected void updateWidgetBounds(Rectangle bounds) {
    this.updateWidgetBounds(
        Point.fixed(
            (int) bounds.getCenterX(),
            (int) bounds.getCenterY()
        ),
        false,
        true
    );
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

    Bounds parentBounds = this.bounds();
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

    bounds.setOuterPosition(x, y, REASON);

    this.editing.render(stack, mouse, partialTicks);
    this.highlightEditing(stack);
  }

  private void highlightEditing(Stack stack) {
    Bounds parentBounds = this.bounds();
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

    KeyStrokeWidget clicked = this.findFirstChildIf(AbstractWidget::isHovered);
    if (this.editing == null && clicked == null) {
      return false;
    }

    this.editing = clicked;
    this.selectConsumer.accept(clicked == null ? null : clicked.config());

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
    if (this.editing == null || this.dragStartTime == -1) {
      return false;
    }

    long dragStartTime = this.dragStartTime;
    this.dragStartTime = -1;
    if (System.currentTimeMillis() < dragStartTime + DRAGGING_DELAY) {
      return true;
    }

    if (this.updateKeyStrokePosition(this.editing)) {
      this.updateSize();
      this.hudWidgetConfig.widget().updateKeyStrokeBounds();
    }

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

    KeyStrokeWidget keyStrokeWidget = this.findFirstChildIf(
        child -> child.config() == keyStrokeConfig
    );

    if (keyStrokeWidget != null) {
      this.editing = keyStrokeWidget;
      this.dragStartTime = -1;
    }
  }

  private boolean updateKeyStrokePosition(KeyStrokeWidget widget) {
    Bounds keyStrokeBounds = widget.bounds();
    float x = keyStrokeBounds.getX();
    float y = keyStrokeBounds.getY();

    Key key = widget.key();
    Key anchorKey = this.hudWidgetConfig.base().get();
    KeyStrokeConfig anchorConfig = this.hudWidgetConfig.getKeyStroke(anchorKey);
    if (key == anchorKey) {
      return this.updateAnchorPosition(anchorConfig, x, y);
    }

    Widget anchor = this.findFirstChildIf(child -> child.config() == anchorConfig);
    if (anchor == null) {
      return false;
    }

    Bounds anchorBounds = anchor.bounds();
    float anchorX = anchorBounds.getX();
    float anchorY = anchorBounds.getY();

    float newX = x - anchorX;
    float newY = y - anchorY;
    KeyStrokeConfig config = widget.config();
    if (config.getX() == newX && config.getY() == newY) {
      return false;
    }

    System.out.println(
        "Update " + key + " from x:" + config.getX() + " y:" + config.getY() + " to x:" + newX
            + " y:" + newY);

    config.updatePosition(newX, newY);
    return true;
  }

  private boolean updateAnchorPosition(KeyStrokeConfig anchor, float x, float y) {
    KeyStrokeWidget childWidget = this.findFirstChildIf(child -> child.config() != anchor);
    if (childWidget == null) {
      anchor.updatePosition(0, 0);
      return true;
    }

    Bounds childBounds = childWidget.bounds();
    float childX = childBounds.getX();
    float childY = childBounds.getY();

    float diffX = x - childX + childWidget.config().getX();
    float diffY = y - childY + childWidget.config().getY();

    float newX = anchor.getX() + diffX;
    float newY = anchor.getY() + diffY;
    if (anchor.getX() == newX && anchor.getY() == newY) {
      return false;
    }

    System.out.println(
        "Update anchor from x:" + anchor.getX() + " y:" + anchor.getY() + " to x:" + newX
            + " y:" + newY);

    anchor.updatePosition(
        newX,
        newY
    );

    for (KeyStrokeConfig keyStroke : this.hudWidgetConfig.getKeyStrokes()) {
      if (keyStroke == anchor) {
        continue;
      }

      keyStroke.updatePosition(
          keyStroke.getX() - diffX,
          keyStroke.getY() - diffY
      );
    }

    return true;
  }

  private void updateSize() {
    float minX = 0;
    float minY = 0;
    float maxX = 0;
    float maxY = 0;

    KeyStrokeConfig anchorConfig = null;
    Key key = this.hudWidgetConfig.base().get();
    for (KeyStrokeConfig keyStrokeConfig : this.hudWidgetConfig.getKeyStrokes()) {
      if (keyStrokeConfig.key() == key) {
        anchorConfig = keyStrokeConfig;
        continue;
      }

      if (minX > keyStrokeConfig.getX()) {
        minX = keyStrokeConfig.getX();
      }

      if (minY > keyStrokeConfig.getY()) {
        minY = keyStrokeConfig.getY();
      }

      if (maxX < keyStrokeConfig.getX()) {
        maxX = keyStrokeConfig.getX();
      }

      if (maxY < keyStrokeConfig.getY()) {
        maxY = keyStrokeConfig.getY();
      }
    }

    if (anchorConfig == null) {
      return;
    }

    anchorConfig.updatePosition(
        -minX,
        -minY
    );
  }
}
