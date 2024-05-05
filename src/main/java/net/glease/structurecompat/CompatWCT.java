package net.glease.structurecompat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryViewCell;

import com.gtnewhorizon.structurelib.util.InventoryUtility;

import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;

@Compat("ae2wct")
public class CompatWCT {

    public CompatWCT() {

        InventoryUtility.registerStackExtractor("0999-ae2wct-need-before-ae2", new WirelessTerminalStackExtractor() {

            @Override
            public boolean isValidSource(ItemStack source, EntityPlayerMP player) {
                return source.getItem() instanceof IWirelessCraftingTerminalItem;
            }

            @Override
            protected boolean rangeCheck(ItemStack source, EntityPlayerMP player, IGrid targetGrid) {
                if (((IWirelessCraftingTerminalItem) source.getItem()).checkForBooster(source)) return true;
                return super.rangeCheck(source, player, targetGrid);
            }

            @Override
            protected IViewCellStorage getViewCellStorage(ItemStack is) {
                return () -> new WCTInventoryViewCell(is);
            }
        });
    }
}
