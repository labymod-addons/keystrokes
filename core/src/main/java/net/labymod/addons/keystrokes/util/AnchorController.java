package net.labymod.addons.keystrokes.util;

import java.util.Collection;
import net.labymod.addons.keystrokes.widgets.KeyStrokeWidget;
import net.labymod.api.client.gui.HorizontalAlignment;
import net.labymod.api.client.gui.VerticalAlignment;
import net.labymod.api.client.gui.screen.key.KeyHandler;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;

public class AnchorController {

  private final Anchor anchor;

  public AnchorController() {
    this.anchor = new Anchor();
  }

  public Anchor updateAnchor(
      Collection<KeyStrokeWidget> children,
      KeyStrokeWidget selected,
      float x,
      float y,
      float width,
      float height
  ) {
    if (KeyHandler.isControlDown()) {
      return this.anchor.reset();
    }

    double nearestDistance = Integer.MAX_VALUE;
    KeyStrokeWidget nearestWidget = null;
    for (KeyStrokeWidget child : children) {
      if (child == selected) {
        continue;
      }

      double distance = this.distanceTo(child, x, y, width, height);
      if (distance <= nearestDistance) {
        nearestDistance = distance;
        nearestWidget = child;
      }
    }

    if (nearestWidget == null || nearestDistance > 33) {
      return this.anchor.reset();
    }

    this.anchor.widget = nearestWidget;

    width /= 2;
    height /= 2;

    Bounds bounds = nearestWidget.bounds();
    float nearestX = bounds.getX();
    float nearestY = bounds.getY();
    float nearestWidth = bounds.getWidth() / 2;
    float nearestHeight = bounds.getHeight() / 2;

    if (nearestX + nearestWidth < x + width / 2) {
      this.anchor.horizontalSide = HorizontalAlignment.RIGHT;
    } else if (nearestX - nearestWidth > x - width / 2) {
      this.anchor.horizontalSide = HorizontalAlignment.LEFT;
    } else {
      this.anchor.horizontalSide = null;
    }

    if (nearestY + nearestHeight < y + height / 2) {
      this.anchor.verticalSide = VerticalAlignment.BOTTOM;
    } else if (nearestY - nearestHeight > y - height / 2) {
      this.anchor.verticalSide = VerticalAlignment.TOP;
    } else {
      this.anchor.verticalSide = null;
    }

    return this.anchor;
  }

  private float distanceTo(KeyStrokeWidget widget, float x, float y, float width, float height) {
    Bounds bounds = widget.bounds();
    float widgetX = bounds.getX();
    float widgetY = bounds.getY();
    float widgetWidth = bounds.getWidth() / 2;
    float widgetHeight = bounds.getHeight() / 2;

    float diffX = widgetX - x;
    float diffY = widgetY - y;
    float diffWidth = widgetWidth - width;
    float diffHeight = widgetHeight - height;

    return (float) Math.sqrt(
        diffX * diffX + diffY * diffY + diffWidth * diffWidth + diffHeight * diffHeight
    );
  }

  public Anchor anchor() {
    return this.anchor;
  }

  public static class Anchor {

    private KeyStrokeWidget widget;
    private HorizontalAlignment horizontalSide;
    private VerticalAlignment verticalSide;

    private Anchor() {
    }

    public boolean isValid() {
      return this.widget != null && !(this.horizontalSide == null && this.verticalSide == null);
    }

    public KeyStrokeWidget getWidget() {
      return this.widget;
    }

    public HorizontalAlignment getHorizontalSide() {
      return this.horizontalSide;
    }

    public VerticalAlignment getVerticalSide() {
      return this.verticalSide;
    }

    private Anchor reset() {
      this.widget = null;
      this.horizontalSide = null;
      this.verticalSide = null;
      return this;
    }
  }
}
