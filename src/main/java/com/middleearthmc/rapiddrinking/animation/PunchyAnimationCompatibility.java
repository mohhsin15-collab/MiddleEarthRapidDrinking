package com.middleearthmc.rapiddrinking.animation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.BooleanSupplier;

/**
 * Optional compatibility bridge: no direct Punchy! dependency is required.
 * Punchy! temporarily yields its first-person arms only during a sip.
 */
@Mod.EventBusSubscriber(modid = "middleearthrapidwater", value = Dist.CLIENT,
                        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PunchyAnimationCompatibility {
    private PunchyAnimationCompatibility() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (!ModList.get().isLoaded("punchy")) return;
            try {
                Class<?> api = Class.forName("punchy.api.PunchyApi");
                api.getMethod("disable", String.class, BooleanSupplier.class).invoke(
                        null, "middleearthrapidwater", (BooleanSupplier) ClientDrinkAnimation::active);
            } catch (ReflectiveOperationException | LinkageError error) {
                // Don't crash the game if Punchy! API changes or is absent.
                System.err.println("[Middle Earth Rapid Drinking] Punchy compatibility unavailable: " + error);
            }
        });
    }
}
