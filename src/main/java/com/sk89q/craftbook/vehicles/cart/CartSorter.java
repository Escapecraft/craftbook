package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.VehicleCore;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.vehicles.CartBlockImpactEvent;
import com.sk89q.worldedit.blocks.BlockID;

/*
 * @contributor LordEnki
 */

public class CartSorter extends CartBlockMechanism {

    public CartSorter (ItemInfo material) {
        super(material);
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // care?
        if (!event.getBlocks().matches(getMaterial())) return;
        if (event.isMinor()) return;

        // validate
        if (!event.getBlocks().matches("sort")) return;
        ChangedSign sign = event.getBlocks().getSign();

        // pi(sign)hich sort conditions apply
        // (left dominates if both apply)
        Hand dir = Hand.STRAIGHT;
        if (isSortApplicable(sign.getLine(2), event.getMinecart())) {
            dir = Hand.LEFT;
        } else if (isSortApplicable(sign.getLine(3), event.getMinecart())) {
            dir = Hand.RIGHT;
        }

        // pick the track block to modify and the curve to give it.
        // perhaps oddly, it's the sign facing that determines the concepts of left and right, and not the track.
        // this is required since there's not a north track and a south track; just a north-south track type.
        byte trackData;
        BlockFace next = SignUtil.getFacing(event.getBlocks().sign);
        if (CraftBookPlugin.inst().useOldBlockFace()) {

            switch (next) {
                case WEST:
                    switch (dir) {
                        case LEFT:
                            trackData = 9;
                            break;
                        case RIGHT:
                            trackData = 8;
                            break;
                        default:
                            trackData = 0;
                    }
                    break;
                case EAST:
                    switch (dir) {
                        case LEFT:
                            trackData = 7;
                            break;
                        case RIGHT:
                            trackData = 6;
                            break;
                        default:
                            trackData = 0;
                    }
                    break;
                case NORTH:
                    switch (dir) {
                        case LEFT:
                            trackData = 6;
                            break;
                        case RIGHT:
                            trackData = 9;
                            break;
                        default:
                            trackData = 1;
                    }
                    break;
                case SOUTH:
                    switch (dir) {
                        case LEFT:
                            trackData = 8;
                            break;
                        case RIGHT:
                            trackData = 7;
                            break;
                        default:
                            trackData = 1;
                    }
                    break;
                default:
                    return;
            }
        } else {
            switch (next) {
                case SOUTH:
                    switch (dir) {
                        case LEFT:
                            trackData = 9;
                            break;
                        case RIGHT:
                            trackData = 8;
                            break;
                        default:
                            trackData = 0;
                    }
                    break;
                case NORTH:
                    switch (dir) {
                        case LEFT:
                            trackData = 7;
                            break;
                        case RIGHT:
                            trackData = 6;
                            break;
                        default:
                            trackData = 0;
                    }
                    break;
                case WEST:
                    switch (dir) {
                        case LEFT:
                            trackData = 6;
                            break;
                        case RIGHT:
                            trackData = 9;
                            break;
                        default:
                            trackData = 1;
                    }
                    break;
                case EAST:
                    switch (dir) {
                        case LEFT:
                            trackData = 8;
                            break;
                        case RIGHT:
                            trackData = 7;
                            break;
                        default:
                            trackData = 1;
                    }
                    break;
                default:
                    return;
            }
        }
        Block targetTrack = event.getBlocks().rail.getRelative(next);

        // now check sanity real quick that there's actually a track after this,
        // and then make the change.
        if (targetTrack.getTypeId() == BlockID.MINECART_TRACKS) {
            targetTrack.setData(trackData);
        }
    }

    private enum Hand {
        STRAIGHT, LEFT, RIGHT
    }

    public static boolean isSortApplicable(String line, Minecart minecart) {

        if (line.equalsIgnoreCase("All")) return true;
        Entity test = minecart.getPassenger();
        Player player = null;
        if (test instanceof Player) {
            player = (Player) test;
        }

        if ((line.equalsIgnoreCase("Unoccupied") || line.equalsIgnoreCase("Empty")) && minecart.getPassenger() == null)
            return true;

        if (line.equalsIgnoreCase("Storage") && minecart instanceof StorageMinecart) return true;
        else if (line.equalsIgnoreCase("Powered") && minecart instanceof PoweredMinecart) return true;
        else if (line.equalsIgnoreCase("Minecart")) return true;

        if ((line.equalsIgnoreCase("Occupied") || line.equalsIgnoreCase("Full")) && minecart.getPassenger() != null)
            return true;

        if (line.equalsIgnoreCase("Animal") && test instanceof Animals) return true;

        if (line.equalsIgnoreCase("Mob") && test instanceof Monster) return true;

        if ((line.equalsIgnoreCase("Player") || line.equalsIgnoreCase("Ply")) && player != null) return true;

        String[] parts = RegexUtil.COLON_PATTERN.split(line);

        if (parts.length >= 2) if (player != null && parts[0].equalsIgnoreCase("Held")) {
            try {
                int item = Integer.parseInt(parts[1]);
                if (player.getItemInHand().getTypeId() == item) return true;
            } catch (NumberFormatException ignored) {
            }
        } else if (player != null && parts[0].equalsIgnoreCase("Ply")) {
            if (parts[1].equalsIgnoreCase(player.getName())) return true;
        } else if (parts[0].equalsIgnoreCase("Mob")) {
            String testMob = parts[1];
            return test.getType().toString().equalsIgnoreCase(testMob);
        } else if (minecart instanceof StorageMinecart && parts[0].equalsIgnoreCase("Ctns")) {
            StorageMinecart storageCart = (StorageMinecart) minecart;
            Inventory storageInventory = storageCart.getInventory();

            if (parts.length == 4) {
                try {
                    int item = Integer.parseInt(parts[1]);
                    short durability = Short.parseShort(parts[2]);
                    int index = Math.min(Math.max(Integer.parseInt(parts[3]) - 1, 0),
                            storageInventory.getContents().length - 1);
                    ItemStack indexed = storageInventory.getContents()[index];
                    if (indexed != null && indexed.equals(new ItemStack(item, 1, durability))) return true;
                } catch (NumberFormatException ignored) {
                }
            } else if (parts.length == 3) {
                try {
                    int item = Integer.parseInt(parts[1]);
                    short durability = Short.parseShort(parts[2]);
                    if (storageInventory.contains(new ItemStack(item, 1, durability))) return true;
                } catch (NumberFormatException ignored) {
                }
            } else {
                try {
                    int item = Integer.parseInt(parts[1]);
                    if (storageInventory.contains(item)) return true;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (line.startsWith("#")) {
            if (player != null) {
                String selectedStation = VehicleCore.inst().getStation(player.getName());
                return line.equalsIgnoreCase("#" + selectedStation);
            }
        }

        return false;
    }

    @Override
    public String getName() {

        return "Sorter";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"Sort"};
    }
}