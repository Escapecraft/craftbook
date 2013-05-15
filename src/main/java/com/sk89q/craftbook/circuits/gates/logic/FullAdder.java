package com.sk89q.craftbook.circuits.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;

public class FullAdder extends AbstractIC {

    public FullAdder(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Full Adder";
    }

    @Override
    public String getSignTitle() {

        return "FULL ADDER";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean A = chip.getInput(0);
        boolean B = chip.getInput(1);
        boolean C = chip.getInput(2);

        boolean S = A ^ B ^ C;
        boolean Ca = A & B | (A ^ B) & C;

        chip.setOutput(0, S);
        chip.setOutput(1, Ca);
        chip.setOutput(2, Ca);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new FullAdder(getServer(), sign, this);
        }
    }
}