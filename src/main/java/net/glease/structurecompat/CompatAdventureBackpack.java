package net.glease.structurecompat;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import com.darkona.adventurebackpack.inventory.InventoryBackpack;
import com.darkona.adventurebackpack.item.ItemAdventureBackpack;
import com.darkona.adventurebackpack.playerProperties.BackpackProperty;
import com.darkona.adventurebackpack.util.Wearing;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.fluid.FluidStackExtractor;
import com.gtnewhorizon.structurelib.util.InventoryUtility;

@Compat("adventurebackpack")
public class CompatAdventureBackpack {

    public CompatAdventureBackpack() {
        InventoryUtility.registerInventoryProvider(
                "6000-adventure-backpack",
                player -> !Wearing.isWearingBackpack(player) ? null
                        : new InventoryBackpack(Wearing.getWearingBackpack(player)));
        InventoryUtility.registerStackExtractor(
                "1000-adventure-backpack",
                source -> source != null && source.getItem() instanceof ItemAdventureBackpack
                        ? new InventoryBackpack(source)
                        : null);
        StructureLibAPI.registerFluidStackExtractor("1000-adventure-backpack", new BackpackTankExtractor());
        StructureLibAPI.registerFluidSourceProvider("1000-adventure-backpack", player -> {
            // A worn backpack is kept in the backpack data of the player, not in their inventory, so autoplace never
            // walks into it and it has to be offered as a source of its own.
            if (Wearing.getWearingBackpack(player) == null) return null;
            return (resource, simulate) -> new FluidStack(resource, drain(player, resource, simulate));
        });
    }

    /**
     * Drain fluid out of the tanks of a backpack a player wears. The client is told about it, as it draws the content
     * of those tanks on the backpack overlay.
     */
    private static int drain(EntityPlayer player, FluidStack resource, boolean simulate) {
        if (resource == null || resource.getFluid() == null) throw new IllegalArgumentException();

        ItemStack backpack = Wearing.getWearingBackpack(player);
        if (backpack == null) return 0;

        int drained = drainTanks(backpack, resource, simulate);
        if (drained > 0 && !simulate) BackpackProperty.sync(player);
        return drained;
    }

    /**
     * Lets autoplace pay for a fluid block with the fluid held in the tanks of a backpack a player carries.
     */
    private static class BackpackTankExtractor implements FluidStackExtractor {

        @Override
        public boolean isValidSource(ItemStack stack) {
            return stack != null && stack.getItem() instanceof ItemAdventureBackpack;
        }

        @Override
        public int drain(ItemStack container, FluidStack resource, boolean simulate) {
            return drainTanks(container, resource, simulate);
        }
    }

    /**
     * Drain the two tanks of the given backpack. Both of them are drained in place, as they live inside the NBT of the
     * backpack, which is written back only when the drain actually happens.
     */
    private static int drainTanks(@Nullable ItemStack backpack, FluidStack resource, boolean simulate) {
        if (resource == null || resource.getFluid() == null) throw new IllegalArgumentException();
        if (backpack == null || !(backpack.getItem() instanceof ItemAdventureBackpack)) return 0;

        InventoryBackpack inventory = new InventoryBackpack(backpack);
        int remaining = resource.amount;
        for (FluidTank tank : inventory.getTanksArray()) {
            if (remaining <= 0) break;
            FluidStack contents = tank.getFluid();
            if (contents == null || contents.amount <= 0 || !contents.isFluidEqual(resource)) continue;
            FluidStack drained = tank.drain(Math.min(contents.amount, remaining), !simulate);
            if (drained == null || drained.amount <= 0) continue;
            remaining -= drained.amount;
        }

        int drained = resource.amount - remaining;
        if (drained > 0 && !simulate) inventory.dirtyTanks();
        return drained;
    }
}
