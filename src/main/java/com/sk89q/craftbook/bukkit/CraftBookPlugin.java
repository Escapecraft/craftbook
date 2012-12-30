package com.sk89q.craftbook.bukkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.craftbook.LanguageManager;
import com.sk89q.craftbook.LocalComponent;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.commands.TopLevelCommands;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.SimpleInjector;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.util.FatalConfigurationLoadingException;

public class CraftBookPlugin extends JavaPlugin {

    /**
     * Required dependencies
     */
    private WorldEditPlugin worldEditPlugin;

    /**
     * Optional dependencies
     */
    private Economy economy;
    private ProtocolLibrary protocolLib;
    private WorldGuardPlugin worldGuardPlugin;

    /**
     * The instance for CraftBook
     */
    private static CraftBookPlugin instance;

    /**
     * The language manager
     */
    private LanguageManager languageManager;

    /**
     * The random
     */
    private Random random = new Random();

    /**
     * Manager for commands. This automatically handles nested commands,
     * permissions checking, and a number of other fancy command things.
     * We just set it up and register commands against it.
     */
    private CommandsManager<CommandSender> commands;

    /**
     * Handles all configuration.
     */
    private BukkitConfiguration config;

    /**
     * Used for backwards compatability of block faces.
     */
    private Boolean useOldBlockFace;

    /**
     * The currently enabled LocalComponents
     */
    private List<LocalComponent> components = new ArrayList<LocalComponent>();

    /**
     * Construct objects. Actual loading occurs when the plugin is enabled, so
     * this merely instantiates the objects.
     */
    public CraftBookPlugin() {

        // Set the instance
        instance = this;
    }

    /**
     * Called on plugin enable.
     */
    @Override
    public void onEnable() {

        // Check plugin for checking the active states of a plugin
        Plugin checkPlugin;

        // Check for WorldEdit
        checkPlugin = getServer().getPluginManager().getPlugin("WorldEdit");
        if (checkPlugin != null && checkPlugin instanceof WorldEditPlugin) {
            worldEditPlugin = (WorldEditPlugin) checkPlugin;
        } else {
            try {
                //noinspection UnusedDeclaration
                @SuppressWarnings("unused")
                String s = WorldEditPlugin.CUI_PLUGIN_CHANNEL;
            } catch (Throwable t) {
                logger().warning("WorldEdit detection has failed!");
                logger().warning("WorldEdit is a required dependency, Craftbook disabled!");
                return;
            }
        }

        // Setup Config and the Commands Manager
        final CraftBookPlugin plugin = this;
        config = new BukkitConfiguration(new YAMLProcessor(new File(getDataFolder(), "config.yml"), true), this);
        commands = new CommandsManager<CommandSender>() {

            @Override
            public boolean hasPermission(CommandSender player, String perm) {

                return plugin.hasPermission(player, perm);
            }
        };

        // Initialize the language manager.
        createDefaultConfiguration(new File(getDataFolder(), "en_US.txt"), "en_US.txt", true);
        languageManager = new LanguageManager();

        // Set the proper command injector
        commands.setInjector(new SimpleInjector(this));

        // Register command classes
        final CommandsManagerRegistration reg = new CommandsManagerRegistration(this, commands);
        reg.register(TopLevelCommands.class);

        // Need to create the plugins/WorldGuard folder
        getDataFolder().mkdirs();

        PermissionsResolverManager.initialize(this);

        // Load the configuration
        try {
            config.load();
        } catch (FatalConfigurationLoadingException e) {
            e.printStackTrace();
            getServer().shutdown();
        }

        // Resolve Vault
        try {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(
                    net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) economy = economyProvider.getProvider();
            else economy = null;
        } catch (Throwable e) {
            economy = null;
        }

        // Resolve ProtocolLib

        checkPlugin = getServer().getPluginManager().getPlugin("ProtocolLib");
        if (checkPlugin != null && checkPlugin instanceof ProtocolLibrary) {
            protocolLib = (ProtocolLibrary) checkPlugin;
        } else protocolLib = null;

        // Resolve WorldGuard
        checkPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (checkPlugin != null && checkPlugin instanceof WorldEditPlugin) {
            worldGuardPlugin = (WorldGuardPlugin) checkPlugin;
        } else worldGuardPlugin = null;

        // Let's start the show
        // Circuits
        CircuitCore circuitCore = new CircuitCore();
        circuitCore.enable();
        components.add(circuitCore);
        // Mechanics
        MechanicalCore mechanicalCore = new MechanicalCore();
        mechanicalCore.enable();
        components.add(mechanicalCore);
        // Vehicles
        VehicleCore vehicleCore = new VehicleCore();
        vehicleCore.enable();
        components.add(vehicleCore);

    }

