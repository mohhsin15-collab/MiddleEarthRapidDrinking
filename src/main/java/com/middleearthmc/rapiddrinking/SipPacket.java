package com.middleearthmc.rapiddrinking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Original empty client-to-server sip request, unchanged. */
public final class SipPacket {
    public static void encode(SipPacket packet, FriendlyByteBuf buf) { }
    public static SipPacket decode(FriendlyByteBuf buf) { return new SipPacket(); }

    public static void handle(SipPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) RapidDrinking.sip(sender);
        });
        context.setPacketHandled(true);
    }
}
