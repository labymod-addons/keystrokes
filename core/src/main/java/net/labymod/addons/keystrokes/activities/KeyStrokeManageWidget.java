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

package net.labymod.addons.keystrokes.activities;

import java.awt.*;
import java.util.function.Consumer;
import net.labymod.addons.keystrokes.KeyStrokeConfig;
import net.labymod.addons.keystrokes.event.KeyStrokeUpdateEvent;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidgetConfig;
import net.labymod.addons.keystrokes.widgets.KeyStrokeGridWidget;
import net.labymod.addons.keystrokes.widgets.KeyStrokeWidget;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.bounds.Point;
import net.labymod.api.util.bounds.Rectangle;

@AutoWidget
public class KeyStrokeManageWidget extends KeyStrokeGridWidget {

  private static final RectangleRenderer RECTANGLE_RENDERER = Laby.labyAPI().renderPipeline()
      .rectangleRenderer();
  private static final Key CLICK_KEY = MouseButton.LEFT;

  private static final long DRAGGING_DELAY = 100L;

  private final Consumer<KeyStrokeConfig> selectConsumer;

  private KeyStrokeWidget selected;
  private long draggingStartTime;
  private float offsetX;
  private float offsetY;

  protected KeyStrokeManageWidget(
      KeyStrokesHudWidgetConfig config,
      Consumer<KeyStrokeConfig> selectConsumer
  ) {
    super(config);

    this.selectConsumer = selectConsumer;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
    this.updateWidgetBounds(this.bounds());
  }

  @Override
  public void renderWidget(Stack stack, MutableMouse mouse, float partialTicks) {
    if (this.selected == null) {
      super.renderWidget(stack, mouse, partialTicks);
      return;
    }

    if (this.draggingStartTime == -1
        || this.draggingStartTime + DRAGGING_DELAY > System.currentTimeMillis()) {
      super.renderWidget(stack, mouse, partialTicks);
      this.highlightSelected(stack);
      return;
    }

    super.renderDebug(stack);
    for (Widget child : this.children) {
      if (child == this.selected) {
        continue;
      }

      child.render(stack, mouse, partialTicks);
    }

    Bounds parentBounds = this.bounds();
    Bounds bounds = this.selected.bounds();
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

    this.selected.render(stack, mouse, partialTicks);
    this.highlightSelected(stack);
  }

  @Override
  public boolean mouseClicked(MutableMouse mouse, MouseButton button) {
    if (button != CLICK_KEY) {
      return false;
    }

    KeyStrokeWidget clicked = this.findFirstChildIf(AbstractWidget::isHovered);
    if (this.selected == null && clicked == null) {
      return false;
    }

    this.selected = clicked;
    this.selectConsumer.accept(clicked == null ? null : clicked.config());

    if (clicked == null) {
      return false;
    }

    this.offsetX = mouse.getX() - this.selected.bounds().getX();
    this.offsetY = mouse.getY() - this.selected.bounds().getY();
    this.draggingStartTime = System.currentTimeMillis();
    return true;
  }

  @Override
  public boolean mouseReleased(MutableMouse mouse, MouseButton mouseButton) {
    if (this.selected == null || this.draggingStartTime == -1) {
      return false;
    }

    long dragStartTime = this.draggingStartTime;
    this.draggingStartTime = -1;
    if (System.currentTimeMillis() < dragStartTime + DRAGGING_DELAY) {
      return true;
    }

    if (this.updateKeyStrokePosition(this.selected)) {
      this.updateSize();
      Laby.fireEvent(new KeyStrokeUpdateEvent(false));
    }

    return true;
  }

  @Override
  public void onBoundsChanged(Rectangle previousRect, Rectangle newRect) {
    this.updateWidgetBounds(newRect);
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
  public void reload() {
    super.reload();
    this.updateWidgetBounds(this.bounds());
  }

  private void highlightSelected(Stack stack) {
    Bounds parentBounds = this.bounds();
    Bounds bounds = this.selected.bounds();
    RECTANGLE_RENDERER
        .pos(
            parentBounds.getX(),
            bounds.getCenterY() - 0.5F,
            bounds.getX(),
            bounds.getCenterY() + 0.5F
        )
        .color(Color.GRAY.getRGB())
        .render(stack);

    RECTANGLE_RENDERER
        .pos(
            bounds.getCenterX() - 0.5F,
            parentBounds.getY(),
            bounds.getCenterX() + 0.5F,
            bounds.getY()
        )
        .color(Color.GRAY.getRGB())
        .render(stack);

    RECTANGLE_RENDERER
        .pos(
            bounds.getMaxX(),
            bounds.getCenterY() - 0.5F,
            parentBounds.getMaxX(),
            bounds.getCenterY() + 0.5F
        )
        .color(Color.GRAY.getRGB())
        .render(stack);

    RECTANGLE_RENDERER
        .pos(
            bounds.getCenterX() - 0.5F,
            parentBounds.getMaxY(),
            bounds.getCenterX() + 0.5F,
            bounds.getMaxY()
        )
        .color(Color.GRAY.getRGB())
        .render(stack);
  }

  private boolean updateKeyStrokePosition(KeyStrokeWidget widget) {
    Bounds keyStrokeBounds = widget.bounds();
    float x = keyStrokeBounds.getX();
    float y = keyStrokeBounds.getY();

    KeyStrokeConfig config = widget.config();
    Key key = config.key();
    Key anchorKey = this.config.base().get();
    if (key == anchorKey) {
      return this.updateAnchorPosition(config, x, y);
    }

    KeyStrokeConfig anchorConfig = this.config.getKeyStroke(anchorKey);
    Widget anchor = this.findFirstChildIf(child -> child.config() == anchorConfig);
    if (anchor == null) {
      return false;
    }

    Bounds anchorBounds = anchor.bounds();
    float anchorX = anchorBounds.getX();
    float anchorY = anchorBounds.getY();

    float newX = x - anchorX;
    float newY = y - anchorY;
    if (config.getX() == newX && config.getY() == newY) {
      return false;
    }

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

    anchor.updatePosition(
        newX,
        newY
    );

    for (KeyStrokeConfig keyStroke : this.config.getKeyStrokes()) {
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

  public void updateSize() {
    float minX = 0;
    float minY = 0;
    float maxX = 0;
    float maxY = 0;

    KeyStrokeConfig anchorConfig = null;
    Key key = this.config.base().get();
    for (KeyStrokeConfig keyStrokeConfig : this.config.getKeyStrokes()) {
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

  public void select(KeyStrokeConfig keyStrokeConfig) {
    if (keyStrokeConfig == null) {
      this.selected = null;
      return;
    }

    KeyStrokeWidget keyStrokeWidget = this.findFirstChildIf(
        child -> child.config() == keyStrokeConfig
    );

    if (keyStrokeWidget != null) {
      this.selected = keyStrokeWidget;
      this.draggingStartTime = -1;
    }
  }
}
