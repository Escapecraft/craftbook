package com.sk89q.craftbook.mech.ai;

import org.bukkit.entity.Entity;

public class BaseAIMechanic {

    public MechanismsPlugin plugin;
    public Entity entity;

    public BaseAIMechanic(MechanismsPlugin plugin, Entity entity) {

        this.plugin = plugin;
        this.entity = entity;
    }
}