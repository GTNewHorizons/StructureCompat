package net.glease.structurecompat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.fluid.FluidStackExtractor;

import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;

@Compat("okbackpack")
public class CompatOKBackpack {

    public CompatOKBackpack() {
        StructureLibAPI.registerFluidStackExtractor("1000-okbackpack", new BackpackTankExtractor());
        StructureLibAPI.registerFluidSourceProvider("1000-okbackpack", player -> {
            // A backpack worn in an armor slot, or in a baubles slot, is not part of the inventories autoplace walks,
            // so it has to be offered as a source of its own.
            if (!BackpackEntityHelpers.visitWornBackpacks(player, context -> true)) return null;
            return (resource, simulate) -> new FluidStack(resource, drainWorn(player, resource, simulate));
        });
    }

    /**
     * Lets autoplace pay for a fluid block with the fluid held in the tanks of a backpack a player carries.
     */
    private static class BackpackTankExtractor implements FluidStackExtractor {

        @Override
        public boolean isValidSource(ItemStack stack) {
            return BackpackEntityHelpers.isBackpackStack(stack, false);
        }

        @Override
        public int drain(ItemStack container, FluidStack resource, boolean simulate) {
            BackpackWrapper wrapper = BackpackEntityHelpers.getWrapper(container);
            if (wrapper == null) return 0;

            int drained = drainTanks(wrapper, resource, simulate);
            if (drained > 0 && !simulate) {
                // The tanks live inside the NBT of the backpack, so the item has to be written back. markDirty only
                // flags the wrapper, and nothing flushes that flag unless the backpack happens to be open.
                wrapper.markDirty();
                wrapper.writeToItem();
            }
            return drained;
        }
    }

    /**
     * Drain the tanks of the backpacks a player wears, one after another until the request is met. A player can wear
     * more than one, e.g. one in an armor slot and another one in a baubles slot.
     */
    private static int drainWorn(EntityPlayerMP player, FluidStack resource, boolean simulate) {
        if (resource == null || resource.getFluid() == null) throw new IllegalArgumentException();

        int[] taken = new int[1];
        BackpackEntityHelpers.visitWornBackpacks(player, context -> {
            int remaining = resource.amount - taken[0];
            if (remaining <= 0) return true;

            BackpackWrapper wrapper = context.getWrapper();
            if (wrapper == null) return false;

            int drained = drainTanks(wrapper, new FluidStack(resource, remaining), simulate);
            if (drained > 0) {
                taken[0] += drained;
                // Writes the backpack back to its item, and keeps the client in sync while that backpack is open.
                if (!simulate) BackpackEntityHelpers.persistBackpack(context);
            }
            return taken[0] >= resource.amount;
        });
        return taken[0];
    }

    /**
     * Drain every tank upgrade of the given backpack that holds the requested fluid.
     */
    private static int drainTanks(BackpackWrapper wrapper, FluidStack resource, boolean simulate) {
        if (resource == null || resource.getFluid() == null) throw new IllegalArgumentException();

        int remaining = resource.amount;
        for (ITankUpgrade tank : wrapper.gatherCapabilityUpgrades(ITankUpgrade.class).values()) {
            if (remaining <= 0) break;
            FluidStack contents = tank.getContents();
            if (contents == null || contents.amount <= 0 || !contents.isFluidEqual(resource)) continue;
            FluidStack drained = tank.drain(Math.min(contents.amount, remaining), !simulate);
            if (drained == null || drained.amount <= 0) continue;
            remaining -= drained.amount;
        }
        return resource.amount - remaining;
    }
}
