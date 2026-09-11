package com.gtemc.datapack;

import com.gtemc.GTEMCAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Генератор датапака для ProjectE.
 * Создаёт JSON файлы с EMC значениями для GregTech CEu Modern.
 * 
 * ProjectE использует формат pe_custom_conversions для определения EMC.
 * Формат: data/<domain>/pe_custom_conversions/<name>.json
 */
public class GTDatapackGenerator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    
    // Базовые EMC значения для материалов GT
    private static final Map<String, Long> MATERIAL_EMC = new LinkedHashMap<>();
    
    // Множители для разных форм материалов
    private static final Map<String, Double> FORM_MULTIPLIERS = new LinkedHashMap<>();

    static {
        // Базовые EMC за слиток/пыль
        MATERIAL_EMC.put("iron", 256L);
        MATERIAL_EMC.put("gold", 2048L);
        MATERIAL_EMC.put("copper", 128L);
        MATERIAL_EMC.put("tin", 64L);
        MATERIAL_EMC.put("bronze", 96L);
        MATERIAL_EMC.put("silver", 512L);
        MATERIAL_EMC.put("lead", 64L);
        MATERIAL_EMC.put("nickel", 128L);
        MATERIAL_EMC.put("zinc", 64L);
        MATERIAL_EMC.put("brass", 96L);
        MATERIAL_EMC.put("steel", 512L);
        MATERIAL_EMC.put("aluminium", 512L);
        MATERIAL_EMC.put("stainless_steel", 1024L);
        MATERIAL_EMC.put("titanium", 4096L);
        MATERIAL_EMC.put("tungsten", 2048L);
        MATERIAL_EMC.put("tungsten_steel", 4096L);
        MATERIAL_EMC.put("osmium", 4096L);
        MATERIAL_EMC.put("platinum", 4096L);
        MATERIAL_EMC.put("iridium", 8192L);
        MATERIAL_EMC.put("chrome", 1024L);
        MATERIAL_EMC.put("electrum", 1024L);
        MATERIAL_EMC.put("invar", 256L);
        MATERIAL_EMC.put("cupronickel", 128L);
        MATERIAL_EMC.put("kanthal", 256L);
        MATERIAL_EMC.put("nichrome", 512L);
        MATERIAL_EMC.put("magnalium", 256L);
        MATERIAL_EMC.put("red_alloy", 128L);
        MATERIAL_EMC.put("blue_alloy", 512L);
        MATERIAL_EMC.put("soldering_alloy", 128L);
        MATERIAL_EMC.put("battery_alloy", 128L);
        MATERIAL_EMC.put("naquadah", 16384L);
        MATERIAL_EMC.put("naquadah_alloy", 32768L);
        MATERIAL_EMC.put("enriched_naquadah", 65536L);
        MATERIAL_EMC.put("naquadria", 131072L);
        MATERIAL_EMC.put("neutronium", 262144L);
        MATERIAL_EMC.put("diamond", 8192L);
        MATERIAL_EMC.put("emerald", 8192L);
        MATERIAL_EMC.put("ruby", 8192L);
        MATERIAL_EMC.put("sapphire", 8192L);
        MATERIAL_EMC.put("green_sapphire", 8192L);
        MATERIAL_EMC.put("opal", 8192L);
        MATERIAL_EMC.put("amethyst", 8192L);
        MATERIAL_EMC.put("topaz", 8192L);
        MATERIAL_EMC.put("lapis", 864L);
        MATERIAL_EMC.put("nether_quartz", 128L);
        MATERIAL_EMC.put("redstone", 32L);
        MATERIAL_EMC.put("glowstone", 384L);
        MATERIAL_EMC.put("coal", 64L);
        MATERIAL_EMC.put("charcoal", 16L);
        MATERIAL_EMC.put("obsidian", 64L);
        MATERIAL_EMC.put("ender_pearl", 1024L);
        MATERIAL_EMC.put("blaze", 768L);
        MATERIAL_EMC.put("sulfur", 32L);
        MATERIAL_EMC.put("saltpeter", 48L);
        MATERIAL_EMC.put("graphite", 192L);
        MATERIAL_EMC.put("carbon", 64L);
        MATERIAL_EMC.put("silicon", 128L);
        MATERIAL_EMC.put("rubber", 48L);
        MATERIAL_EMC.put("polyethylene", 96L);
        MATERIAL_EMC.put("polyvinyl_chloride", 128L);
        MATERIAL_EMC.put("polytetrafluoroethylene", 256L);
        MATERIAL_EMC.put("polybenzimidazole", 1024L);
        MATERIAL_EMC.put("epoxid", 192L);
        MATERIAL_EMC.put("polyphenylene_sulfide", 256L);
        MATERIAL_EMC.put("cellulose", 32L);
        
        // Множители для форм
        FORM_MULTIPLIERS.put("ingot", 1.0);
        FORM_MULTIPLIERS.put("dust", 1.0);
        FORM_MULTIPLIERS.put("small_dust", 0.25);
        FORM_MULTIPLIERS.put("tiny_dust", 0.111);
        FORM_MULTIPLIERS.put("nugget", 0.111);
        FORM_MULTIPLIERS.put("block", 9.0);
        FORM_MULTIPLIERS.put("plate", 0.25);
        FORM_MULTIPLIERS.put("double_plate", 0.5);
        FORM_MULTIPLIERS.put("dense_plate", 2.25);
        FORM_MULTIPLIERS.put("rod", 0.5);
        FORM_MULTIPLIERS.put("long_rod", 1.0);
        FORM_MULTIPLIERS.put("bolt", 0.125);
        FORM_MULTIPLIERS.put("screw", 0.125);
        FORM_MULTIPLIERS.put("ring", 0.25);
        FORM_MULTIPLIERS.put("foil", 0.25);
        FORM_MULTIPLIERS.put("gear", 4.0);
        FORM_MULTIPLIERS.put("small_gear", 2.0);
        FORM_MULTIPLIERS.put("wire", 0.25);
        FORM_MULTIPLIERS.put("fine_wire", 0.125);
        FORM_MULTIPLIERS.put("spring", 0.5);
        FORM_MULTIPLIERS.put("small_spring", 0.25);
        FORM_MULTIPLIERS.put("frame", 2.0);
        FORM_MULTIPLIERS.put("rotor", 4.25);
        FORM_MULTIPLIERS.put("ore", 2.0);
        FORM_MULTIPLIERS.put("crushed", 1.0);
        FORM_MULTIPLIERS.put("purified_ore", 1.1);
        FORM_MULTIPLIERS.put("refined_ore", 1.2);
        FORM_MULTIPLIERS.put("gem", 1.0);
        FORM_MULTIPLIERS.put("exquisite_gem", 4.0);
        FORM_MULTIPLIERS.put("flawless_gem", 1.5);
        FORM_MULTIPLIERS.put("flawed_gem", 0.75);
        FORM_MULTIPLIERS.put("chipped_gem", 0.5);
        FORM_MULTIPLIERS.put("lens", 0.75);
        FORM_MULTIPLIERS.put("round", 0.125);
    }

    /**
     * Генерация датапака.
     */
    public static void generateDatapack() {
        GTEMCAddon.LOGGER.info("[GTDatapackGenerator] Начало генерации датапака...");
        
        try {
            // Путь к папке minecraft (для датапаков)
            Path minecraftDir = getMinecraftDir();
            Path datapackDir = minecraftDir.resolve("datapacks").resolve("gtcemcaddon");
            Path dataDir = datapackDir.resolve("data").resolve("gtcemcaddon").resolve("pe_custom_conversions");
            
            // Создаём директории
            dataDir.toFile().mkdirs();
            
            // Генерируем pack.mcmeta
            generatePackMcmeta(datapackDir);
            
            // Генерируем основные EMC значения
            generateBaseValues(dataDir);
            
            // Генерируем конверсии для форм материалов
            generateMaterialForms(dataDir);
            
            // Генерируем конверсии для жидкостей
            generateFluidConversions(dataDir);
            
            GTEMCAddon.LOGGER.info("[GTDatapackGenerator] Датапак сгенерирован в: {}", datapackDir);
            
        } catch (Exception e) {
            GTEMCAddon.LOGGER.error("[GTDatapackGenerator] Ошибка при генерации датапака", e);
        }
    }

    /**
     * Получение пути к папке Minecraft.
     */
    private static Path getMinecraftDir() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            return Paths.get(userHome, "AppData", "Roaming", ".minecraft");
        } else if (os.contains("mac")) {
            return Paths.get(userHome, "Library", "Application Support", "minecraft");
        } else {
            return Paths.get(userHome, ".minecraft");
        }
    }

    /**
     * Генерация pack.mcmeta для датапака.
     */
    private static void generatePackMcmeta(Path datapackDir) throws IOException {
        JsonObject pack = new JsonObject();
        JsonObject packMeta = new JsonObject();
        packMeta.addProperty("pack_format", 15);
        packMeta.addProperty("description", "GTCe Modern EMC Addon - Auto-generated EMC values for GregTech");
        pack.add("pack", packMeta);
        
        writeFile(datapackDir.resolve("pack.mcmeta"), GSON.toJson(pack));
    }

    /**
     * Генерация базовых EMC значений для материалов.
     */
    private static void generateBaseValues(Path dataDir) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("comment", "Base EMC values for GregTech CEu Modern materials");
        
        JsonObject values = new JsonObject();
        JsonObject before = new JsonObject();
        
        // Добавляем базовые значения для слитков/пылей
        for (Map.Entry<String, Long> entry : MATERIAL_EMC.entrySet()) {
            String materialName = entry.getKey();
            long emc = entry.getValue();
            
            // Слиток
            before.addProperty("gtceu:" + materialName + "_ingot", emc);
            
            // Пыль (такая же стоимость)
            before.addProperty("gtceu:" + materialName + "_dust", emc);
        }
        
        values.add("before", before);
        root.add("values", values);
        
        writeFile(dataDir.resolve("base_values.json"), GSON.toJson(root));
        GTEMCAddon.LOGGER.info("[GTDatapackGenerator] Сгенерирован base_values.json с {} материалами", MATERIAL_EMC.size());
    }

    /**
     * Генерация конверсий для всех форм материалов.
     */
    private static void generateMaterialForms(Path dataDir) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("comment", "Conversions for all GregTech material forms");
        
        JsonObject groups = new JsonObject();
        
        for (Map.Entry<String, Long> materialEntry : MATERIAL_EMC.entrySet()) {
            String materialName = materialEntry.getKey();
            long baseEMC = materialEntry.getValue();
            
            JsonObject materialGroup = new JsonObject();
            materialGroup.addProperty("comment", "Forms of " + materialName);
            
            JsonArray conversions = new JsonArray();
            
            for (Map.Entry<String, Double> formEntry : FORM_MULTIPLIERS.entrySet()) {
                String formName = formEntry.getKey();
                double multiplier = formEntry.getValue();
                
                // Пропускаем базовые формы (ingot, dust) - они уже в base_values
                if (formName.equals("ingot") || formName.equals("dust")) continue;
                
                long formEMC = Math.max(1, (long)(baseEMC * multiplier));
                String itemId = "gtceu:" + materialName + "_" + formName;
                
                // Создаём конверсию: базовая форма → эта форма
                JsonObject conversion = new JsonObject();
                conversion.addProperty("output", itemId);
                
                JsonObject ingredients = new JsonObject();
                
                // Рассчитываем количество базового материала
                double ratio = multiplier;
                if (ratio >= 1) {
                    ingredients.addProperty("gtceu:" + materialName + "_ingot", (int)ratio);
                } else {
                    // Для маленьких форм используем дробные значения через count
                    conversion.addProperty("count", (int)(1.0 / ratio));
                    ingredients.addProperty("gtceu:" + materialName + "_ingot", 1);
                }
                
                conversion.add("ingredients", ingredients);
                conversions.add(conversion);
            }
            
            materialGroup.add("conversions", conversions);
            groups.add("forms_" + materialName, materialGroup);
        }
        
        root.add("groups", groups);
        
        writeFile(dataDir.resolve("material_forms.json"), GSON.toJson(root));
        GTEMCAddon.LOGGER.info("[GTDatapackGenerator] Сгенерирован material_forms.json");
    }

    /**
     * Генерация конверсий для жидкостей.
     */
    private static void generateFluidConversions(Path dataDir) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("comment", "Fluid EMC conversions for GregTech CEu Modern");
        
        JsonObject groups = new JsonObject();
        JsonObject fluidsGroup = new JsonObject();
        fluidsGroup.addProperty("comment", "GregTech material fluids");
        
        JsonArray conversions = new JsonArray();
        
        // Для каждого материала с жидкой формой
        for (Map.Entry<String, Long> entry : MATERIAL_EMC.entrySet()) {
            String materialName = entry.getKey();
            long ingotEMC = entry.getValue();
            
            // В GT 1 слиток = 144 mB
            // EMC за 1000 mB = ingotEMC * (1000/144) ≈ ingotEMC * 7
            long fluidEMC = ingotEMC * 7;
            
            // Устанавливаем базовое значение для жидкости
            JsonObject valueConversion = new JsonObject();
            valueConversion.addProperty("output", "FLUID|gtceu:" + materialName);
            valueConversion.addProperty("count", 1000);
            
            JsonObject ingredients = new JsonObject();
            ingredients.addProperty("gtceu:" + materialName + "_ingot", 7);
            valueConversion.add("ingredients", ingredients);
            
            conversions.add(valueConversion);
        }
        
        // Химические жидкости
        addChemicalFluid(conversions, "sulfuric_acid", 128);
        addChemicalFluid(conversions, "hydrochloric_acid", 96);
        addChemicalFluid(conversions, "nitric_acid", 128);
        addChemicalFluid(conversions, "hydrogen", 16);
        addChemicalFluid(conversions, "oxygen", 16);
        addChemicalFluid(conversions, "nitrogen", 16);
        addChemicalFluid(conversions, "chlorine", 32);
        addChemicalFluid(conversions, "methane", 64);
        addChemicalFluid(conversions, "ethylene", 96);
        addChemicalFluid(conversions, "benzene", 256);
        addChemicalFluid(conversions, "polyethylene", 192);
        addChemicalFluid(conversions, "polyvinyl_chloride", 256);
        addChemicalFluid(conversions, "polytetrafluoroethylene", 512);
        addChemicalFluid(conversions, "polybenzimidazole", 2048);
        addChemicalFluid(conversions, "epoxid", 384);
        addChemicalFluid(conversions, "rubber", 128);
        addChemicalFluid(conversions, "creosote", 8);
        addChemicalFluid(conversions, "seed_oil", 16);
        addChemicalFluid(conversions, "lubricant", 64);
        addChemicalFluid(conversions, "concrete", 4);
        
        fluidsGroup.add("conversions", conversions);
        groups.add("fluids", fluidsGroup);
        
        root.add("groups", groups);
        
        writeFile(dataDir.resolve("fluid_conversions.json"), GSON.toJson(root));
        GTEMCAddon.LOGGER.info("[GTDatapackGenerator] Сгенерирован fluid_conversions.json");
    }

    /**
     * Добавление химической жидкости в конверсии.
     */
    private static void addChemicalFluid(JsonArray conversions, String fluidName, int emcPerBucket) {
        JsonObject conversion = new JsonObject();
        conversion.addProperty("output", "FLUID|gtceu:" + fluidName);
        conversion.addProperty("count", 1000);
        
        // Используем FAKE|single_emc как базовую единицу
        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("FAKE|single_emc", emcPerBucket);
        conversion.add("ingredients", ingredients);
        
        conversions.add(conversion);
    }

    /**
     * Запись JSON в файл.
     */
    private static void writeFile(Path path, String content) throws IOException {
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(content);
        }
    }
}
