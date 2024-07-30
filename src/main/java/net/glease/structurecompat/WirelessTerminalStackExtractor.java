package net.glease.structurecompat;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import com.gtnewhorizon.structurelib.util.InventoryIterable;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.items.storage.ItemViewCell;
import appeng.tile.networking.TileWireless;
import appeng.util.prioitylist.IPartitionList;

abstract class WirelessTerminalStackExtractor extends MEInventoryStackExtractor {

    @Override
    public boolean isValidSource(ItemStack source, EntityPlayerMP player) {
        return AEApi.instance().registries().wireless().isWirelessTerminal(source);
    }

    @Override
    protected Pair<IEnergySource, IMEInventoryHandler<IAEItemStack>> fromItem(ItemStack source, EntityPlayerMP player) {
        if (!AEApi.instance().registries().wireless().isWirelessTerminal(source)) return null;
        IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(source);
        if (wh == null) return null;
        String key = wh.getEncryptionKey(source);
        long serial;
        try {
            serial = Long.parseLong(key);
        } catch (NumberFormatException e) {
            return null;
        }
        ILocatable locatable = AEApi.instance().registries().locatable().getLocatableBy(serial);
        if (!(locatable instanceof IGridHost)) return null;
        IGridNode gn = ((IGridHost) locatable).getGridNode(ForgeDirection.UNKNOWN);
        if (gn == null) return null;
        IGrid grid = gn.getGrid();
        if (grid == null) return null;
        IStorageGrid sg = grid.getCache(IStorageGrid.class);
        if (sg == null) return null;
        IMEMonitor<IAEItemStack> rawInventory = sg.getItemInventory();
        if (rawInventory == null) return null;
        if (!rangeCheck(source, player, grid)) return null;
        IEnergySource power = new WirelessTerminalEnergySource(wh, source, player);
        IViewCellStorage viewCellStorage = getViewCellStorage(source);
        IPartitionList<IAEItemStack> filter;
        if (viewCellStorage == null) {
            filter = null;
        } else {
            filter = ItemViewCell.createFilter(
                    StreamSupport
                            .stream(new InventoryIterable<>(viewCellStorage.getViewCellStorage()).spliterator(), false)
                            .toArray(ItemStack[]::new));
        }

        if (filter == null) return Pair.of(power, rawInventory);
        return Pair.of(power, new ExtractionFilteredMEInventoryHandler(rawInventory, filter));
    }

    protected boolean rangeCheck(ItemStack source, EntityPlayerMP player, IGrid targetGrid) {
        final IMachineSet tw = targetGrid.getMachines(TileWireless.class);

        for (final IGridNode n : tw) {
            final IWirelessAccessPoint wap = (IWirelessAccessPoint) n.getMachine();
            DimensionalCoord loc = wap.getLocation();
            if (!loc.isInWorld(player.getEntityWorld())) continue;
            double range = wap.getRange();
            double distX = player.posX - loc.x, distY = player.posY - loc.y, distZ = player.posZ - loc.z;
            double dist = distX * distX + distY * distY + distZ * distZ;
            if (dist < range * range) return true;
        }
        return false;
    }

    protected abstract IViewCellStorage getViewCellStorage(ItemStack is);

    private static class WirelessTerminalEnergySource implements IEnergySource {

        private final IWirelessTermHandler wth;
        private final ItemStack is;
        private final EntityPlayerMP player;

        public WirelessTerminalEnergySource(IWirelessTermHandler wth, ItemStack is, EntityPlayerMP player) {
            this.wth = wth;
            this.is = is;
            this.player = player;
        }

        @Override
        public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
            if (this.wth != null && this.is != null) {
                if (mode == Actionable.SIMULATE) {
                    return this.wth.hasPower(this.player, amt, this.is) ? amt : 0;
                }
                return this.wth.usePower(this.player, amt, this.is) ? amt : 0;
            }
            return 0.0;
        }
    }

    private static class ExtractionFilteredMEInventoryHandler implements IMEInventoryHandler<IAEItemStack> {

        private final IMEMonitor<IAEItemStack> rawInventory;
        private final IPartitionList<IAEItemStack> filter;

        public ExtractionFilteredMEInventoryHandler(IMEMonitor<IAEItemStack> rawInventory,
                IPartitionList<IAEItemStack> filter) {
            this.rawInventory = rawInventory;
            this.filter = filter;
        }

        @Override
        public AccessRestriction getAccess() {
            return rawInventory.getAccess();
        }

        @Override
        public boolean isPrioritized(IAEItemStack input) {
            return rawInventory.isPrioritized(input);
        }

        @Override
        public boolean canAccept(IAEItemStack input) {
            return rawInventory.canAccept(input);
        }

        @Override
        public int getPriority() {
            return rawInventory.getPriority();
        }

        @Override
        public int getSlot() {
            return rawInventory.getSlot();
        }

        @Override
        public boolean validForPass(int i) {
            return rawInventory.validForPass(i);
        }

        @Override
        public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src) {
            // inject is not filtered
            return rawInventory.injectItems(input, type, src);
        }

        @Override
        public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src) {
            if (!filter.isListed(request)) return null;
            return rawInventory.extractItems(request, mode, src);
        }

        @Override
        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out, int iteration) {
            return rawInventory.getAvailableItems(new IItemList<IAEItemStack>() {

                public void addStorage(IAEItemStack option) {
                    if (filter.isListed(option)) out.addStorage(option);
                }

                public void addCrafting(IAEItemStack option) {
                    if (filter.isListed(option)) out.addCrafting(option);
                }

                public void addRequestable(IAEItemStack option) {
                    if (filter.isListed(option)) out.addRequestable(option);
                }

                public IAEItemStack getFirstItem() {
                    return out.getFirstItem();
                }

                public int size() {
                    return out.size();
                }

                public Iterator<IAEItemStack> iterator() {
                    return out.iterator();
                }

                public void resetStatus() {
                    out.resetStatus();
                }

                public void add(IAEItemStack option) {
                    out.add(option);
                }

                public IAEItemStack findPrecise(IAEItemStack i) {
                    return out.findPrecise(i);
                }

                public Collection<IAEItemStack> findFuzzy(IAEItemStack input, FuzzyMode fuzzy) {
                    return out.findFuzzy(input, fuzzy);
                }

                public boolean isEmpty() {
                    return out.isEmpty();
                }

                public void forEach(Consumer<? super IAEItemStack> action) {
                    out.forEach(action);
                }

                public Spliterator<IAEItemStack> spliterator() {
                    return out.spliterator();
                }
            }, iteration);
        }

        @Override
        public StorageChannel getChannel() {
            return rawInventory.getChannel();
        }
    }
}
