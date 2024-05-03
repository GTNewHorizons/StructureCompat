package net.glease.structurecompat;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlockAnyMeta;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.gtnewhorizon.structurelib.alignment.constructable.IMultiblockInfoContainer;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import WayofTime.alchemicalWizardry.AlchemicalWizardry;
import WayofTime.alchemicalWizardry.ModBlocks;
import WayofTime.alchemicalWizardry.common.tileEntity.TEAltar;

@Compat("AWWayofTime")
public class CompatBloodMagic {

    private static final String STRUCTURE_ALTAR = "tier1";
    private static final String STRUCTURE_TIER_2 = "tier2";
    private static final String STRUCTURE_TIER_3 = "tier3";
    private static final String STRUCTURE_TIER_4 = "tier4";
    private static final String STRUCTURE_TIER_5 = "tier5";
    private static final String STRUCTURE_TIER_6 = "tier6";

    public static final int[][] TIER_OFFSET = { { 0, 0, 0 }, { 1, -1, 1 }, { 3, 1, 3 }, { 5, 2, 5 }, { 8, -3, 8 },
            { 11, 3, 11 } };

    public CompatBloodMagic() {
        registerAltarStructureInfo();
    }

    private static void registerAltarStructureInfo() {
        StructureDefinition.Builder<TEAltar> altarBuilder = IStructureDefinition.builder();
        String T6_S = "                       ";
        String T6_SR = "r                     r";
        // region Structure
        // spotless:off
        IStructureDefinition<TEAltar> structureAltar = altarBuilder
            .addShape(STRUCTURE_ALTAR, new String[][] {{"a"}})
            .addShape(STRUCTURE_TIER_2, transpose(new String[][] {{"rrr","r r","rrr"}}))
            .addShape(STRUCTURE_TIER_3, transpose(new String[][] {{"g     g","       ","       ","       ","       ","       ","g     g"},
                                                        {"t     t","       ","       ","       ","       ","       ","t     t"},
                                                        {"t     t","       ","       ","       ","       ","       ","t     t"},
                                                        {" rrrrr ","r     r","r     r","r     r","r     r","r     r"," rrrrr "}}))
            .addShape(STRUCTURE_TIER_4, transpose(new String[][]
                {{"b         b","           ","           ","           ","           ","           ","           ","           ","           ","           ","b         b"},
                 {"v         v","           ","           ","           ","           ","           ","           ","           ","           ","           ","v         v"},
                 {"v         v","           ","           ","           ","           ","           ","           ","           ","           ","           ","v         v"},
                 {"v         v","           ","           ","           ","           ","           ","           ","           ","           ","           ","v         v"},
                 {"v         v","           ","           ","           ","           ","           ","           ","           ","           ","           ","v         v"},
                 {"  rrrrrrr  ","           ","r         r","r         r","r         r","r         r","r         r","r         r","r         r","           ","  rrrrrrr  "}}))
            .addShape(STRUCTURE_TIER_5, transpose(new String[][] {{"e               e","                 ","                 ","                 ","                 ","                 ","                 ","                 ","                 ","                 ","                 ","                 ","                 ","                 ","                 ","                 ","e               e"},
                {"  rrrrrrrrrrrrr  ","                 ","r               r","r               r","r               r","r               r","r               r","r               r","r               r","r               r","r               r","r               r","r               r","r               r","r               r","                 ","  rrrrrrrrrrrrr  "}}))
            .addShape(STRUCTURE_TIER_6, transpose(new String[][]
                {
                    {"c                     c",T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,"c                     c"},
                    {"i                     i",T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,"i                     i"},
                    {"i                     i",T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,"i                     i"},
                    {"i                     i",T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,"i                     i"},
                    {"i                     i",T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,"i                     i"},
                    {"i                     i",T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,"i                     i"},
                    {"i                     i",T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,"i                     i"},
                    {"i                     i",T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,T6_S,"i                     i"},
                    {"  rrrrrrrrrrrrrrrrrrr  ",T6_S,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_SR,T6_S, "  rrrrrrrrrrrrrrrrrrr  "},
                }))
            .addElement('a',ofBlock(ModBlocks.blockAltar,0))
            .addElement('r',ofChain(ofBlockAnyMeta(ModBlocks.bloodRune),ofBlockAnyMeta(ModBlocks.speedRune),ofBlockAnyMeta(ModBlocks.runeOfSelfSacrifice),ofBlockAnyMeta(ModBlocks.runeOfSacrifice),ofBlockAnyMeta(ModBlocks.efficiencyRune)))
            .addElement('g',ofBlock(AlchemicalWizardry.specialAltarBlock[1].getBlock(),0))
            .addElement('t',ofBlock(AlchemicalWizardry.specialAltarBlock[0].getBlock(),0))
            .addElement('b',ofBlock(AlchemicalWizardry.specialAltarBlock[3].getBlock(),0))
            .addElement('v',ofBlock(AlchemicalWizardry.specialAltarBlock[2].getBlock(),0))
            .addElement('e',ofBlock(AlchemicalWizardry.specialAltarBlock[4].getBlock(),0))
            .addElement('c',ofBlock(AlchemicalWizardry.specialAltarBlock[6].getBlock(),0))
            .addElement('i',ofBlock(AlchemicalWizardry.specialAltarBlock[5].getBlock(),0))
            .build();
        IMultiblockInfoContainer.registerTileClass(
            TEAltar.class,
            new CompatBloodMagic.AltarMultiblockInfoContainer(structureAltar));
        // spotless:on
    }

