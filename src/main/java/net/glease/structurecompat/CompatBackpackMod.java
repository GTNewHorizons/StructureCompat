package net.glease.structurecompat;

import com.gtnewhorizon.structurelib.util.InventoryUtility;
import de.eydamos.backpack.item.ItemBackpackBase;
import de.eydamos.backpack.util.BackpackUtil;
import java.util.Arrays;

@Compat("Backpack")
public class CompatBackpackMod {
    public CompatBackpackMod() {
        // currently, each read/write into the every backpack would require several disk IO.
        // automated access into this inventory would probably cause some very significant lag,
        // so we only register an inventory provider for ender bags.
        InventoryUtility.registerEnableEnderCondition(player -> Arrays.stream(player.inventory.mainInventory)
                .anyMatch(stack -> stack != null
                        && stack.getItem() instanceof ItemBackpackBase
                        && BackpackUtil.isEnderBackpack(stack)));
    }
}
