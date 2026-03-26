package com.bodyhealthmod.common.network;

import com.bodyhealthmod.common.capability.BodyHealthCapability;
import com.bodyhealthmod.common.capability.IBodyHealth;
import com.bodyhealthmod.common.damage.BodyPart;
import com.bodyhealthmod.common.damage.BodyPartHealth;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Sent server → client whenever a player is hit.
 * Carries which part was hit and the vanilla damage amount — purely for HUD display.
 * Does NOT carry or modify any health values.
 */
public class MessageSyncBodyHealth implements IMessage {

    private int   hitPartOrdinal;
    private float hitDamage;

    public MessageSyncBodyHealth() {}

    public MessageSyncBodyHealth(IBodyHealth bodyHealth) {
        // Find the part that was most recently hit (highest non-zero display ticks)
        BodyPart latestPart = BodyPart.CHEST;
        int maxTicks = 0;
        for (BodyPart bp : BodyPart.values()) {
            BodyPartHealth bph = bodyHealth.getPart(bp);
            if (bph.getHitDisplayTicks() > maxTicks) {
                maxTicks   = bph.getHitDisplayTicks();
                latestPart = bp;
            }
        }
        this.hitPartOrdinal = latestPart.ordinal();
        this.hitDamage      = bodyHealth.getPart(latestPart).getLastHitDamage();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        hitPartOrdinal = buf.readInt();
        hitDamage      = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(hitPartOrdinal);
        buf.writeFloat(hitDamage);
    }

    public static class Handler implements IMessageHandler<MessageSyncBodyHealth, IMessage> {
        @Override
        public IMessage onMessage(MessageSyncBodyHealth msg, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EntityPlayer player = Minecraft.getMinecraft().player;
                if (player == null) return;

                IBodyHealth bh = player.getCapability(BodyHealthCapability.BODY_HEALTH, null);
                if (bh == null) return;

                BodyPart part = BodyPart.values()[msg.hitPartOrdinal];
                // Record the hit on the client for HUD display only
                bh.getPart(part).recordHit(msg.hitDamage);
            });
            return null;
        }
    }
}
