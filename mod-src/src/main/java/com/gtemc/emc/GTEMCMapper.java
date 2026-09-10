package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Основной EMC-маппер для GregTech CEu Modern.
 * Регистрируется через ProjectE API и рассчитывает EMC-стоимость
 * для всех предметов и жидкостей GT на основе рецептов машин.
 */
public class GTEMCMapper {

    // Хранилище рассчитанных EMC для предметов
    private static final Map<ResourceLocation, Long> itemEMC = new HashMap<>();
    
    // Хранилище рассчитанных EMC для жидкостей (за 1000 mB)
    private static final Map<ResourceLocation, Long> fluidEMC = new HashMap<>();
    
    // Множество уже обработанных рецептов
    private static final Set<String> processedRecipes = new HashSet<>();
    
    private static boolean registered = false;

    /**
     * Регистрация маппера в системе ProjectE.
     */
    public static void register() {
        if (registered) return;
        registered = true;
        
        GTEMCAddon.LOGGER.info("[GTEMCAddon] EMC маппер зарегистрирован");
        
        // Регистрируем через ProjectE API
        moze_intel.projecte.api.ProjectEAPI.getEMCRegistrationEvent().addListener(event -> {
            addMappings(event.getMappingCollector());
        });
    }

    /**
     * Основной метод добавления EMC-маппингов.
     */
    public static void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Начинается расчёт EMC для GregTech предметов и жидкостей...");
        
        long startTime = System.currentTimeMillis();
        
        // Шаг 1: Собираем базовые EMC
        collectBaseEMC(mapper);
        
        // Шаг 2: Инициализируем FluidEMCRegistry
        FluidEMCRegistry.initialize(mapper);
        
        // Шаг 3: Итеративный обход рецептов GT
        int maxIterations = GTEMCConfig.MAX_ITERATIONS.get();
        int iteration = 0;
        int totalMapped = 0;
        
        do {
            iteration++;
            int mappedThisIteration = 0;
            
            if (GTEMCConfig.CALCULATE_ITEMS.get()) {
                mappedThisIteration += processItemRecipes(mapper);
            }
            
            if (GTEMCConfig.CALCULATE_FLUIDS.get()) {
                mappedThisIteration += processFluidRecipes(mapper);
            }
            
            totalMapped += mappedThisIteration;
            
            if (GTEMCConfig.DEBUG_LOGGING.get()) {
                GTEMCAddon.LOGGER.info("[GTEMCAddon] Итерация {}: назначено {} новых EMC значений", 
                    iteration, mappedThisIteration);
            }
            
            if (mappedThisIteration == 0) {
                GTEMCAddon.LOGGER.info("[GTEMCAddon] Стабилизация достигнута на итерации {}", iteration);
                break;
            }
            
        } while (iteration < maxIterations);
        
