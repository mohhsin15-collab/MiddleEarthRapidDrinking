package com.middleearthmc.rapiddrinking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import toughasnails.api.thirst.ThirstHelper;
import toughasnails.api.thirst.IThirst;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(RapidDrinking.MODID)
public final class RapidDrinking {
    public static final String MODID = "middleearthrapidwater";
    private static final String NETWORK_VERSION = "1";
    static final Logger LOGGER = LogUtils.getLogger();

    static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "sips"),
            () -> NETWORK_VERSION,
            NETWORK_VERSION::equals,
            NETWORK_VERSION::equals);

    static final ForgeConfigSpec CONFIG;
    static final ForgeConfigSpec.IntValue COOLDOWN_TICKS;
    static final ForgeConfigSpec.IntValue THIRST_PER_SIP;
    static final ForgeConfigSpec.DoubleValue HYDRATION_PER_SIP;
    static final ForgeConfigSpec.DoubleValue DIRTY_WATER_CHANCE;
    static final ForgeConfigSpec.IntValue DIRTY_WATER_SECONDS;
    static final ForgeConfigSpec.BooleanValue DRINK_SOUND;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("hand_drinking");
        COOLDOWN_TICKS = b.comment("Minimum game ticks between successful sips; 4 = up to 5 sips/s.").defineInRange("cooldown_ticks", 4, 1, 40);
        THIRST_PER_SIP = b.comment("Thirst units restored per click.").defineInRange("thirst_per_sip", 1, 1, 5);
        HYDRATION_PER_SIP = b.comment("Hydration multiplier passed to TAN IThirst.drink.").defineInRange("hydration_per_sip", 0.10D, 0.0D, 1.0D);
        DIRTY_WATER_CHANCE = b.comment("Chance to apply TAN's Thirst effect from untreated natural water. 0 disables.").defineInRange("dirty_water_chance", 0.25D, 0.0D, 1.0D);
        DIRTY_WATER_SECONDS = b.comment("Duration of the dirty-water Thirst effect.").defineInRange("dirty_water_seconds", 20, 1, 120);
        DRINK_SOUND = b.comment("Play a quiet sip sound after a successful click.").define("drink_sound", true);
        b.pop();
        CONFIG = b.build();
    }

    // Enforced on the game server, not the client: prevents packet spam and duplicate drinks.
    private static final Map<UUID, Long> NEXT_SIP_TICK = new HashMap<>();
    private static boolean warnedMissingEffect;

    public RapidDrinking() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG);
        NETWORK.registerMessage(0, SipPacket.class,
                SipPacket::encode, SipPacket::decode, SipPacket::handle);
    }

    static boolean lookingAtWater(net.minecraft.world.entity.player.Player player) {
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 end = start.add(player.getLookAngle().scale(4.5D));
        BlockHitResult hit = player.level().clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, player));
        return hit.getType() == HitResult.Type.BLOCK
                && player.level().getFluidState(hit.getBlockPos()).is(FluidTags.WATER)
                && player.level().getFluidState(hit.getBlockPos()).isSource();
    }

    static void sip(ServerPlayer player) {
        if (player.isSpectator() || !player.isShiftKeyDown()
                || !player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                || !lookingAtWater(player) || !ThirstHelper.isThirstEnabled()
                || !ThirstHelper.canDrink(player, false)) {
            return;
        }

        long tick = player.serverLevel().getServer().getTickCount();
        UUID playerId = player.getUUID();
        if (tick < NEXT_SIP_TICK.getOrDefault(playerId, Long.MIN_VALUE)) return;

        IThirst thirst = ThirstHelper.getThirst(player);
        if (thirst == null || thirst.getThirst() >= 20) return;
        NEXT_SIP_TICK.put(playerId, tick + COOLDOWN_TICKS.get());

        // Uses TAN's thirst/hydration implementation; no substitute player data.
        thirst.drink(THIRST_PER_SIP.get(), HYDRATION_PER_SIP.get().floatValue());

        if (player.getRandom().nextDouble() < DIRTY_WATER_CHANCE.get()) {
            MobEffect dirtyWater = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("toughasnails", "thirst"));
            if (dirtyWater != null) {
                player.addEffect(new MobEffectInstance(dirtyWater, DIRTY_WATER_SECONDS.get() * 20, 0));
            } else if (!warnedMissingEffect) {
                warnedMissingEffect = true;
                LOGGER.warn("TAN's thirst status effect was not found at toughasnails:thirst; dirty-water penalty was skipped");
            }
        }
        if (DRINK_SOUND.get()) {
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK,
                    SoundSource.PLAYERS, 0.12F, 1.1F);
        }
    }
}
