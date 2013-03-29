package com.sk89q.craftbook.circuits.gates.world.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;

public class Driller extends AbstractSelfTriggeredIC {

    public Driller (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void think (ChipState chip) {

        if(!chip.getInput(0))
            chip.setOutput(0, drill());
    }

    @Override
    public String getTitle () {

        return "Driller";
    }

    @Override
    public String getSignTitle () {

        return "DRILLER";
    }

    public boolean drill() {

        if(new Random().nextInt(100) < 70)
            return false;

        Block center = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, -1, 0);
        InventoryHolder holder = null;

        if(center.getRelative(0, 2, 0).getState() instanceof InventoryHolder) {
            holder = (InventoryHolder) center.getRelative(0, 2, 0).getState();
        }

        boolean hasHadTrue = false;

        if(drillLine(holder, center.getRelative(-1, 0, -1)))
            hasHadTrue = true;
        if(!hasHadTrue && drillLine(holder, center.getRelative(-1, 0, 0)))
            hasHadTrue = true;
        if(!hasHadTrue && drillLine(holder, center.getRelative(-1, 0, 1)))
            hasHadTrue = true;
        if(!hasHadTrue && drillLine(holder, center.getRelative(0, 0, -1)))
            hasHadTrue = true;
        if(!hasHadTrue && drillLine(holder, center.getRelative(0, 0, 0)))
            hasHadTrue = true;
        if(!hasHadTrue && drillLine(holder, center.getRelative(0, 0, 1)))
            hasHadTrue = true;
        if(!hasHadTrue && drillLine(holder, center.getRelative(1, 0, -1)))
            hasHadTrue = true;
        if(!hasHadTrue && drillLine(holder, center.getRelative(1, 0, 0)))
            hasHadTrue = true;
        if(!hasHadTrue && drillLine(holder, center.getRelative(1, 0, 1)))
            hasHadTrue = true;

        return hasHadTrue;
    }

    public boolean drillLine(InventoryHolder chest, Block blockToBreak) {

        boolean hasChest = chest != null;

        while(blockToBreak.getTypeId() == 0) {

            if(blockToBreak.getLocation().getBlockY() == 0)
                return false;
            blockToBreak = blockToBreak.getRelative(0, -1, 0);
            if(blockToBreak.getTypeId() == BlockID.BEDROCK)
                return false;
        }

        List<ItemStack> drops = new ArrayList<ItemStack>(blockToBreak.getDrops());
        if(hasChest && chest.getInventory().getItem(0) != null) {
            drops = new ArrayList<ItemStack>(blockToBreak.getDrops(chest.getInventory().getItem(0)));
        }

        for(ItemStack stack : drops) {

            List<ItemStack> toDrop = new ArrayList<ItemStack>();
            toDrop.add(stack);

            if(hasChest) {
                toDrop = new ArrayList<ItemStack>(chest.getInventory().addItem(toDrop.toArray(new ItemStack[1])).values());
            }

            if(!toDrop.isEmpty())
                for(ItemStack d : toDrop)
                    BukkitUtil.toSign(getSign()).getBlock().getWorld().dropItemNaturally(BukkitUtil.toSign(getSign()).getBlock().getLocation().add(0.5, 0.5, 0.5), d);
        }

        blockToBreak.setTypeId(0);

        return true;
    }

    @Override
    public void trigger (ChipState chip) {

        if(chip.getInput(0))
            chip.setOutput(0, drill());
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Driller(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Breaks a line of blocks from the IC block.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {null, null};
            return lines;
        }
    }
}