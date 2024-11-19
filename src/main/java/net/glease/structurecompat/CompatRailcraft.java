package net.glease.structurecompat;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlocksTiered;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.withChannel;
import static net.glease.structurecompat.StructureUtilityExt.notBlock;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ChannelDataAccessor;
import com.gtnewhorizon.structurelib.alignment.constructable.IMultiblockInfoContainer;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.ITierConverter;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.blocks.machine.alpha.EnumMachineAlpha;
import mods.railcraft.common.blocks.machine.alpha.TileBlastFurnace;
import mods.railcraft.common.blocks.machine.alpha.TileCokeOven;
import mods.railcraft.common.blocks.machine.alpha.TileRockCrusher;
import mods.railcraft.common.blocks.machine.alpha.TileSteamOven;
import mods.railcraft.common.blocks.machine.alpha.TileTankWater;
import mods.railcraft.common.blocks.machine.beta.EnumMachineBeta;
import mods.railcraft.common.blocks.machine.beta.TileBoilerFirebox;
import mods.railcraft.common.blocks.machine.beta.TileBoilerFireboxFluid;
import mods.railcraft.common.blocks.machine.beta.TileBoilerFireboxSolid;
import mods.railcraft.common.core.Railcraft;

@Compat(Railcraft.MOD_ID)
public class CompatRailcraft {

    // (width, height)
    public static final int[][] BOILER_DIMENSIONS = { { 1, 1 }, { 2, 2 }, { 2, 3 }, { 3, 2 }, { 3, 3 }, { 3, 4 } };

    public CompatRailcraft() {
        registerBoilerStructureInfo();
        String[][] hollow3x3 = { { "     ", " xxx ", " xxx ", " xxx ", "     ", },
                { " xxx ", "xbbbx", "xbbbx", "xbbbx", " xxx " }, { " xxx ", "xbbbx", "xb-bx", "xbbbx", " xxx " },
                { " xxx ", "xbbbx", "xbbbx", "xbbbx", " xxx " }, { "     ", " xxx ", " xxx ", " xxx ", "     ", }, };
        IMultiblockInfoContainer.registerTileClass(
                TileCokeOven.class,
                new SimpleMultiblockInfoContainer<>(
                        IStructureDefinition.builder().addShape("main", hollow3x3).addElement(
                                'b',
                                ofBlock(RailcraftBlocks.getBlockMachineAlpha(), EnumMachineAlpha.COKE_OVEN.ordinal()))
                                .addElement(
                                        'x',
                                        notBlock(
                                                RailcraftBlocks.getBlockMachineAlpha(),
                                                EnumMachineAlpha.COKE_OVEN.ordinal()))
                                .build(),
                        2,
                        2,
                        1,
                        true,
                        "Coke Oven"));
        IMultiblockInfoContainer.registerTileClass(
                TileTankWater.class,
                new SimpleMultiblockInfoContainer<>(
                        IStructureDefinition.builder().addShape("main", hollow3x3).addElement(
                                'b',
                                ofBlock(RailcraftBlocks.getBlockMachineAlpha(), EnumMachineAlpha.TANK_WATER.ordinal()))
                                .addElement(
                                        'x',
                                        notBlock(
                                                RailcraftBlocks.getBlockMachineAlpha(),
                                                EnumMachineAlpha.TANK_WATER.ordinal()))
                                .build(),
                        2,
                        2,
                        1,
                        true,
                        "Water Tank"));
        IMultiblockInfoContainer.registerTileClass(
                TileSteamOven.class,
                new SimpleMultiblockInfoContainer<>(
                        IStructureDefinition.builder().addShape(
                                "main",
                                new String[][] { { "    ", " xx ", " xx ", "    ", },
                                        { " xx ", "xoox", "xoox", " xx ", }, { " xx ", "xoox", "xoox", " xx ", },
                                        { "    ", " xx ", " xx ", "    ", }, })
                                .addElement(
                                        'o',
                                        ofBlock(
                                                RailcraftBlocks.getBlockMachineAlpha(),
                                                EnumMachineAlpha.STEAM_OVEN.ordinal()))
                                .addElement(
                                        'x',
                                        notBlock(
                                                RailcraftBlocks.getBlockMachineAlpha(),
                                                EnumMachineAlpha.STEAM_OVEN.ordinal()))
                                .build(),
                        1,
                        2,
                        1,
                        true,
                        "Steam Oven"));
        IMultiblockInfoContainer.registerTileClass(
                TileRockCrusher.class,
                new SimpleMultiblockInfoContainer<>(
                        IStructureDefinition.builder().addShape(
                                "main",
                                new String[][] { { "    ", " xxx ", " xxx ", "    ", },
                                        { " xx ", "xcccx", "xcccx", " xx ", }, { " xx ", "xcccx", "xcccx", " xx ", },
                                        { "    ", " xxx ", " xxx ", "    ", }, })
                                .addElement(
                                        'c',
                                        ofBlock(
                                                RailcraftBlocks.getBlockMachineAlpha(),
                                                EnumMachineAlpha.ROCK_CRUSHER.ordinal()))
                                .addElement(
                                        'x',
                                        notBlock(
                                                RailcraftBlocks.getBlockMachineAlpha(),
                                                EnumMachineAlpha.ROCK_CRUSHER.ordinal()))
                                .build(),
                        2,
                        2,
                        1,
                        "Rock Crusher"));
        IMultiblockInfoContainer.registerTileClass(
                TileBlastFurnace.class,
                new SimpleMultiblockInfoContainer<>(
                        IStructureDefinition.builder()
                                .addShape(
                                        "main",
                                        new String[][] { { "     ", " xxx ", " xxx ", " xxx ", " xxx ", "     ", },
                                                { " xxx ", "xbbbx", "xbbbx", "xbbbx", "xbbbx", " xxx ", },
                                                { " xxx ", "xbbbx", "xb-bx", "xb-bx", "xbbbx", " xxx ", },
                                                { " xxx ", "xbbbx", "xbbbx", "xbbbx", "xbbbx", " xxx ", },
                                                { "     ", " xxx ", " xxx ", " xxx ", " xxx ", "     ", }, })
                                .addElement(
                                        'b',
                                        ofBlock(
                                                RailcraftBlocks.getBlockMachineAlpha(),
                                                EnumMachineAlpha.BLAST_FURNACE.ordinal()))
                                .addElement(
                                        'x',
                                        notBlock(
                                                RailcraftBlocks.getBlockMachineAlpha(),
                                                EnumMachineAlpha.BLAST_FURNACE.ordinal()))
                                .build(),
                        2,
                        4,
                        1,
                        "Blast Furnace"));
    }

