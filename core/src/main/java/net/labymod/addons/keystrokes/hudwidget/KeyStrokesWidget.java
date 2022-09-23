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

package net.labymod.addons.keystrokes.hudwidget;

import java.awt.*;
import net.labymod.addons.keystrokes.KeyStrokeConfig;
import net.labymod.addons.keystrokes.widgets.KeyStrokeWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.bounds.Rectangle;

public class KeyStrokesWidget extends SimpleWidget {

  private static final float HEIGHT = 20;

  private final KeyStrokesHudWidgetConfig hudWidgetConfig;
  private boolean reload;

  protected float x;
  protected float y;
  protected float maxX;
  protected float maxY;

  public KeyStrokesWidget(KeyStrokesHudWidgetConfig hudWidgetConfig) {
    this.hudWidgetConfig = hudWidgetConfig;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
    this.bounds.setSize(20, 20);
  }

  @Override
  public void render(Stack stack, MutableMouse mouse, float partialTicks) {
    Bounds bounds = this.bounds;
    RectangleRenderer rectangleRenderer = this.labyAPI.renderPipeline().rectangleRenderer();
    rectangleRenderer.renderOutline(stack, bounds, Color.RED.getRGB(), 1);
    rectangleRenderer.pos(bounds.getX(), bounds.getCenterY() - 0.5F, bounds.getMaxX(),
        bounds.getCenterY() + 0.5F).color(Color.RED.getRGB()).render(stack);
    rectangleRenderer.pos(bounds.getCenterX() - 0.5F, bounds.getY(), bounds.getCenterX() + 0.5F,
        bounds.getMaxY()).color(Color.RED.getRGB()).render(stack);

    for (Widget child : this.children) {
      if (child instanceof KeyStrokeWidget) {
        KeyStrokeWidget keyStrokeWidget = (KeyStrokeWidget) child;
        if (keyStrokeWidget.getKey() != this.hudWidgetConfig.base().get()) {
          continue;
        }

        KeyStrokeConfig config = keyStrokeWidget.getKeyStroke();
        rectangleRenderer.renderOutline(stack, child.bounds(), Color.YELLOW.getRGB(), 1);

        rectangleRenderer
            .pos(child.bounds().getX() - config.getX(), child.bounds().getY() - config.getY())
            .size(1)
            .color(Color.YELLOW.getRGB())
            .render(stack);
        break;
      }
    }

    super.render(stack, mouse, partialTicks);
  }

  @Override
  public void tick() {
    super.tick();
    if (this.hudWidgetConfig.getKeyStrokes().size() == this.children.size()) {
      return;
    }

    this.reload = true;
    this.updateWidgetBounds(this.bounds);
  }

  protected void updateWidgetBounds(Rectangle bounds) {
    if (this.reload) {
      this.children.clear();
    }

    KeyStrokeConfig anchor = this.hudWidgetConfig.getKeyStroke(this.hudWidgetConfig.base().get());
    anchor.updateWidth(anchor.key());

    float anchorX = anchor.getX();
    float anchorY = anchor.getY();

    float x = bounds.getX() + anchorX;
    float y = bounds.getY() + anchorY;

    float width = anchorX + anchor.getWidth();
    float height = anchorY + HEIGHT;

    for (KeyStrokeConfig keyStroke : this.hudWidgetConfig.getKeyStrokes()) {
      Key key = keyStroke.key();
      if (keyStroke != anchor) {
        keyStroke.updateWidth(key);

        if (keyStroke.getX() >= 0 && width < anchorX + keyStroke.getX() + keyStroke.getWidth()) {
          width = anchorX + keyStroke.getX() + keyStroke.getWidth();
        }

        if (keyStroke.getY() >= 0 && height < anchorY + keyStroke.getY() + HEIGHT) {
          height = anchorY + keyStroke.getY() + HEIGHT;
        }

        if (this.x > keyStroke.getX()) {
          this.x = keyStroke.getX();
        }

        if (this.y > keyStroke.getY()) {
          this.y = keyStroke.getY();
        }

        if (this.maxX < keyStroke.getX() + keyStroke.getWidth()) {
          this.maxX = keyStroke.getX() + keyStroke.getWidth();
        }

        if (this.maxY < keyStroke.getY() + HEIGHT) {
          this.maxY = keyStroke.getY() + HEIGHT;
        }
      }

      KeyStrokeWidget keyStrokeWidget = null;
      if (this.reload) {
        keyStrokeWidget = new KeyStrokeWidget(key, keyStroke);
      } else {
        for (Widget child : this.children) {
          if (child instanceof KeyStrokeWidget) {
            KeyStrokeWidget widget = (KeyStrokeWidget) child;
            if (widget.getKeyStroke() == keyStroke) {
              keyStrokeWidget = widget;
              break;
            }
          }
        }

        if (keyStrokeWidget == null) {
          break;
        }
      }

      Bounds widgetBounds = keyStrokeWidget.bounds();

      float widgetX;
      float widgetY;
      if (keyStroke == anchor) {
        widgetX = x;
        widgetY = y;
      } else {
        widgetX = x + keyStroke.getX();
        widgetY = y + keyStroke.getY();
      }

      widgetBounds.setX(widgetX);
      widgetBounds.setY(widgetY);
      widgetBounds.setHeight(HEIGHT);
      widgetBounds.setWidth(keyStroke.getWidth());

      if (this.reload) {
        this.addChild(keyStrokeWidget);
      }
    }

    this.bounds.setSize(width, height);
    this.reload = false;
  }

  @Override
  public void onBoundsChanged(Rectangle previousRect, Rectangle bounds) {
    super.onBoundsChanged(previousRect, bounds);
    this.updateWidgetBounds(bounds);
  }

  public float getX() {
    return this.x;
  }

  public float getY() {
    return this.y;
  }

  public float getMaxX() {
    return this.maxX;
  }

  public float getMaxY() {
    return this.maxY;
  }
}
