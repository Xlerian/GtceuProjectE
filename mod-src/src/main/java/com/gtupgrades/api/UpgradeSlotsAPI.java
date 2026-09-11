package com.gtupgrades.api;

import com.gregtechceu.gtceu.api.machine.MetaTileEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gtupgrades.handler.ExtraUpgradeSlotsHandler;
import com.gtupgrades.mixin.MetaTileEntityMixin;
import net.minecraft.world.item.ItemStack;

/**
 * API для доступа к дополнительным слотам апгрейдов из KubeJS и других модов
 */
public class UpgradeSlotsAPI {
    
    /**
     * Получение обработчика дополнительных слотов для машины
     * @param machine Машина GregTech
     * @return Обработчик слотов или null если не поддерживается
     */
    public static NotifiableItemStackHandler getExtraUpgradeSlots(MetaTileEntity machine) {
        if (machine instanceof MetaTileEntityMixin mixin) {
            return mixin.getExtraUpgradeSlotsHandler();
        }
        return null;
    }
    
    /**
     * Получение количества ускорителей в машине
     * @param machine Машина GregTech
     * @return Количество ускорителей
     */
    public static int getAcceleratorCount(MetaTileEntity machine) {
        if (machine instanceof MetaTileEntityMixin mixin) {
            return mixin.getAcceleratorCount();
        }
        return 0;
    }
    
    /**
     * Установка ускорителя в конкретный слот
     * @param machine Машина GregTech
     * @param slot Индекс слота (0-based)
     * @param stack Предмет-ускоритель
     * @return true если успешно установлено
     */
    public static boolean setAccelerator(MetaTileEntity machine, int slot, ItemStack stack) {
        NotifiableItemStackHandler handler = getExtraUpgradeSlots(machine);
        if (handler == null) return false;
        
        if (slot < 0 || slot >= handler.getSlots()) return false;
        
        if (!ExtraUpgradeSlotsHandler.isAccelerator(stack)) {
            return false; // Не является ускорителем
        }
        
        handler.setStackInSlot(slot, stack);
        return true;
    }
    
    /**
     * Очистка всех слотов ускорителей
     * @param machine Машина GregTech
     */
    public static void clearAccelerators(MetaTileEntity machine) {
        NotifiableItemStackHandler handler = getExtraUpgradeSlots(machine);
        if (handler == null) return;
        
        for (int i = 0; i < handler.getSlots(); i++) {
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
    
    /**
     * Расчёт модификатора скорости для машины
     * @param machine Машина GregTech
     * @return Множитель длительности рецепта
     */
    public static float getSpeedModifier(MetaTileEntity machine) {
        int acceleratorCount = getAcceleratorCount(machine);
        return ExtraUpgradeSlotsHandler.calculateSpeedModifier(acceleratorCount);
    }
    
    /**
     * Расчёт модификатора энергии для машины
     * @param machine Машина GregTech
     * @return Множитель потребления энергии
     */
    public static float getEnergyModifier(MetaTileEntity machine) {
        int acceleratorCount = getAcceleratorCount(machine);
        return ExtraUpgradeSlotsHandler.calculateEnergyModifier(acceleratorCount);
    }
}
