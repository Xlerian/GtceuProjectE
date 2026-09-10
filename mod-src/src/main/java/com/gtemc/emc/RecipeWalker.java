package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
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
 */
public class RecipeWalker {

    private final Set<String> processedRecipes = new HashSet<>();
    private int totalProcessed = 0;
    private int totalAssigned = 0;

    public int walkAllRecipes(
            IMappingCollector<NormalizedSimpleStack, Long> mapper,
            Map<ResourceLocation, Long> itemEMC,
            Map<ResourceLocation, Long> fluidEMC) {
        
        int maxIterations = GTEMCConfig.MAX_ITERATIONS.get();
        int totalNew = 0;
        
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            int newThisIteration = walkOnce(mapper, itemEMC, fluidEMC);
            totalNew += newThisIteration;
            
            if (newThisIteration == 0) {
                GTEMCAddon.LOGGER.info("[RecipeWalker] Стабилизация на итерации {}", iteration + 1);
                break;
            }
        }
        return totalNew;
    }

    private int walkOnce(
            IMappingCollector<NormalizedSimpleStack, Long> mapper,
            Map<ResourceLocation, Long> itemEMC,
            Map<ResourceLocation, Long> fluidEMC) {
        
        int newMappings = 0;
        
        for (GTRecipeType recipeType : getAllRecipeTypes()) {
            Collection<GTRecipe> recipes = recipeType.getRecipes();
            if (recipes == null) continue;
            
            for (GTRecipe recipe : recipes) {
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

    private long calculateInputEMC(GTRecipe recipe, 
                                    Map<ResourceLocation, Long> itemEMC,
                                    Map<ResourceLocation, Long> fluidEMC) {
        long totalEMC = 0;
        
        for (var input : recipe.getInputs()) {
            if (input.getContent() instanceof ItemStack stack) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl == null) return -1;
                Long emc = itemEMC.get(rl);
                if (emc == null) return -1;
                totalEMC += emc * stack.getCount();
            }
        }
        
        for (var fluidInput : recipe.getFluidInputs()) {
            if (fluidInput.getContent() instanceof FluidStack fluidStack) {
                ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
                if (fluidRL == null) return -1;
                Long emcPerBucket = fluidEMC.get(fluidRL);
                if (emcPerBucket == null) return -1;
                int amount = fluidStack.getAmount();
                totalEMC += (emcPerBucket * amount) / 1000L;
            }
        }
        return totalEMC;
    }

    private int processItemOutputs(GTRecipe recipe, long inputEMC, 
                                    Map<ResourceLocation, Long> itemEMC) {
        int newMappings = 0;
        List<ItemStack> outputs = getValidOutputs(recipe);
        if (outputs.isEmpty()) return 0;
        
        long emcPerUnit = inputEMC / outputs.size();
        if (emcPerUnit <= 0) return 0;
        
        for (ItemStack output : outputs) {
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(output.getItem());
            if (rl == null || rl.getNamespace().equals("minecraft")) continue;
            
            if (!itemEMC.containsKey(rl)) {
                itemEMC.put(rl, emcPerUnit);
                newMappings++;
            }
        }
        return newMappings;
    }

    private int processFluidOutputs(GTRecipe recipe, long inputEMC,
                                     Map<ResourceLocation, Long> fluidEMC) {
        int newMappings = 0;
        
        for (var output : recipe.getFluidOutputs()) {
            if (output.getContent() instanceof FluidStack fluidStack) {
                ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
                if (fluidRL == null) continue;
                
                int amount = fluidStack.getAmount();
                long emcPerBucket = (inputEMC * 1000L) / amount;
                
                if (emcPerBucket > 0 && !fluidEMC.containsKey(fluidRL)) {
                    fluidEMC.put(fluidRL, emcPerBucket);
                    newMappings++;
                }
            }
        }
        return newMappings;
    }

    private List<ItemStack> getValidOutputs(GTRecipe recipe) {
        List<ItemStack> outputs = new ArrayList<>();
        
        for (var output : recipe.getOutputs()) {
            if (output.getContent() instanceof ItemStack stack) {
                outputs.add(stack.copy());
            }
        }
        
        int minChance = GTEMCConfig.MIN_CHANCE_THRESHOLD.get();
        if (minChance < 100) {
            var chanced = recipe.getChancedOutputs();
            if (chanced != null) {
                for (var chancedOutput : chanced) {
                    int chance = chancedOutput.getChance();
                    if (chance >= minChance * 100 && chancedOutput.getContent() instanceof ItemStack stack) {
                        outputs.add(stack.copy());
                    }
                }
            }
        }
        return outputs;
    }

    private boolean isCircularRecipe(GTRecipe recipe, Map<ResourceLocation, Long> itemEMC) {
        Set<ResourceLocation> inputs = new HashSet<>();
        Set<ResourceLocation> outputs = new HashSet<>();
        
        for (var input : recipe.getInputs()) {
            if (input.getContent() instanceof ItemStack stack) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl != null) inputs.add(rl);
            }
        }
        
        for (var output : recipe.getOutputs()) {
            if (output.getContent() instanceof ItemStack stack) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl != null) outputs.add(rl);
            }
        }
        
        return !outputs.isEmpty() && inputs.containsAll(outputs);
    }

    private List<GTRecipeType> getAllRecipeTypes() {
        return List.of(
            GTRecipeTypes.FURNACE_RECIPES,
            GTRecipeTypes.ALLOY_SMELTER_RECIPES,
            GTRecipeTypes.ASSEMBLER_RECIPES,
            GTRecipeTypes.BENDER_RECIPES,
            GTRecipeTypes.WIREMILL_RECIPES,
            GTRecipeTypes.CHEMICAL_RECIPES,
            GTRecipeTypes.MIXER_RECIPES,
            GTRecipeTypes.BLAST_RECIPES,
            GTRecipeTypes.CENTRIFUGE_RECIPES,
            GTRecipeTypes.ELECTROLYZER_RECIPES,
            GTRecipeTypes.CHEMICAL_BATH_RECIPES,
            GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES,
            GTRecipeTypes.FLUID_HEATER_RECIPES,
            GTRecipeTypes.DISTILLATION_RECIPES,
            GTRecipeTypes.LASER_ENGRAVER_RECIPES,
            GTRecipeTypes.AUTOCLAVE_RECIPES,
            GTRecipeTypes.COMPRESSOR_RECIPES,
            GTRecipeTypes.EXTRACTOR_RECIPES,
            GTRecipeTypes.CUTTER_RECIPES,
            GTRecipeTypes.LATHE_RECIPES,
            GTRecipeTypes.MACERATOR_RECIPES,
            GTRecipeTypes.ARC_FURNACE_RECIPES,
            GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
            GTRecipeTypes.FORMING_PRESS_RECIPES
        );
    }
}
