package net.glease.structurecompat;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.gtnewhorizon.structurelib.util.InventoryUtility;

import forestry.storage.inventory.ItemInventoryBackpack;
import forestry.storage.items.ItemBackpack;

@Compat("Forestry")
public class CompatForestry {

    public CompatForestry() {
        InventoryUtility.registerStackExtractor(
                "1000-forestry-backpack",
                source -> source != null && source.getItem() instanceof ItemBackpack
                        ? new MyItemInventoryBackpack(source)
                        : null);
    }

    // hacky wacky, but works, for now.
    private static class MyItemInventoryBackpack extends ItemInventoryBackpack {

        private final ItemStack source;

        public MyItemInventoryBackpack(ItemStack source) {
            super(null, ((ItemBackpack) source.getItem()).getBackpackSize(), source);
            this.source = source;
        }

        @Override
        protected ItemStack getParent() {
            // this might be called during <init>. give it some dummy itemstack to not NPE
            return source == null ? new ItemStack(Items.feather) : source;
        }
    }
}
