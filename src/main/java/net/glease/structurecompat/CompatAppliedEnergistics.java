package net.glease.structurecompat;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGridHost;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.items.storage.ItemViewCell;
import appeng.util.prioitylist.IPartitionList;
import com.gtnewhorizon.structurelib.util.InventoryIterable;
import com.gtnewhorizon.structurelib.util.InventoryUtility;
import com.gtnewhorizon.structurelib.util.InventoryUtility.ItemStackCounter;
import com.gtnewhorizon.structurelib.util.InventoryUtility.ItemStackExtractor;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

@Compat("appliedenergistics2")
public class CompatAppliedEnergistics {
    public CompatAppliedEnergistics() {
        InventoryUtility.registerStackExtractor("1000-ae2-wireless", new MEInventoryStackExtractor() {
            @Override
            protected Pair<IEnergySource, IMEInventoryHandler<IAEItemStack>> fromItem(
                    ItemStack source, EntityPlayerMP player) {
                if (!AEApi.instance().registries().wireless().isWirelessTerminal(source)) return null;
                IWirelessTermHandler wh =
                        AEApi.instance().registries().wireless().getWirelessTerminalHandler(source);
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
                WirelessTerminalGuiObject guiObject = new WirelessTerminalGuiObject(
                        wh, source, player, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int)
                                player.posZ);
                if (!guiObject.rangeCheck()) return null;
                IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter(StreamSupport.stream(
                                new InventoryIterable<>(guiObject.getViewCellStorage()).spliterator(), false)
                        .toArray(ItemStack[]::new));
                IMEMonitor<IAEItemStack> rawInventory = guiObject.getItemInventory();
                return Pair.of(guiObject, new ExtractionFilteredMEInventoryHandler(rawInventory, filter));
            }
        });
        InventoryUtility.registerStackExtractor("1000-ae2-portable-cell", new MEInventoryStackExtractor() {
            @Override
            protected Pair<IEnergySource, IMEInventoryHandler<IAEItemStack>> fromItem(
                    ItemStack source, EntityPlayerMP player) {
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

    private abstract static class MEInventoryStackExtractor implements ItemStackExtractor {
        private static int extractFromMEInventory(
                IEnergySource source,
                IAEItemStack toExtract,
                boolean simulate,
                EntityPlayerMP player,
                IMEInventoryHandler<IAEItemStack> inv) {
            PlayerSource actionSource = new PlayerSource(player, null);
            IAEItemStack extracted;
            if (simulate) {
                extracted = inv.extractItems(toExtract, Actionable.SIMULATE, actionSource);
            } else {
                extracted = AEApi.instance().storage().poweredExtraction(source, inv, toExtract, actionSource);
            }
            return Math.toIntExact(extracted == null ? 0 : extracted.getStackSize());
        }

        protected abstract Pair<IEnergySource, IMEInventoryHandler<IAEItemStack>> fromItem(
                ItemStack source, EntityPlayerMP player);

        @Override
        public boolean isAPIImplemented(APIType type) {
            return type == APIType.EXTRACT_ONE_STACK || type == APIType.MAIN;
        }

        @Override
        public int takeFromStack(
                Predicate<ItemStack> predicate,
                boolean simulate,
                int count,
                ItemStackCounter store,
                ItemStack source,
                ItemStack filter,
                EntityPlayerMP player) {
            // sanity check
            if (source == null || source.stackSize <= 0) return 0;
            Pair<IEnergySource, IMEInventoryHandler<IAEItemStack>> pair = fromItem(source, player);
            if (pair == null) return 0;
            IEnergySource energy = pair.getLeft();
            IMEInventoryHandler<IAEItemStack> cellInventory = pair.getRight();
            IItemList<IAEItemStack> items =
                    cellInventory.getAvailableItems(AEApi.instance().storage().createPrimitiveItemList());
            // limit the extraction count to maximum supported by power, so we don't have to constantly
            // check for power limits later on.
            count = Math.min(count, (int) energy.extractAEPower(count, Actionable.SIMULATE, PowerMultiplier.ONE));
            int found = 0;
            for (IAEItemStack stackAE : items) {
                ItemStack stack = stackAE.getItemStack();
                if (!predicate.test(stack)) continue;
                IAEItemStack toExtract = stackAE.copy().setStackSize(Math.min(count, found + stack.stackSize) - found);
                int extracted = extractFromMEInventory(energy, toExtract, simulate, player, cellInventory);
                if (extracted != 0) {
                    store.add(stack, extracted);
                    found += extracted;
                    if (found >= count) return found;
                }
            }
            return found;
        }

        @Override
        public int getItem(ItemStack source, ItemStack toExtract, boolean simulate, EntityPlayerMP player) {
            // sanity check
            if (source == null || source.stackSize <= 0) return 0;
            Pair<IEnergySource, IMEInventoryHandler<IAEItemStack>> pair = fromItem(source, player);
            if (pair == null) return 0;
            return extractFromMEInventory(
                    pair.getLeft(),
                    AEApi.instance().storage().createItemStack(toExtract),
                    simulate,
                    player,
                    pair.getRight());
        }
    }

    private static class ExtractionFilteredMEInventoryHandler implements IMEInventoryHandler<IAEItemStack> {
        private final IMEMonitor<IAEItemStack> rawInventory;
        private final IPartitionList<IAEItemStack> filter;

        public ExtractionFilteredMEInventoryHandler(
                IMEMonitor<IAEItemStack> rawInventory, IPartitionList<IAEItemStack> filter) {
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
        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
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
            });
        }

        @Override
        public StorageChannel getChannel() {
            return rawInventory.getChannel();
        }
    }
}
