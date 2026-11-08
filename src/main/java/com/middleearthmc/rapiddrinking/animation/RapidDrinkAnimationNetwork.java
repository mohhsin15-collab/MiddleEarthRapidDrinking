package com.middleearthmc.rapiddrinking.animation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Visual-only server-to-client notification. The existing server-side
 * hydration and cooldown rules are NOT changed by this packet.
 */
public final class RapidDrinkAnimationNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("middleearthrapidwater", "hand_drink_animation"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered;

    private RapidDrinkAnimationNetwork() {}

    /** Call once from the existing @Mod class constructor. */
    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(
                0, PlayDrinkAnimation.class,
                (packet, buffer) -> {},
                buffer -> new PlayDrinkAnimation(),
                RapidDrinkAnimationNetwork::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /**
     * Call only after a water sip was successfully accepted and applied to TAN.
     * Never call for a failed attempt, full thirst, or a rejected water source.
     */
    public static void sendSuccessfulSip(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PlayDrinkAnimation());
    }

    private static void handle(PlayDrinkAnimation packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> ClientDrinkAnimation::start));
        context.setPacketHandled(true);
    }

    private static final class PlayDrinkAnimation {}
}
