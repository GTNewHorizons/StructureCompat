package net.glease.structurecompat;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.Pair;

import com.gtnewhorizon.structurelib.util.InventoryUtility;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.IterationCounter;

abstract class MEInventoryStackExtractor implements InventoryUtility.ItemStackExtractor {

    private static int extractFromMEInventory(IEnergySource source, IAEItemStack toExtract, boolean simulate,
            EntityPlayerMP player, IMEInventoryHandler<IAEItemStack> inv) {
        PlayerSource actionSource = new PlayerSource(player, null);
        IAEItemStack extracted;
        if (simulate) {
            extracted = inv.extractItems(toExtract, Actionable.SIMULATE, actionSource);
        } else {
            extracted = AEApi.instance().storage().poweredExtraction(source, inv, toExtract, actionSource);
        }
        return Math.toIntExact(extracted == null ? 0 : extracted.getStackSize());
    }

    protected abstract Pair<IEnergySource, IMEInventoryHandler<IAEItemStack>> fromItem(ItemStack source,
            EntityPlayerMP player);

    @Override
    public boolean isAPIImplemented(APIType type) {
        return type == APIType.EXTRACT_ONE_STACK || type == APIType.MAIN;
    }

    @Override
    public int takeFromStack(Predicate<ItemStack> predicate, boolean simulate, int count,
            InventoryUtility.ItemStackCounter store, ItemStack source, ItemStack filter, EntityPlayerMP player) {
        // sanity check
        if (source == null || source.stackSize <= 0) return 0;
        Pair<IEnergySource, IMEInventoryHandler<IAEItemStack>> pair = fromItem(source, player);
        if (pair == null) return 0;
        IEnergySource energy = pair.getLeft();
        IMEInventoryHandler<IAEItemStack> cellInventory = pair.getRight();
        IItemList<IAEItemStack> items = cellInventory
                .getAvailableItems(AEApi.instance().storage().createPrimitiveItemList(), IterationCounter.fetchNewId());
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
