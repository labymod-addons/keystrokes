package net.labymod.addons.keystrokes;

import com.google.inject.Singleton;
import java.util.Set;
import net.labymod.addons.keystrokes.hudwidget.KeyStrokesHudWidget;
import net.labymod.addons.keystrokes.listener.HudWidgetRegisterListener;
import net.labymod.addons.keystrokes.listener.HudWidgetUnregisterListener;
import net.labymod.addons.keystrokes.listener.KeyListener;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonListener;
import org.spongepowered.include.com.google.common.collect.Sets;

@Singleton
@AddonListener
public class KeyStrokes extends LabyAddon<KeyStrokesConfiguration> {

  private final Set<KeyStrokesHudWidget> hudWidgets = Sets.newHashSet();

  @Override
  protected void enable() {
    this.registerSettingCategory();

    this.registerListener(HudWidgetRegisterListener.class);
    this.registerListener(HudWidgetUnregisterListener.class);
    this.registerListener(KeyListener.class);

    this.labyAPI().hudWidgetService().registerWidgetType(KeyStrokesHudWidget.class);
  }

  @Override
  protected Class<? extends KeyStrokesConfiguration> configurationClass() {
    return KeyStrokesConfiguration.class;
  }

  public Set<KeyStrokesHudWidget> getHudWidgets() {
    return this.hudWidgets;
  }

  public void registerHudWidget(KeyStrokesHudWidget widget) {
    this.hudWidgets.add(widget);
  }

  public void unregisterHudWidget(KeyStrokesHudWidget widget) {
    this.hudWidgets.remove(widget);
  }
}
