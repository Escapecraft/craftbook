package com.sk89q.craftbook.mech.crafting;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.GeneralUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.io.File;
import java.util.Collection;
import java.util.Map.Entry;

/**
 * Custom Crafting Recipe Handler
 *
 * @author Me4502
 */
public class CustomCrafting {

    protected final RecipeManager recipes;
    protected final CraftBookPlugin plugin = CraftBookPlugin.inst();

    public CustomCrafting() {

        recipes = new RecipeManager(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),
                "crafting-recipes.yml")),
                plugin.getDataFolder());
        Collection<RecipeManager.Recipe> recipeCollection = recipes.getRecipes();
        for (RecipeManager.Recipe r : recipeCollection) {
            try {
                if (r.getType() == RecipeManager.Recipe.RecipeType.SHAPELESS) {
                    ShapelessRecipe sh = new ShapelessRecipe(r.getResult().getItemStack());
                    for (CraftingItemStack is : r.getIngredients()) {
                        sh.addIngredient(is.getAmount(), is.getMaterial(), is.getData());
                    }
                    plugin.getServer().addRecipe(sh);
                } else if (r.getType() == RecipeManager.Recipe.RecipeType.SHAPED2X2) {
                    ShapedRecipe sh = new ShapedRecipe(r.getResult().getItemStack());
                    sh.shape(r.getShape());
                    for (Entry<CraftingItemStack, Character> is : r.getShapedIngredients().entrySet()) {
                        sh.setIngredient(is.getValue(), is.getKey().getMaterial(), is.getKey().getData());
                    }
                    plugin.getServer().addRecipe(sh);
                } else if (r.getType() == RecipeManager.Recipe.RecipeType.SHAPED3X3) {
                    ShapedRecipe sh = new ShapedRecipe(r.getResult().getItemStack());
                    sh.shape(r.getShape());
                    for (Entry<CraftingItemStack, Character> is : r.getShapedIngredients().entrySet()) {
                        sh.setIngredient(is.getValue(), is.getKey().getMaterial(), is.getKey().getData());
                    }
                    plugin.getServer().addRecipe(sh);
                } else if (r.getType() == RecipeManager.Recipe.RecipeType.FURNACE) {
                    FurnaceRecipe sh = new FurnaceRecipe(r.getResult().getItemStack(), r.getResult().getMaterial());
                    for (CraftingItemStack is : r.getIngredients()) {
                        sh.setInput(is.getMaterial(), is.getData());
                    }
                    plugin.getServer().addRecipe(sh);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Corrupt or invalid recipe!");
                plugin.getLogger().severe("Please either delete custom-crafting.yml, " +
                        "" + "or fix the issues with your recipes file!");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load recipe!");
                plugin.getLogger().severe(GeneralUtil.getStackTrace(e));
            }
        }
    }
}