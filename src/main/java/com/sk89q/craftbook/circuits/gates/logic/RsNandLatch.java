package com.sk89q.craftbook.circuits.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;

/**
 * Simulates the function of a SR latch made from NAND gates.
 */
public class RsNandLatch extends AbstractIC {

    public RsNandLatch(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "RS NAND latch";
    }

    @Override
    public String getSignTitle() {

        return "RS NAND LATCH";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean set = !chip.get(0);
        boolean reset = !chip.get(1);
        if (!set && !reset) {
            chip.set(3, true);
        } else if (!set && reset) {
            chip.set(3, true);
        } else if (!reset) {
            chip.set(3, false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new RsNandLatch(getServer(), sign, this);
        }
    }
}
