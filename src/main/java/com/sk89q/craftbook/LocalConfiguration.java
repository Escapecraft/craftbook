package com.sk89q.craftbook;
import com.sk89q.craftbook.mech.CustomDropManager;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A implementation of Configuration based off of {@link com.sk89q.worldedit.LocalConfiguration} for CraftBook.
 */
public abstract class LocalConfiguration {


    // Circuits
    // Circuits - IC
    public boolean ICEnabled = true;
    public boolean ICCached = true;
    public boolean ICShortHandEnabled = true;
    public Set<String> disabledICs = new HashSet<String>();

    // Circuits - Wiring
    public boolean netherrackEnabled = false;
    public boolean pumpkinsEnabled = false;
    public boolean glowstoneEnabled = false;
    public int glowstoneOffBlock = BlockID.GLASS;

    // Circuits - Pipes
    public boolean pipesEnabled = true;
    public boolean pipesDiagonal = false;
    public int pipeInsulator = BlockID.CLOTH;

    // Mechanics
    // Mechanics - AI
    public boolean aiEnabled = true;
    public boolean aiZombieEnabled = true;
    public boolean aiSkeletonEnabled = true;
    // Mechanics - Ammeter
    public boolean ammeterEnabled = true;
    public int ammeterItem = ItemID.COAL;
    // Mechanics - Area
    public boolean areaEnabled = true;
    public boolean areaAllowRedstone = true;
    public boolean areaUseSchematics = true;
    public int areaMaxAreaSize = 5000;
    public int areaMaxAreaPerUser = 30;
    // Mechanics - Bookcase
    public boolean bookcaseEnabled = true;
    public String bookcaseReadLine = "You pick up a book...";
    // Mechanics - Bridge
    public boolean bridgeEnabled = true;
    public boolean bridgeAllowRedstone = true;
    public int bridgeMaxLength = 30;
    public int bridgeMaxWidth = 5;
    public List<Integer> bridgeBlocks = Arrays.asList(4, 5, 20, 43);
    // Mechanics - Cauldron
    public boolean cauldronEnabled = true;
    public boolean cauldronUseSpoons = true;
    // Mechanics - Chair
    public boolean chairEnabled = true;
    public boolean chairSneak = true;
    public boolean chairHealth = true;
    public List<Integer> chairBlocks = Arrays.asList(53, 67, 108, 109, 114, 128, 134, 135, 136);
    // Mechanics - Chunk Anchor
    public boolean chunkAnchorEnabled = true;
    // Mechanics - Command Signs
    public boolean commandSignEnabled = true;
    // Mechanics - Cooking Pot
    public boolean cookingPotEnabled = true;
    public boolean cookingPotFuel = true;
    public boolean cookingPotOres = false;
    public boolean cookingPotSignOpen = true;
    // Mechanics - Custom Crafting
    public boolean customCraftingEnabled = true;
    // Mechanics - Custom Dispensing
    public boolean customDispensingEnabled = true;
    // Mechanics - Custom Drops
    public boolean customDropEnabled = true;
    public boolean customDropPermissions = false;
    public CustomDropManager customDrops;
    // Mechanics - Door
    public boolean doorEnabled = true;
    public boolean doorAllowRedstone = true;
    public int doorMaxLength = 30;
    public int doorMaxWidth = 5;
    public List<Integer> doorBlocks = Arrays.asList(4, 5, 20, 43);
    // Mechanics - Elevator
    public boolean elevatorEnabled = true;
    public boolean elevatorButtonEnabled = true;
    public boolean elevatorLoop = false;
    // Mechanics - Gate
    public boolean gateEnabled = true;
    public boolean gateAllowRedstone = true;
    public boolean gateLimitColumns = true;
    public int gateColumnLimit = 14;
    public List<Integer> gateBlocks = Arrays.asList(85, 101, 102, 113);
    // Mechanics - Hidden Switch
    public boolean hiddenSwitchEnabled = true;
    public boolean hiddenSwitchAnyside = true;
    // Mechanics - Legacy Cauldron
    public boolean legacyCauldronEnabled = true;
    public int legacyCauldronBlock = BlockID.STONE;
    // Mechanics - Lightstone
    public boolean lightstoneEnabled = true;
    public int lightstoneItem = ItemID.LIGHTSTONE_DUST;
    // Mechanics - Light Switch
    public boolean lightSwitchEnabled = true;
    public int lightSwitchMaxRange = 10;
    public int lightSwitchMaxLights = 20;
    // Mechanics - Map Changer
    public boolean mapChangerEnabled = true;
    // Mechanics - Paintings
    public boolean paintingsEnabled = true;
    // Mechanics - Payment
    public boolean paymentEnabled = true;
    // Mechanics - Snow
    public boolean snowEnabled = true;
    public boolean snowTrample = true;
    public boolean snowPlace = true;
    public boolean snowRealistic = false;
    public boolean snowHighPiles = false;
    public boolean snowJumpTrample = false;
    // Mechanics - Teleporter
    public boolean teleporterEnabled = true;
    public boolean teleporterRequireSign = false;
    public int teleporterMaxRange = 0;
    // Mechanics - XPStorer
    public boolean xpStorerEnabled = true;
    public int xpStorerBlock = BlockID.MOB_SPAWNER;

    // Vehicles
    // Vehicles - Messenger
    public boolean messengerEnabled = true;

    /**
     * Loads the configuration.
     */
    public abstract void load();

    /**
     * Get the working directory to work from.
     *
     * @return
     */
    public File getWorkingDirectory() {

        return new File(".");
    }
}
