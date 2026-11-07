package com.middleearthmc.rapiddrinking;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Called for an actual use-key click, not continuously while the key is held. */
@Mod.EventBusSubscriber(modid = RapidDrinking.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {
    private ClientEvents() { }

    @SubscribeEvent
    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem() || event.getHand() != InteractionHand.MAIN_HAND) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;
        if (!mc.player.isShiftKeyDown() || !mc.player.getMainHandItem().isEmpty()) return;
        if (!RapidDrinking.lookingAtWater(mc.player)) return;
        // Ownership is transferred to this addon; disable TAN's own hand drinking.
        event.setCanceled(true);
        RapidDrinking.NETWORK.sendToServer(new SipPacket());
        event.setSwingHand(true); // Forge performs one swing despite cancellation.
    }
}
