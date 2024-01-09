package net.glease.structurecompat;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import com.gtnewhorizon.structurelib.StructureLib;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.structure.AutoPlaceEnvironment;
import com.gtnewhorizon.structurelib.structure.IItemSource;
import com.gtnewhorizon.structurelib.structure.IStructureElement;

public class StructureUtilityExt {

    public static <T> IStructureElement<T> notBlock(Block block, int meta) {
        return new IStructureElement<T>() {

            @Override
            public boolean check(T t, World world, int x, int y, int z) {
                return world.getBlock(x, y, z) != block || world.getBlockMetadata(x, y, z) != meta;
            }

            @Override
            public boolean spawnHint(T t, World world, int x, int y, int z, ItemStack trigger) {
                if (!check(t, world, x, y, z)) {
                    StructureLibAPI.hintParticle(
                            world,
                            x,
                            y,
                            z,
                            StructureLibAPI.getBlockHint(),
                            StructureLibAPI.HINT_BLOCK_META_ERROR);
                    StructureLibAPI.markHintParticleError(StructureLib.getCurrentPlayer(), world, x, y, z);
                    return true;
                }
                return false;
            }

            @Override
            public boolean placeBlock(T t, World world, int x, int y, int z, ItemStack trigger) {
                return false;
            }

            @Deprecated
            @Override
            public PlaceResult survivalPlaceBlock(T t, World world, int x, int y, int z, ItemStack trigger,
                    IItemSource s, EntityPlayerMP actor, Consumer<IChatComponent> chatter) {
                if (check(t, world, x, y, z)) return PlaceResult.SKIP;
                chatter.accept(
                        new ChatComponentTranslation(
                                "structureelement.error.cannot_be_block",
                                x,
                                y,
                                z,
                                new ItemStack(block, meta).func_151000_E()));
                return PlaceResult.REJECT;
            }

            @Override
            public PlaceResult survivalPlaceBlock(T t, World world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                if (check(t, world, x, y, z)) return PlaceResult.SKIP;
                env.getChatter().accept(
                        new ChatComponentTranslation(
                                "structureelement.error.cannot_be_block",
                                x,
                                y,
                                z,
                                new ItemStack(block, meta).func_151000_E()));
                return PlaceResult.REJECT;
            }

            @Nullable
            @Override
            public BlocksToPlace getBlocksToPlace(T t, World world, int x, int y, int z, ItemStack trigger,
                    AutoPlaceEnvironment env) {
                return null;
            }
        };
    }

}
