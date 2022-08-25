package net.glease.structurecompat;

import com.gtnewhorizon.structurelib.IStructureCompat;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.discovery.ASMDataTable.ASMData;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.IIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Tags.MODID,
        version = Tags.VERSION,
        name = Tags.MODNAME,
        acceptedMinecraftVersions = "[1.7.10]",
        dependencies = "required-after:structurelib")
public class StructureCompat implements IStructureCompat {

    public static final Logger LOG = LogManager.getLogger(Tags.MODID);
    private List<String> compats;

    @SidedProxy(
            serverSide = "net.glease.structurecompat.CommonProxy",
            clientSide = "net.glease.structurecompat.ClientProxy")
    static CommonProxy proxy;

    @SuppressWarnings("unchecked")
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOG.debug("Starting to identify compats");
        List<String> list = new ArrayList<>();
        for (ASMData d : event.getAsmData().getAll(Compat.class.getName())) {
            if (((List<String>) d.getAnnotationInfo().get("value")).stream().allMatch(Loader::isModLoaded)) {
                LOG.debug("Compat {} will be loaded", d.getObjectName());
                list.add(d.getObjectName());
            } else {
                LOG.debug("Compat {} will not be loaded", d.getObjectName());
            }
        }
        compats = list;
        LOG.info("Identified {} compat(s) to load.", compats.size());

        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        int success = 0;
        for (String compat : compats) {
            LOG.debug("Activating compat {}", compat);
            try {
                Class.forName(compat).getConstructor().newInstance();
                success += 1; // if only java has try-catch-else
            } catch (InvocationTargetException e) {
                LOG.error("Compat activation errored!", e.getTargetException());
            } catch (ReflectiveOperationException e) {
                LOG.error("Cannot load compat!", e);
            }
        }
        LOG.info("Successfully activated {} compat(s).", success);
    }

    @Override
    public void markTextureUsed(IIcon o) {
        proxy.markTextureUsed(o);
    }
}
