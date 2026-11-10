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

import net.labymod.addons.keystrokes.mousetracker.DemoPath;
import net.labymod.addons.keystrokes.mousetracker.MouseMotion;
import net.labymod.addons.keystrokes.mousetracker.MouseTrackerLayout;
import net.labymod.addons.keystrokes.mousetracker.NanoClock;
import net.labymod.addons.keystrokes.mousetracker.PadShape;
import net.labymod.addons.keystrokes.mousetracker.TrailMesh;
import net.labymod.addons.keystrokes.mousetracker.TrailSampler;
import net.labymod.addons.keystrokes.mousetracker.TurnInput;
import net.labymod.addons.keystrokes.util.KeyTracker;
import net.labymod.api.client.gfx.pipeline.renderer.text.TextRenderingOptions;
import net.labymod.api.client.gui.hud.hudwidget.SimpleHudWidget;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.state.RoundedData;
import net.labymod.api.client.gui.screen.state.ScreenCanvas;
import net.labymod.api.client.gui.screen.state.states.GuiCustomGeometryRenderState.GeometryRenderer;
import net.labymod.api.client.gui.screen.state.states.GuiRectangleRenderState.RectConfig;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.entity.player.ClientPlayerTurnEvent;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.laby3d.pipeline.RenderStates;
import net.labymod.api.laby3d.pipeline.material.GuiMaterial;
import net.labymod.api.laby3d.pipeline.material.Material;
import net.labymod.api.util.logging.Logging;
import net.labymod.laby3d.api.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class MouseTrackerHudWidget extends SimpleHudWidget<MouseTrackerHudWidgetConfig> {

  private static final Logging LOGGER = Logging.create(MouseTrackerHudWidget.class);

  private static final int TEXT_OPTIONS = TextRenderingOptions.SHADOW
      | TextRenderingOptions.CENTERED
      | TextRenderingOptions.USE_FLOATING_POINT_VALUES;

  // Width of the transparent fringe along the trail, in scaled GUI pixels
  private static final float TRAIL_FEATHER = 0.5F;

  private static final long NANOS_PER_MILLI = 1_000_000L;

  private final NanoClock clock = NanoClock.SYSTEM;
  private final MouseMotion motion = new MouseMotion();
  private final TrailSampler demoSampler = new TrailSampler();
  private final TrailMesh trailMesh = new TrailMesh();
  private final GeometryRenderer trailRenderer = this::renderTrail;

  private final TileConfigCache padConfig = new TileConfigCache();
  private final TileConfigCache cpsBoxConfig = new TileConfigCache();
  private RectConfig guideRectConfig;
  private int guideRectColor;

  private Material trailMaterial;
  private KeyTracker leftClicks;
  private KeyTracker rightClicks;
  private boolean nonFiniteTurnLogged;

  public MouseTrackerHudWidget() {
    super("mouseTracker", MouseTrackerHudWidgetConfig.class);
  }

  @Override
  public void load(MouseTrackerHudWidgetConfig config) {
    super.load(config);

    // Every HUD profile has its own config, so start over with a centered dot
    this.motion.reset();
    this.demoSampler.clear();
    this.leftClicks = null;
    this.rightClicks = null;
  }

  @Override
  public boolean isVisibleInGame() {
    return true;
  }

  // Runs first and only reads the event, so the dot also shows turns that are cancelled later
  @Subscribe(Priority.FIRST)
  public void onPlayerTurn(ClientPlayerTurnEvent event) {
    if (this.config == null || !this.isEnabled()) {
      return;
    }

    boolean accepted = TurnInput.accept(event, this.motion, this.clock.nanoTime());
    if (!accepted && !this.nonFiniteTurnLogged) {
      this.nonFiniteTurnLogged = true;
      LOGGER.debug("Ignored a camera turn with a non-finite delta ({}, {})",
          event.getX(), event.getY());
    }
  }

  @Subscribe
  public void onMouseButton(MouseButtonEvent event) {
    if (event.action() != Action.CLICK || this.config == null || !this.isEnabled()) {
      return;
    }

    KeyTracker tracker;
    if (event.button() == MouseButton.LEFT) {
      tracker = this.leftClicks;
    } else if (event.button() == MouseButton.RIGHT) {
      tracker = this.rightClicks;
    } else {
      return;
    }

    if (tracker != null && this.labyAPI.minecraft().isMouseLocked()) {
      tracker.press();
    }
  }

  @Override
  public void render(
      RenderPhase phase,
      ScreenContext context,
      boolean isEditorContext,
      HudSize size
  ) {
    MouseTrackerHudWidgetConfig config = this.config;
    int padSize = MouseTrackerLayout.size(config.size().get());
    boolean showCps = config.showCps().get();
    int cpsGap = MouseTrackerLayout.cpsGap(config.cpsGap().get());
    if (!phase.canRender()) {
      size.set((float) padSize, MouseTrackerLayout.widgetHeight(padSize, showCps, cpsGap));
      return;
    }

    long now = this.clock.nanoTime();
    PadShape shape = config.padShape().get();

    TrailSampler sampler;
    double dotX;
    double dotY;
    if (isEditorContext) {
      // The editor plays a demo and leaves the live dot alone
      this.demoSampler.catchUp(DemoPath.INSTANCE, now);
      sampler = this.demoSampler;
      dotX = DemoPath.INSTANCE.xAt(now);
      dotY = DemoPath.INSTANCE.yAt(now);
    } else {
      this.motion.update(now, config.sensitivity().get(), config.returnTime().get(), shape);
      sampler = this.motion.sampler();
      dotX = this.motion.model().x();
      dotY = this.motion.model().y();
    }

    ScreenCanvas canvas = context.canvas();
    float scale = config.scale().get();
    if (!(scale > 0.0F)) {
      scale = 1.0F;
    }

    float dotSize = MouseTrackerLayout.dotSize(config.dotSize().get());
    boolean outline = config.outline().get();
    boolean roundedCorners = config.roundedCorners().get();
    int roundedRadius = config.roundedRadius().get();
    int backgroundColor = config.backgroundColor().get().get();
    int outlineColor = config.outlineColor().get().get();
    float half = padSize / 2.0F;

    // Pad
    if (shape == PadShape.CIRCLE) {
      float radius = MouseTrackerLayout.circleRadius(half, scale);
      canvas.submitCircle(half, half, radius, backgroundColor);
      if (outline) {
        canvas.submitCircle(
            half, half,
            MouseTrackerLayout.circleRadius(half - 1.0F, scale), radius,
            outlineColor
        );
      }
    } else {
      float cornerRadius = roundedCorners
          ? MouseTrackerLayout.cornerRadius(padSize, roundedRadius, scale)
          : 0.0F;
      canvas.submitGuiRect(
          0.0F, 0.0F, padSize, padSize,
          this.padConfig.get(backgroundColor, cornerRadius, outline, outlineColor)
      );
    }

    // Center lines, the vertical one split so the lines do not overlap in the middle
    if (config.showCenterCross().get()) {
      RectConfig guideConfig = this.guideConfig(config.guideColor().get().get());
      float inner = half - 1.5F;
      canvas.submitGuiRect(1.0F, half - 0.5F, padSize - 2.0F, 1.0F, guideConfig);
      canvas.submitGuiRect(half - 0.5F, 1.0F, 1.0F, inner, guideConfig);
      canvas.submitGuiRect(half - 0.5F, half + 0.5F, 1.0F, inner, guideConfig);
    }

    // Trail and dot
    float travel = MouseTrackerLayout.travel(padSize, dotSize, outline);
    if (config.showTrail().get()) {
      long duration = MouseTrackerLayout.trailMillis(config.trailDuration().get())
          * NANOS_PER_MILLI;
      int vertices = this.trailMesh.build(
          sampler,
          now,
          duration,
          dotX, dotY,
          half, half,
          travel,
          MouseTrackerLayout.trailWidth(dotSize),
          TRAIL_FEATHER / scale,
          config.trailColor().get().get()
      );
      if (vertices > 0) {
        canvas.submitCustomGeometry(
            this.trailMaterial(),
            0.0F, 0.0F, padSize, padSize,
            this.trailRenderer
        );
      }
    }

    canvas.submitCircle(
        half + (float) dotX * travel,
        half + (float) dotY * travel,
        MouseTrackerLayout.circleRadius(dotSize / 2.0F, scale),
        config.dotColor().get().get()
    );

    // Click boxes
    this.updateClickTrackers(showCps);
    if (!showCps) {
      return;
    }

    float boxY = MouseTrackerLayout.cpsBoxY(padSize, cpsGap);
    float boxWidth = MouseTrackerLayout.cpsBoxWidth(padSize);
    float boxRadius = roundedCorners
        ? MouseTrackerLayout.cornerRadius(MouseTrackerLayout.CPS_BOX_HEIGHT, roundedRadius, scale)
        : 0.0F;
    RectConfig boxConfig = this.cpsBoxConfig.get(
        backgroundColor, boxRadius, outline, outlineColor
    );
    int textColor = config.textColor().get().get();
    int leftCount = isEditorContext ? 0 : this.leftClicks.getCount();
    int rightCount = isEditorContext ? 0 : this.rightClicks.getCount();

    this.submitClickBox(canvas, 0.0F, boxY, boxWidth, boxConfig, leftCount, textColor);
    this.submitClickBox(
        canvas,
        MouseTrackerLayout.rightCpsBoxX(padSize), boxY, boxWidth,
        boxConfig,
        rightCount,
        textColor
    );
  }

  private void submitClickBox(
      ScreenCanvas canvas,
      float x,
      float y,
      float width,
      RectConfig boxConfig,
      int count,
      int textColor
  ) {
    float height = MouseTrackerLayout.CPS_BOX_HEIGHT;
    canvas.submitGuiRect(x, y, width, height, boxConfig);

    // Centered like the text on the key tiles
    float textY = y + height / 2.0F - canvas.getLineHeight() / 2.0F + 0.8F;
    canvas.submitText(
        MouseTrackerLayout.cpsLabel(count),
        x + width / 2.0F + 0.4F, textY,
        textColor,
        1.0F,
        TEXT_OPTIONS
    );
  }

  private void updateClickTrackers(boolean showCps) {
    if (!showCps) {
      this.leftClicks = null;
      this.rightClicks = null;
      return;
    }

    if (this.leftClicks == null) {
      this.leftClicks = new KeyTracker();
      this.rightClicks = new KeyTracker();
    }

    this.leftClicks.update();
    this.rightClicks.update();
  }

  private RectConfig guideConfig(int argb) {
    if (this.guideRectConfig == null || this.guideRectColor != argb) {
      this.guideRectConfig = RectConfig.builder().setArgb(argb).build();
      this.guideRectColor = argb;
    }

    return this.guideRectConfig;
  }

  private Material trailMaterial() {
    if (this.trailMaterial == null) {
      this.trailMaterial = GuiMaterial.untextured(RenderStates.GUI);
    }

    return this.trailMaterial;
  }

  private void renderTrail(Matrix4f pose, VertexConsumer consumer) {
    TrailMesh mesh = this.trailMesh;
    for (int index = 0, count = mesh.vertexCount(); index < count; index++) {
      consumer.addVertex2D(pose, mesh.vertexX(index), mesh.vertexY(index))
          .setBlankUv()
          .setColor(mesh.vertexColor(index));
    }
  }

  /**
   * Keeps the rectangle config of a tile until one of its inputs changes, so a frame does not
   * build new configs. The tiles use the same recipe as the key tiles.
   */
  private static final class TileConfigCache {

    private RectConfig config;
    private int argb;
    private float radius;
    private boolean outline;
    private int outlineColor;

    private RectConfig get(int argb, float radius, boolean outline, int outlineColor) {
      if (this.config != null
          && this.argb == argb
          && Float.compare(this.radius, radius) == 0
          && this.outline == outline
          && this.outlineColor == outlineColor) {
        return this.config;
      }

      this.config = RectConfig.builder()
          .setArgb(argb)
          .setRoundedData(RoundedData.builder()
              .setRadius(radius)
              .setUpperEdgeSoftness(0.2F)
              .setLowerEdgeSoftness(-0.5F)
              .setBorderColor(outlineColor)
              .setBorderThickness(outline ? 1 : 0)
              .build())
          .build();
      this.argb = argb;
      this.radius = radius;
      this.outline = outline;
      this.outlineColor = outlineColor;
      return this.config;
    }
  }
}