    /**
     * Called on plugin disable.
     */
    @Override
    public void onDisable() {

        for (LocalComponent component : components) {
            component.disable();
        }
        config.unload();
    }

    /**
     * Handle a command.
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label,
            String[] args) {

        try {
            commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }

    /**
     * This retrieves the CraftBookPlugin instance for static access.
     *
     * @return Returns a CraftBookPlugin
     */
    public static CraftBookPlugin inst() {

        return instance;
    }

    /**
     * This retrives the CraftBookPlugin logger.
     *
     * @return Returns the CraftBookPlugin {@link Logger}
     */
    public static Logger logger() {

        return inst().getLogger();
    }

    /**
     * This retrives the CraftBookPlugin logger.
     *
     * @return Returns the CraftBookPlugin {@link Logger}
     */
    public static Server server() {

        return inst().getServer();
    }

    /**
     * This is a method used to register events for a class under CraftBook.
     */
    public static void registerEvents(Listener listener) {

        inst().getServer().getPluginManager().registerEvents(listener, inst());
    }

    /**
     * This is a method used to register commands for a class.
     */
    public void registerCommands(Class<?> clazz) {

        final CommandsManagerRegistration reg = new CommandsManagerRegistration(this, commands);
        reg.register(clazz);
    }

    /**
     * Get the global ConfigurationManager.
     * USe this to access global configuration values and per-world configuration values.
     *
     * @return The global ConfigurationManager
     */
    public BukkitConfiguration getConfiguration() {

        return config;
    }

    /**
     * This method is used to get the CraftBook {@link LanguageManager}.
     *
     * @return The CraftBook {@link LanguageManager}
     */
    public LanguageManager getLanguageManager() {

        return languageManager;
    }

    /**
     * This method is used to get CraftBook's {@link Random}.
     *
     * @return CraftBook's {@link Random}
     */
    public Random getRandom() {

        return random;
    }

    /**
     * Check whether a player is in a group.
     * This calls the corresponding method in PermissionsResolverManager
     *
     * @param player The player to check
     * @param group  The group
     *
     * @return whether {@code player} is in {@code group}
     */
    public boolean inGroup(Player player, String group) {

        try {
            return PermissionsResolverManager.getInstance().inGroup(player, group);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Get the groups of a player.
     * This calls the corresponding method in PermissionsResolverManager.
     *
     * @param player The player to check
     *
     * @return The names of each group the playe is in.
     */
    public String[] getGroups(Player player) {

        try {
            return PermissionsResolverManager.getInstance().getGroups(player);
        } catch (Throwable t) {
            t.printStackTrace();
            return new String[0];
        }
    }

    /**
     * Gets the name of a command sender. This is a unique name and this
     * method should never return a "display name".
     *
     * @param sender The sender to get the name of
     *
     * @return The unique name of the sender.
     */
    public String toUniqueName(CommandSender sender) {

        if (sender instanceof ConsoleCommandSender) {
            return "*Console*";
        } else {
            return sender.getName();
        }
    }

    /**
     * Gets the name of a command sender. This play be a display name.
     *
     * @param sender The CommandSender to get the name of.
     *
     * @return The name of the given sender
     */
    public String toName(CommandSender sender) {

        if (sender instanceof ConsoleCommandSender) {
            return "*Console*";
        } else if (sender instanceof Player) {
            return ((Player) sender).getDisplayName();
        } else {
            return sender.getName();
        }
    }

    /**
     * Checks permissions.
     *
     * @param sender The sender to check the permission on.
     * @param perm   The permission to check the permission on.
     *
     * @return whether {@code sender} has {@code perm}
     */
    public boolean hasPermission(CommandSender sender, String perm) {

        if (sender.isOp()) {
            if (sender instanceof Player) {
                if (!getConfiguration().noOpPermissions) return true;
            } else {
                return true;
            }
        }

        // Invoke the permissions resolver
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return PermissionsResolverManager.getInstance().hasPermission(player.getWorld().getName(),
                    player.getName(), perm);
        }

        return false;
    }

    /**
     * Checks permissions and throws an exception if permission is not met.
     *
     * @param sender The sender to check the permission on.
     * @param perm   The permission to check the permission on.
     *
     * @throws CommandPermissionsException if {@code sender} doesn't have {@code perm}
     */
    public void checkPermission(CommandSender sender, String perm)
            throws CommandPermissionsException {

        if (!hasPermission(sender, perm)) {
            throw new CommandPermissionsException();
        }
    }

    /**
     * Checks to see if the sender is a player, otherwise throw an exception.
     *
     * @param sender The {@link CommandSender} to check
     *
     * @return {@code sender} casted to a player
     *
     * @throws CommandException if {@code sender} isn't a {@link Player}
     */
    public Player checkPlayer(CommandSender sender)
            throws CommandException {

        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new CommandException("A player is expected.");
        }
    }

