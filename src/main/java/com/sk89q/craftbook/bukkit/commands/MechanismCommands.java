package com.sk89q.craftbook.bukkit.commands;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import org.bukkit.command.CommandSender;

/**
 * @author Silthus
 */
public class MechanismCommands {

    @Command(aliases = {"area"}, desc = "Commands to manage Craftbook Areas")
    @NestedCommand(AreaCommands.class)
    public void area(CommandContext context, CommandSender sender) {}

    @Command(aliases = {"cauldron"}, desc = "Commands to manage the Craftbook Cauldron")
    @NestedCommand(CauldronCommands.class)
    public void cauldron(CommandContext context, CommandSender sender) {}
}
