package com.gtemc.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Конфигурация мода GTEMCAddon.
 * Позволяет настроить параметры расчёта EMC.
 */
public class GTEMCConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Максимальное количество итераций калькулятора
    public static final ForgeConfigSpec.IntValue MAX_ITERATIONS;
    
    // Минимальный шанс выхода для учёта побочного продукта (0-100)
    public static final ForgeConfigSpec.IntValue MIN_CHANCE_THRESHOLD;
    
    // Базовое EMC за 1000 mB жидкости (если не определено иначе)
    public static final ForgeConfigSpec.IntValue DEFAULT_FLUID_EMC_PER_BUCKET;
    
    // Включить расчёт для жидкостей
    public static final ForgeConfigSpec.BooleanValue CALCULATE_FLUIDS;
    
    // Включить расчёт для предметов
    public static final ForgeConfigSpec.BooleanValue CALCULATE_ITEMS;
    
    // Логировать процесс расчёта
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING;

    static {
        BUILDER.comment("GTCe Modern EMC Addon Configuration")
               .push("general");

        MAX_ITERATIONS = BUILDER
                .comment("Максимальное количество итераций калькулятора EMC. " +
                         "Увеличьте для сложных цепочек (микросхемы, наноматериалы).")
                .defineInRange("maxIterations", 20, 5, 50);

        MIN_CHANCE_THRESHOLD = BUILDER
                .comment("Минимальный шанс выхода (в процентах) для учёта побочного продукта. " +
                         "Рецепты с шансом ниже этого значения будут игнорироваться.")
                .defineInRange("minChanceThreshold", 100, 0, 100);

        DEFAULT_FLUID_EMC_PER_BUCKET = BUILDER
                .comment("Базовое EMC за 1000 mB (1 ведро) жидкости, если EMC не определено из рецептов.")
                .defineInRange("defaultFluidEMCPerBucket", 0, 0, 1000000);

        CALCULATE_FLUIDS = BUILDER
                .comment("Включить автоматический расчёт EMC для жидкостей GregTech.")
                .define("calculateFluids", true);

        CALCULATE_ITEMS = BUILDER
                .comment("Включить автоматический расчёт EMC для предметов GregTech.")
                .define("calculateItems", true);

        DEBUG_LOGGING = BUILDER
                .comment("Включить подробное логирование процесса расчёта EMC.")
                .define("debugLogging", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
