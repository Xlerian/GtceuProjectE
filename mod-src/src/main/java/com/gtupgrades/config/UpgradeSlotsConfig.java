package com.gtupgrades.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Конфигурация мода GT Upgrade Slots
 */
public class UpgradeSlotsConfig {
    
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    // Количество дополнительных слотов для апгрейдов
    public static final ForgeConfigSpec.IntValue EXTRA_UPGRADE_SLOTS;
    
    // Множитель скорости за каждый ускоритель (0.25 = 25% ускорение)
    public static final ForgeConfigSpec.DoubleValue SPEED_MULTIPLIER_PER_ACCELERATOR;
    
    // Множитель энергии за каждый ускоритель (0.40 = 40% увеличение потребления)
    public static final ForgeConfigSpec.DoubleValue ENERGY_MULTIPLIER_PER_ACCELERATOR;
    
    // Минимальное время выполнения рецепта (в тиках)
    public static final ForgeConfigSpec.IntValue MIN_RECIPE_DURATION;
    
    // ID предмета-ускорителя
    public static final ForgeConfigSpec.ConfigValue<String> ACCELERATOR_ITEM_ID;
    
    // Включить логирование
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING;
    
    static {
        BUILDER.comment("GT Upgrade Slots Configuration")
               .push("general");
        
        EXTRA_UPGRADE_SLOTS = BUILDER
                .comment("Количество дополнительных слотов для апгрейдов-ускорителей")
                .defineInRange("extraUpgradeSlots", 4, 0, 16);
        
        SPEED_MULTIPLIER_PER_ACCELERATOR = BUILDER
                .comment("Множитель скорости за каждый ускоритель (0.25 = 25% ускорение)")
                .defineInRange("speedMultiplier", 0.25, 0.0, 1.0);
        
        ENERGY_MULTIPLIER_PER_ACCELERATOR = BUILDER
                .comment("Множитель энергии за каждый ускоритель (0.40 = 40% увеличение)")
                .defineInRange("energyMultiplier", 0.40, 0.0, 2.0);
        
        MIN_RECIPE_DURATION = BUILDER
                .comment("Минимальное время выполнения рецепта (в тиках)")
                .defineInRange("minRecipeDuration", 1, 1, 100);
        
        ACCELERATOR_ITEM_ID = BUILDER
                .comment("ID предмета-ускорителя (namespace:item)")
                .define("acceleratorItemId", "gtupgradeslots:accelerator_plate");
        
        DEBUG_LOGGING = BUILDER
                .comment("Включить подробное логирование")
                .define("debugLogging", false);
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
