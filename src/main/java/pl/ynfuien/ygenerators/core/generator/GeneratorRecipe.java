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
import pl.ynfuien.ygenerators.YGenerators;
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

    private HashMap<Character, String> ingredients = new HashMap<>();


    public GeneratorRecipe(Generator generator) {
        this.generator = generator;
    }

    public boolean loadFromConfigSection(ConfigurationSection config) {
        // Return if recipe doesn't contain ingredients key
        if (!config.contains("ingredients")) {
            logError("Recipe doesn't contain ingredients key!");
            return false;
        }

        // Get whether recipe have to be shaped
        if (config.contains("shaped")) {
            shaped = config.getBoolean("shaped");
        }

        // Reduce durability by already used
        if (config.contains("reduce-durability-by-already-used")) {
            reduceDurabilityByAlreadyUsed = config.getBoolean("reduce-durability-by-already-used");
        }

        // If shaped is true
        if (shaped) {
            // Return if recipe doesn't have shape
            if (!config.contains("shape")) {
                logError("Recipe doesn't contain shape key!");
                return false;
            }

            // Get shape list
            List<String> shapeList = config.getStringList("shape");
            // Return if shape has less than 3 rows.
            // If has more than 3, then only first 3 will be taken anyway
            if (shapeList.size() < 3) {
                logError("Shape must have 3 rows!");
                return false;
            }

            // Get all three rows and return if row is longer than 3 chars

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
            ConfigurationSection ingredientsConfigSection = config.getConfigurationSection("ingredients");

            // Loop through ingredients
            for (String ingredientChar : ingredientsConfigSection.getKeys(false)) {
                // Return if ingredient char is longer than 1 char
                if (ingredientChar.length() > 1) {
                    logError(String.format("[Ingredient-%s] Ingredient char can't be longer than 1 char!", ingredientChar));
                    return false;
                }

                // Get ingredient under ingredient char
                String ingredient = ingredientsConfigSection.getString(ingredientChar);

                // Set ingredient in lower case
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
                ingredients.put(ingredientChar.toCharArray()[0], ingredient);
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

        // Get ingredients list
        List<String> ingredientsList = config.getStringList("ingredients");

        int i = 0;
        // Loop through list
        for (String ingredient : ingredientsList) {
            // Set ingredient in lower case
            ingredient = ingredient.toLowerCase();

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
            i++;
        }

        return true;
    }

    public boolean registerRecipe(Generators generators) {
        // Get generator item
        GeneratorItem item = generator.getItem();
        // Create recipe result item
        ItemStack recipeResultItem = item.getItemStack();
        // Create namespaced key
        NamespacedKey namespacedKey = new NamespacedKey(YGenerators.getInstance(), generator.getName());

        // If recipe is shaped
        if (shaped) {
            // Create shaped recipe
            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, recipeResultItem);

            // Set shape of recipe
            shapedRecipe.shape(
                    firstRow,
                    secondRow,
                    thirdRow
            );

            // Loop through ingredients
            for (Character character : ingredients.keySet()) {
                // Get ingredient under char
                String ingredient = ingredients.get(character);

                Material material;
                // If ingredient is generator item
                if (ingredient.startsWith("generator:")) {
                    // Get ingredient generator name
                    String ingredientGeneName = ingredient.substring(10);
                    // Skip recipe if provided generator doesn't exist
                    if (!generators.has(ingredientGeneName)) {
                        logError(String.format("Generator '%s' provided in ingredient for character '%s' doesn't exist!", ingredientGeneName, character));
                        return false;
                    }

                    // Set material to generator item material
                    material = generators.get(ingredientGeneName).getItem().material();
                } else {
                    material = Material.valueOf(ingredient);
                }

                // Set shaped recipe ingredient
                shapedRecipe.setIngredient(character, material);
            }

            // Adding recipe
            Bukkit.addRecipe(shapedRecipe);
            return true;
        }


        // If recipe isn't shaped

        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(namespacedKey, recipeResultItem);

        // Loop through ingredients
        for (String ingredient : ingredients.values()) {
            Material material;
            // If ingredient is generator item
            if (ingredient.startsWith("generator:")) {
                // Get ingredient generator name
                String ingredientGeneName = ingredient.substring(10);
                // Skip recipe if provided generator doesn't exist
                if (!generators.has(ingredientGeneName)) {
                    logError(String.format("Generator '%s' provided in ingredient doesn't exist!", ingredientGeneName));
                    return false;
                }

                // Set material to generator item material
                material = generators.get(ingredientGeneName).getItem().material();
            } else {
                material = Material.valueOf(ingredient);
            }

            // Set shaped recipe ingredient
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
