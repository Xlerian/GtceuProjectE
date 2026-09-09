package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.api.materials.Material;
import com.gregtechceu.gtceu.api.materials.properties.PropertyKey;
import com.gregtechceu.gtceu.api.materials.properties.FluidProperty;
import com.gregtechceu.gtceu.api.materials.properties.IngotProperty;
import com.gregtechceu.gtceu.api.materials.properties.GemProperty;
import com.gregtechceu.gtceu.api.materials.properties.DustProperty;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Основной EMC-маппер для GregTech CEu Modern.
 * Регистрируется через ProjectE API и рассчитывает EMC-стоимость
 * для всех предметов и жидкостей GT на основе рецептов машин.
 * 
 * Алгоритм:
 * 1. Собирает базовые EMC из ванильных/уже определённых значений
 * 2. Итеративно обходит рецепты GT машин
 * 3. Если все ингредиенты имеют EMC - рассчитывает EMC для выхода
 * 4. Повторяет до стабилизации или достижения лимита итераций
 */
public class GTEMCMapper {

    // Хранилище рассчитанных EMC для предметов (ResourceLocation -> EMC за 1 шт.)
    private static final Map<ResourceLocation, Long> itemEMC = new HashMap<>();
    
    // Хранилище рассчитанных EMC для жидкостей (ResourceLocation -> EMC за 1000 mB)
    private static final Map<ResourceLocation, Long> fluidEMC = new HashMap<>();
    
    // Множество уже обработанных рецептов (предотвращение зацикливания)
    private static final Set<String> processedRecipes = new HashSet<>();
    
    // Флаг регистрации
    private static boolean registered = false;

    /**
     * Регистрация маппера в системе ProjectE.
     * Вызывается при инициализации мода.
     */
    public static void register() {
        if (registered) return;
        registered = true;
        
        GTEMCAddon.LOGGER.info("[GTEMCAddon] EMC маппер зарегистрирован");
        
        // Регистрируем наш конвертер через ProjectE API
        // ProjectE вызовет addMappings() когда будет готов собирать данные
        moze_intel.projecte.api.ProjectEAPI.getEMCRegistrationEvent().addListener(event -> {
            addMappings(event.getMappingCollector());
        });
    }

