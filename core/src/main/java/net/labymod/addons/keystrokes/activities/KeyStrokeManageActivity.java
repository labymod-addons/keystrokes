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

import java.util.Set;
import java.util.function.Consumer;
import net.labymod.addons.keystrokes.KeyStrokeConfig;
import net.labymod.addons.keystrokes.event.KeyStrokeUpdateEvent;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidgetConfig;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.LabyScreen;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.key.InputType;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.event.Subscribe;
import org.jetbrains.annotations.Nullable;

@Link("edit.lss")
@AutoActivity
public class KeyStrokeManageActivity extends Activity {

  private static final MutableMouse DUMMY_MOUSE = new MutableMouse(-5, -5);
  private final KeyStrokesHudWidgetConfig hudWidgetConfig;
  private final KeyStrokeManageWidget manageWidget;

  private Consumer<KeyStrokeConfig> addCallback;

  private FlexibleContentWidget content;
  private DivWidget overlayWidget;
  private KeyStrokeConfig selected;

  public KeyStrokeManageActivity(KeyStrokesHudWidgetConfig hudWidgetConfig) {
    this.hudWidgetConfig = hudWidgetConfig;

    this.manageWidget = new KeyStrokeManageWidget(this.hudWidgetConfig,
        selected -> this.selected = selected
    );

    this.manageWidget.addId("manage");
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    this.content = new FlexibleContentWidget();
    this.content.addId("content");

    DivWidget manageContainer = new DivWidget();
    manageContainer.addId("manage-container");

    this.overlayWidget = this.overlayWidget();
    this.overlayWidget.addId("overlay");
    this.overlayWidget.setVisible(false);
    manageContainer.addChild(this.overlayWidget);

    manageContainer.addChild(this.manageWidget);
    this.content.addFlexibleContent(manageContainer);

    HorizontalListWidget manageButtonContainer = new HorizontalListWidget();
    manageButtonContainer.addId("manage-button-container");

    ButtonWidget addButton = ButtonWidget.i18n("keystrokes.activity.edit.add.text");
    addButton.addId("add-button");
    addButton.setPressable(() -> {
      this.overlayWidget.setVisible(true);
      this.addCallback = config -> {
        this.hudWidgetConfig.addKeyStroke(config);
        this.manageWidget.select(config);
        Laby.fireEvent(new KeyStrokeUpdateEvent(true));
      };
    });

    manageButtonContainer.addEntry(addButton);

    ButtonWidget removeButton = ButtonWidget.i18n("keystrokes.activity.edit.remove.text");
    removeButton.addId("remove-button");
    removeButton.setPressable(() -> {
      if (this.selected == null) {
        return;
      }

      Set<KeyStrokeConfig> keyStrokes = this.hudWidgetConfig.getKeyStrokes();
      if (keyStrokes.size() == 1) {
        this.overlayWidget.setVisible(true);
        this.addCallback = config -> {
          if (config == this.selected) {
            this.manageWidget.select(this.selected);
            return;
          }

          this.hudWidgetConfig.base().set(config.key());
          config.updatePosition(
              this.selected.getX(),
              this.selected.getY()
          );

          this.hudWidgetConfig.removeKeyStroke(this.selected);
          this.hudWidgetConfig.addKeyStroke(config);
          Laby.fireEvent(new KeyStrokeUpdateEvent(true));
          this.manageWidget.select(config);
        };
        return;
      }

      if (this.selected.key() == this.hudWidgetConfig.base().get()) {
        KeyStrokeConfig config = null;
        for (KeyStrokeConfig keyStroke : this.hudWidgetConfig.getKeyStrokes()) {
          if (keyStroke != this.selected) {
            config = keyStroke;
            break;
          }
        }

        if (config == null) {
          return;
        }

        this.hudWidgetConfig.removeKeyStroke(this.selected);

        float anchorX = config.getX();
        float anchorY = config.getY();
        for (KeyStrokeConfig keyStroke : this.hudWidgetConfig.getKeyStrokes()) {
          if (keyStroke == config) {
            continue;
          }

          float x = keyStroke.getX();
          float y = keyStroke.getY();
          if (x == anchorX) {
            x = 0;
          } else {
            x = x - anchorX;
          }

          if (y == anchorY) {
            y = 0;
          } else {
            y = y - anchorY;
          }

          keyStroke.updatePosition(
              x,
              y
          );
        }

        config.updatePosition(
            anchorX + this.selected.getX(),
            anchorY + this.selected.getY()
        );

        this.hudWidgetConfig.base().set(config.key());
      }

      this.hudWidgetConfig.removeKeyStroke(this.selected);
      this.manageWidget.updateSize();
      Laby.fireEvent(new KeyStrokeUpdateEvent(true));
      this.manageWidget.select(null);
    });
    manageButtonContainer.addEntry(removeButton);

    this.content.addContent(manageButtonContainer);
    this.document.addChild(this.content);
  }

