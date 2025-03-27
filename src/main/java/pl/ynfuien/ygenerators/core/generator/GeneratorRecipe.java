package pl.ynfuien.ygenerators.core.generator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ygenerators.core.Generators;

import java.util.HashMap;
import java.util.List;

public class GeneratorRecipe {
    private final Generator generator;

    private boolean shaped = true;
    private boolean reduceDurabilityByAlreadyUsed = true;

    private String firstRow = null;
    private String secondRow = null;
    private String thirdRow = null;

    private final HashMap<Character, String> ingredients = new HashMap<>();


    public GeneratorRecipe(Generator generator) {
        this.generator = generator;
    }

    /**
     * Internal method for loading this class.
     */
    public boolean load(ConfigurationSection config) {
        if (!config.contains("ingredients")) {
            logError("Recipe doesn't contain ingredients key!");
            return false;
        }

        if (config.contains("shaped")) shaped = config.getBoolean("shaped");

        if (config.contains("reduce-durability-by-already-used")) {
            reduceDurabilityByAlreadyUsed = config.getBoolean("reduce-durability-by-already-used");
        }

        // If shaped is true
        if (shaped) {
            if (!config.contains("shape")) {
                logError("Recipe doesn't contain shape key!");
                return false;
            }

            List<String> shapeList = config.getStringList("shape");
            if (shapeList.size() < 3) {
                logError("Shape must have 3 rows!");
                return false;
            }

            // First row
            firstRow = shapeList.get(0);
            if (firstRow.length() > 3) {
                logError("[First-Row] Row can't be longer than 3 chars!");
                return false;
            }

            // Second row
            secondRow = shapeList.get(1);
            if (secondRow.length() > 3) {
                logError("[Second-Row] Row can't be longer than 3 chars!");
                return false;
            }

            // Third row
            thirdRow = shapeList.get(2);
            if (thirdRow.length() > 3) {
                logError("[Third-Row] Row can't be longer than 3 chars!");
                return false;
            }


            // Get ingredients config section
            ConfigurationSection ingredientsConfig = config.getConfigurationSection("ingredients");

            // Loop through ingredients
            for (String ingredientChar : ingredientsConfig.getKeys(false)) {
                // Return if ingredient char is longer than 1 char
                if (ingredientChar.length() > 1) {
                    logError(String.format("[Ingredient-%s] Ingredient char can't be longer than 1 char!", ingredientChar));
                    return false;
                }

                String ingredient = ingredientsConfig.getString(ingredientChar);
                ingredient = ingredient.toLowerCase();

                // If ingredient value isn't generator item
                if (!ingredient.startsWith("generator:")) {
                    // Get material from ingredient
                    Material material = Material.matchMaterial(ingredient);

                    // If material is incorrect
                    if (material == null) {
                        logError(String.format("[Ingredient-%s] '%s' is incorrect!", ingredientChar, ingredient));
                        return false;
                    }

                    // Return if ingredient isn't obtainable item
                    if (!material.isItem()) {
                        logError(String.format("[Ingredient-%s] '%s' isn't obtainable item!", ingredientChar, ingredient));
                        return false;
                    }

                    // Set ingredient to material name
                    ingredient = material.name();
                }


                // Put ingredient in ingredients hashmap
                ingredients.put(ingredientChar.charAt(0), ingredient);
            }

            // Get all shape chars in one string
            String allShapeChars = new StringBuilder(firstRow).append(secondRow).append(thirdRow).toString();
            int shapeChars = 0;
            // Loop through all shape chars
            for (Character character : allShapeChars.toCharArray()) {
                // Return if ingredients doesn't contain shape char
                if (!ingredients.containsKey(character)) {
                    logError(String.format("Shape character '%s' doesn't have assigned ingredient!", character));
                    return false;
                }
                shapeChars++;
            }

            // Return if shape doesn't have any chars
            if (shapeChars == 0) {
                logError("Shape doesn't have any chars!");
                return false;
            }

            return true;
        }

        // If shaped isn't true

        List<String> ingredientsList = config.getStringList("ingredients");

        for (int i = 0; i < ingredientsList.size(); i++) {
            String ingredient = ingredientsList.get(i).toLowerCase();

            // If ingredient isn't generator item
            if (!ingredient.startsWith("generator:")) {
                // Get material from ingredient
                Material material = Material.matchMaterial(ingredient);

                // If material is incorrect
                if (material == null) {
                    logError(String.format("Ingredient '%s' is incorrect!", ingredient));
                    return false;
                }

                // Return if ingredient isn't obtainable item
                if (!material.isItem()) {
                    logError(String.format("Ingredient '%s' isn't obtainable item!", ingredient));
                    return false;
                }

                // Set ingredient to material name
                ingredient = material.name();
            }

            // Put ingredient in ingredients hashmap
            ingredients.put((char) i, ingredient);
        }

        return true;
    }

    public boolean registerRecipe() {
        Generators generators = generator.getGenerators();

        GeneratorItem item = generator.getItem();
        ItemStack recipeResultItem = item.getItemStack();
        NamespacedKey namespacedKey = new NamespacedKey(generators.getInstance(), generator.getName());

        if (shaped) {
            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, recipeResultItem);
            shapedRecipe.shape(
                    firstRow,
                    secondRow,
                    thirdRow
            );

            for (Character character : ingredients.keySet()) {
                String ingredient = ingredients.get(character);

                Material material = Material.matchMaterial(ingredient);
                // If ingredient is generator item
                if (ingredient.startsWith("generator:")) {
                    String ingredientGeneName = ingredient.substring(10);
                    if (!generators.has(ingredientGeneName)) {
                        logError(String.format("Generator '%s' provided in ingredient for character '%s' doesn't exist!", ingredientGeneName, character));
                        return false;
                    }

                    material = generators.get(ingredientGeneName).getItem().getMaterial();
                }

                shapedRecipe.setIngredient(character, material);
            }

            // Adding recipe
            Bukkit.addRecipe(shapedRecipe);
            return true;
        }


        // If recipe isn't shaped

        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(namespacedKey, recipeResultItem);

        for (String ingredient : ingredients.values()) {
            Material material = Material.matchMaterial(ingredient);
            // If ingredient is generator item
            if (ingredient.startsWith("generator:")) {
                String ingredientGeneName = ingredient.substring(10);
                if (!generators.has(ingredientGeneName)) {
                    logError(String.format("Generator '%s' provided in ingredient doesn't exist!", ingredientGeneName));
                    return false;
                }

                material = generators.get(ingredientGeneName).getItem().getMaterial();
            }

            shapelessRecipe.addIngredient(material);
        }

        // Add recipe
        Bukkit.addRecipe(shapelessRecipe);
        return true;
    }

    private void logError(String message) {
        YLogger.warn(String.format("[GeneratorRecipe-%s] %s", generator.getName(), message));
    }

    // Gets generator
    @NotNull
    public Generator getGenerator() {
        return generator;
    }

    // Gets shaped
    public boolean isShaped() {
        return shaped;
    }

    // Gets whether reduce durability by already used
    public boolean getReduceDurabilityByAlreadyUsed() {
        return reduceDurabilityByAlreadyUsed;
    }

    // Gets rows
    @Nullable
    public String getFirstRow() {
        return firstRow;
    }
    @Nullable
    public String getSecondRow() {
        return secondRow;
    }
    @Nullable
    public String getThirdRow() {
        return thirdRow;
    }

    // Gets ingredients
    @NotNull
    public HashMap<Character, String> getIngredients() {
        return ingredients;
    }
}
