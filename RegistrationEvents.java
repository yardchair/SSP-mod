package com.bodyhealthmod.common.events;

import com.bodyhealthmod.common.capability.BodyHealthCapability;
import com.bodyhealthmod.common.capability.IBodyHealth;
import com.bodyhealthmod.common.network.MessageSyncBodyHealth;
import com.bodyhealthmod.common.network.NetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.ResourceLocation;

public class RegistrationEvents {

    private static final ResourceLocation BODY_HEALTH_KEY =
            new ResourceLocation("bodyhealthmod", "body_health");

    // ------------------------------------------------------------------
    // Attach capability to every player
    // ------------------------------------------------------------------

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(BODY_HEALTH_KEY, new com.bodyhealthmod.common.capability.BodyHealthProvider());
        }
    }

    // ------------------------------------------------------------------
    // Observe damage — LOWEST priority so vanilla and other mods run first.
    // We never cancel, never modify, never touch player.setHealth().
    // We only read the damage amount and record it for HUD display.
    // ------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingHurt(LivingHurtEvent event) {
        // Already cancelled by something else — still fine to observe
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        IBodyHealth bh = player.getCapability(BodyHealthCapability.BODY_HEALTH, null);
        if (bh == null) return;

        // Record which part was "hit" and with how much damage — cosmetic only.
        // event.getAmount() is the vanilla damage value, completely unmodified by us.
        bh.recordHit(event.getAmount());

        // Send the hit info to the client so the HUD can display it.
        if (player instanceof EntityPlayerMP) {
            NetworkHandler.CHANNEL.sendTo(
                    new MessageSyncBodyHealth(bh),
                    (EntityPlayerMP) player
            );
        }

        // *** We do NOT call event.setCanceled(true) ***
        // *** We do NOT call player.setHealth()      ***
        // Vanilla handles everything. We are read-only observers.
    }

    // ------------------------------------------------------------------
    // Death — clear hit display counters
    // ------------------------------------------------------------------

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        IBodyHealth bh = player.getCapability(BodyHealthCapability.BODY_HEALTH, null);
        if (bh != null) bh.resetAll();
    }

    // ------------------------------------------------------------------
    // Respawn — nothing to copy since data is transient display-only
    // ------------------------------------------------------------------

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        // Hit display resets naturally; no state needs copying.
    }
}

