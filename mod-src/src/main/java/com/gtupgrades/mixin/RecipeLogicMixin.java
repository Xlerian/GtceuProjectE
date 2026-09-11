package com.gtupgrades.mixin;

import com.gregtechceu.gtceu.api.machine.MetaTileEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gtupgrades.config.UpgradeSlotsConfig;
import com.gtupgrades.handler.ExtraUpgradeSlotsHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin к логике рецептов для применения модификаторов ускорителей
 */
@Mixin(value = RecipeHelper.class, remap = false)
public abstract class RecipeLogicMixin {
    
    /**
     * Модификация длительности рецепта на основе ускорителей
     */
    @Inject(method = "getRecipeDuration", at = @At("HEAD"), cancellable = true)
    private static void modifyRecipeDuration(GTRecipe recipe, CallbackInfoReturnable<Integer> cir) {
        // Получаем машину из контекста (если доступна)
        // В реальной реализации нужно передавать машину через параметры
        // Здесь показан пример логики
        
        int baseDuration = recipe.duration;
        
        // Получаем количество ускорителей (в реальной реализации из машины)
        int acceleratorCount = getAcceleratorCountFromContext();
        
        if (acceleratorCount > 0) {
            float speedModifier = ExtraUpgradeSlotsHandler.calculateSpeedModifier(acceleratorCount);
            int modifiedDuration = Math.round(baseDuration * speedModifier);
            
            // Защита от отрицательных значений
            int minDuration = UpgradeSlotsConfig.MIN_RECIPE_DURATION.get();
            modifiedDuration = Math.max(minDuration, modifiedDuration);
            
            cir.setReturnValue(modifiedDuration);
        }
    }
    
    /**
     * Модификация потребления энергии на основе ускорителей
     */
    @Inject(method = "getRecipeEUt", at = @At("HEAD"), cancellable = true)
    private static void modifyRecipeEnergy(GTRecipe recipe, CallbackInfoReturnable<Long> cir) {
        long baseEUt = RecipeHelper.getRecipeEUt(recipe);
        
        int acceleratorCount = getAcceleratorCountFromContext();
        
        if (acceleratorCount > 0) {
            float energyModifier = ExtraUpgradeSlotsHandler.calculateEnergyModifier(acceleratorCount);
            long modifiedEUt = Math.round(baseEUt * energyModifier);
            
            cir.setReturnValue(modifiedEUt);
        }
    }
    
    /**
     * Получение количества ускорителей из контекста
     * В реальной реализации нужно получать из текущей машины
     */
    private static int getAcceleratorCountFromContext() {
        // TODO: Реализовать получение машины из контекста выполнения рецепта
        // Это требует дополнительного механизма передачи контекста
        return 0; // Заглушка
    }
}
