package com.sk89q.craftbook.bukkit.commands;
import org.bukkit.command.CommandSender;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.NestedCommand;

/**
 * Author: Turtle9598
 */
public class TopLevelCommands {

    public TopLevelCommands(CraftBookPlugin plugin) {

    }

    @Command(aliases = {"craftbook", "cb"}, desc = "CraftBook Plugin commands")
    @NestedCommand(Commands.class)
    public void craftBookCmds(CommandContext context, CommandSender sender) {

    }

    public class Commands {

        @Command(aliases = "reload", desc = "Reloads the CraftBook Common config")
        public void reload(CommandContext context, CommandSender sender) {

            CraftBookPlugin.inst().reloadConfiguration();
            sender.sendMessage("The CraftBook config has been reloaded.");
        }
    }
}
