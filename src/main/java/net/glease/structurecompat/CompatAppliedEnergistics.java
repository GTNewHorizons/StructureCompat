package net.glease.structurecompat;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;

import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.util.InventoryUtility;
import com.gtnewhorizon.structurelib.util.InventoryUtility.InventoryProvider;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.items.contents.WirelessTerminalViewCells;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;

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
        StructureLibAPI.registerFluidSourceProvider("1000-ae2-wireless", player -> {
            // Nothing to offer while the player carries no terminal, so the buckets they carry are asked first.
            if (findWirelessTerminal(player) == null) return null;
            return (resource, simulate) -> drainNetwork(player, resource, simulate);
        });
    }

    /**
     * Take fluid out of the ME network the wireless terminal of the given player is linked to, paying the energy of
     * that terminal for it exactly like taking items out of the network through the terminal does.
     */
    private static FluidStack drainNetwork(EntityPlayerMP player, FluidStack resource, boolean simulate) {
        if (resource == null || resource.getFluid() == null) throw new IllegalArgumentException();

        WirelessTerminalGuiObject terminal = openTerminal(player);
        if (terminal == null) return new FluidStack(resource, 0);

        IMEMonitor<IAEFluidStack> storage = terminal.getFluidInventory();
        if (storage == null) return new FluidStack(resource, 0);

        IAEFluidStack request = AEFluidStack.create(new FluidStack(resource, resource.amount));
        BaseActionSource actionSource = new PlayerSource(player, terminal);
        // A dry run must not touch the energy of the terminal, so only the real extraction is powered.
        IAEFluidStack extracted = simulate ? storage.extractItems(request, Actionable.SIMULATE, actionSource)
                : Platform.poweredExtraction(terminal, storage, request, actionSource);
        if (extracted == null || extracted.getStackSize() <= 0) return new FluidStack(resource, 0);

        return new FluidStack(resource, (int) Math.min(extracted.getStackSize(), resource.amount));
    }

    /**
     * The wireless terminal of the given player, ready to reach its network, or null when the player carries no usable
     * terminal, when it is not linked, or when it is out of range of a wireless access point.
     * <p>
     * The terminal itself is only used to reach the network, it is never opened or moved, so any inventory slot will
     * do.
     */
    @Nullable
    private static WirelessTerminalGuiObject openTerminal(EntityPlayerMP player) {
        ItemStack terminal = findWirelessTerminal(player);
        if (terminal == null) return null;

        IWirelessTermHandler handler = AEApi.instance().registries().wireless().getWirelessTerminalHandler(terminal);
        if (handler == null) return null;

        WirelessTerminalGuiObject gui = new WirelessTerminalGuiObject(
                handler,
                terminal,
                player,
                player.getEntityWorld(),
                -1,
                0,
                0);
        return gui.rangeCheck() ? gui : null;
    }

    /**
     * The first wireless terminal the given player carries, looked for in every inventory registered with StructureLib,
     * which means the main inventory of the player, the baubles they wear and every backpack a mod registered.
     */
    @Nullable
    private static ItemStack findWirelessTerminal(EntityPlayerMP player) {
        for (InventoryProvider<?> provider : InventoryUtility.getInventoryProviders(player)) {
            Iterable<ItemStack> inventory = provider.getInventory(player);
            if (inventory == null) continue;
            for (ItemStack stack : inventory) {
                if (stack != null && AEApi.instance().registries().wireless().isWirelessTerminal(stack)) return stack;
            }
        }
        return null;
    }

    /**
     * Moved out to reduce scope of SuppressWarnings
     */
    @SuppressWarnings("unchecked")
    private static IMEInventoryHandler<IAEItemStack> getCellInventory(ItemStack source) {
        return AEApi.instance().registries().cell().getCellInventory(source, null, StorageChannel.ITEMS);
    }

}
