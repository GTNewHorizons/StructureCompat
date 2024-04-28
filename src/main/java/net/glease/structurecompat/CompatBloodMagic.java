package net.glease.structurecompat;

import WayofTime.alchemicalWizardry.ModBlocks;
import WayofTime.alchemicalWizardry.common.tileEntity.TEAltar;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.partitionBy;


import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;


import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizon.structurelib.alignment.constructable.ChannelDataAccessor;
import com.gtnewhorizon.structurelib.alignment.constructable.IMultiblockInfoContainer;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.blocks.machine.beta.EnumMachineBeta;


@Compat("AWWayofTime")
public class CompatBloodMagic
{
    // (width, height)
    public static final int[][] BOILER_DIMENSIONS = { { 1, 1 }, { 2, 2 }, { 2, 3 }, { 3, 2 }, { 3, 3 }, { 3, 4 } };

    public CompatBloodMagic()
    {
        registerBoilerStructureInfo();
    }

    private static void registerBoilerStructureInfo() {
        String solidPrefix = "st";
        StructureDefinition.Builder<TEAltar> b = IStructureDefinition.builder();
        int[][] ints = BOILER_DIMENSIONS;
        for (int i = 0, intsLength = ints.length; i < intsLength; i++) {
            int[] dimension = ints[i];
            String[][] shape1 = new String[dimension[1]][];
            for (int i1 = 0; i1 < dimension[1]; i1++) {
                shape1[i1] = new String[dimension[0] + 1];
                for (int i2 = 0; i2 < dimension[0]; i2++) {
                    shape1[i1][i2] = Strings.repeat("b", dimension[1]);
                }
                shape1[i1][dimension[0]] = Strings.repeat("f", dimension[1]);
            }
            b.addShape(solidPrefix + i, shape1);
        }
        Block blockMachineBeta = RailcraftBlocks.getBlockMachineBeta();
        Block blockAltar = ModBlocks.blockAltar;
        IStructureDefinition<TEAltar> structureBoiler = b
            .addElement('f', ofBlock(blockAltar,0))
            .addElement(
                'b',
                partitionBy(
                    (t, i) -> ChannelDataAccessor.getChannelData(i, "boiler") <= 1,
                    ImmutableMap.of(
                        true,
                        ofBlock(blockMachineBeta, EnumMachineBeta.BOILER_TANK_LOW_PRESSURE.ordinal()),
                        false,
                        ofBlock(
                            blockMachineBeta,
                            EnumMachineBeta.BOILER_TANK_HIGH_PRESSURE.ordinal()))))
            .build();
        IMultiblockInfoContainer.registerTileClass(
            TEAltar.class,
            new CompatBloodMagic.AltarMultiblockInfoContainer(structureBoiler, solidPrefix));
    }

    private static ExtendedFacing noSideWay(ExtendedFacing aSide) {
        return aSide.getDirection().offsetY != 0 ? ExtendedFacing.DEFAULT : aSide;
    }

    private static class SimpleMultiblockInfoContainer<T extends TileEntity> implements IMultiblockInfoContainer<T>
    {

        public static final String MAIN_PIECE_NAME = "main";
        private final IStructureDefinition<? super T> structrue;
        private final String[] desc; // TODO somehow retranslate these at resource reload
        private final int offsetA, offsetB, offsetC;
        private final boolean allowSideway;

        public SimpleMultiblockInfoContainer(IStructureDefinition<? super T> structrue, int offsetA, int offsetB,
                                             int offsetC, String... desc) {
            this(structrue, offsetA, offsetB, offsetC, false, desc);
        }

        public SimpleMultiblockInfoContainer(IStructureDefinition<? super T> structrue, int offsetA, int offsetB,
                                             int offsetC, boolean allowSideway, String... desc) {
            this.structrue = structrue;
            this.allowSideway = allowSideway;
            this.desc = desc;
            this.offsetA = offsetA;
            this.offsetB = offsetB;
            this.offsetC = offsetC;
        }

