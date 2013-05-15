package com.sk89q.craftbook.circuits.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;

public class InvertedRsNandLatch extends AbstractIC {

    public InvertedRsNandLatch(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Inverted RS NAND latch";
    }

    @Override
    public String getSignTitle() {

        return "INV RS NAND LAT";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean set = chip.get(0);
        boolean reset = chip.get(1);

        if (!set && !reset) {
            chip.set(3, true);
        } else if (set && !reset) {
            chip.set(3, false);
        } else if (!set && reset) {
            chip.set(3, true);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new InvertedRsNandLatch(getServer(), sign, this);
        }
    }
}
