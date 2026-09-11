package com.gtupgrades;

import com.gtupgrades.config.UpgradeSlotsConfig;
import com.gtupgrades.kubejs.GTUpgradeSlotsKubeJSPlugin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GT Upgrade Slots - Главный класс мода
 * Добавляет дополнительные слоты для апгрейдов-ускорителей в машины GregTech Modern
 */
@Mod(GTUpgradeSlotsMod.MOD_ID)
public class GTUpgradeSlotsMod {
    
    public static final String MOD_ID = "gtupgradeslots";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public GTUpgradeSlotsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Регистрация конфигурации
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, UpgradeSlotsConfig.SPEC, "gtupgradeslots-common.toml");
        
        // Инициализация
        modEventBus.addListener(this::commonSetup);
        
        // Регистрация KubeJS плагина (если KubeJS загружен)
        if (isKubeJSLoaded()) {
            LOGGER.info("[GTUpgradeSlots] KubeJS обнаружен, регистрируем плагин...");
            GTUpgradeSlotsKubeJSPlugin.register();
        }
        
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("[GTUpgradeSlots] Мод инициализирован");
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[GTUpgradeSlots] Common setup завершён");
    }
    
    /**
     * Проверка загружен ли KubeJS
     */
    private boolean isKubeJSLoaded() {
        try {
            Class.forName("dev.latvian.mods.kubejs.KubeJSPlugin");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
