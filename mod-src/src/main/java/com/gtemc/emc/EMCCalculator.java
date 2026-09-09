package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Калькулятор EMC для GregTech CEu Modern.
 * Финализирует расчёт и применяет значения к ProjectE.
 * 
 * Этот класс отвечает за:
 * 1. Применение рассчитанных EMC к мапперу ProjectE
 * 2. Разрешение конфликтов (если несколько рецептов дают разный EMC)
 * 3. Финальную валидацию значений
 */
public class EMCCalculator {

    private final Map<ResourceLocation, Long> itemEMC = new HashMap<>();
    private final Map<ResourceLocation, Long> fluidEMC = new HashMap<>();
    private final Map<ResourceLocation, List<Long>> itemEMCCandidates = new HashMap<>();
    private final Map<ResourceLocation, List<Long>> fluidEMCCandidates = new HashMap<>();

    /**
     * Добавление кандидата EMC для предмета.
     * Если уже есть значение, выбирается минимальное (для предотвращения эксплойтов).
     */
    public void addItemEMCCandidate(ResourceLocation itemRL, long emc) {
        if (emc <= 0) return;
        
        itemEMCCandidates.computeIfAbsent(itemRL, k -> new ArrayList<>()).add(emc);
        
        // Выбираем минимальное значение (защита от завышения через дорогие рецепты)
        long currentBest = itemEMC.getOrDefault(itemRL, Long.MAX_VALUE);
        if (emc < currentBest) {
            itemEMC.put(itemRL, emc);
        }
    }

    /**
     * Добавление кандидата EMC для жидкости.
     */
    public void addFluidEMCCandidate(ResourceLocation fluidRL, long emcPerBucket) {
        if (emcPerBucket <= 0) return;
        
        fluidEMCCandidates.computeIfAbsent(fluidRL, k -> new ArrayList<>()).add(emcPerBucket);
        
        // Для жидкостей тоже берём минимальное
        long currentBest = fluidEMC.getOrDefault(fluidRL, Long.MAX_VALUE);
        if (emcPerBucket < currentBest) {
            fluidEMC.put(fluidRL, emcPerBucket);
        }
    }

