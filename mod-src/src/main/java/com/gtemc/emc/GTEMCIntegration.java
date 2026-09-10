package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Интеграционный слой между GTEMCMapper и ProjectE API.
 * Управляет полным циклом расчёта EMC для GregTech CEu Modern.
 */
public class GTEMCIntegration {

    private static GTEMCIntegration instance;
    
    private final EMCCalculator calculator;
    private final RecipeWalker recipeWalker;
    private boolean initialized = false;

    private GTEMCIntegration() {
        this.calculator = new EMCCalculator();
        this.recipeWalker = new RecipeWalker();
    }

    /**
     * Получение singleton экземпляра.
     */
    public static GTEMCIntegration getInstance() {
        if (instance == null) {
            instance = new GTEMCIntegration();
        }
        return instance;
    }

    /**
     * Основной метод интеграции с ProjectE.
     */
    public void processMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        if (initialized) {
            GTEMCAddon.LOGGER.warn("[GTEMCIntegration] Уже инициализирован, пропускаем повторный вызов");
            return;
        }
        
        long startTime = System.currentTimeMillis();
        GTEMCAddon.LOGGER.info("[GTEMCIntegration] === Начало расчёта EMC для GregTech CEu Modern ===");
        
        try {
            // Шаг 1: Инициализация базовых значений
            GTEMCAddon.LOGGER.info("[GTEMCIntegration] Шаг 1: Инициализация базовых EMC...");
            initializeBaseEMC(mapper);
            
            // Шаг 2: Итеративный обход рецептов
            GTEMCAddon.LOGGER.info("[GTEMCIntegration] Шаг 2: Обход рецептов GT...");
            int newMappings = recipeWalker.walkAllRecipes(
                    mapper,
                    calculator.getItemEMC(),
                    calculator.getFluidEMC()
            );
            GTEMCAddon.LOGGER.info("[GTEMCIntegration] Обход завершён. Новых EMC: {}", newMappings);
            
            // Шаг 3: Валидация
            GTEMCAddon.LOGGER.info("[GTEMCIntegration] Шаг 3: Валидация значений...");
            calculator.validate();
            
            // Шаг 4: Применение к мапперу
            GTEMCAddon.LOGGER.info("[GTEMCIntegration] Шаг 4: Применение к ProjectE...");
            calculator.applyToMapper(mapper);
            
            initialized = true;
            
        } catch (Exception e) {
            GTEMCAddon.LOGGER.error("[GTEMCIntegration] Критическая ошибка при расчёте EMC!", e);
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        GTEMCAddon.LOGGER.info("[GTEMCIntegration] === Расчёт завершён за {} мс ===", elapsed);
        GTEMCAddon.LOGGER.info("[GTEMCIntegration] Предметов: {}, Жидкостей: {}, Кандидатов: {}",
                calculator.getItemEMC().size(),
                calculator.getFluidEMC().size(),
                calculator.getTotalCandidates());
    }

    /**
     * Инициализация базовых EMC значений.
     */
    private void initializeBaseEMC(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        // Базовые ванильные EMC
        Map<String, Long> baseItems = new LinkedHashMap<>();
        baseItems.put("minecraft:cobblestone", 1L);
        baseItems.put("minecraft:stone", 1L);
        baseItems.put("minecraft:dirt", 1L);
        baseItems.put("minecraft:sand", 1L);
        baseItems.put("minecraft:oak_log", 32L);
        baseItems.put("minecraft:coal", 64L);
        baseItems.put("minecraft:iron_ingot", 256L);
        baseItems.put("minecraft:gold_ingot", 2048L);
        baseItems.put("minecraft:diamond", 8192L);
        baseItems.put("minecraft:emerald", 8192L);
        baseItems.put("minecraft:netherite_ingot", 12288L);
        baseItems.put("minecraft:redstone", 32L);
        baseItems.put("minecraft:glowstone_dust", 384L);
        baseItems.put("minecraft:lapis_lazuli", 864L);
        baseItems.put("minecraft:quartz", 128L);
        baseItems.put("minecraft:obsidian", 64L);
        baseItems.put("minecraft:end_stone", 1L);
        baseItems.put("minecraft:netherrack", 1L);
        baseItems.put("minecraft:gunpowder", 48L);
        baseItems.put("minecraft:blaze_rod", 768L);
        baseItems.put("minecraft:ender_pearl", 1024L);
        baseItems.put("minecraft:ghast_tear", 4096L);
        baseItems.put("minecraft:string", 12L);
        baseItems.put("minecraft:leather", 64L);
        baseItems.put("minecraft:clay_ball", 64L);
        baseItems.put("minecraft:bone", 128L);
        baseItems.put("minecraft:spider_eye", 128L);
        baseItems.put("minecraft:rotten_flesh", 32L);
        baseItems.put("minecraft:iron_ore", 256L);
        baseItems.put("minecraft:gold_ore", 2048L);
        baseItems.put("minecraft:diamond_ore", 8192L);
        baseItems.put("minecraft:coal_ore", 64L);
        baseItems.put("minecraft:lapis_ore", 864L);
        baseItems.put("minecraft:redstone_ore", 32L);
        baseItems.put("minecraft:emerald_ore", 8192L);
        baseItems.put("minecraft:copper_ore", 128L);
        baseItems.put("minecraft:raw_iron", 256L);
        baseItems.put("minecraft:raw_gold", 2048L);
        baseItems.put("minecraft:raw_copper", 128L);
        
        for (Map.Entry<String, Long> entry : baseItems.entrySet()) {
            ResourceLocation rl = new ResourceLocation(entry.getKey());
            calculator.addItemEMCCandidate(rl, entry.getValue());
        }
        
        // Инициализируем базовые жидкости
        FluidEMCRegistry.initialize(mapper);
        
        // Копируем жидкости из FluidEMCRegistry в calculator
        for (Map.Entry<ResourceLocation, Long> entry : FluidEMCRegistry.getAllFluidEMC().entrySet()) {
            calculator.addFluidEMCCandidate(entry.getKey(), entry.getValue());
        }
        
        // Регистрируем базовые GT материалы
        registerBaseGTMaterials();
        
        GTEMCAddon.LOGGER.info("[GTEMCIntegration] Базовые EMC инициализированы: {} предметов, {} жидкостей",
                calculator.getItemEMC().size(), calculator.getFluidEMC().size());
    }

