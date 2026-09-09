package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Обходчик рецептов GregTech.
 * Реализует итеративный алгоритм обхода рецептов для расчёта EMC.
 * 
 * Алгоритм:
 * 1. Берём рецепты, где ВСЕ ингредиенты уже имеют EMC
 * 2. Рассчитываем EMC для выхода
 * 3. Повторяем, пока есть новые назначения или не достигнут лимит
 * 
 * Защита от зацикливания:
 * - Ограничение по количеству итераций (configurable)
 * - Множество уже обработанных рецептов
 * - Игнорирование chanced outputs с шансом < 100%
 */
public class RecipeWalker {

    private final Set<String> processedRecipes = new HashSet<>();
    private final Set<String> circularDependencyTracker = new HashSet<>();
    private int totalProcessed = 0;
    private int totalAssigned = 0;

    /**
     * Запуск полного обхода рецептов.
     * 
     * @param mapper ProjectE маппер
     * @param itemEMC текущая карта EMC предметов
     * @param fluidEMC текущая карта EMC жидкостей
     * @return количество новых назначенных EMC
     */
    public <T extends NormalizedSimpleStack<T, ?>> int walkAllRecipes(
            IMappingCollector<T, Long> mapper,
            Map<ResourceLocation, Long> itemEMC,
            Map<ResourceLocation, Long> fluidEMC) {
        
        int maxIterations = GTEMCConfig.MAX_ITERATIONS.get();
        int totalNew = 0;
        
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            int newThisIteration = walkOnce(mapper, itemEMC, fluidEMC);
            totalNew += newThisIteration;
            
            if (GTEMCConfig.DEBUG_LOGGING.get()) {
                GTEMCAddon.LOGGER.info("[RecipeWalker] Итерация {}/{}: +{} EMC (всего обработано: {})",
                        iteration + 1, maxIterations, newThisIteration, totalProcessed);
            }
            
            // Стабилизация - ничего нового не добавлено
            if (newThisIteration == 0) {
                GTEMCAddon.LOGGER.info("[RecipeWalker] Стабилизация на итерации {}. " +
                        "Всего обработано рецептов: {}, назначено EMC: {}",
                        iteration + 1, totalProcessed, totalAssigned);
                break;
            }
        }
        
