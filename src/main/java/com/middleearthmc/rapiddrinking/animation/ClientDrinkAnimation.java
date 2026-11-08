package com.middleearthmc.rapiddrinking.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * First-person empty-hand scoop -> mouth -> return animation.
 * Only the rendering pose changes; the server owns the actual drink action.
 * Uses the normal Minecraft player skin and sleeves.
 */
@Mod.EventBusSubscriber(modid = "middleearthrapidwater", value = Dist.CLIENT,
                        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientDrinkAnimation {
    private static final int LENGTH_TICKS = 12;
    private static int ticksLeft = 0;

    private ClientDrinkAnimation() {}

    /** Triggered on the client after a successful server-side sip. */
    public static void start() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !mc.player.getMainHandItem().isEmpty()) return;

        // Continuous rapid drinking: don't restart every 4 ticks and keep
        // the hand permanently stuck at the start. Restart near the end.
        if (ticksLeft <= 2) ticksLeft = LENGTH_TICKS;
    }

    /** Queried by Punchy! to yield control only while we draw the scoop. */
    public static boolean active() {
        Minecraft mc = Minecraft.getInstance();
        return ticksLeft > 0 && mc.player != null && mc.level != null
                && mc.player.getMainHandItem().isEmpty();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !mc.player.getMainHandItem().isEmpty()) {
            ticksLeft = 0;
        } else if (ticksLeft > 0) {
            --ticksLeft;
        }
    }

    @SubscribeEvent
    public static void renderArm(RenderArmEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!active() || event.getPlayer() != mc.player) return;

        // Only animate the main empty hand; no duplicated or hidden offhand.
        HumanoidArm arm = mc.player.getMainArm();
        if (event.getArm() != arm) return;
        if (!(mc.getEntityRenderDispatcher().getRenderer(event.getPlayer())
                instanceof PlayerRenderer renderer)) return;

        float partial = mc.getFrameTime();
        float progress = (LENGTH_TICKS - ticksLeft + partial) / LENGTH_TICKS;
        progress = Math.max(0f, Math.min(1f, progress));

        // A 3-stage motion: reach down for water, raise to lips, lower arm.
        // The sin curve guarantees no abrupt jump at either end.
        float scoop = smoothstep(0.0f, 0.30f, progress);
        float sip = smoothstep(0.30f, 0.62f, progress);
        float lower = smoothstep(0.68f, 1.00f, progress);
        float downAmount = scoop * (1f - sip);
        float upAmount = sip * (1f - lower);
        float offset = arm == HumanoidArm.RIGHT ? 1f : -1f;

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        try {
            // Reach towards water (below the screen), then lift the hand
            // toward the face. Mirror horizontally for left-handed players.
            pose.translate(-offset * (0.05f * downAmount + 0.14f * upAmount),
                           -0.14f * downAmount + 0.23f * upAmount,
                           -0.10f * downAmount - 0.33f * upAmount);
            pose.mulPose(Axis.XP.rotationDegrees(30f * downAmount - 60f * upAmount));
            pose.mulPose(Axis.YP.rotationDegrees(-offset * 12f * upAmount));
            pose.mulPose(Axis.ZP.rotationDegrees(-offset * 10f * upAmount));

            if (arm == HumanoidArm.RIGHT) {
                renderer.renderRightHand(pose, event.getMultiBufferSource(),
                                         event.getPackedLight(), event.getPlayer());
            } else {
                renderer.renderLeftHand(pose, event.getMultiBufferSource(),
                                        event.getPackedLight(), event.getPlayer());
            }
        } finally {
            pose.popPose();
        }
        // Prevent vanilla from drawing the unanimated arm on top.
        event.setCanceled(true);
    }

    private static float smoothstep(float start, float end, float x) {
        float t = Math.max(0f, Math.min(1f, (x - start) / (end - start)));
        return t * t * (3f - 2f * t);
    }
}
