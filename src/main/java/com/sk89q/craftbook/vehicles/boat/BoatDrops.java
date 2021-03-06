package com.sk89q.craftbook.vehicles.boat;

import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.blocks.ItemID;

public class BoatDrops implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleDestroy(VehicleDestroyEvent event) {

        if (!(event.getVehicle() instanceof Boat)) return;

        if (event.getAttacker() == null) {
            Boat boat = (Boat) event.getVehicle();
            boat.getLocation().getWorld().dropItemNaturally(boat.getLocation(), new ItemStack(ItemID.WOOD_BOAT));
            boat.remove();
            event.setCancelled(true);
        }
    }
}