    /**
     * Регистрация базовых материалов GT как предметов с EMC.
     */
    private void registerBaseGTMaterials() {
        Map<String, Long> materials = new LinkedHashMap<>();
        materials.put("iron", 256L);
        materials.put("gold", 2048L);
        materials.put("copper", 128L);
        materials.put("tin", 64L);
        materials.put("bronze", 96L);
        materials.put("silver", 512L);
        materials.put("lead", 64L);
        materials.put("nickel", 128L);
        materials.put("zinc", 64L);
        materials.put("brass", 96L);
        materials.put("steel", 512L);
        materials.put("aluminium", 512L);
        materials.put("stainless_steel", 1024L);
        materials.put("titanium", 4096L);
        materials.put("tungsten", 2048L);
        materials.put("tungsten_steel", 4096L);
        materials.put("osmium", 4096L);
        materials.put("platinum", 4096L);
        materials.put("iridium", 8192L);
        materials.put("chrome", 1024L);
        materials.put("electrum", 1024L);
        materials.put("invar", 256L);
        materials.put("cupronickel", 128L);
        materials.put("kanthal", 256L);
        materials.put("nichrome", 512L);
        materials.put("magnalium", 256L);
        materials.put("red_alloy", 128L);
        materials.put("blue_alloy", 512L);
        materials.put("soldering_alloy", 128L);
        materials.put("battery_alloy", 128L);
        materials.put("naquadah", 16384L);
        materials.put("naquadah_alloy", 32768L);
        materials.put("enriched_naquadah", 65536L);
        materials.put("naquadria", 131072L);
        materials.put("neutronium", 262144L);
        materials.put("diamond", 8192L);
        materials.put("emerald", 8192L);
        materials.put("ruby", 8192L);
        materials.put("sapphire", 8192L);
        materials.put("green_sapphire", 8192L);
        materials.put("opal", 8192L);
        materials.put("amethyst", 8192L);
        materials.put("topaz", 8192L);
        materials.put("lapis", 864L);
        materials.put("nether_quartz", 128L);
        materials.put("redstone", 32L);
        materials.put("glowstone", 384L);
        materials.put("coal", 64L);
        materials.put("charcoal", 16L);
        materials.put("obsidian", 64L);
        materials.put("ender_pearl", 1024L);
        materials.put("blaze", 768L);
        materials.put("sulfur", 32L);
        materials.put("saltpeter", 48L);
        materials.put("graphite", 192L);
        materials.put("carbon", 64L);
        materials.put("silicon", 128L);
        materials.put("rubber", 48L);
        materials.put("polyethylene", 96L);
        materials.put("polyvinyl_chloride", 128L);
        materials.put("polytetrafluoroethylene", 256L);
        materials.put("polybenzimidazole", 1024L);
        materials.put("epoxid", 192L);
        materials.put("polyphenylene_sulfide", 256L);
        materials.put("cellulose", 32L);
        
        for (Map.Entry<String, Long> entry : materials.entrySet()) {
            String materialName = entry.getKey();
            long baseEMC = entry.getValue();
            
            registerAllForms(materialName, baseEMC);
        }
    }

    /**
     * Регистрация всех форм материала с пропорциональным EMC.
     */
    private void registerAllForms(String materialName, long ingotEMC) {
        String ns = "gtceu";
        
        Map<String, Double> forms = new LinkedHashMap<>();
        forms.put("ingot", 1.0);
        forms.put("dust", 1.0);
        forms.put("small_dust", 0.25);
        forms.put("tiny_dust", 0.111);
        forms.put("nugget", 0.111);
        forms.put("block", 9.0);
        forms.put("plate", 0.25);
        forms.put("double_plate", 0.5);
        forms.put("dense_plate", 2.25);
        forms.put("rod", 0.5);
        forms.put("long_rod", 1.0);
        forms.put("bolt", 0.125);
        forms.put("screw", 0.125);
        forms.put("ring", 0.25);
        forms.put("foil", 0.25);
        forms.put("gear", 4.0);
        forms.put("small_gear", 2.0);
        forms.put("wire", 0.25);
        forms.put("fine_wire", 0.125);
        forms.put("spring", 0.5);
        forms.put("small_spring", 0.25);
        forms.put("frame", 2.0);
        forms.put("rotor", 4.25);
        forms.put("ore", 2.0);
        forms.put("crushed", 1.0);
        forms.put("purified_ore", 1.1);
        forms.put("refined_ore", 1.2);
        forms.put("gem", 1.0);
        forms.put("exquisite_gem", 4.0);
        forms.put("flawless_gem", 1.5);
        forms.put("flawed_gem", 0.75);
        forms.put("chipped_gem", 0.5);
        forms.put("lens", 0.75);
        forms.put("round", 0.125);
        
        for (Map.Entry<String, Double> form : forms.entrySet()) {
            long emc = Math.max(1, (long)(ingotEMC * form.getValue()));
            ResourceLocation rl = new ResourceLocation(ns, materialName + "_" + form.getKey());
            calculator.addItemEMCCandidate(rl, emc);
        }
    }
}
