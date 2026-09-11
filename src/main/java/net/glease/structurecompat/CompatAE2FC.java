package net.glease.structurecompat;

import static com.glodblock.github.util.Util.hasInfinityBoosterCard;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.fluid.FluidStackExtractor;
import com.gtnewhorizon.structurelib.util.InventoryUtility;

import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.items.contents.WirelessTerminalViewCells;

@Compat("ae2fc")
public class CompatAE2FC {

    public CompatAE2FC() {

        InventoryUtility.registerStackExtractor("0999-ae2fc-need-before-ae2", new WirelessTerminalStackExtractor() {

            @Override
            public boolean isValidSource(ItemStack source, EntityPlayerMP player) {
                return source.getItem() instanceof ItemBaseWirelessTerminal;
            }

            @Override
            protected Pair<IEnergySource, IMEInventoryHandler<IAEItemStack>> fromItem(ItemStack source,
                    EntityPlayerMP player) {
                if (!(source.getItem() instanceof ItemBaseWirelessTerminal)) return null;
                return super.fromItem(source, player);
            }

            @Override
            protected boolean rangeCheck(ItemStack source, EntityPlayerMP player, IGrid targetGrid) {
                if (hasInfinityBoosterCard(source)) return true;
                return super.rangeCheck(source, player, targetGrid);
            }

            @Override
            protected IViewCellStorage getViewCellStorage(ItemStack is) {
                return () -> new WirelessTerminalViewCells(is);
            }
        });

        StructureLibAPI.registerFluidStackExtractor("1000-ae2fc-fluid-drop", new FluidDropExtractor());
    }

    /**
     * Lets autoplace pay for a fluid block with the fluid drops a player carries. A drop stack is fluid in item form,
     * one drop being one milli bucket, so StructureLib cannot tell it apart from a plain item.
     */
    private static class FluidDropExtractor implements FluidStackExtractor {

        @Override
        public boolean isValidSource(ItemStack stack) {
            return ItemFluidDrop.isFluidStack(stack);
        }

        @Override
        public int drain(ItemStack container, FluidStack resource, boolean simulate) {
            FluidStack contained = ItemFluidDrop.getFluidStack(container);
            if (contained == null || !contained.isFluidEqual(resource)) return 0;

            int amount = Math.min(contained.amount, resource.amount);
            if (amount <= 0) return 0;
            if (!simulate) {
                // One drop is one milli bucket, so the size of the stack is the amount of fluid it holds.
                container.stackSize -= amount;
            }
            return amount;
        }

        @Override
        public boolean convertsContainer(ItemStack container, FluidStack resource) {
            // Only a stack that is paid off completely is spent, a partly drained one stays in its slot.
            return container.stackSize <= resource.amount;
        }

        @Nullable
        @Override
        public ItemStack getReplacement(ItemStack container) {
            return null;
        }
    }
}
