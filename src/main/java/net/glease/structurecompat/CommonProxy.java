package net.glease.structurecompat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {}

    public void markTextureUsed(IIcon o) {}

    public boolean checkServerUtilitiesPermission(World world, EntityPlayer actor, int x, int z) {
        return true;
    }
}