    /**
     * Wrap a player as a LocalPlayer.
     *
     * @param player The player to wrap
     *
     * @return The wrapped player
     */
    public LocalPlayer wrapPlayer(Player player) {

        return new BukkitPlayer(this, player);
    }

    /**
     * Reload configuration
     */
    public void reloadConfiguration() {

        // TODO Work
    }

    /**
     * Create a default configuration file from the .jar.
     *
     * @param actual      The destination file
     * @param defaultName The name of the file inside the jar's defaults folder
     * @param force       If it should make the file even if it already exists
     */
    public void createDefaultConfiguration(File actual, String defaultName, boolean force) {

        // Make parent directories
        File parent = actual.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        if (actual.exists() && !force) {
            return;
        }

        InputStream input = null;
        JarFile file = null;
        try {
            file = new JarFile(getFile());
            ZipEntry copy = file.getEntry("defaults/" + defaultName);
            if (copy == null) {
                file.close();
                throw new FileNotFoundException();
            }
            input = file.getInputStream(copy);
        } catch (IOException e) {
            getLogger().severe("Unable to read default configuration: " + defaultName);
        }

        if (input != null) {
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(actual);
                byte[] buf = new byte[8192];
                int length = 0;
                while ((length = input.read(buf)) > 0) {
                    output.write(buf, 0, length);
                }

                if(!force)
                    getLogger().info("Default configuration file written: "
                            + actual.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                try {
                    file.close();
                } catch (IOException e) {
                }

                try {
                    input.close();
                } catch (IOException ignore) {
                }

                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ignore) {
                }
            }
        }
    }

    /**
     * Gets the Vault {@link Economy} service if it exists, this method should be used for economic actions.
     *
     * This method can return null.
     *
     * @return The vault {@link Economy} service
     */
    public Economy getEconomy() {

        return economy;
    }

    /**
     * This method is used to determine whether ProtocolLib is
     * enabled on the server.
     *
     * @return True if ProtocolLib was found
     */
    public boolean hasProtocolLib() {

        return protocolLib != null;
    }

    /**
     * Gets a copy of the {@link WorldEditPlugin}.
     *
     * This method cannot return null.
     *
     * @return The {@link WorldEditPlugin} instance
     */
    public WorldEditPlugin getWorldEdit() {

        return worldEditPlugin;
    }

    /**
     * Gets the {@link WorldGuardPlugin} for non-build checks.
     *
     * This method can return null.
     *
     * @return {@link WorldGuardPlugin}
     */
    public WorldGuardPlugin getWorldGuard() {

        return worldGuardPlugin;
    }

    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled.
     *
     * @param player The player to check.
     * @param loc    The location to check at.
     *
     * @return whether {@code player} can build at {@code loc}
     *
     * @see GlobalRegionManager#canBuild(org.bukkit.entity.Player, org.bukkit.Location)
     */
    public boolean canBuild(Player player, Location loc) {

        return worldGuardPlugin == null || worldGuardPlugin.getGlobalRegionManager().canBuild(player, loc);
    }

    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled or WorldGuard is not found.
     *
     * @param player The player to check
     * @param block  The block to check at.
     *
     * @return whether {@code player} can build at {@code block}'s location
     *
     * @see GlobalRegionManager#canBuild(org.bukkit.entity.Player, org.bukkit.block.Block)
     */
    public boolean canBuild(Player player, Block block) {

        return worldGuardPlugin == null || worldGuardPlugin.getGlobalRegionManager().canBuild(player, block);
    }

    /**
     * Checks to see if a player can use at a location. This will return
     * true if region protection is disabled or WorldGuard is not found.
     *
     * @param player The player to check.
     * @param loc    The location to check at.
     *
     * @return whether {@code player} can build at {@code loc}
     *
     * @see GlobalRegionManager#canBuild(org.bukkit.entity.Player, org.bukkit.Location)
     */
    public boolean canUse(Player player, Location loc) {

        return worldGuardPlugin == null || worldGuardPlugin.getGlobalRegionManager().allows(new StateFlag("use",
                true), loc, new com.sk89q.worldguard.bukkit.BukkitPlayer(worldGuardPlugin, player));
    }

    /**
     * Check to see if it should use the old block face methods.
     *
     * @return whether it shoudl use the old blockface methods.
     */
    public boolean useOldBlockFace() {

        if (useOldBlockFace == null) {
            Location loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
            useOldBlockFace = loc.getBlock().getRelative(BlockFace.WEST).getX() == 0;
        }

        return useOldBlockFace;
    }
}