    private static class AltarMultiblockInfoContainer implements IMultiblockInfoContainer<TEAltar> {

        private final IStructureDefinition<TEAltar> structureAltar;

        public AltarMultiblockInfoContainer(IStructureDefinition<TEAltar> structureAltar) {
            this.structureAltar = structureAltar;
        }

        @Override
        public void construct(ItemStack triggerStack, boolean hintsOnly, TEAltar ctx, ExtendedFacing aSide) {
            int tier = triggerStack.stackSize;
            if (tier > 6) {
                tier = 6;
            }
            for (int i = 1; i <= tier; i++) {
                this.structureAltar.buildOrHints(
                        ctx,
                        triggerStack,
                        "tier" + i,
                        ctx.getWorldObj(),
                        ExtendedFacing.DEFAULT,
                        ctx.xCoord,
                        ctx.yCoord,
                        ctx.zCoord,
                        TIER_OFFSET[i - 1][0],
                        TIER_OFFSET[i - 1][1],
                        TIER_OFFSET[i - 1][2],
                        hintsOnly);
            }
        }

        @Override
        public int survivalConstruct(ItemStack triggerStack, int elementBudge, ISurvivalBuildEnvironment env,
                TEAltar ctx, ExtendedFacing aSide) {
            int built = 0;
            int tier = triggerStack.stackSize;
            if (tier > 6) {
                tier = 6;
            }
            if (ctx.getTier() >= tier) return -1;

            for (int i = 1; i <= tier; i++) {
                built += this.structureAltar.survivalBuild(
                        ctx,
                        triggerStack,
                        "tier" + i,
                        ctx.getWorldObj(),
                        ExtendedFacing.DEFAULT,
                        ctx.xCoord,
                        ctx.yCoord,
                        ctx.zCoord,
                        TIER_OFFSET[i - 1][0],
                        TIER_OFFSET[i - 1][1],
                        TIER_OFFSET[i - 1][2],
                        elementBudge,
                        env,
                        false);
            }
            return built;
        }

        @Override
        public String[] getDescription(ItemStack stackSize) {
            return new String[] { StatCollector.translateToLocal("structureinfo.bloodmagic.altar.1"),
                    StatCollector.translateToLocal("structureinfo.bloodmagic.altar.2") };
        }
    }
}
