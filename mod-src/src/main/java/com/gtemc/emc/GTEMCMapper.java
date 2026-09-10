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
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Основной EMC-маппер для GregTech CEu Modern.
 */
public class GTEMCMapper {

    private static final Map<ResourceLocation, Long> itemEMC = new HashMap<>();
    private static final Map<ResourceLocation, Long> fluidEMC = new HashMap<>();
    private static final Set<String> processedRecipes = new HashSet<>();
    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;
        GTEMCAddon.LOGGER.info("[GTEMCAddon] EMC маппер зарегистрирован");
        
        moze_intel.projecte.api.ProjectEAPI.getEMCRegistrationEvent().addListener(event -> {
            addMappings(event.getMappingCollector());
        });
    }

    public static void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Начинается расчёт EMC...");
        long startTime = System.currentTimeMillis();
        
        collectBaseEMC(mapper);
        FluidEMCRegistry.initialize(mapper);
        
        int maxIterations = GTEMCConfig.MAX_ITERATIONS.get();
        int iteration = 0;
        
        do {
            iteration++;
            int mappedThisIteration = 0;
            
            if (GTEMCConfig.CALCULATE_ITEMS.get()) {
                mappedThisIteration += processItemRecipes(mapper);
            }
            if (GTEMCConfig.CALCULATE_FLUIDS.get()) {
                mappedThisIteration += processFluidRecipes(mapper);
            }
            
            if (mappedThisIteration == 0) {
                GTEMCAddon.LOGGER.info("[GTEMCAddon] Стабилизация на итерации {}", iteration);
                break;
            }
        } while (iteration < maxIterations);
        
        long elapsed = System.currentTimeMillis() - startTime;
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Завершено за {} мс. Предметов: {}, Жидкостей: {}",
                elapsed, itemEMC.size(), fluidEMC.size());
    }

    private static void collectBaseEMC(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        Map<ItemStack, Long> vanillaEMC = getVanillaBaseEMC();
        for (Map.Entry<ItemStack, Long> entry : vanillaEMC.entrySet()) {
            ItemStack stack = entry.getKey();
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (rl != null) {
                itemEMC.put(rl, entry.getValue());
            }
        }
        collectMaterialBaseEMC();
    }

    private static Map<ItemStack, Long> getVanillaBaseEMC() {
        Map<ItemStack, Long> base = new HashMap<>();
        base.put(new ItemStack(Items.COBBLESTONE), 1L);
        base.put(new ItemStack(Items.STONE), 1L);
        base.put(new ItemStack(Items.DIRT), 1L);
        base.put(new ItemStack(Items.COAL), 64L);
        base.put(new ItemStack(Items.IRON_INGOT), 256L);
        base.put(new ItemStack(Items.GOLD_INGOT), 2048L);
        base.put(new ItemStack(Items.DIAMOND), 8192L);
        base.put(new ItemStack(Items.EMERALD), 8192L);
        base.put(new ItemStack(Items.REDSTONE), 32L);
        base.put(new ItemStack(Items.QUARTZ), 128L);
        base.put(new ItemStack(Items.OBSIDIAN), 64L);
        base.put(new ItemStack(Items.BLAZE_ROD), 768L);
        base.put(new ItemStack(Items.ENDER_PEARL), 1024L);
        return base;
    }

    private static void collectMaterialBaseEMC() {
        Map<String, Long> materialEMC = new HashMap<>();
        materialEMC.put("iron", 256L);
        materialEMC.put("gold", 2048L);
        materialEMC.put("copper", 128L);
        materialEMC.put("tin", 64L);
        materialEMC.put("steel", 512L);
        materialEMC.put("titanium", 4096L);
        materialEMC.put("tungsten", 2048L);
        materialEMC.put("iridium", 8192L);
        
        for (Map.Entry<String, Long> entry : materialEMC.entrySet()) {
            registerMaterialForms(entry.getKey(), entry.getValue());
        }
    }

    private static void registerMaterialForms(String materialName, long baseIngotEMC) {
        String namespace = "gtceu";
        registerItemEMC(namespace, materialName + "_ingot", baseIngotEMC);
        registerItemEMC(namespace, materialName + "_dust", baseIngotEMC);
        registerItemEMC(namespace, materialName + "_plate", baseIngotEMC / 4);
        registerItemEMC(namespace, materialName + "_gear", baseIngotEMC * 4);
        registerItemEMC(namespace, materialName + "_wire", baseIngotEMC / 4);
        registerFluidEMC(namespace, materialName, baseIngotEMC * 9);
    }

    private static void registerItemEMC(String namespace, String path, long emc) {
        if (emc <= 0) return;
        itemEMC.put(new ResourceLocation(namespace, path), emc);
    }

    private static void registerFluidEMC(String namespace, String materialName, long emcPerBucket) {
        if (emcPerBucket <= 0) return;
        fluidEMC.put(new ResourceLocation(namespace, materialName), emcPerBucket);
    }

    private static int processItemRecipes(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        int newMappings = 0;
        
        for (GTRecipeType recipeType : getGTRecipeTypes()) {
            for (GTRecipe recipe : recipeType.getRecipes()) {
                String recipeId = recipe.getId().toString();
                if (processedRecipes.contains(recipeId)) continue;
                
                long inputEMC = calculateInputEMC(recipe);
                if (inputEMC < 0) continue;
                
                List<ItemStack> outputs = getRecipeOutputs(recipe);
                if (outputs.isEmpty()) continue;
                
                long emcPerOutput = inputEMC / outputs.size();
                if (emcPerOutput <= 0) continue;
                
                for (ItemStack output : outputs) {
                    ResourceLocation outputRL = ForgeRegistries.ITEMS.getKey(output.getItem());
                    if (outputRL == null) continue;
                    if (outputRL.getNamespace().equals("minecraft")) continue;
                    
                    if (!itemEMC.containsKey(outputRL)) {
                        itemEMC.put(outputRL, emcPerOutput);
                        newMappings++;
                    }
                }
                processedRecipes.add(recipeId);
            }
        }
        return newMappings;
    }

    private static int processFluidRecipes(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        int newMappings = 0;
        
        for (GTRecipeType recipeType : getGTRecipeTypes()) {
            for (GTRecipe recipe : recipeType.getRecipes()) {
                String recipeId = recipe.getId().toString() + "_fluid";
                if (processedRecipes.contains(recipeId)) continue;
                
                long inputEMC = calculateTotalInputEMC(recipe);
                if (inputEMC < 0) continue;
                
                List<FluidStack> fluidOutputs = getRecipeFluidOutputs(recipe);
                for (FluidStack fluidOutput : fluidOutputs) {
                    ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidOutput.getFluid());
                    if (fluidRL == null) continue;
                    
                    int amount = fluidOutput.getAmount();
                    long emcPerBucket = (inputEMC * 1000L) / amount;
                    
                    if (emcPerBucket > 0 && !fluidEMC.containsKey(fluidRL)) {
                        fluidEMC.put(fluidRL, emcPerBucket);
                        newMappings++;
                    }
                }
                processedRecipes.add(recipeId);
            }
        }
        return newMappings;
    }

    private static long calculateInputEMC(GTRecipe recipe) {
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
        return totalEMC;
    }

    private static long calculateTotalInputEMC(GTRecipe recipe) {
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

    private static List<ItemStack> getRecipeOutputs(GTRecipe recipe) {
        List<ItemStack> outputs = new ArrayList<>();
        for (var output : recipe.getOutputs()) {
            if (output.getContent() instanceof ItemStack stack) {
                outputs.add(stack.copy());
            }
        }
        
        if (GTEMCConfig.MIN_CHANCE_THRESHOLD.get() < 100) {
            var chancedOutputs = recipe.getChancedOutputs();
            if (chancedOutputs != null) {
                for (var chancedOutput : chancedOutputs) {
                    int chance = chancedOutput.getChance();
                    int threshold = GTEMCConfig.MIN_CHANCE_THRESHOLD.get() * 100;
                    if (chance >= threshold && chancedOutput.getContent() instanceof ItemStack stack) {
                        outputs.add(stack.copy());
                    }
                }
            }
        }
        return outputs;
    }

    private static List<FluidStack> getRecipeFluidOutputs(GTRecipe recipe) {
        List<FluidStack> outputs = new ArrayList<>();
        for (var output : recipe.getFluidOutputs()) {
            if (output.getContent() instanceof FluidStack fluidStack) {
                outputs.add(fluidStack.copy());
            }
        }
        return outputs;
    }

    private static List<GTRecipeType> getGTRecipeTypes() {
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