        return totalNew;
    }

    /**
     * Один проход по всем рецептам.
     */
    private <T extends NormalizedSimpleStack<T, ?>> int walkOnce(
            IMappingCollector<T, Long> mapper,
            Map<ResourceLocation, Long> itemEMC,
            Map<ResourceLocation, Long> fluidEMC) {
        
        int newMappings = 0;
        
        for (GTRecipeType recipeType : getAllRecipeTypes()) {
            Collection<GTRecipe> recipes = recipeType.getRecipes();
            if (recipes == null) continue;
            
            for (GTRecipe recipe : recipes) {
                String recipeId = recipeType.getRegistryName() + ":" + recipe.getId();
                
                if (processedRecipes.contains(recipeId)) continue;
                
                // Проверяем на циклическую зависимость
                if (isCircularRecipe(recipe, itemEMC)) {
                    processedRecipes.add(recipeId);
                    continue;
                }
                
                processedRecipes.add(recipeId);
                totalProcessed++;
                
                // Рассчитываем EMC входов
                long inputEMC = calculateInputEMC(recipe, itemEMC, fluidEMC);
                if (inputEMC < 0) continue; // Не все ингредиенты имеют EMC
                
                // Обрабатываем предметные выходы
                newMappings += processItemOutputs(recipe, inputEMC, itemEMC);
                
                // Обрабатываем жидкостные выходы
                if (GTEMCConfig.CALCULATE_FLUIDS.get()) {
                    newMappings += processFluidOutputs(recipe, inputEMC, fluidEMC);
                }
            }
        }
        
        totalAssigned += newMappings;
        return newMappings;
    }

    /**
     * Расчёт EMC всех входов рецепта.
     * Возвращает -1 если хоть один ингредиент не имеет EMC.
     */
    private long calculateInputEMC(GTRecipe recipe, 
                                    Map<ResourceLocation, Long> itemEMC,
                                    Map<ResourceLocation, Long> fluidEMC) {
        long totalEMC = 0;
        
        // Предметные входы
        List<Content> itemInputs = recipe.getInputs();
        if (itemInputs != null) {
            for (Content input : itemInputs) {
                Object content = input.getContent();
                if (content instanceof ItemStack stack) {
                    ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (rl == null) return -1;
                    
                    Long emc = itemEMC.get(rl);
                    if (emc == null) return -1;
                    
                    totalEMC += emc * stack.getCount();
                }
            }
        }
        
        // Жидкостные входы
        List<Content> fluidInputs = recipe.getFluidInputs();
        if (fluidInputs != null) {
            for (Content fluidInput : fluidInputs) {
                Object content = fluidInput.getContent();
                if (content instanceof FluidStack fluidStack) {
                    ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
                    if (fluidRL == null) return -1;
                    
                    Long emcPerBucket = fluidEMC.get(fluidRL);
                    if (emcPerBucket == null) return -1;
                    
                    // Пропорционально: EMC = (emcPerBucket * amount) / 1000
                    int amount = fluidStack.getAmount();
                    totalEMC += (emcPerBucket * amount) / 1000L;
                }
            }
        }
        
        return totalEMC;
    }

    /**
     * Обработка предметных выходов рецепта.
     */
    private int processItemOutputs(GTRecipe recipe, long inputEMC, 
                                    Map<ResourceLocation, Long> itemEMC) {
        int newMappings = 0;
        
        List<ItemStack> outputs = getValidOutputs(recipe);
        if (outputs.isEmpty()) return 0;
        
        // EMC за единицу = общий вход / количество выходов
        long emcPerUnit = inputEMC / outputs.size();
        if (emcPerUnit <= 0) return 0;
        
        for (ItemStack output : outputs) {
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(output.getItem());
            if (rl == null) continue;
            
            // Пропускаем ванильные предметы (они уже имеют EMC)
            if (rl.getNamespace().equals("minecraft")) continue;
            
            if (!itemEMC.containsKey(rl)) {
                itemEMC.put(rl, emcPerUnit);
                newMappings++;
                
                if (GTEMCConfig.DEBUG_LOGGING.get()) {
                    GTEMCAddon.LOGGER.debug("[RecipeWalker] EMC {} -> {} (из рецепта {})",
                            emcPerUnit, rl, recipe.getId());
                }
            }
        }
        
        return newMappings;
    }

    /**
     * Обработка жидкостных выходов рецепта.
     */
    private int processFluidOutputs(GTRecipe recipe, long inputEMC,
                                     Map<ResourceLocation, Long> fluidEMC) {
        int newMappings = 0;
        
        List<FluidStack> fluidOutputs = getFluidOutputs(recipe);
        if (fluidOutputs.isEmpty()) return 0;
        
        for (FluidStack fluidOutput : fluidOutputs) {
            ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidOutput.getFluid());
            if (fluidRL == null) continue;
            
            int amount = fluidOutput.getAmount(); // mB
            long emcPerBucket = (inputEMC * 1000L) / amount;
            
            if (emcPerBucket <= 0) continue;
            
            if (!fluidEMC.containsKey(fluidRL)) {
                fluidEMC.put(fluidRL, emcPerBucket);
                newMappings++;
                
                if (GTEMCConfig.DEBUG_LOGGING.get()) {
                    GTEMCAddon.LOGGER.debug("[RecipeWalker] Fluid EMC {} за 1000mB -> {} (из рецепта {})",
                            emcPerBucket, fluidRL, recipe.getId());
                }
            }
        }
        
        return newMappings;
    }

    /**
     * Получение валидных выходов рецепта (игнорирует chanced outputs с низким шансом).
     */
    private List<ItemStack> getValidOutputs(GTRecipe recipe) {
        List<ItemStack> outputs = new ArrayList<>();
        
        // Основные выходы (всегда 100% шанс)
        List<Content> mainOutputs = recipe.getOutputs();
        if (mainOutputs != null) {
            for (Content output : mainOutputs) {
                if (output.getContent() instanceof ItemStack stack) {
                    outputs.add(stack.copy());
                }
            }
        }
        
        // Chanced outputs - проверяем порог
        int minChance = GTEMCConfig.MIN_CHANCE_THRESHOLD.get();
        if (minChance < 100) {
            List<Content> chanced = recipe.getChancedOutputs();
            if (chanced != null) {
                for (Content chancedOutput : chanced) {
                    // GT использует формат: шанс от 0 до 10000 (10000 = 100%)
                    int chance = chancedOutput.getChance();
                    int threshold = minChance * 100;
                    
                    if (chance >= threshold) {
                        if (chancedOutput.getContent() instanceof ItemStack stack) {
                            outputs.add(stack.copy());
                        }
                    }
                }
            }
        }
        
        return outputs;
    }

    /**
     * Получение жидкостных выходов.
     */
    private List<FluidStack> getFluidOutputs(GTRecipe recipe) {
        List<FluidStack> outputs = new ArrayList<>();
        
        List<Content> fluidOutputs = recipe.getFluidOutputs();
        if (fluidOutputs != null) {
            for (Content output : fluidOutputs) {
                if (output.getContent() instanceof FluidStack fluidStack) {
                    outputs.add(fluidStack.copy());
                }
            }
        }
        
        return outputs;
    }

    /**
     * Проверка на циклическую зависимость.
     * Если вход и выход рецепта - один и тот же предмет, это цикл.
     */
    private boolean isCircularRecipe(GTRecipe recipe, Map<ResourceLocation, Long> itemEMC) {
        Set<ResourceLocation> inputs = new HashSet<>();
        Set<ResourceLocation> outputs = new HashSet<>();
        
        List<Content> recipeInputs = recipe.getInputs();
        if (recipeInputs != null) {
            for (Content input : recipeInputs) {
                if (input.getContent() instanceof ItemStack stack) {
                    ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (rl != null) inputs.add(rl);
                }
            }
        }
        
        List<Content> recipeOutputs = recipe.getOutputs();
        if (recipeOutputs != null) {
            for (Content output : recipeOutputs) {
                if (output.getContent() instanceof ItemStack stack) {
                    ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (rl != null) outputs.add(rl);
                }
            }
        }
        
        // Если все выходы уже есть во входах - это цикл
        if (!outputs.isEmpty() && inputs.containsAll(outputs)) {
            return true;
        }
        
        return false;
    }

    /**
     * Получение всех типов рецептов GT для обхода.
     */
    private List<GTRecipeType> getAllRecipeTypes() {
        List<GTRecipeType> types = new ArrayList<>();
        
        // Все основные типы машин
        types.add(GTRecipeTypes.FURNACE_RECIPES);
        types.add(GTRecipeTypes.ALLOY_SMELTER_RECIPES);
        types.add(GTRecipeTypes.ASSEMBLER_RECIPES);
        types.add(GTRecipeTypes.BENDER_RECIPES);
        types.add(GTRecipeTypes.WIREMILL_RECIPES);
        types.add(GTRecipeTypes.CHEMICAL_RECIPES);
        types.add(GTRecipeTypes.MIXER_RECIPES);
        types.add(GTRecipeTypes.BLAST_RECIPES);
        types.add(GTRecipeTypes.CENTRIFUGE_RECIPES);
        types.add(GTRecipeTypes.ELECTROLYZER_RECIPES);
        types.add(GTRecipeTypes.CHEMICAL_BATH_RECIPES);
        types.add(GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES);
        types.add(GTRecipeTypes.FLUID_HEATER_RECIPES);
        types.add(GTRecipeTypes.DISTILLATION_RECIPES);
        types.add(GTRecipeTypes.FERMENTING_RECIPES);
        types.add(GTRecipeTypes.LASER_ENGRAVER_RECIPES);
        types.add(GTRecipeTypes.AUTOCLAVE_RECIPES);
        types.add(GTRecipeTypes.COMPRESSOR_RECIPES);
        types.add(GTRecipeTypes.EXTRACTOR_RECIPES);
        types.add(GTRecipeTypes.CUTTER_RECIPES);
        types.add(GTRecipeTypes.LATHE_RECIPES);
        types.add(GTRecipeTypes.MACERATOR_RECIPES);
        types.add(GTRecipeTypes.FORGE_HAMMER_RECIPES);
        types.add(GTRecipeTypes.ARC_FURNACE_RECIPES);
        types.add(GTRecipeTypes.PACKER_RECIPES);
        types.add(GTRecipeTypes.UNPACKER_RECIPES);
        types.add(GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES);
        types.add(GTRecipeTypes.FORMING_PRESS_RECIPES);
        types.add(GTRecipeTypes.SIFTER_RECIPES);
        types.add(GTRecipeTypes.POLARIZER_RECIPES);
        types.add(GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES);
        
        return types;
    }

    /**
     * Получение статистики обходчика.
     */
    public int getTotalProcessed() {
        return totalProcessed;
    }

    public int getTotalAssigned() {
        return totalAssigned;
    }
}