    /**
     * Расчёт EMC предмета на основе рецепта.
     * 
     * @param inputs список входов (ItemStack или FluidStack)
     * @param output выходной ItemStack
     * @return рассчитанный EMC или -1 если не все ингредиенты имеют EMC
     */
    public long calculateItemEMCFromRecipe(List<Object> inputs, ItemStack output) {
        long totalInputEMC = 0;
        
        for (Object input : inputs) {
            if (input instanceof ItemStack itemInput) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(itemInput.getItem());
                if (rl == null) return -1;
                
                Long emc = itemEMC.get(rl);
                if (emc == null) return -1;
                
                totalInputEMC += emc * itemInput.getCount();
            } else if (input instanceof FluidStack fluidInput) {
                ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidInput.getFluid());
                if (fluidRL == null) return -1;
                
                Long emcPerBucket = fluidEMC.get(fluidRL);
                if (emcPerBucket == null) return -1;
                
                // Пропорционально: (emcPerBucket * amount) / 1000
                int amount = fluidInput.getAmount();
                totalInputEMC += (emcPerBucket * amount) / 1000L;
            }
        }
        
        // EMC за единицу выхода
        return totalInputEMC / output.getCount();
    }

    /**
     * Расчёт EMC жидкости на основе рецепта.
     * 
     * @param inputs список входов (ItemStack или FluidStack)
     * @param output выходной FluidStack
     * @return EMC за 1000 mB или -1 если не все ингредиенты имеют EMC
     */
    public long calculateFluidEMCFromRecipe(List<Object> inputs, FluidStack output) {
        long totalInputEMC = 0;
        
        for (Object input : inputs) {
            if (input instanceof ItemStack itemInput) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(itemInput.getItem());
                if (rl == null) return -1;
                
                Long emc = itemEMC.get(rl);
                if (emc == null) return -1;
                
                totalInputEMC += emc * itemInput.getCount();
            } else if (input instanceof FluidStack fluidInput) {
                ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidInput.getFluid());
                if (fluidRL == null) return -1;
                
                Long emcPerBucket = fluidEMC.get(fluidRL);
                if (emcPerBucket == null) return -1;
                
                int amount = fluidInput.getAmount();
                totalInputEMC += (emcPerBucket * amount) / 1000L;
            }
        }
        
        // EMC за 1000 mB выходной жидкости
        int outputAmount = output.getAmount();
        return (totalInputEMC * 1000L) / outputAmount;
    }

    /**
     * Применение всех рассчитанных значений к мапперу ProjectE.
     */
    public <T extends NormalizedSimpleStack<T, ?>> void applyToMapper(IMappingCollector<T, Long> mapper) {
        GTEMCAddon.LOGGER.info("[EMCCalculator] Применение {} предметов и {} жидкостей к ProjectE...",
                itemEMC.size(), fluidEMC.size());
        
        int appliedItems = 0;
        int appliedFluids = 0;
        int errors = 0;
        
        // Применяем EMC для предметов
        for (Map.Entry<ResourceLocation, Long> entry : itemEMC.entrySet()) {
            ResourceLocation rl = entry.getKey();
            long emc = entry.getValue();
            
            try {
                NSSItem nssItem = NSSItem.createItem(rl);
                mapper.setValueBefore(nssItem, emc);
                appliedItems++;
            } catch (Exception e) {
                errors++;
                if (GTEMCConfig.DEBUG_LOGGING.get()) {
                    GTEMCAddon.LOGGER.warn("[EMCCalculator] Ошибка применения EMC для {}: {}", rl, e.getMessage());
                }
            }
        }
        
        // Применяем EMC для жидкостей
        for (Map.Entry<ResourceLocation, Long> entry : fluidEMC.entrySet()) {
            ResourceLocation rl = entry.getKey();
            long emcPerBucket = entry.getValue();
            
            try {
                NSSFluid nssFluid = NSSFluid.createFluid(rl);
                mapper.setValueBefore(nssFluid, emcPerBucket);
                appliedFluids++;
            } catch (Exception e) {
                errors++;
                if (GTEMCConfig.DEBUG_LOGGING.get()) {
                    GTEMCAddon.LOGGER.warn("[EMCCalculator] Ошибка применения EMC для жидкости {}: {}", rl, e.getMessage());
                }
            }
        }
        
        GTEMCAddon.LOGGER.info("[EMCCalculator] Применено: {} предметов, {} жидкостей, {} ошибок",
                appliedItems, appliedFluids, errors);
    }

    /**
     * Валидация рассчитанных EMC.
     * Проверяет, что все значения разумны (не отрицательные, не слишком большие).
     */
    public void validate() {
        long maxReasonableEMC = 10_000_000_000L; // 10 миллиардов
        
        // Валидация предметов
        itemEMC.entrySet().removeIf(entry -> {
            if (entry.getValue() <= 0) {
                GTEMCAddon.LOGGER.warn("[EMCCalculator] Удалён невалидный EMC для предмета {}: {}",
                        entry.getKey(), entry.getValue());
                return true;
            }
            if (entry.getValue() > maxReasonableEMC) {
                GTEMCAddon.LOGGER.warn("[EMCCalculator] EMC для предмета {} слишком велик: {}. Ограничиваем.",
                        entry.getKey(), entry.getValue());
                entry.setValue(maxReasonableEMC);
            }
            return false;
        });
        
        // Валидация жидкостей
        fluidEMC.entrySet().removeIf(entry -> {
            if (entry.getValue() <= 0) {
                GTEMCAddon.LOGGER.warn("[EMCCalculator] Удалён невалидный EMC для жидкости {}: {}",
                        entry.getKey(), entry.getValue());
                return true;
            }
            if (entry.getValue() > maxReasonableEMC) {
                GTEMCAddon.LOGGER.warn("[EMCCalculator] EMC для жидкости {} слишком велик: {}. Ограничиваем.",
                        entry.getKey(), entry.getValue());
                entry.setValue(maxReasonableEMC);
            }
            return false;
        });
    }

    /**
     * Получение финальной карты EMC для предметов.
     */
    public Map<ResourceLocation, Long> getItemEMC() {
        return Collections.unmodifiableMap(itemEMC);
    }

    /**
     * Получение финальной карты EMC для жидкостей.
     */
    public Map<ResourceLocation, Long> getFluidEMC() {
        return Collections.unmodifiableMap(fluidEMC);
    }

    /**
     * Получение статистики кандидатов.
     */
    public int getTotalCandidates() {
        int total = 0;
        for (List<Long> candidates : itemEMCCandidates.values()) {
            total += candidates.size();
        }
        for (List<Long> candidates : fluidEMCCandidates.values()) {
            total += candidates.size();
        }
        return total;
    }
}
