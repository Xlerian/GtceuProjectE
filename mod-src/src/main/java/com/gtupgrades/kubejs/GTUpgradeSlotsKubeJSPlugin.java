package com.gtupgrades.kubejs;

import com.gtupgrades.GTUpgradeSlotsMod;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.ScriptType;

/**
 * KubeJS плагин для предоставления API дополнительных слотов апгрейдов
 */
public class GTUpgradeSlotsKubeJSPlugin extends KubeJSPlugin {
    
    /**
     * Регистрация плагина в KubeJS
     */
    public static void register() {
        try {
            KubeJSPlugin.registerPlugin(new GTUpgradeSlotsKubeJSPlugin());
            GTUpgradeSlotsMod.LOGGER.info("[GTUpgradeSlots] KubeJS плагин зарегистрирован");
        } catch (Exception e) {
            GTUpgradeSlotsMod.LOGGER.error("[GTUpgradeSlots] Ошибка регистрации KubeJS плагина", e);
        }
    }
    
    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public void registerEvents() {
        super.registerEvents();
        
        // Регистрация типов событий
        // GTUpgradeSlotsEvents.register();
    }
    
    @Override
    public void registerClasses(ScriptType type, dev.latvian.mods.rhino.ClassShutter clazz) {
        super.registerClasses(type, clazz);
        
        // Предоставление API в скрипты
        if (type == ScriptType.SERVER) {
            // Регистрация классов для доступа из скриптов
            clazz.allowClass("com.gtupgrades.api.UpgradeSlotsAPI");
            clazz.allowClass("com.gtupgrades.handler.ExtraUpgradeSlotsHandler");
        }
    }
}
