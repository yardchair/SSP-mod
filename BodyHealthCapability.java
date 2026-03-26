package com.bodyhealthmod.common.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class BodyHealthCapability {

    @CapabilityInject(IBodyHealth.class)
    public static Capability<IBodyHealth> BODY_HEALTH = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(
                IBodyHealth.class,
                new NoopStorage(),
                BodyHealthImpl::new
        );
    }

    /**
     * No-op storage — hit display data is transient and does not need to be saved.
     * Vanilla health is unchanged and saved by Minecraft as normal.
     */
    public static class NoopStorage implements Capability.IStorage<IBodyHealth> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<IBodyHealth> cap, IBodyHealth instance, EnumFacing side) {
            return null;
        }

        @Override
        public void readNBT(Capability<IBodyHealth> cap, IBodyHealth instance, EnumFacing side, NBTBase nbt) {
            // Nothing to restore
        }
    }
}
