package pl.ynfuien.ygenerators.listeners;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.generator.Generator;
import pl.ynfuien.ygenerators.core.generator.GeneratorItem;
import pl.ynfuien.ygenerators.core.generator.GeneratorRecipe;

import java.util.Arrays;
import java.util.HashMap;

public class PrepareItemCraftListener implements Listener {
    // This listener handles:
    // - crafting generators
    // - combining generators to repair them

    private final YGenerators instance;
    private final Generators generators;

    public PrepareItemCraftListener(YGenerators instance) {
        this.instance = instance;
        generators = instance.getGenerators();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.isRepair()) return;

        CraftingInventory crafting = event.getInventory();
        if (crafting.isEmpty()) return;

        Player player = (Player) event.getView().getPlayer();

        // Generator repair
        boolean success = handleGeneratorRepair(crafting, player);
        if (success) return;


        Recipe recipe = event.getRecipe();
        if (recipe == null) return;

        // Check for generators that can't be used as ingredients
        NamespacedKey namespacedKey = ((Keyed) recipe).getKey();
        if (!namespacedKey.getNamespace().equalsIgnoreCase(instance.getName())) {
            preventUsingGeneratorInCrafting(crafting);
            return;
        }

        // Generator crafting
        String generatorName = namespacedKey.getKey();

        Generator generator = generators.get(generatorName);
        if (generator == null) {
            preventUsingGeneratorInCrafting(crafting);
            return;
        }

        GeneratorRecipe generatorRecipe = generator.getRecipe();
        if (generatorRecipe == null) return;

        ItemStack[] matrix = crafting.getMatrix().clone();
        HashMap<Character, String> ingredients = generatorRecipe.getIngredients();

        double durability = generator.getDurability();

        // Whether crafted generator durability should be reduced by used durability of ingredient generators
        if (generatorRecipe.getReduceDurabilityByAlreadyUsed()) {
            double usedDurability = 0;

            for (String value : ingredients.values()) {
                if (!value.startsWith("generator:")) continue;

                // Get generator name
                String name = value.substring(10);

                boolean found = false;
                for (int i = 0; i < matrix.length; i++) {
                    ItemStack item = matrix[i];
                    if (item == null) continue;

                    // Get generator name from item
                    PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                    String ingredientGeneratorName = pdc.get(GeneratorItem.NSKey.GENERATOR, PersistentDataType.STRING);

                    if (ingredientGeneratorName == null) continue;
                    if (!name.equals(ingredientGeneratorName)) continue;

                    matrix[i] = null;
                    found = true;

                    Double ingredientGeneDurability = pdc.get(GeneratorItem.NSKey.DURABILITY, PersistentDataType.DOUBLE);
                    if (ingredientGeneDurability == null) continue;

                    Generator ingredientGenerator = generators.get(ingredientGeneratorName);
                    if (ingredientGenerator == null) continue;

                    usedDurability += ingredientGenerator.getDurability() - ingredientGeneDurability;
                    break;
                }

                if (!found) {
                    crafting.setResult(null);
                    return;
                }
            }

            // Set durability to (crafted generator full durability - ingredient generators used durability)
            // if used durability isn't higher than crafted generator full durability
            if (usedDurability < durability) durability = durability - usedDurability;
        }

        // Set crafting result to generator item's item stack
        crafting.setResult(generator.getItem().getItemStack(player, durability));
    }

    private boolean handleGeneratorRepair(CraftingInventory crafting, Player player) {
        ItemStack[] ingredients = Arrays.stream(crafting.getMatrix()).filter(item -> item != null).toArray(ItemStack[]::new);
        if (ingredients.length != 2) return false;

        // First item
        ItemStack first = ingredients[0];
        PersistentDataContainer firstPdc = first.getItemMeta().getPersistentDataContainer();
        String firstGene = firstPdc.get(GeneratorItem.NSKey.GENERATOR, PersistentDataType.STRING);
        if (firstGene == null) return false;

        // Second item
        ItemStack second = ingredients[1];
        PersistentDataContainer secondPdc = second.getItemMeta().getPersistentDataContainer();
        String secondGene = secondPdc.get(GeneratorItem.NSKey.GENERATOR, PersistentDataType.STRING);
        if (secondGene == null) return false;

        // Return if first and second generators aren't the same
        if (!firstGene.equals(secondGene)) return false;

        Generator generator = generators.get(firstGene);
        if (generator == null) return false;
        if (!generator.getCraftingRepair()) return false;

        Double firstDurability = firstPdc.get(GeneratorItem.NSKey.DURABILITY, PersistentDataType.DOUBLE);
        if (firstDurability == null) return false;
        Double secondDurability = secondPdc.get(GeneratorItem.NSKey.DURABILITY, PersistentDataType.DOUBLE);
        if (secondDurability == null) return false;

        double totalDurability = firstDurability + secondDurability;
        double durability = Math.min(totalDurability, generator.getDurability());

        ItemStack resultGenerator = generator.getItem().getItemStack(player, durability);

        crafting.setResult(resultGenerator);
        return true;
    }

    private void preventUsingGeneratorInCrafting(CraftingInventory crafting) {
        ItemStack[] matrix = crafting.getMatrix();

        for (ItemStack item : matrix) {
            if (item == null) continue;

            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            String generatorName = pdc.get(GeneratorItem.NSKey.GENERATOR, PersistentDataType.STRING);
            if (generatorName == null) continue;

            Generator generator = generators.get(generatorName);
            if (generator == null) continue;

            if (!generator.getItem().canBeUsedInCrafting()) {
                crafting.setResult(null);
                return;
            }
        }
    }
}