        long elapsed = System.currentTimeMillis() - startTime;
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Расчёт EMC завершён за {} мс. " +
                "Итераций: {}, Всего назначено: {} предметов, {} жидкостей",
                elapsed, iteration, itemEMC.size(), fluidEMC.size());
    }

    /**
     * Сбор базовых EMC из ванильных предметов.
     */
    private static void collectBaseEMC(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Сбор базовых EMC значений...");
        
        Map<ItemStack, Long> vanillaEMC = getVanillaBaseEMC();
        
        for (Map.Entry<ItemStack, Long> entry : vanillaEMC.entrySet()) {
            ItemStack stack = entry.getKey();
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (rl != null) {
                itemEMC.put(rl, entry.getValue());
            }
        }
        
        collectMaterialBaseEMC();
        
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Собрано {} базовых EMC для предметов", itemEMC.size());
    }

    /**
     * Получение базовых EMC из ванильных предметов.
     */
    private static Map<ItemStack, Long> getVanillaBaseEMC() {
        Map<ItemStack, Long> base = new HashMap<>();
        
        base.put(new ItemStack(Items.COBBLESTONE), 1L);
        base.put(new ItemStack(Items.STONE), 1L);
        base.put(new ItemStack(Items.DIRT), 1L);
        base.put(new ItemStack(Items.SAND), 1L);
        base.put(new ItemStack(Items.OAK_LOG), 32L);
        base.put(new ItemStack(Items.COAL), 64L);
        base.put(new ItemStack(Items.IRON_INGOT), 256L);
        base.put(new ItemStack(Items.GOLD_INGOT), 2048L);
        base.put(new ItemStack(Items.DIAMOND), 8192L);
        base.put(new ItemStack(Items.EMERALD), 8192L);
        base.put(new ItemStack(Items.NETHERITE_INGOT), 12288L);
        base.put(new ItemStack(Items.REDSTONE), 32L);
        base.put(new ItemStack(Items.GLOWSTONE_DUST), 384L);
        base.put(new ItemStack(Items.LAPIS_LAZULI), 864L);
        base.put(new ItemStack(Items.QUARTZ), 128L);
        base.put(new ItemStack(Items.OBSIDIAN), 64L);
        base.put(new ItemStack(Items.END_STONE), 1L);
        base.put(new ItemStack(Items.NETHERRACK), 1L);
        base.put(new ItemStack(Items.GUNPOWDER), 48L);
        base.put(new ItemStack(Items.BLAZE_ROD), 768L);
        base.put(new ItemStack(Items.ENDER_PEARL), 1024L);
        base.put(new ItemStack(Items.GHAST_TEAR), 4096L);
        base.put(new ItemStack(Items.STRING), 12L);
        base.put(new ItemStack(Items.LEATHER), 64L);
        base.put(new ItemStack(Items.CLAY_BALL), 64L);
        base.put(new ItemStack(Items.BONE), 128L);
        base.put(new ItemStack(Items.SPIDER_EYE), 128L);
        base.put(new ItemStack(Items.ROTTEN_FLESH), 32L);
        
        return base;
    }

    /**
     * Сбор EMC для базовых материалов GregTech.
     */
    private static void collectMaterialBaseEMC() {
        Map<String, Long> materialEMC = new HashMap<>();
        materialEMC.put("iron", 256L);
        materialEMC.put("gold", 2048L);
        materialEMC.put("diamond", 8192L);
        materialEMC.put("emerald", 8192L);
        materialEMC.put("copper", 128L);
        materialEMC.put("tin", 64L);
        materialEMC.put("bronze", 96L);
        materialEMC.put("silver", 512L);
        materialEMC.put("lead", 64L);
        materialEMC.put("nickel", 128L);
        materialEMC.put("zinc", 64L);
        materialEMC.put("brass", 96L);
        materialEMC.put("steel", 512L);
        materialEMC.put("stainless_steel", 1024L);
        materialEMC.put("titanium", 4096L);
        materialEMC.put("tungsten", 2048L);
        materialEMC.put("tungsten_steel", 4096L);
        materialEMC.put("osmium", 4096L);
        materialEMC.put("platinum", 4096L);
        materialEMC.put("iridium", 8192L);
        materialEMC.put("redstone", 32L);
        materialEMC.put("glowstone", 384L);
        materialEMC.put("lapis", 864L);
        materialEMC.put("nether_quartz", 128L);
        materialEMC.put("coal", 64L);
        materialEMC.put("charcoal", 16L);
        materialEMC.put("obsidian", 64L);
        materialEMC.put("ender_pearl", 1024L);
        materialEMC.put("blaze", 768L);
        
        for (Map.Entry<String, Long> entry : materialEMC.entrySet()) {
            String materialName = entry.getKey();
            long baseEMC = entry.getValue();
            registerMaterialForms(materialName, baseEMC);
        }
    }

    /**
     * Регистрация EMC для всех форм материала.
     */
    private static void registerMaterialForms(String materialName, long baseIngotEMC) {
        String namespace = "gtceu";
        
        registerItemEMC(namespace, materialName + "_ingot", baseIngotEMC);
        registerItemEMC(namespace, materialName + "_dust", baseIngotEMC);
        registerItemEMC(namespace, "small_" + materialName + "_dust", baseIngotEMC / 4);
        registerItemEMC(namespace, "tiny_" + materialName + "_dust", baseIngotEMC / 9);
        registerItemEMC(namespace, materialName + "_block", baseIngotEMC * 9);
        registerItemEMC(namespace, materialName + "_plate", baseIngotEMC / 4);
        registerItemEMC(namespace, materialName + "_double_plate", baseIngotEMC / 2);
        registerItemEMC(namespace, materialName + "_rod", baseIngotEMC / 2);
        registerItemEMC(namespace, materialName + "_long_rod", baseIngotEMC);
        registerItemEMC(namespace, materialName + "_bolt", baseIngotEMC / 8);
        registerItemEMC(namespace, materialName + "_screw", baseIngotEMC / 8);
        registerItemEMC(namespace, materialName + "_ring", baseIngotEMC / 4);
        registerItemEMC(namespace, materialName + "_gear", baseIngotEMC * 4);
        registerItemEMC(namespace, materialName + "_small_gear", baseIngotEMC * 2);
        registerItemEMC(namespace, materialName + "_wire", baseIngotEMC / 4);
        registerItemEMC(namespace, materialName + "_fine_wire", baseIngotEMC / 8);
        registerItemEMC(namespace, materialName + "_nugget", baseIngotEMC / 9);
        registerItemEMC(namespace, materialName + "_ore", baseIngotEMC * 2);
        registerItemEMC(namespace, materialName + "_crushed", baseIngotEMC);
        registerItemEMC(namespace, materialName + "_purified_ore", baseIngotEMC);
        registerItemEMC(namespace, materialName + "_refined_ore", baseIngotEMC);
        
        registerFluidEMC(namespace, materialName, baseIngotEMC * 9);
    }

    private static void registerItemEMC(String namespace, String path, long emc) {
        if (emc <= 0) return;
        ResourceLocation rl = new ResourceLocation(namespace, path);
        itemEMC.put(rl, emc);
    }

    private static void registerFluidEMC(String namespace, String materialName, long emcPerBucket) {
        if (emcPerBucket <= 0) return;
        ResourceLocation rl = new ResourceLocation(namespace, materialName);
        fluidEMC.put(rl, emcPerBucket);
    }

    /**
     * Обработка рецептов для предметов.
     */
    private static int processItemRecipes(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        int newMappings = 0;
        
        for (var recipeType : getGTRecipeTypes()) {
            for (var recipe : recipeType.getRecipes()) {
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
                    if (outputRL.getNamespace().equals("minecraft") && 
                        !isVanillaWithGTEquivalent(output)) continue;
                    
                    if (!itemEMC.containsKey(outputRL)) {
                        itemEMC.put(outputRL, emcPerOutput);
                        newMappings++;
                        
                        if (GTEMCConfig.DEBUG_LOGGING.get()) {
                            GTEMCAddon.LOGGER.debug("[GTEMCAddon] Назначен EMC {} для {} (рецепт: {})",
                                    emcPerOutput, outputRL, recipeId);
                        }
                    }
                }
                
                processedRecipes.add(recipeId);
            }
        }
        
        return newMappings;
    }

    /**
     * Обработка рецептов для жидкостей.
     */
    private static int processFluidRecipes(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        int newMappings = 0;
        
        for (var recipeType : getGTRecipeTypes()) {
            for (var recipe : recipeType.getRecipes()) {
                String recipeId = recipe.getId().toString() + "_fluid";
                if (processedRecipes.contains(recipeId)) continue;
                
                long inputEMC = calculateTotalInputEMC(recipe);
                if (inputEMC < 0) continue;
                
                List<FluidStack> fluidOutputs = getRecipeFluidOutputs(recipe);
                if (fluidOutputs.isEmpty()) continue;
                
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
                            GTEMCAddon.LOGGER.debug("[GTEMCAddon] Назначен EMC {} за 1000mB для жидкости {} (рецепт: {})",
                                    emcPerBucket, fluidRL, recipeId);
                        }
                    }
                }
                
                processedRecipes.add(recipeId);
            }
        }
        
        return newMappings;
    }

    /**
     * Расчёт EMC всех ингредиентов рецепта (только предметы).
     */
    private static long calculateInputEMC(var recipe) {
        long totalEMC = 0;
        
        var inputs = recipe.getInputs();
        for (var input : inputs) {
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

    /**
     * Расчёт полного EMC входа (предметы + жидкости).
     */
    private static long calculateTotalInputEMC(var recipe) {
        long totalEMC = 0;
        
        var itemInputs = recipe.getInputs();
        for (var input : itemInputs) {
            if (input.getContent() instanceof ItemStack stack) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl == null) return -1;
                
                Long emc = itemEMC.get(rl);
                if (emc == null) return -1;
                
                totalEMC += emc * stack.getCount();
            }
        }
        
        var fluidInputs = recipe.getFluidInputs();
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
        
        return totalEMC;
    }

    /**
     * Получение предметных выходов рецепта.
     */
    private static List<ItemStack> getRecipeOutputs(var recipe) {
        List<ItemStack> outputs = new ArrayList<>();
        
        var outputs_list = recipe.getOutputs();
        for (var output : outputs_list) {
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
     * Получение жидкостных выходов рецепта.
     */
    private static List<FluidStack> getRecipeFluidOutputs(var recipe) {
        List<FluidStack> outputs = new ArrayList<>();
        
        var fluidOutputs = recipe.getFluidOutputs();
        for (var output : fluidOutputs) {
            if (output.getContent() instanceof FluidStack fluidStack) {
                outputs.add(fluidStack.copy());
            }
        }
        
        return outputs;
    }

    /**
     * Получение всех типов рецептов GT.
     */
    private static List<?> getGTRecipeTypes() {
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

    private static boolean isVanillaWithGTEquivalent(ItemStack stack) {
        return stack.getItem() == Items.IRON_INGOT || 
               stack.getItem() == Items.GOLD_INGOT ||
               stack.getItem() == Items.DIAMOND;
    }

    public static Map<ResourceLocation, Long> getItemEMCMap() {
        return Collections.unmodifiableMap(itemEMC);
    }

    public static Map<ResourceLocation, Long> getFluidEMCMap() {
        return Collections.unmodifiableMap(fluidEMC);
    }
}
