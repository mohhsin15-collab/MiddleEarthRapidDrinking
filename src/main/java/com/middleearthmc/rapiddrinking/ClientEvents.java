package com.middleearthmc.rapiddrinking;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RapidDrinking.MODID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem() || event.getHand() != InteractionHand.MAIN_HAND) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;
        if (!mc.player.isCrouching() || !mc.player.getMainHandItem().isEmpty()) return;
        if (!RapidDrinking.lookingAtWater(mc.player)) return;

        event.setCanceled(true);
        RapidDrinking.NETWORK.sendToServer(new SipPacket());
        // Do not request a vanilla hand swing: the verified sip packet
        // starts our scoop animation, avoiding Punchy!'s punch animation.
        event.setSwingHand(false);
    }
}
