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
import net.labymod.addons.keystrokes.util.AnchorController;
import net.labymod.addons.keystrokes.util.AnchorController.Anchor;
import net.labymod.addons.keystrokes.widgets.KeyStrokeGridWidget;
import net.labymod.addons.keystrokes.widgets.KeyStrokeWidget;
import net.labymod.api.Laby;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.gui.HorizontalAlignment;
import net.labymod.api.client.gui.VerticalAlignment;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.KeyHandler;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.state.ScreenCanvas;
import net.labymod.api.client.gui.screen.state.TextFlags;
import net.labymod.api.client.gui.screen.state.states.GuiRectangleRenderState.RectConfig;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.render.font.FontSize.PredefinedFontSize;
import net.labymod.api.util.I18n;
import net.labymod.api.util.bounds.Point;
import net.labymod.api.util.bounds.Rectangle;

@AutoWidget
public class KeyStrokeManageWidget extends KeyStrokeGridWidget {

  private static final AnchorController CONTROLLER = new AnchorController();

  private static final Key CLICK_KEY = MouseButton.LEFT;

  private static final long DRAGGING_DELAY = 100L;

  private final Consumer<KeyStrokeConfig> selectConsumer;

  private final String disableDocking;
  private final String disabledDocking;

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
    this.disableDocking = I18n.translate("keystrokes.activity.edit.docking.disable");
    this.disabledDocking = I18n.translate("keystrokes.activity.edit.docking.disabled");
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
    this.updateWidgetBounds(this.bounds());
  }

  @Override
  public void renderWidget(ScreenContext context) {
    if (this.selected == null) {
      super.renderWidget(context);
      return;
    }

    if (this.draggingStartTime == -1
        || this.draggingStartTime + DRAGGING_DELAY > System.currentTimeMillis()) {
      super.renderWidget(context);
      this.highlightSelected(context);
      return;
    }

    super.renderDebug(context);
    for (Widget child : this.children) {
      if (child == this.selected) {
        continue;
      }

      child.render(context);
    }

    MutableMouse mouse = context.mouse();

    Bounds parentBounds = this.bounds();
    Bounds bounds = this.selected.bounds();
    float width = bounds.getWidth();
    float x = mouse.getX() - this.offsetX;
    if (x < parentBounds.getX()) {
      x = parentBounds.getX();
    } else {
      if (x + width > parentBounds.getMaxX()) {
        x = parentBounds.getMaxX() - width;
      }
    }

    float height = bounds.getHeight();
    float y = mouse.getY() - this.offsetY;
    if (y < parentBounds.getY()) {
      y = parentBounds.getY();
    } else {
      if (y + height > parentBounds.getMaxY()) {
        y = parentBounds.getMaxY() - height;
      }
    }

    Anchor anchor = CONTROLLER.updateAnchor(this.children, this.selected, x, y, width, height);
    this.highlightAnchor(context, anchor);

    bounds.setOuterPosition(x, y, REASON);
    this.selected.render(context);
    this.highlightSelected(context);

    String infoText = KeyHandler.isControlDown() ? this.disabledDocking : this.disableDocking;

    context.canvas().submitText(
        infoText,
        this.bounds().getCenterX(), this.bounds().getMaxY() - 10,
        NamedTextColor.GRAY.getValue(),
        PredefinedFontSize.SMALL,
        TextFlags.CENTERED | TextFlags.SHADOW
    );
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

    Anchor anchor = CONTROLLER.anchor();
    if (anchor.isValid()) {
      Bounds bounds = this.selected.bounds();
      Bounds anchorBounds = anchor.getWidget().bounds();

      float x = 0;
      float y = 0;
      HorizontalAlignment horizontalSide = anchor.getHorizontalSide();
      if (horizontalSide == HorizontalAlignment.LEFT) {
        x -= bounds.getWidth() + 2;
      } else if (horizontalSide == HorizontalAlignment.RIGHT) {
        x += anchorBounds.getWidth() + 2;
      }

      VerticalAlignment verticalSide = anchor.getVerticalSide();
      if (verticalSide == VerticalAlignment.TOP) {
        y -= bounds.getHeight() + 2;
      } else if (verticalSide == VerticalAlignment.BOTTOM) {
        y += anchorBounds.getHeight() + 2;
      }

      bounds.setOuterPosition(anchorBounds.getX() + x, anchorBounds.getY() + y, REASON);
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

  private void highlightAnchor(ScreenContext context, Anchor anchor) {
    if (!anchor.isValid()) {
      return;
    }

    ScreenCanvas canvas = context.canvas();

    Bounds bounds = anchor.getWidget().bounds();
    HorizontalAlignment horizontalSide = anchor.getHorizontalSide();
    if (horizontalSide == HorizontalAlignment.LEFT) {
      canvas.submitGuiRect(
          Rectangle.absolute(
              bounds.getX() - 1, bounds.getY(),
              bounds.getX(), bounds.getMaxY()
          ),
          RectConfig.builder()
              .setArgb(Color.WHITE.getRGB())
              .build()
      );
    } else if (horizontalSide == HorizontalAlignment.RIGHT) {
      canvas.submitGuiRect(
          Rectangle.absolute(
              bounds.getMaxX(), bounds.getY(),
              bounds.getMaxX() + 1, bounds.getMaxY()
          ),
          RectConfig.builder()
              .setArgb(Color.WHITE.getRGB())
              .build()
      );
    }

    VerticalAlignment verticalSide = anchor.getVerticalSide();
    if (verticalSide == VerticalAlignment.TOP) {
      canvas.submitGuiRect(
          Rectangle.absolute(
              bounds.getX(), bounds.getY() - 1,
              bounds.getMaxX(), bounds.getY()
          ),
          RectConfig.builder()
              .setArgb(Color.WHITE.getRGB())
              .build()
      );
    } else if (verticalSide == VerticalAlignment.BOTTOM) {
      canvas.submitGuiRect(
          Rectangle.absolute(
              bounds.getX(), bounds.getMaxY(),
              bounds.getMaxX(), bounds.getMaxY() + 1
          ),
          RectConfig.builder()
              .setArgb(Color.WHITE.getRGB())
              .build()
      );
    }
  }

  private void highlightSelected(ScreenContext context) {
    Bounds parentBounds = this.bounds();
    Bounds bounds = this.selected.bounds();

    ScreenCanvas canvas = context.canvas();

    canvas.submitGuiRect(
        parentBounds.getX(),
        bounds.getCenterY() - 0.5F,
        bounds.getX() - parentBounds.getX(),
        1,
        RectConfig.builder()
            .setArgb(Color.GRAY.getRGB())
            .build()
    );

    canvas.submitGuiRect(
        bounds.getCenterX() - 0.5F,
        parentBounds.getY(),
        1,
        bounds.getY() - parentBounds.getY(),
        RectConfig.builder()
            .setArgb(Color.GRAY.getRGB())
            .build()
    );

    canvas.submitGuiRect(
        bounds.getMaxX(),
        bounds.getCenterY() - 0.5F,
        parentBounds.getMaxX() - bounds.getMaxX(),
        1,
        RectConfig.builder()
            .setArgb(Color.GRAY.getRGB())
            .build()
    );

    canvas.submitGuiRect(
        bounds.getCenterX() - 0.5F,
        bounds.getMaxY(),
        1,
        parentBounds.getMaxY() - bounds.getMaxY(),
        RectConfig.builder()
            .setArgb(Color.GRAY.getRGB())
            .build()
    );
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
