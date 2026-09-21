package net.glease.structurecompat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import com.cleanroommc.bogosorter.api.BeforeSortEvent;
import com.gtnewhorizon.structurelib.item.ItemConstructableTrigger;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Compat("bogosorter")
public class CompatBogoSorter {

    private static final int MOUSE_MIDDLE = -98;

    public CompatBogoSorter() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onBeforeSortInGui(final BeforeSortEvent event) {
        // Cancel Bogo inventory sort if projector is picked-up/held.
        // Bogo sort is usually bound to middle-click.
        // This is just to avoid annoying player experience.
        if (!event.isFromKeybind()) return;

        final boolean isInGui = event.isInGui();
        if (event.getSortKeyCode() != MOUSE_MIDDLE) return;

        ItemStack item = null;

        if (isInGui) item = event.getPlayer().inventory.getItemStack();
        else item = event.getPlayer().getHeldItem();

        if (item == null || !(item.getItem() instanceof ItemConstructableTrigger)) return;

        // Cancel sort
        event.setCanceled(true);
    }
}
