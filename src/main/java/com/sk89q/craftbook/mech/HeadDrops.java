package com.sk89q.craftbook.mech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.MechanicListenerAdapter;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

public class HeadDrops implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().headDropsEnabled) return;
        if(CraftBookPlugin.inst().getConfiguration().headDropsPlayerKillOnly && event.getEntity().getKiller() == null) return;

        if(event.getEntity().getKiller() != null && !event.getEntity().getKiller().hasPermission("craftbook.mech.headdrops.kill"))
            return;

        double chance = Math.min(1, CraftBookPlugin.inst().getConfiguration().headDropsDropRate);
        if(CraftBookPlugin.inst().getConfiguration().headDropsCustomDropRate.containsKey(event.getEntityType().getName()))
            chance = Math.min(1, CraftBookPlugin.inst().getConfiguration().headDropsCustomDropRate.get(event.getEntityType().getName()));

        if(event.getEntity().getKiller() != null && event.getEntity().getKiller().getItemInHand() != null && event.getEntity().getKiller().getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS))
            chance = Math.min(1, chance + CraftBookPlugin.inst().getConfiguration().headDropsLootingRateModifier * event.getEntity().getKiller().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS));

        if(CraftBookPlugin.inst().getRandom().nextDouble() > chance)
            return;

        ItemStack toDrop = null;

        switch(event.getEntityType()) {

            case PLAYER:
                if(!CraftBookPlugin.inst().getConfiguration().headDropsPlayers)
                    return;
                String playerName = ((Player) event.getEntity()).getName();
                toDrop = new ItemStack(ItemID.HEAD, 1, (short)3);
                toDrop.setData(new MaterialData(ItemID.HEAD,(byte)3));
                SkullMeta meta = (SkullMeta) toDrop.getItemMeta();
                meta.setOwner(playerName);
                meta.setDisplayName(ChatColor.RESET + playerName + "'s Head");
                toDrop.setItemMeta(meta);
                break;
            case ZOMBIE:
                if(!CraftBookPlugin.inst().getConfiguration().headDropsMobs)
                    return;
                toDrop = new ItemStack(ItemID.HEAD, 1, (short)2);
                toDrop.setData(new MaterialData(ItemID.HEAD,(byte)2));
                break;
            case CREEPER:
                if(!CraftBookPlugin.inst().getConfiguration().headDropsMobs)
                    return;
                toDrop = new ItemStack(ItemID.HEAD, 1, (short)4);
                toDrop.setData(new MaterialData(ItemID.HEAD,(byte)4));
                break;
            case SKELETON:
                if(!CraftBookPlugin.inst().getConfiguration().headDropsMobs)
                    return;
                if(((Skeleton) event.getEntity()).getSkeletonType() == SkeletonType.WITHER && !CraftBookPlugin.inst().getConfiguration().headDropsDropOverrideNatural)
                    return;
                toDrop = new ItemStack(ItemID.HEAD, 1, (short) (((Skeleton) event.getEntity()).getSkeletonType() == SkeletonType.WITHER ? 1 : 0));
                toDrop.setData(new MaterialData(ItemID.HEAD, (byte)(((Skeleton) event.getEntity()).getSkeletonType() == SkeletonType.WITHER ? 1 : 0)));
                break;
            default:
                if(!CraftBookPlugin.inst().getConfiguration().headDropsMobs)
                    return;
                MobSkullType type = MobSkullType.getFromEntityType(event.getEntityType());
                String mobName = null;
                if(type != null)
                    mobName = type.getPlayerName();
                if(CraftBookPlugin.inst().getConfiguration().headDropsCustomSkins.containsKey(event.getEntityType().name()))
                    mobName = CraftBookPlugin.inst().getConfiguration().headDropsCustomSkins.get(event.getEntityType().name());
                if(mobName == null || mobName.isEmpty())
                    break;
                toDrop = new ItemStack(ItemID.HEAD, 1, (short)3);
                toDrop.setData(new MaterialData(ItemID.HEAD,(byte)3));
                SkullMeta itemMeta = (SkullMeta) toDrop.getItemMeta();
                itemMeta.setDisplayName(ChatColor.RESET + EntityType.valueOf(event.getEntityType().name()).getName() + " Head");
                itemMeta.setOwner(mobName);
                toDrop.setItemMeta(itemMeta);
                break;
        }

        if(ItemUtil.isStackValid(toDrop)) {
            event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), toDrop);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().headDropsEnabled) return;
        if(!CraftBookPlugin.inst().getConfiguration().headDropsMiningDrops) return;
        if(MechanicListenerAdapter.ignoredEvents.contains(event))
            return;
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        if(event.getBlock().getTypeId() == BlockID.HEAD) {

            Skull skull = (Skull)event.getBlock().getState();
            if(!skull.hasOwner())
                return;
            String playerName = ChatColor.stripColor(skull.getOwner());

            EntityType type = MobSkullType.getEntityType(playerName);

            ItemStack stack = new ItemStack(ItemID.HEAD, 1, (short)3);
            stack.setData(new MaterialData(ItemID.HEAD, (byte)3));
            SkullMeta meta = (SkullMeta) stack.getItemMeta();
            meta.setOwner(playerName);

            if(type != null && !CraftBookPlugin.inst().getConfiguration().headDropsMobs)
                return;
            if(type == null && !CraftBookPlugin.inst().getConfiguration().headDropsPlayers)
                return;

            if(!event.getPlayer().hasPermission("craftbook.mech.headdrops.break")) {

                event.getPlayer().sendMessage("You don't have permission to break heads!");
                return;
            }

            if(type != null)
                meta.setDisplayName(ChatColor.RESET + type.getName().replace("_", " ") + " Head");
            else
                meta.setDisplayName(ChatColor.RESET + playerName + "'s Head");

            stack.setItemMeta(meta);

            if(!CraftBookPlugin.inst().canBuild(event.getPlayer(), event.getBlock(), false))
                return;

            event.setCancelled(true);
            event.getBlock().setTypeId(0);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), stack);
        }
    }

    private enum MobSkullType {

        BLAZE("Blaze_Head"),
        ENDERMAN("Violit"),
        GHAST("_QuBra_"),
        MUSHROOM_COW("Mooshroom_Stew"),
        SQUID("squidette8"),
        SILVERFISH("AlexVMiner"),
        ENDER_DRAGON("KingEndermen"),
        SLIME("HappyHappyMan"),
        SNOWMAN("Koebasti"),
        IRON_GOLEM("zippie007"),
        HORSE("gavertoso"),
        PIG("XlexerX"),
        PIG_ZOMBIE("ManBearPigZombie"),
        BAT("coolwhip101", "bozzobrain"),
        SPIDER("Kelevra_V"),
        VILLAGER("Villager"),
        SHEEP("Eagle_Peak"),
        COW("VerifiedBernard");

        MobSkullType(String playerName, String ... oldNames) {

            this.playerName = playerName;
            this.oldNames = new ArrayList<String>(Arrays.asList(oldNames));
        }

        private String playerName;
        private List<String> oldNames;

        public String getPlayerName() {

            return playerName;
        }

        public boolean isOldName(String name) {

            return oldNames.contains(name);
        }

        public static MobSkullType getFromEntityType(EntityType entType) {

            try {
                return MobSkullType.valueOf(entType.name());
            } catch(Exception e){
                return null;
            }
        }

        public static EntityType getEntityType(String name) {

            for(MobSkullType type : values())
                if(type.getPlayerName().equals(name) || type.isOldName(name) || CraftBookPlugin.inst().getConfiguration().headDropsCustomSkins.containsKey(type.name()) && CraftBookPlugin.inst().getConfiguration().headDropsCustomSkins.get(type.name()).equals(name))
                    return EntityType.valueOf(type.name());

            return null;
        }
    }
}