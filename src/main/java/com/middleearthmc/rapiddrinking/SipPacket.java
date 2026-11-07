package com.middleearthmc.rapiddrinking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** The server does not trust client-reported positions, liquids, or cooldowns. */
public final class SipPacket {
    static void encode(SipPacket packet, FriendlyByteBuf buffer) { }
    static SipPacket decode(FriendlyByteBuf buffer) { return new SipPacket(); }
    static void handle(SipPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) RapidDrinking.sip(sender);
        });
        ctx.setPacketHandled(true);
    }
}
