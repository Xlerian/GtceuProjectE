package com.gtemc;

import com.gtemc.datapack.GTDatapackGenerator;
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
 * GTCe Modern EMC Addon - Главный класс мода.
 * Генерирует датапак для ProjectE с EMC значениями для GregTech CEu Modern.
 */
@Mod(GTEMCAddon.MOD_ID)
public class GTEMCAddon {

    public static final String MOD_ID = "gtcemcaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public GTEMCAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("[GTEMCAddon] Мод инициализирован");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[GTEMCAddon] Common setup - генерация датапака для ProjectE");
        
        // Генерируем датапак при загрузке
        event.enqueueWork(() -> {
            try {
                GTDatapackGenerator.generateDatapack();
                LOGGER.info("[GTEMCAddon] Датапак успешно сгенерирован");
            } catch (Exception e) {
                LOGGER.error("[GTEMCAddon] Ошибка при генерации датапака", e);
            }
        });
    }
}
