package net.glease.structurecompat;

import static com.glodblock.github.util.Util.hasInfinityBoosterCard;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.Pair;

import com.glodblock.github.common.item.ItemBaseWirelessTerminal;
import com.gtnewhorizon.structurelib.util.InventoryUtility;

import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.items.contents.WirelessTerminalViewCells;

@Compat("ae2wct")
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
    }
}