        @Override
        public void construct(ItemStack stackSize, boolean hintsOnly, T ctx, ExtendedFacing aSide) {
            structrue.buildOrHints(
                ctx,
                stackSize,
                MAIN_PIECE_NAME,
                ctx.getWorldObj(),
                allowSideway ? aSide : noSideWay(aSide),
                ctx.xCoord,
                ctx.yCoord,
                ctx.zCoord,
                offsetA,
                offsetB,
                offsetC,
                hintsOnly);
        }

        @Override
        public int survivalConstruct(ItemStack stackSize, int elementBudge, ISurvivalBuildEnvironment env, T ctx,
                                     ExtendedFacing aSide) {
            return structrue.survivalBuild(
                ctx,
                stackSize,
                MAIN_PIECE_NAME,
                ctx.getWorldObj(),
                allowSideway ? aSide : noSideWay(aSide),
                ctx.xCoord,
                ctx.yCoord,
                ctx.zCoord,
                offsetA,
                offsetB,
                offsetC,
                elementBudge,
                env,
                false);
        }

        @Override
        public String[] getDescription(ItemStack stackSize) {
            return desc;
        }
    }

    private static class AltarMultiblockInfoContainer implements IMultiblockInfoContainer<TEAltar>
    {

        private final IStructureDefinition<TEAltar> structureBoiler;
        private final String piecePrefix;

        public AltarMultiblockInfoContainer(IStructureDefinition<TEAltar> structureBoiler,
                                             String piecePrefix) {
            this.structureBoiler = structureBoiler;
            this.piecePrefix = piecePrefix;
        }

        @Override
        public void construct(ItemStack stackSize, boolean hintsOnly, TEAltar ctx, ExtendedFacing aSide) {
            int width = ChannelDataAccessor.getChannelData(stackSize, "width"),
                height = ChannelDataAccessor.getChannelData(stackSize, "height");
            for (int i = 0, boilerDimensionsLength = BOILER_DIMENSIONS.length; i < boilerDimensionsLength; i++) {
                int[] dimension = BOILER_DIMENSIONS[i];
                if (dimension[0] == width && dimension[1] == height) {
                    structureBoiler.buildOrHints(
                        ctx,
                        stackSize,
                        piecePrefix + i,
                        ctx.getWorldObj(),
                        noSideWay(aSide),
                        ctx.xCoord,
                        ctx.yCoord,
                        ctx.zCoord,
                        width / 2,
                        height,
                        0,
                        hintsOnly);
                    return;
                }
            }
        }

        @Override
        public int survivalConstruct(ItemStack stackSize, int elementBudge, ISurvivalBuildEnvironment env,
                                     TEAltar ctx, ExtendedFacing aSide) {
            int width = ChannelDataAccessor.getChannelData(stackSize, "width"),
                height = ChannelDataAccessor.getChannelData(stackSize, "height");
            for (int i = 0, boilerDimensionsLength = BOILER_DIMENSIONS.length; i < boilerDimensionsLength; i++) {
                int[] dimension = BOILER_DIMENSIONS[i];
                if (dimension[0] == width && dimension[1] == height) {
                    return structureBoiler.survivalBuild(
                        ctx,
                        stackSize,
                        piecePrefix + i,
                        ctx.getWorldObj(),
                        noSideWay(aSide),
                        ctx.xCoord,
                        ctx.yCoord,
                        ctx.zCoord,
                        width / 2,
                        height,
                        0,
                        elementBudge,
                        env,
                        false);
                }
            }
            env.getActor().addChatMessage(
                new ChatComponentTranslation(
                    "structureinfo.railcraft.boiler.error.invalid_dimension",
                    width,
                    height));
            return -1;
        }

        @Override
        public String[] getDescription(ItemStack stackSize) {
            return new String[] { StatCollector.translateToLocal("structureinfo.railcraft.boiler.0"),
                StatCollector.translateToLocal("structureinfo.railcraft.boiler.1"),
                StatCollector.translateToLocal("structureinfo.railcraft.boiler.2"),
                StatCollector.translateToLocal("structureinfo.railcraft.boiler.3"),
                StatCollector.translateToLocal("structureinfo.railcraft.boiler.4"),
                StatCollector.translateToLocal("structureinfo.railcraft.boiler.5"), };
        }
    }

}
