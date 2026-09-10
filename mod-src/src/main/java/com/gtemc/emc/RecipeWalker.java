package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
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
 */
public class RecipeWalker {

    private final Set<String> processedRecipes = new HashSet<>();
    private final Set<String> circularDependencyTracker = new HashSet<>();
    private int totalProcessed = 0;
    private int totalAssigned = 0;

    /**
     * Запуск полного обхода рецептов.
     */
    public int walkAllRecipes(
            IMappingCollector<NormalizedSimpleStack, Long> mapper,
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
    private int walkOnce(
            IMappingCollector<NormalizedSimpleStack, Long> mapper,
            Map<ResourceLocation, Long> itemEMC,
            Map<ResourceLocation, Long> fluidEMC) {
        
        int newMappings = 0;
        
        for (var recipeType : getAllRecipeTypes()) {
            var recipes = recipeType.getRecipes();
            if (recipes == null) continue;
            
            for (var recipe : recipes) {
                String recipeId = recipeType.getRegistryName() + ":" + recipe.getId();
                
                if (processedRecipes.contains(recipeId)) continue;
                
                if (isCircularRecipe(recipe, itemEMC)) {
                    processedRecipes.add(recipeId);
                    continue;
                }
                
                processedRecipes.add(recipeId);
                totalProcessed++;
                
                long inputEMC = calculateInputEMC(recipe, itemEMC, fluidEMC);
                if (inputEMC < 0) continue;
                
                newMappings += processItemOutputs(recipe, inputEMC, itemEMC);
                
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
     */
    private long calculateInputEMC(Object recipe, 
                                    Map<ResourceLocation, Long> itemEMC,
                                    Map<ResourceLocation, Long> fluidEMC) {
        long totalEMC = 0;
        
        var gtRecipe = (com.gregtechceu.gtceu.api.recipe.GTRecipe) recipe;
        
        var itemInputs = gtRecipe.getInputs();
        if (itemInputs != null) {
            for (var input : itemInputs) {
                if (input.getContent() instanceof ItemStack stack) {
                    ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (rl == null) return -1;
                    
                    Long emc = itemEMC.get(rl);
                    if (emc == null) return -1;
                    
                    totalEMC += emc * stack.getCount();
                }
            }
        }
        
        var fluidInputs = gtRecipe.getFluidInputs();
        if (fluidInputs != null) {
            for (var fluidInput : fluidInputs) {
                if (fluidInput.getContent() instanceof FluidStack fluidStack) {
                    ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
                    if (fluidRL == null) return -1;
                    
                    Long emcPerBucket = fluidEMC.get(fluidRL);
                    if (emcPerBucket == null) return -1;
                    
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
    private int processItemOutputs(Object recipe, long inputEMC, 
                                    Map<ResourceLocation, Long> itemEMC) {
        int newMappings = 0;
        
        var gtRecipe = (com.gregtechceu.gtceu.api.recipe.GTRecipe) recipe;
        List<ItemStack> outputs = getValidOutputs(gtRecipe);
        if (outputs.isEmpty()) return 0;
        
        long emcPerUnit = inputEMC / outputs.size();
        if (emcPerUnit <= 0) return 0;
        
        for (ItemStack output : outputs) {
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(output.getItem());
            if (rl == null) continue;
            
            if (rl.getNamespace().equals("minecraft")) continue;
            
            if (!itemEMC.containsKey(rl)) {
                itemEMC.put(rl, emcPerUnit);
                newMappings++;
                
                if (GTEMCConfig.DEBUG_LOGGING.get()) {
                    GTEMCAddon.LOGGER.debug("[RecipeWalker] EMC {} -> {} (из рецепта {})",
                            emcPerUnit, rl, gtRecipe.getId());
                }
            }
        }
        
        return newMappings;
    }

    /**
     * Обработка жидкостных выходов рецепта.
     */
    private int processFluidOutputs(Object recipe, long inputEMC,
                                     Map<ResourceLocation, Long> fluidEMC) {
        int newMappings = 0;
        
        var gtRecipe = (com.gregtechceu.gtceu.api.recipe.GTRecipe) recipe;
        List<FluidStack> fluidOutputs = getFluidOutputs(gtRecipe);
        if (fluidOutputs.isEmpty()) return 0;
        
        for (FluidStack fluidOutput : fluidOutputs) {
            ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidOutput.getFluid());
            if (fluidRL == null) continue;
            
            int amount = fluidOutput.getAmount();
            long emcPerBucket = (inputEMC * 1000L) / amount;
            
            if (emcPerBucket <= 0) continue;
            
            if (!fluidEMC.containsKey(fluidRL)) {
                fluidEMC.put(fluidRL, emcPerBucket);
                newMappings++;
                
                if (GTEMCConfig.DEBUG_LOGGING.get()) {
                    GTEMCAddon.LOGGER.debug("[RecipeWalker] Fluid EMC {} за 1000mB -> {} (из рецепта {})",
                            emcPerBucket, fluidRL, gtRecipe.getId());
                }
            }
        }
        
        return newMappings;
    }

    /**
     * Получение валидных выходов рецепта.
     */
    private List<ItemStack> getValidOutputs(com.gregtechceu.gtceu.api.recipe.GTRecipe recipe) {
        List<ItemStack> outputs = new ArrayList<>();
        
        var mainOutputs = recipe.getOutputs();
        if (mainOutputs != null) {
            for (var output : mainOutputs) {
                if (output.getContent() instanceof ItemStack stack) {
                    outputs.add(stack.copy());
                }
            }
        }
        
        int minChance = GTEMCConfig.MIN_CHANCE_THRESHOLD.get();
        if (minChance < 100) {
            var chanced = recipe.getChancedOutputs();
            if (chanced != null) {
                for (var chancedOutput : chanced) {
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
    private List<FluidStack> getFluidOutputs(com.gregtechceu.gtceu.api.recipe.GTRecipe recipe) {
        List<FluidStack> outputs = new ArrayList<>();
        
        var fluidOutputs = recipe.getFluidOutputs();
        if (fluidOutputs != null) {
            for (var output : fluidOutputs) {
                if (output.getContent() instanceof FluidStack fluidStack) {
                    outputs.add(fluidStack.copy());
                }
            }
        }
        
        return outputs;
    }

    /**
     * Проверка на циклическую зависимость.
     */
    private boolean isCircularRecipe(Object recipe, Map<ResourceLocation, Long> itemEMC) {
        var gtRecipe = (com.gregtechceu.gtceu.api.recipe.GTRecipe) recipe;
        
        Set<ResourceLocation> inputs = new HashSet<>();
        Set<ResourceLocation> outputs = new HashSet<>();
        
        var recipeInputs = gtRecipe.getInputs();
        if (recipeInputs != null) {
            for (var input : recipeInputs) {
                if (input.getContent() instanceof ItemStack stack) {
                    ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (rl != null) inputs.add(rl);
                }
            }
        }
        
        var recipeOutputs = gtRecipe.getOutputs();
        if (recipeOutputs != null) {
            for (var output : recipeOutputs) {
                if (output.getContent() instanceof ItemStack stack) {
                    ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (rl != null) outputs.add(rl);
                }
            }
        }
        
        if (!outputs.isEmpty() && inputs.containsAll(outputs)) {
            return true;
        }
        
        return false;
    }

    /**
     * Получение всех типов рецептов GT.
     */
    private List<?> getAllRecipeTypes() {
        List<Object> types = new ArrayList<>();
        
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.FURNACE_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.ALLOY_SMELTER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.BENDER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.WIREMILL_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.MIXER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.BLAST_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.CENTRIFUGE_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.ELECTROLYZER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_BATH_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.FLUID_HEATER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.DISTILLATION_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.FERMENTING_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.LASER_ENGRAVER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.AUTOCLAVE_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.COMPRESSOR_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.EXTRACTOR_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.CUTTER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.LATHE_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORGE_HAMMER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.ARC_FURNACE_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.PACKER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.UNPACKER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORMING_PRESS_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.SIFTER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.POLARIZER_RECIPES);
        types.add(com.gregtechceu.gtceu.common.data.GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES);
        
        return types;
    }

    public int getTotalProcessed() {
        return totalProcessed;
    }

    public int getTotalAssigned() {
        return totalAssigned;
    }
}