  public DivWidget overlayWidget() {
    DivWidget overlay = new DivWidget();

    DivWidget overlayContent = new DivWidget();
    overlayContent.addId("overlay-content");

    ComponentWidget titleAdd = ComponentWidget.i18n("keystrokes.activity.edit.add.title");
    titleAdd.addId("title-add");
    overlayContent.addChild(titleAdd);

    ComponentWidget titleExit = ComponentWidget.i18n("keystrokes.activity.edit.add.exit");
    titleExit.addId("title-exit");
    overlayContent.addChild(titleExit);

    overlay.addChild(overlayContent);
    return overlay;
  }

  @Override
  public void render(Stack stack, MutableMouse mouse, float partialTicks) {
    if (this.overlayWidget == null || !this.overlayWidget.isVisible()) {
      super.render(stack, mouse, partialTicks);
      return;
    }

    if (this.content != null) {
      this.content.render(stack, DUMMY_MOUSE, partialTicks);
    }

    this.overlayWidget.render(stack, mouse, partialTicks);
  }

  @Override
  public void onCloseScreen() {
    super.onCloseScreen();
    Laby.fireEvent(new KeyStrokeUpdateEvent(false));
    Laby.references().hudWidgetRegistry().saveConfig();
  }

  @Override
  public boolean mouseClicked(MutableMouse mouse, MouseButton mouseButton) {
    if (this.overlayWidget == null || !this.overlayWidget.isVisible() || this.addCallback == null) {
      return super.mouseClicked(mouse, mouseButton);
    }

    float x = 0;
    float y = 0;
    if (this.manageWidget.getMaxX() < this.manageWidget.getMaxY()) {
      x = this.manageWidget.getMaxX() + 2;
    } else {
      y = this.manageWidget.getMaxY() + 2;
    }

    KeyStrokeConfig keyStrokeConfig = new KeyStrokeConfig(mouseButton, x, y);
    this.addCallback.accept(keyStrokeConfig);

    this.overlayWidget.setVisible(false);
    return true;
  }

  @Override
  public boolean keyPressed(Key key, InputType type) {
    if (this.overlayWidget == null || !this.overlayWidget.isVisible() || this.addCallback == null) {
      return super.keyPressed(key, type);
    }

    if (key != Key.ESCAPE) {
      float x = 0;
      float y = 0;
      if (this.manageWidget.getMaxX() < this.manageWidget.getMaxY()) {
        x = this.manageWidget.getMaxX() + 2;
      } else {
        y = this.manageWidget.getMaxY() + 2;
      }

      KeyStrokeConfig keyStrokeConfig = new KeyStrokeConfig(key, x, y);
      this.addCallback.accept(keyStrokeConfig);
    }

    this.overlayWidget.setVisible(false);
    return true;
  }

  @Override
  public boolean shouldHandleEscape() {
    return this.overlayWidget != null && this.overlayWidget.isVisible();
  }

  @Override
  public <T extends LabyScreen> @Nullable T renew() {
    return new KeyStrokeManageActivity(this.hudWidgetConfig).generic();
  }

  @Subscribe
  public void onKeyStrokeUpdate(KeyStrokeUpdateEvent event) {
    if (event.isRelevantForEditing() && this.manageWidget != null) {
      this.manageWidget.reload();
    }
  }
}
