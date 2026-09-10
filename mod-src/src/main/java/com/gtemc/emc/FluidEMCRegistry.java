package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.api.nss.NSSFluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * Реестр EMC для жидкостей GregTech.
 * Управляет EMC-стоимостью жидкостей из GTCEu Modern.
 * 
 * Базовое соотношение: 1000 mB (1 ведро) = базовая EMC стоимость жидкости.
 * При расчёте рецептов: EMC = (emcPerBucket * recipeAmount) / 1000
 */
public class FluidEMCRegistry {

    // EMC за 1000 mB для каждой жидкости
    private static final Map<ResourceLocation, Long> fluidEMCValues = new HashMap<>();
    
    // Жидкости с приоритетом (не будут перезаписаны)
    private static final Map<ResourceLocation, Long> priorityFluids = new HashMap<>();

    /**
     * Инициализация реестра базовыми значениями жидкостей.
     */
    public static void initialize(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Инициализация реестра жидкостей EMC...");
        
        // Базовые ванильные жидкости
        registerBaseFluids();
        
        // Жидкости GregTech материалов
        registerGTMaterialFluids();
        
        // Применяем к мапперу ProjectE
        applyToMapper(mapper);
        
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Инициализировано {} жидкостей с EMC", fluidEMCValues.size());
    }

    /**
     * Регистрация базовых ванильных жидкостей.
     */
    private static void registerBaseFluids() {
        // Вода - базовая стоимость
        registerFluid("minecraft:water", 1L);
        
        // Лава - стоимость как у обсидиана (64) * пропорция
        registerFluid("minecraft:lava", 64L);
        
        // Молоко
        registerFluid("minecraft:milk", 128L);
    }

    /**
     * Регистрация жидкостей GregTech материалов.
     * EMC жидкости = EMC слитка * (1000 / 144) ≈ EMC слитка * 7
     * (т.к. 1 слиток = 144 mB в GT)
     */
    private static void registerGTMaterialFluids() {
        // Металлы (жидкое состояние в EBF/печи)
        registerFluid("gtceu:iron", 256L * 7);           // 1792
        registerFluid("gtceu:gold", 2048L * 7);          // 14336
        registerFluid("gtceu:copper", 128L * 7);         // 896
        registerFluid("gtceu:tin", 64L * 7);             // 448
        registerFluid("gtceu:bronze", 96L * 7);          // 672
        registerFluid("gtceu:silver", 512L * 7);         // 3584
        registerFluid("gtceu:lead", 64L * 7);            // 448
        registerFluid("gtceu:nickel", 128L * 7);         // 896
        registerFluid("gtceu:zinc", 64L * 7);            // 448
        registerFluid("gtceu:brass", 96L * 7);           // 672
        registerFluid("gtceu:steel", 512L * 7);          // 3584
        registerFluid("gtceu:stainless_steel", 1024L * 7); // 7168
        registerFluid("gtceu:titanium", 4096L * 7);      // 28672
        registerFluid("gtceu:tungsten", 2048L * 7);      // 14336
        registerFluid("gtceu:tungsten_steel", 4096L * 7); // 28672
        registerFluid("gtceu:osmium", 4096L * 7);        // 28672
        registerFluid("gtceu:platinum", 4096L * 7);      // 28672
        registerFluid("gtceu:iridium", 8192L * 7);       // 57344
        registerFluid("gtceu:aluminium", 512L * 7);      // 3584
        registerFluid("gtceu:chrome", 1024L * 7);        // 7168
        registerFluid("gtceu:electrum", 1024L * 7);      // 7168
        registerFluid("gtceu:invar", 256L * 7);          // 1792
        registerFluid("gtceu:cupronickel", 128L * 7);    // 896
        registerFluid("gtceu:kanthal", 256L * 7);        // 1792
        registerFluid("gtceu:nichrome", 512L * 7);       // 3584
        registerFluid("gtceu:magnalium", 256L * 7);      // 1792
        registerFluid("gtceu:red_alloy", 128L * 7);      // 896
        registerFluid("gtceu:blue_alloy", 512L * 7);     // 3584
        registerFluid("gtceu:soldering_alloy", 128L * 7); // 896
        registerFluid("gtceu:battery_alloy", 128L * 7);  // 896
        registerFluid("gtceu:naquadah", 16384L * 7);     // 114688
        registerFluid("gtceu:naquadah_alloy", 32768L * 7); // 229376
        registerFluid("gtceu:enriched_naquadah", 65536L * 7); // 458752
        registerFluid("gtceu:naquadria", 131072L * 7);   // 917504
        registerFluid("gtceu:neutronium", 262144L * 7);  // 1835008
        
        // Химические жидкости
        registerFluid("gtceu:sulfuric_acid", 128L);      // H2SO4
        registerFluid("gtceu:hydrochloric_acid", 96L);   // HCl
        registerFluid("gtceu:nitric_acid", 128L);        // HNO3
        registerFluid("gtceu:nitrogen_dioxide", 64L);    // NO2
        registerFluid("gtceu:sulfur_dioxide", 48L);      // SO2
        registerFluid("gtceu:sulfur_trioxide", 64L);     // SO3
        registerFluid("gtceu:chlorine", 32L);            // Cl2
        registerFluid("gtceu:hydrogen", 16L);            // H2
        registerFluid("gtceu:oxygen", 16L);              // O2
        registerFluid("gtceu:nitrogen", 16L);            // N2
        registerFluid("gtceu:fluorine", 32L);            // F2
        registerFluid("gtceu:methane", 64L);             // CH4
        registerFluid("gtceu:ethane", 96L);              // C2H6
        registerFluid("gtceu:propane", 128L);            // C3H8
        registerFluid("gtceu:butane", 160L);             // C4H10
        registerFluid("gtceu:benzene", 256L);            // C6H6
        registerFluid("gtceu:toluene", 288L);            // C7H8
        registerFluid("gtceu:phenol", 320L);             // C6H5OH
        registerFluid("gtceu:ethylene", 96L);            // C2H4
        registerFluid("gtceu:propylene", 128L);          // C3H6
        registerFluid("gtceu:vinyl_chloride", 160L);     // C2H3Cl
        registerFluid("gtceu:styrene", 320L);            // C8H8
        registerFluid("gtceu:polyethylene", 192L);       // (C2H4)n
        registerFluid("gtceu:polyvinyl_chloride", 256L); // (C2H3Cl)n
        registerFluid("gtceu:polytetrafluoroethylene", 512L); // PTFE
        registerFluid("gtceu:polybenzimidazole", 2048L); // PBI
        registerFluid("gtceu:epoxid", 384L);             // Epoxid
        registerFluid("gtceu:polyphenylene_sulfide", 512L); // PPS
        registerFluid("gtceu:rubber", 128L);             // Резина
        registerFluid("gtceu:glycerol", 192L);           // Глицерин
        registerFluid("gtceu:ethanol", 128L);            // Этанол
        registerFluid("gtceu:methanol", 96L);            // Метанол
        registerFluid("gtceu:acetone", 160L);            // Ацетон
        registerFluid("gtceu:creosote", 8L);             // Креозот
        registerFluid("gtceu:seed_oil", 16L);            // Растительное масло
        registerFluid("gtceu:fish_oil", 16L);            // Рыбий жир
        registerFluid("gtceu:lubricant", 64L);           // Смазка
        registerFluid("gtceu:glue", 32L);                // Клей
        registerFluid("gtceu:drilling_fluid", 48L);      // Буровой раствор
        registerFluid("gtceu:concrete", 4L);             // Бетон
        registerFluid("gtceu:superconductor_base", 4096L); // Основа сверхпроводника
        registerFluid("gtceu:uranium_hexafluoride", 8192L); // UF6
        registerFluid("gtceu:enriched_uranium_hexafluoride", 16384L); // обогащённый UF6
        
        // Приоритетные жидкости (не перезаписываются)
        setPriority("minecraft:water", 1L);
        setPriority("minecraft:lava", 64L);
    }