    private static void registerBoilerStructureInfo() {
        String solidPrefix = "st";
        String fluidPrefix = "ft";
        StructureDefinition.Builder<TileBoilerFirebox> b = IStructureDefinition.builder();
        int[][] ints = BOILER_DIMENSIONS;
        for (int i = 0, intsLength = ints.length; i < intsLength; i++) {
            int[] dimension = ints[i];
            String[][] shape1 = new String[dimension[1]][];
            String[][] shape2 = new String[dimension[1]][];
            for (int i1 = 0; i1 < dimension[1]; i1++) {
                shape1[i1] = new String[dimension[0] + 1];
                for (int i2 = 0; i2 < dimension[0]; i2++) {
                    shape1[i1][i2] = Strings.repeat("b", dimension[1]);
                }
                shape2[i1] = Arrays.copyOf(shape1[i1], shape1[i1].length);
                shape1[i1][dimension[0]] = Strings.repeat("f", dimension[1]);
                shape2[i1][dimension[0]] = Strings.repeat("F", dimension[1]);
            }
            b.addShape(solidPrefix + i, shape1);
            b.addShape(fluidPrefix + i, shape2);
        }
        Block blockMachineBeta = RailcraftBlocks.getBlockMachineBeta();
        IStructureDefinition<TileBoilerFirebox> structureBoiler = b
                .addElement(
                        'f',
                        ofBlock(blockMachineBeta, EnumMachineBeta.BOILER_FIREBOX_SOLID.ordinal()))
                .addElement(
                        'F',
                        ofBlock(blockMachineBeta, EnumMachineBeta.BOILER_FIREBOX_FLUID.ordinal()))
                .addElement(
                        'b',
                        withChannel(
                                "boiler",
                                ofBlocksTiered(
                                        getBoilerTier(),
                                        ImmutableList.of(
                                                Pair.of(
                                                        blockMachineBeta,
                                                        EnumMachineBeta.BOILER_TANK_LOW_PRESSURE.ordinal()),
                                                Pair.of(
                                                        blockMachineBeta,
                                                        EnumMachineBeta.BOILER_TANK_HIGH_PRESSURE.ordinal())),
                                        null,
                                        (c, d) -> {},
                                        e -> 0)))
                .build();
        IMultiblockInfoContainer.registerTileClass(
                TileBoilerFireboxFluid.class,
                new BoilerMultiblockInfoContainer(structureBoiler, fluidPrefix));
        IMultiblockInfoContainer.registerTileClass(
                TileBoilerFireboxSolid.class,
                new BoilerMultiblockInfoContainer(structureBoiler, solidPrefix));
    }

    public static ITierConverter<Integer> getBoilerTier() {
        return (block, meta) -> {
            if (block == RailcraftBlocks.getBlockMachineBeta()) {
                if (meta == EnumMachineBeta.BOILER_TANK_LOW_PRESSURE.ordinal()) {
                    return 1;
                }

                if (meta == EnumMachineBeta.BOILER_TANK_HIGH_PRESSURE.ordinal()) {
                    return 2;
                }
            }
            return 0;
        };
    }

    private static ExtendedFacing noSideWay(ExtendedFacing aSide) {
        return aSide.getDirection().offsetY != 0 ? ExtendedFacing.DEFAULT : aSide;
    }

    private static class SimpleMultiblockInfoContainer<T extends TileEntity> implements IMultiblockInfoContainer<T> {

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

    private static class BoilerMultiblockInfoContainer implements IMultiblockInfoContainer<TileBoilerFirebox> {

        private final IStructureDefinition<TileBoilerFirebox> structureBoiler;
        private final String piecePrefix;

        public BoilerMultiblockInfoContainer(IStructureDefinition<TileBoilerFirebox> structureBoiler,
                String piecePrefix) {
            this.structureBoiler = structureBoiler;
            this.piecePrefix = piecePrefix;
        }

        @Override
        public void construct(ItemStack stackSize, boolean hintsOnly, TileBoilerFirebox ctx, ExtendedFacing aSide) {
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
                            ctx.getX(),
                            ctx.getY(),
                            ctx.getZ(),
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
                TileBoilerFirebox ctx, ExtendedFacing aSide) {
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
                            ctx.getX(),
                            ctx.getY(),
                            ctx.getZ(),
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
