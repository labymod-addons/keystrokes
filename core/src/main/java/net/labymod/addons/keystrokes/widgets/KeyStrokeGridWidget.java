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
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.state.ScreenCanvas;
import net.labymod.api.client.gui.screen.state.states.GuiRectangleRenderState.RectConfig;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.util.bounds.ModifyReason;
import net.labymod.api.util.bounds.Point;
import net.labymod.api.util.bounds.Rectangle;

public class KeyStrokeGridWidget extends AbstractWidget<KeyStrokeWidget> {

  public static final float DEFAULT_HEIGHT = 20;
  public static final float DEFAULT_WIDTH = 20;

  protected static final ModifyReason REASON = ModifyReason.of("keyStrokeAdjustment");

  protected final KeyStrokesHudWidgetConfig config;

  protected boolean reload;
  private float minX;
  private float minY;
  private float maxX;
  private float maxY;

  protected KeyStrokeGridWidget(KeyStrokesHudWidgetConfig config) {
    this.config = config;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
    this.reload = true;
  }

  protected void updateWidgetBounds(Rectangle bounds) {
    this.updateWidgetBounds(Point.fixed((int) bounds.getX(), (int) bounds.getY()), true, false);
  }

  protected void updateWidgetBounds(Point point, boolean adjustBounds, boolean centered) {
    if (this.reload) {
      this.children.clear();
    }

    this.minX = 0;
    this.minY = 0;
    this.maxX = 0;
    this.maxY = 0;

    KeyStrokeConfig anchor = this.config.anchorConfig();
    anchor.updateWidth(anchor.key());

    float anchorX = anchor.getX();
    float anchorY = anchor.getY();

    float width = anchorX + anchor.getWidth();
    float height = anchorY + DEFAULT_HEIGHT;

    Set<KeyStrokeConfig> keyStrokes = this.config.getKeyStrokes();
    for (KeyStrokeConfig keyStroke : keyStrokes) {
      Key key = keyStroke.key();

      if (keyStroke != anchor) {
        keyStroke.updateWidth(key);

        float keyHeight = keyStroke.getHeight(this.config);
        if (adjustBounds) {
          if (keyStroke.getX() >= 0 && width < anchorX + keyStroke.getX() + keyStroke.getWidth()) {
            width = anchorX + keyStroke.getX() + keyStroke.getWidth();
          }

          if (keyStroke.getY() >= 0 && height < anchorY + keyStroke.getY() + keyHeight) {
            height = anchorY + keyStroke.getY() + keyHeight;
          }
        }

        if (this.minX > keyStroke.getX()) {
          this.minX = keyStroke.getX();
        }

        if (this.minY > keyStroke.getY()) {
          this.minY = keyStroke.getY();
        }

        if (this.maxX < keyStroke.getX() + keyStroke.getWidth()) {
          this.maxX = keyStroke.getX() + keyStroke.getWidth();
        }

        if (this.maxY < keyStroke.getY() + keyHeight) {
          this.maxY = keyStroke.getY() + keyHeight;
        }
      }
    }

    float x = point.getX() + anchorX;
    float y = point.getY() + anchorY;
    if (centered) {
      x -= (this.maxX - this.minX) / 2;
      y -= (this.maxY - this.minX) / 2;
    }

    for (KeyStrokeConfig keyStroke : keyStrokes) {
      KeyStrokeWidget keyStrokeWidget = null;
      if (this.reload) {
        keyStrokeWidget = new KeyStrokeWidget(keyStroke.key(), keyStroke, this.config);
      } else {
        keyStrokeWidget = this.findFirstChildIf(child -> child.config() == keyStroke);
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

      widgetBounds.setPosition((int) widgetX, (int) widgetY, REASON);
      widgetBounds.setSize(
          (int) keyStroke.getWidth(),
          (int) keyStroke.getHeight(this.config),
          REASON
      );

      if (this.reload) {
        if (this.initialized) {
          this.addChildInitialized(keyStrokeWidget);
        } else {
          this.addChild(keyStrokeWidget);
        }
      }
    }

    if (adjustBounds) {
      this.bounds().setSize((int) width, (int) height, REASON);
    }

    this.reload = false;
  }

  protected void reload() {
    this.reload = true;
  }

  protected void renderDebug(ScreenContext context) {
    if (!this.renderDebug()) {
      return;
    }

    Bounds bounds = this.bounds();

    ScreenCanvas canvas = context.canvas();
    canvas.submitOutlineRect(
        bounds,
        1,
        0,
        Color.RED.getRGB()
    );

    canvas.submitGuiRect(
        bounds.getX(),
        bounds.getCenterY() - 0.5F,
        bounds.getMaxX(),
        bounds.getCenterY() + 0.5F,
        RectConfig.builder()
            .setArgb(Color.RED.getRGB())
            .build()
    );

    canvas.submitGuiRect(
        bounds.getCenterX() - 0.5F,
        bounds.getY(),
        bounds.getCenterX() + 0.5F,
        bounds.getMaxY(),
        RectConfig.builder()
            .setArgb(Color.RED.getRGB())
            .build()
    );

    Key key = this.config.base().get();
    KeyStrokeWidget anchorWidget = this.findFirstChildIf(child -> child.key() == key);
    if (anchorWidget == null) {
      return;
    }

    KeyStrokeConfig config = anchorWidget.config();
    canvas.submitOutlineRect(
        anchorWidget.bounds(),
        1,
        0,
        Color.YELLOW.getRGB()
    );

    canvas.submitGuiRect(
        anchorWidget.bounds().getX() - config.getX(),
        anchorWidget.bounds().getY() - config.getY(),
        1, 1,
        RectConfig.builder()
            .setArgb(Color.YELLOW.getRGB())
            .build()
    );
  }

  protected boolean renderDebug() {
    return false;
  }

  public float getMinX() {
    return this.minX;
  }

  public float getMinY() {
    return this.minY;
  }

  public float getMaxX() {
    return this.maxX;
  }

  public float getMaxY() {
    return this.maxY;
  }
}
