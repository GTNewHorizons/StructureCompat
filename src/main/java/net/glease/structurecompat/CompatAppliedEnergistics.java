package net.glease.structurecompat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.Pair;

import com.gtnewhorizon.structurelib.util.InventoryUtility;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.items.contents.WirelessTerminalViewCells;

@Compat("appliedenergistics2")
public class CompatAppliedEnergistics {

    public CompatAppliedEnergistics() {
        InventoryUtility.registerStackExtractor("1000-ae2-wireless", new WirelessTerminalStackExtractor() {

            @Override
            protected IViewCellStorage getViewCellStorage(ItemStack is) {
                return () -> new WirelessTerminalViewCells(is);
            }
        });
        InventoryUtility.registerStackExtractor("1000-ae2-portable-cell", new MEInventoryStackExtractor() {

            @Override
            protected Pair<IEnergySource, IMEInventoryHandler<IAEItemStack>> fromItem(ItemStack source,
                    EntityPlayerMP player) {
                if (!AEApi.instance().definitions().items().portableCell().isSameAs(source)) return null;
                // copied from appeng.items.contents.PortableCellViewer.extractAEPower
                // seriously why this isn't part of ae itself?
                IEnergySource energySource = (amt, mode, usePowerMultiplier) -> {
                    IAEItemPowerStorage item = (IAEItemPowerStorage) source.getItem();
                    amt = usePowerMultiplier.multiply(amt);
                    if (mode == Actionable.SIMULATE)
                        return usePowerMultiplier.divide(Math.min(amt, item.getAECurrentPower(source)));
                    return usePowerMultiplier.divide(item.extractAEPower(source, amt));
                };
                return Pair.of(energySource, getCellInventory(source));
            }
        });
    }

    /**
     * Moved out to reduce scope of SuppressWarnings
     */
    @SuppressWarnings("unchecked")
    private static IMEInventoryHandler<IAEItemStack> getCellInventory(ItemStack source) {
        return AEApi.instance().registries().cell().getCellInventory(source, null, StorageChannel.ITEMS);
    }

}
