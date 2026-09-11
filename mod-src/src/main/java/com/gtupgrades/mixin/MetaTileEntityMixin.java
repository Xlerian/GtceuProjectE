package com.gtupgrades.mixin;

import com.gregtechceu.gtceu.api.machine.MetaTileEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gtupgrades.config.UpgradeSlotsConfig;
import com.gtupgrades.handler.ExtraUpgradeSlotsHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin к базовому классу MetaTileEntity для добавления дополнительных слотов апгрейдов
 */
@Mixin(value = MetaTileEntity.class, remap = false)
public abstract class MetaTileEntityMixin {
    
    @Shadow
    protected abstract void initializeInventory();
    
    private NotifiableItemStackHandler extraUpgradeSlotsHandler;
    
    /**
     * Инжект после инициализации инвентаря для добавления дополнительных слотов
     */
    @Inject(method = "initializeInventory", at = @At("TAIL"))
    private void afterInitializeInventory(CallbackInfo ci) {
        MetaTileEntity self = (MetaTileEntity) (Object) this;
        
        // Создаём обработчик дополнительных слотов
        int extraSlots = UpgradeSlotsConfig.EXTRA_UPGRADE_SLOTS.get();
        if (extraSlots > 0) {
            extraUpgradeSlotsHandler = new NotifiableItemStackHandler(self, extraSlots, ExtraUpgradeSlotsHandler::isAccelerator);
            extraUpgradeSlotsHandler.setFilter(ExtraUpgradeSlotsHandler::isAccelerator);
            
            if (UpgradeSlotsConfig.DEBUG_LOGGING.get()) {
                GTUpgradeSlotsMod.LOGGER.debug("[GTUpgradeSlots] Добавлено {} дополнительных слотов для машины {}", 
                    extraSlots, self.getIdentityPos());
            }
        }
    }
    
    /**
     * Получение обработчика дополнительных слотов апгрейдов
     */
    public NotifiableItemStackHandler getExtraUpgradeSlotsHandler() {
        return extraUpgradeSlotsHandler;
    }
    
    /**
     * Получение количества ускорителей в дополнительных слотах
     */
    public int getAcceleratorCount() {
        if (extraUpgradeSlotsHandler == null) return 0;
        return ExtraUpgradeSlotsHandler.countAccelerators(extraUpgradeSlotsHandler);
    }
}
