package net.glease.structurecompat;

import com.mitchej123.hodgepodge.textures.IPatchedTextureAtlasSprite;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.versioning.*;
import net.minecraft.util.IIcon;

public class ClientProxy extends CommonProxy {
    private boolean notifyHodgepodgeTextureUsed = false;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        try {
            ArtifactVersion accepted =
                    new DefaultArtifactVersion("hodgepodge", VersionRange.createFromVersionSpec("[2.0.0,3)"));
            ModContainer mc = Loader.instance().getIndexedModList().get("hodgepodge");
            if (mc != null) notifyHodgepodgeTextureUsed = accepted.containsVersion(mc.getProcessedVersion());
        } catch (InvalidVersionSpecificationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markTextureUsed(IIcon o) {
        if (notifyHodgepodgeTextureUsed) {
            if (o instanceof IPatchedTextureAtlasSprite) ((IPatchedTextureAtlasSprite) o).markNeedsAnimationUpdate();
        }
    }
}
