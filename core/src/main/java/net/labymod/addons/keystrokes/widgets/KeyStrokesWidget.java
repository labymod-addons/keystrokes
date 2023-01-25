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
import java.util.Set;
import net.labymod.addons.keystrokes.KeyStrokeConfig;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidgetConfig;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.bounds.ModifyReason;
import net.labymod.api.util.bounds.Point;
import net.labymod.api.util.bounds.Rectangle;

public class KeyStrokesWidget extends AbstractWidget<KeyStrokeWidget> {

  private static final ModifyReason REASON = ModifyReason.of("keyStrokeAdjustment");

  public static final float HEIGHT = 20;

  protected final KeyStrokesHudWidgetConfig hudWidgetConfig;
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
    if (!this.isEditing()) {
      Bounds bounds = this.parent.bounds();
      this.bounds().setPosition(bounds.getX(), bounds.getY(), REASON);
      this.bounds().setSize(20, 20, REASON);
      this.reload = true;
      this.updateWidgetBounds(this.bounds());
    }
  }

  @Override
  public void render(Stack stack, MutableMouse mouse, float partialTicks) {
    this.renderDebug(stack);
    super.render(stack, mouse, partialTicks);
  }

  protected void renderDebug(Stack stack) {
    Bounds bounds = this.bounds();
    RectangleRenderer rectangleRenderer = this.labyAPI.renderPipeline().rectangleRenderer();
    rectangleRenderer.renderOutline(stack, bounds, Color.RED.getRGB(), 1);
    rectangleRenderer.pos(bounds.getX(), bounds.getCenterY() - 0.5F, bounds.getMaxX(),
        bounds.getCenterY() + 0.5F).color(Color.RED.getRGB()).render(stack);
    rectangleRenderer.pos(bounds.getCenterX() - 0.5F, bounds.getY(), bounds.getCenterX() + 0.5F,
        bounds.getMaxY()).color(Color.RED.getRGB()).render(stack);

    Key key = this.hudWidgetConfig.base().get();
    KeyStrokeWidget anchorWidget = this.findFirstChildIf(child -> child.key() == key);
    if (anchorWidget == null) {
      return;
    }

    KeyStrokeConfig config = anchorWidget.config();
    rectangleRenderer.renderOutline(stack, anchorWidget.bounds(), Color.YELLOW.getRGB(), 1);

    rectangleRenderer
        .pos(anchorWidget.bounds().getX() - config.getX(),
            anchorWidget.bounds().getY() - config.getY()
        )
        .size(1)
        .color(Color.YELLOW.getRGB())
        .render(stack);
  }

  public void checkForNewKeyStrokes() {
    if (this.hudWidgetConfig.getKeyStrokes().size() == this.children.size()) {
      return;
    }

    this.reload = true;
    this.updateWidgetBounds(this.bounds());
  }

  public void updateKeyStrokeBounds() {
    this.updateWidgetBounds(this.bounds());
  }

  protected void updateWidgetBounds(Rectangle bounds) {
    this.updateWidgetBounds(Point.fixed((int) bounds.getX(), (int) bounds.getY()), true, false);
  }

  protected void updateWidgetBounds(Point point, boolean adjustBounds, boolean centered) {
    if (this.reload) {
      this.children.clear();
    }

    this.x = 0;
    this.y = 0;
    this.maxX = 0;
    this.maxY = 0;

    KeyStrokeConfig anchor = this.hudWidgetConfig.getKeyStroke(this.hudWidgetConfig.base().get());
    anchor.updateWidth(anchor.key());

    float anchorX = anchor.getX();
    float anchorY = anchor.getY();

    float width = anchorX + anchor.getWidth();
    float height = anchorY + HEIGHT;

    Set<KeyStrokeConfig> keyStrokes = this.hudWidgetConfig.getKeyStrokes();
    for (KeyStrokeConfig keyStroke : keyStrokes) {
      Key key = keyStroke.key();
      if (keyStroke != anchor) {
        keyStroke.updateWidth(key);

        if (adjustBounds) {
          if (keyStroke.getX() >= 0 && width < anchorX + keyStroke.getX() + keyStroke.getWidth()) {
            width = anchorX + keyStroke.getX() + keyStroke.getWidth();
          }

          if (keyStroke.getY() >= 0 && height < anchorY + keyStroke.getY() + HEIGHT) {
            height = anchorY + keyStroke.getY() + HEIGHT;
          }
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
    }

    float x = point.getX() + anchorX;
    float y = point.getY() + anchorY;
    if (centered) {
      x -= (this.maxX - this.x) / 2;
      y -= (this.maxY - this.y) / 2;
    }

    for (KeyStrokeConfig keyStroke : keyStrokes) {
      KeyStrokeWidget keyStrokeWidget = null;
      if (this.reload) {
        keyStrokeWidget = new KeyStrokeWidget(keyStroke.key(), keyStroke, this.hudWidgetConfig);
      } else {
        keyStrokeWidget = this.findFirstChildIf(child -> child.config() == keyStroke);
        if (keyStrokeWidget == null) {
          System.out.println("could not find widget for key " + keyStroke.key());
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

      widgetBounds.setPosition(widgetX, widgetY, REASON);
      widgetBounds.setSize(keyStroke.getWidth(), HEIGHT, REASON);

      if (this.reload) {
        if (this.initialized) {
          this.addChildInitialized(keyStrokeWidget);
        } else {
          this.addChild(keyStrokeWidget);
        }
      }
    }

    if (adjustBounds) {
      this.bounds().setSize(width, height, REASON);
    }

    this.reload = false;
  }

  @Override
  public void onBoundsChanged(Rectangle previousRect, Rectangle bounds) {
    super.onBoundsChanged(previousRect, bounds);

    try {
      this.updateWidgetBounds(bounds);
    } catch (StackOverflowError ignored) {
      ignored.printStackTrace();
    }
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

  protected boolean isEditing() {
    return false;
  }
}
