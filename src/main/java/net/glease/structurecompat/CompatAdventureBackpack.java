package net.glease.structurecompat;

import com.darkona.adventurebackpack.inventory.InventoryBackpack;
import com.darkona.adventurebackpack.item.ItemAdventureBackpack;
import com.darkona.adventurebackpack.util.Wearing;
import com.gtnewhorizon.structurelib.util.InventoryUtility;

@Compat("adventurebackpack")
public class CompatAdventureBackpack {
    public CompatAdventureBackpack() {
        InventoryUtility.registerInventoryProvider(
                "6000-adventure-backpack",
                player -> !Wearing.isWearingBackpack(player)
                        ? null
                        : new InventoryBackpack(Wearing.getWearingBackpack(player)));
        InventoryUtility.registerStackExtractor(
                "1000-adventure-backpack",
                source -> source != null && source.getItem() instanceof ItemAdventureBackpack
                        ? new InventoryBackpack(source)
                        : null);
    }
}
