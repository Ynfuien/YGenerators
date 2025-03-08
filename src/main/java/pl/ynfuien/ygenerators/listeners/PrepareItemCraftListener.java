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
import org.bukkit.inventory.meta.ItemMeta;
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
    // - combine generators to repair these

    private final YGenerators instance;
    private final Generators generators;
    public PrepareItemCraftListener(YGenerators instance) {
        this.instance = instance;
        generators = instance.getGenerators();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPrepareItemCraft(PrepareItemCraftEvent e) {
        // Return if event fired on tool repair
        if (e.isRepair()) return;

        // Get crafting
        CraftingInventory crafting = e.getInventory();

        // Return if crafting is empty
        if (crafting.isEmpty()) return;

        // Get player
        Player p = (Player) e.getView().getPlayer();

        // Check for crafting repair of generator
        boolean isRepair = checkGeneratorsRepair(crafting, p);
        // Return if crafting is repair generator
        if (isRepair) return;



        // Get recipe
        Recipe recipe = e.getRecipe();

        // Return if recipe doesn't exist
        if (recipe == null) return;

        // Get recipe namespaced key
        NamespacedKey namespacedKey = ((Keyed) recipe).getKey();

        // Return if namespace of key isn't from this plugin
        if (!namespacedKey.getNamespace().equalsIgnoreCase(instance.getName())) {
            if (preventGeneratorUsageInCrafting(crafting)) crafting.setResult(null);
            return;
        }

        // Get generator name
        String geneName = namespacedKey.getKey();

        // Return if generator with that name doesn't exist
        if (!generators.has(geneName)) {
            if (preventGeneratorUsageInCrafting(crafting)) crafting.setResult(null);
            return;
        }

        // Get generator
        Generator gene = generators.get(geneName);

        // Get generator recipe
        GeneratorRecipe geneRecipe = gene.getRecipe();

        // Return if generator doesn't have recipe
        if (geneRecipe == null) return;

        // Get crafting matrix
        ItemStack[] matrix = crafting.getMatrix().clone();

        // Get ingredients
        HashMap<Character, String> ingredients = geneRecipe.getIngredients();

        // Set crafted generator durability
        double durability = gene.getDurability();

        // Whether crafted generator durability should be reduced by used durability of ingredient generators
        if (geneRecipe.getReduceDurabilityByAlreadyUsed()) {
            double usedDurability = 0;
//            double fullDurability = 0;

            // Loop through generator recipe ingredients
            for (String value : ingredients.values()) {
                // Skip ingredient if it isn't generator
                if (!value.startsWith("generator:")) continue;

                // Get generator name
                String name = value.substring(10);

                // Loop through crafting items and search for generator
                boolean found = false;
                for (int i = 0; i < matrix.length; i++) {
                    ItemStack item = matrix[i];
                    if (item == null) continue;

                    // Get generator name from item
                    ItemMeta meta = item.getItemMeta();
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    String itemGeneName = pdc.get(GeneratorItem.NSKey.GENERATOR, PersistentDataType.STRING);

                    // Skip item if it isn't generator
                    if (itemGeneName == null) continue;

                    // Skip item if it's generator name isn't needed name
                    if (!name.equals(itemGeneName)) continue;

                    // Set crafting item in matrix to null
                    matrix[i] = null;
                    // Set fount o true
                    found = true;
                    // Get durability from generator
                    Double ingredientGeneDurability = pdc.get(GeneratorItem.NSKey.DURABILITY, PersistentDataType.DOUBLE);
                    if (ingredientGeneDurability == null) continue;

                    // Get ingredient generator
                    Generator ingredientGene = generators.get(itemGeneName);
                    if (ingredientGene == null) continue;

                    // Increase used durability by this generator used durability
                    usedDurability += ingredientGene.getDurability() - ingredientGeneDurability;
                    break;
                }

                // Clear crafting result and return if ingredient generator wasn't found
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
        crafting.setResult(gene.getItem().getItemStack(p, durability));
    }

    private boolean checkGeneratorsRepair(CraftingInventory crafting, Player p) {
        // Get crafting matrix
        ItemStack[] matrixItems = Arrays.stream(crafting.getMatrix()).filter(item -> item != null).toArray(ItemStack[]::new);
        // Combine
        // Return if in crafting aren't two items
        if (matrixItems.length != 2) return false;

        // Get first item
        ItemStack first = matrixItems[0];
        PersistentDataContainer firstPdc = first.getItemMeta().getPersistentDataContainer();
        String firstGene = firstPdc.get(GeneratorItem.NSKey.GENERATOR, PersistentDataType.STRING);
        // Return if item doesn't have generator nbt tag
        if (firstGene == null) return false;

        // Get second item
        ItemStack second = matrixItems[1];
        PersistentDataContainer secondPdc = second.getItemMeta().getPersistentDataContainer();
        String secondGene = secondPdc.get(GeneratorItem.NSKey.GENERATOR, PersistentDataType.STRING);
        // Return if item isn't generator
        if (secondGene == null) return false;

        // Return if first and second generators aren't the same
        if (!firstGene.equals(secondGene)) return false;

        // Return if generator with that name doesn't exist
        if (!generators.has(firstGene)) return false;

        // Get generator
        Generator gene = generators.get(firstGene);

        // Return if combining generators to repair is disabled for this generator
        if (!gene.getCraftingRepair()) return false;

        Double firstDurability = firstPdc.get(GeneratorItem.NSKey.DURABILITY, PersistentDataType.DOUBLE);
        if (firstDurability == null) return false;
        Double secondDurability = secondPdc.get(GeneratorItem.NSKey.DURABILITY, PersistentDataType.DOUBLE);
        if (secondDurability == null) return false;

        // Get total durability from both generators
        double total = firstDurability + secondDurability;

        // Set durability of result generator
        double durability = Math.min(total, gene.getDurability());

        // Get result generator item stack
        ItemStack resultGenerator = gene.getItem().getItemStack(p, durability);

        // Set result of crafting
        crafting.setResult(resultGenerator);

        return true;
    }

    private boolean preventGeneratorUsageInCrafting(CraftingInventory crafting) {
        // Get matrix
        ItemStack[] matrix = crafting.getMatrix();

        // Loop through items in crafting
        for (ItemStack item : matrix) {
            // Continue if item is null
            if (item == null) continue;

            // Get generator name from item
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            String geneName = pdc.get(GeneratorItem.NSKey.GENERATOR, PersistentDataType.STRING);

            // Continue if item isn't generator
            if (geneName == null) continue;

            // Continue if generator with that name doesn't exist
            if (!generators.has(geneName)) continue;

            // Get generator
            Generator generator = generators.get(geneName);

            // Prevent crafting if generator can't be used to craft vanilla items
            if (!generator.getItem().canBeUsedInCrafting()) return true;
        }

        return false;
    }
}
