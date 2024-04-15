package net.glease.structurecompat;

import baubles.api.BaublesApi;
import com.gtnewhorizon.structurelib.util.InventoryIterable;
import com.gtnewhorizon.structurelib.util.InventoryUtility;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;

@Compat("Baubles")
public class CompatBaubles {
    public CompatBaubles() {
        InventoryUtility.registerInventoryProvider("5001-baubles", new InventoryUtility.InventoryProvider<InventoryIterable<IInventory>>() {

            @Override
            public InventoryIterable<IInventory> getInventory(EntityPlayerMP player) {
                return new InventoryIterable<>(BaublesApi.getBaubles(player));
            }

            @Override
            public void markDirty(InventoryIterable<IInventory> inv) {
                inv.getInventory().markDirty();
            }
        });
    }
}