    /**
     * Основной метод добавления EMC-маппингов.
     * Вызывается ProjectE при сборке EMC-карты.
     */
    public static <T extends NormalizedSimpleStack<T, ?>> void addMappings(IMappingCollector<T, Long> mapper) {
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Начинается расчёт EMC для GregTech предметов и жидкостей...");
        
        long startTime = System.currentTimeMillis();
        
        // Шаг 1: Собираем базовые EMC из ванильных предметов
        collectBaseEMC(mapper);
        
        // Шаг 2: Инициализируем FluidEMCRegistry базовыми жидкостями
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
            
            // Если ничего нового не добавлено - выходим
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
     * Сбор базовых EMC из ванильных предметов и уже определённых значений.
     */
    private static <T extends NormalizedSimpleStack<T, ?>> void collectBaseEMC(IMappingCollector<T, Long> mapper) {
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Сбор базовых EMC значений...");
        
        // Базовые ванильные EMC (ProjectE уже знает эти значения)
        // Мы используем их как фундамент для расчёта
        Map<ItemStack, Long> vanillaEMC = getVanillaBaseEMC();
        
        for (Map.Entry<ItemStack, Long> entry : vanillaEMC.entrySet()) {
            ItemStack stack = entry.getKey();
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (rl != null) {
                itemEMC.put(rl, entry.getValue());
            }
        }
        
        // Собираем EMC для базовых материалов GT (которые уже имеют EMC из ванили)
        // Например, железные слитки, золотые слитки, алмазы и т.д.
        collectMaterialBaseEMC();
        
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Собрано {} базовых EMC для предметов", itemEMC.size());
    }

    /**
     * Получение базовых EMC из ванильных предметов.
     * ProjectE уже имеет эти значения - мы их используем как отправную точку.
     */
    private static Map<ItemStack, Long> getVanillaBaseEMC() {
        Map<ItemStack, Long> base = new HashMap<>();
        
        // Основные ванильные EMC значения (стандартные ProjectE):
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
     * Связывает GT-материалы с их ванильными эквивалентами.
     */
    private static void collectMaterialBaseEMC() {
        // Связываем GT слитки с ванильными EMC
        // Iron -> 256, Gold -> 2048, Diamond -> 8192 и т.д.
        
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
        
        // Регистрируем EMC для всех форм каждого материала
        for (Map.Entry<String, Long> entry : materialEMC.entrySet()) {
            String materialName = entry.getKey();
            long baseEMC = entry.getValue();
            
            // Регистрируем для всех форм предмета
            registerMaterialForms(materialName, baseEMC);
        }
    }

    /**
     * Регистрация EMC для всех форм материала (ingot, dust, plate, gear и т.д.)
     */
    private static void registerMaterialForms(String materialName, long baseIngotEMC) {
        // Базовая стоимость - за 1 слиток (ingot)
        // Другие формы рассчитываются пропорционально
        
        String namespace = "gtceu";
        
        // Слиток (ingot) - базовая стоимость
        registerItemEMC(namespace, materialName + "_ingot", baseIngotEMC);
        
        // Слиток = 144 mB в GT
        // Пыль (dust) = 1 слиток по стоимости
        registerItemEMC(namespace, materialName + "_dust", baseIngotEMC);
        
        // Маленькая пыль (small_dust) = 1/4 слитка
        registerItemEMC(namespace, "small_" + materialName + "_dust", baseIngotEMC / 4);
        
        // Крошечная пыль (tiny_dust) = 1/9 слитка
        registerItemEMC(namespace, "tiny_" + materialName + "_dust", baseIngotEMC / 9);
        
        // Блок (block) = 9 слитков
        registerItemEMC(namespace, materialName + "_block", baseIngotEMC * 9);
        
        // Пластина (plate) = 1/4 слитка (36 mB)
        registerItemEMC(namespace, materialName + "_plate", baseIngotEMC / 4);
        
        // Двойная пластина (double_plate) = 1/2 слитка
        registerItemEMC(namespace, materialName + "_double_plate", baseIngotEMC / 2);
        
        // Пруток (rod/stick) = 1/2 слитка
        registerItemEMC(namespace, materialName + "_rod", baseIngotEMC / 2);
        
        // Длинный прут (long_rod) = 1 слиток
        registerItemEMC(namespace, materialName + "_long_rod", baseIngotEMC);
        
        // Болт (bolt) = 1/8 слитка
        registerItemEMC(namespace, materialName + "_bolt", baseIngotEMC / 8);
        
        // Винт (screw) = 1/8 слитка
        registerItemEMC(namespace, materialName + "_screw", baseIngotEMC / 8);
        
        // Кольцо (ring) = 1/4 слитка
        registerItemEMC(namespace, materialName + "_ring", baseIngotEMC / 4);
        
        // Шестерня (gear) = 4 слитка
        registerItemEMC(namespace, materialName + "_gear", baseIngotEMC * 4);
        
        // Маленькая шестерня (small_gear) = 2 слитка
        registerItemEMC(namespace, materialName + "_small_gear", baseIngotEMC * 2);
        
        // Проволока (wire) = 1/4 слитка
        registerItemEMC(namespace, materialName + "_wire", baseIngotEMC / 4);
        
        // Fine wire = 1/8 слитка
        registerItemEMC(namespace, materialName + "_fine_wire", baseIngotEMC / 8);
        
        // Слиток (nugget) = 1/9 слитка
        registerItemEMC(namespace, materialName + "_nugget", baseIngotEMC / 9);
        
        // Руда (ore) - базовая стоимость за блок руды / 2 (обычно 2 слитка из руды)
        registerItemEMC(namespace, materialName + "_ore", baseIngotEMC * 2);
        
        // Дроблёная руда (crushed_ore)
        registerItemEMC(namespace, materialName + "_crushed", baseIngotEMC);
        
        // Очищенная руда
        registerItemEMC(namespace, materialName + "_purified_ore", baseIngotEMC);
        
        // Центрифужная руда
        registerItemEMC(namespace, materialName + "_refined_ore", baseIngotEMC);
        
        // Слиток (ingot) для жидкостей - регистрируем EMC жидкости
        registerFluidEMC(namespace, materialName, baseIngotEMC * 9); // 144 mB = 1 ingot, 1000 mB = ~7 ingots
    }

    /**
     * Регистрация EMC для предмета.
     */
    private static void registerItemEMC(String namespace, String path, long emc) {
        if (emc <= 0) return;
        ResourceLocation rl = new ResourceLocation(namespace, path);
        itemEMC.put(rl, emc);
    }

    /**
     * Регистрация EMC для жидкости материала.
     */
    private static void registerFluidEMC(String namespace, String materialName, long emcPerBucket) {
        if (emcPerBucket <= 0) return;
        ResourceLocation rl = new ResourceLocation(namespace, materialName);
        fluidEMC.put(rl, emcPerBucket);
    }

    /**
     * Обработка рецептов для предметов.
     * Возвращает количество новых назначенных EMC.
     */
    private static <T extends NormalizedSimpleStack<T, ?>> int processItemRecipes(IMappingCollector<T, Long> mapper) {
        int newMappings = 0;
        
        // Получаем все типы рецептов GT
        for (GTRecipeType recipeType : getGTRecipeTypes()) {
            // Проходим по всем рецептам этого типа
            for (GTRecipe recipe : recipeType.getRecipes()) {
                String recipeId = recipe.getId().toString();
                
                // Пропускаем уже обработанные
                if (processedRecipes.contains(recipeId)) continue;
                
                // Рассчитываем EMC для входа
                long inputEMC = calculateInputEMC(recipe);
                if (inputEMC < 0) continue; // Не все ингредиенты имеют EMC
                
                // Рассчитываем EMC для выходов
                List<ItemStack> outputs = getRecipeOutputs(recipe);
                if (outputs.isEmpty()) continue;
                
                // EMC за единицу выхода = суммарный вход / количество выходов
                long emcPerOutput = inputEMC / outputs.size();
                if (emcPerOutput <= 0) continue;
                
                // Назначаем EMC для каждого выхода
                for (ItemStack output : outputs) {
                    ResourceLocation outputRL = ForgeRegistries.ITEMS.getKey(output.getItem());
                    if (outputRL == null) continue;
                    if (outputRL.getNamespace().equals("minecraft") && 
                        !isVanillaWithGTEquivalent(output)) continue;
                    
                    // Не перезаписываем если уже есть
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
     * Возвращает количество новых назначенных EMC.
     */
    private static <T extends NormalizedSimpleStack<T, ?>> int processFluidRecipes(IMappingCollector<T, Long> mapper) {
        int newMappings = 0;
        
        for (GTRecipeType recipeType : getGTRecipeTypes()) {
            for (GTRecipe recipe : recipeType.getRecipes()) {
                String recipeId = recipe.getId().toString() + "_fluid";
                if (processedRecipes.contains(recipeId)) continue;
                
                // Рассчитываем полный EMC входа (предметы + жидкости)
                long inputEMC = calculateTotalInputEMC(recipe);
                if (inputEMC < 0) continue;
                
                // Получаем жидкостные выходы
                List<FluidStack> fluidOutputs = getRecipeFluidOutputs(recipe);
                if (fluidOutputs.isEmpty()) continue;
                
                for (FluidStack fluidOutput : fluidOutputs) {
                    ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidOutput.getFluid());
                    if (fluidRL == null) continue;
                    
                    // Рассчитываем EMC за 1000 mB
                    int amount = fluidOutput.getAmount(); // в mB
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
     * Возвращает -1 если хоть один ингредиент не имеет EMC.
     */
    private static long calculateInputEMC(GTRecipe recipe) {
        long totalEMC = 0;
        
        // Обрабатываем предметные входы
        List<Content> inputs = recipe.getInputs();
        for (Content input : inputs) {
            if (input.getContent() instanceof ItemStack stack) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl == null) return -1;
                
                Long emc = itemEMC.get(rl);
                if (emc == null) return -1; // Ингредиент без EMC - пропускаем рецепт
                
                totalEMC += emc * stack.getCount();
            }
        }
        
        return totalEMC;
    }

    /**
     * Расчёт полного EMC входа (предметы + жидкости).
     * Возвращает -1 если хоть один ингредиент не имеет EMC.
     */
    private static long calculateTotalInputEMC(GTRecipe recipe) {
        long totalEMC = 0;
        
        // Предметные входы
        List<Content> itemInputs = recipe.getInputs();
        for (Content input : itemInputs) {
            if (input.getContent() instanceof ItemStack stack) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl == null) return -1;
                
                Long emc = itemEMC.get(rl);
                if (emc == null) return -1;
                
                totalEMC += emc * stack.getCount();
            }
        }
        
        // Жидкостные входы
        List<Content> fluidInputs = recipe.getFluidInputs();
        for (Content fluidInput : fluidInputs) {
            if (fluidInput.getContent() instanceof FluidStack fluidStack) {
                ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
                if (fluidRL == null) return -1;
                
                Long emcPerBucket = fluidEMC.get(fluidRL);
                if (emcPerBucket == null) return -1;
                
                // Пропорционально: EMC = (emcPerBucket * amount) / 1000
                int amount = fluidStack.getAmount(); // в mB
                totalEMC += (emcPerBucket * amount) / 1000L;
            }
        }
        
        return totalEMC;
    }

    /**
     * Получение предметных выходов рецепта.
     * Игнорирует chanced outputs с шансом < 100%.
     */
    private static List<ItemStack> getRecipeOutputs(GTRecipe recipe) {
        List<ItemStack> outputs = new ArrayList<>();
        
        // Основные выходы (100% шанс)
        List<Content> outputs_list = recipe.getOutputs();
        for (Content output : outputs_list) {
            if (output.getContent() instanceof ItemStack stack) {
                outputs.add(stack.copy());
            }
        }
        
        // Chanced outputs - проверяем шанс
        if (GTEMCConfig.MIN_CHANCE_THRESHOLD.get() >= 100) {
            // Игнорируем все chanced outputs если порог = 100%
            return outputs;
        }
        
        List<Content> chancedOutputs = recipe.getChancedOutputs();
        if (chancedOutputs != null) {
            for (Content chancedOutput : chancedOutputs) {
                // Проверяем шанс (GT использует шанс от 0 до 10000, где 10000 = 100%)
                int chance = chancedOutput.getChance();
                int threshold = GTEMCConfig.MIN_CHANCE_THRESHOLD.get() * 100; // переводим % в формат GT
                
                if (chance >= threshold) {
                    if (chancedOutput.getContent() instanceof ItemStack stack) {
                        outputs.add(stack.copy());
                    }
                }
            }
        }
        
        return outputs;
    }

    /**
     * Получение жидкостных выходов рецепта.
     */
    private static List<FluidStack> getRecipeFluidOutputs(GTRecipe recipe) {
        List<FluidStack> outputs = new ArrayList<>();
        
        List<Content> fluidOutputs = recipe.getFluidOutputs();
        for (Content output : fluidOutputs) {
            if (output.getContent() instanceof FluidStack fluidStack) {
                outputs.add(fluidStack.copy());
            }
        }
        
        return outputs;
    }

    /**
     * Получение всех типов рецептов GT.
     */
    private static List<GTRecipeType> getGTRecipeTypes() {
        List<GTRecipeType> types = new ArrayList<>();
        
        // Основные типы машин GT
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
        types.add(GTRecipeTypes.MATERIAL_PRESS_RECIPES);
        
        return types;
    }

    /**
     * Проверка, является ли ванильный предмет GT-эквивалентом.
     */
    private static boolean isVanillaWithGTEquivalent(ItemStack stack) {
        // Ванильные слитки железа/золота уже имеют EMC
        return stack.getItem() == Items.IRON_INGOT || 
               stack.getItem() == Items.GOLD_INGOT ||
               stack.getItem() == Items.DIAMOND;
    }

    /**
     * Получение рассчитанных EMC для предметов (для отладки).
     */
    public static Map<ResourceLocation, Long> getItemEMCMap() {
        return Collections.unmodifiableMap(itemEMC);
    }

    /**
     * Получение рассчитанных EMC для жидкостей (для отладки).
     */
    public static Map<ResourceLocation, Long> getFluidEMCMap() {
        return Collections.unmodifiableMap(fluidEMC);
    }
}
