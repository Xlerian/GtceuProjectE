package com.gtupgrades.handler;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gtupgrades.config.UpgradeSlotsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Обработчик дополнительных слотов для апгрейдов-ускорителей
 */
public class ExtraUpgradeSlotsHandler {
    
    /**
     * Проверка, является ли предмет ускорителем
     */
    public static boolean isAccelerator(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        String acceleratorId = UpgradeSlotsConfig.ACCELERATOR_ITEM_ID.get();
        ResourceLocation acceleratorRL = new ResourceLocation(acceleratorId);
        
        return ForgeRegistries.ITEMS.getKey(stack.getItem()).equals(acceleratorRL);
    }
    
    /**
     * Подсчёт количества ускорителей в обработчике
     */
    public static int countAccelerators(NotifiableItemStackHandler handler) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (isAccelerator(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }
    
    /**
     * Расчёт модификатора скорости на основе количества ускорителей
     * @return Множитель длительности рецепта (меньше = быстрее)
     */
    public static float calculateSpeedModifier(int acceleratorCount) {
        if (acceleratorCount <= 0) return 1.0f;
        
        double speedMultiplier = UpgradeSlotsConfig.SPEED_MULTIPLIER_PER_ACCELERATOR.get();
        float modifier = 1.0f - (float)(acceleratorCount * speedMultiplier);
        
        // Минимальный множитель - не меньше 0.1 (10% от исходного времени)
        return Math.max(0.1f, modifier);
    }
    
    /**
     * Расчёт модификатора энергии на основе количества ускорителей
     * @return Множитель потребления энергии (больше = больше потребление)
     */
    public static float calculateEnergyModifier(int acceleratorCount) {
        if (acceleratorCount <= 0) return 1.0f;
        
        double energyMultiplier = UpgradeSlotsConfig.ENERGY_MULTIPLIER_PER_ACCELERATOR.get();
        return 1.0f + (float)(acceleratorCount * energyMultiplier);
    }
}