    /**
     * Регистрация жидкости с EMC.
     */
    private static void registerFluid(String fluidId, long emcPerBucket) {
        ResourceLocation rl = new ResourceLocation(fluidId);
        fluidEMCValues.put(rl, emcPerBucket);
    }

    /**
     * Установка приоритетной жидкости (не будет перезаписана калькулятором).
     */
    private static void setPriority(String fluidId, long emcPerBucket) {
        ResourceLocation rl = new ResourceLocation(fluidId);
        priorityFluids.put(rl, emcPerBucket);
        fluidEMCValues.put(rl, emcPerBucket);
    }

    /**
     * Применение зарегистрированных значений к мапперу ProjectE.
     */
    private static void applyToMapper(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        for (Map.Entry<ResourceLocation, Long> entry : fluidEMCValues.entrySet()) {
            ResourceLocation fluidRL = entry.getKey();
            long emcPerBucket = entry.getValue();
            
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidRL);
            if (fluid == null || fluid == Fluids.EMPTY) continue;
            
            try {
                // Создаём NSS для жидкости (1000 mB = 1 ведро)
                NSSFluid nssFluid = NSSFluid.createFluid(fluidRL);
                mapper.setValueBefore(nssFluid, emcPerBucket);
                
                if (GTEMCConfig.DEBUG_LOGGING.get()) {
                    GTEMCAddon.LOGGER.debug("[GTEMCAddon] Установлен EMC {} за 1000mB для жидкости {}", 
                            emcPerBucket, fluidRL);
                }
            } catch (Exception e) {
                GTEMCAddon.LOGGER.warn("[GTEMCAddon] Не удалось установить EMC для жидкости {}: {}", 
                        fluidRL, e.getMessage());
            }
        }
    }

    /**
     * Получение EMC для жидкости (за 1000 mB).
     */
    public static long getFluidEMC(ResourceLocation fluidRL) {
        return fluidEMCValues.getOrDefault(fluidRL, 0L);
    }

    /**
     * Получение EMC для жидкости по FluidStack.
     * Возвращает пропорциональную EMC для указанного количества mB.
     */
    public static long getFluidStackEMC(FluidStack fluidStack) {
        ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
        if (fluidRL == null) return 0;
        
        long emcPerBucket = fluidEMCValues.getOrDefault(fluidRL, 0L);
        if (emcPerBucket <= 0) return 0;
        
        // Пропорционально: (emcPerBucket * amount) / 1000
        return (emcPerBucket * fluidStack.getAmount()) / 1000L;
    }

    /**
     * Проверка, является ли жидкость приоритетной.
     */
    public static boolean isPriorityFluid(ResourceLocation fluidRL) {
        return priorityFluids.containsKey(fluidRL);
    }

    /**
     * Регистрация нового EMC для жидкости (используется калькулятором).
     * Не перезаписывает приоритетные жидкости.
     */
    public static boolean registerFluidEMC(ResourceLocation fluidRL, long emcPerBucket) {
        if (isPriorityFluid(fluidRL)) {
            return false; // Не перезаписываем приоритетные
        }
        if (fluidEMCValues.containsKey(fluidRL)) {
            return false; // Уже есть значение
        }
        fluidEMCValues.put(fluidRL, emcPerBucket);
        return true;
    }

    /**
     * Получение всех зарегистрированных жидкостей.
     */
    public static Map<ResourceLocation, Long> getAllFluidEMC() {
        return new HashMap<>(fluidEMCValues);
    }
}
