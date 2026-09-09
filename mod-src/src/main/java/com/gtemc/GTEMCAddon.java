package com.gtemc;

import com.gtemc.config.GTEMCConfig;
import com.gtemc.emc.GTEMCMapper;
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
 * Автоматически рассчитывает и назначает EMC-стоимость для всех предметов
 * и жидкостей из GregTech CEu Modern, используя ProjectE API.
 */
@Mod(GTEMCAddon.MOD_ID)
public class GTEMCAddon {

    public static final String MOD_ID = "gtcemcaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public GTEMCAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Регистрация конфигурации
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GTEMCConfig.SPEC, "gtcemcaddon-common.toml");

        // Инициализация при загрузке
        modEventBus.addListener(this::commonSetup);

        // Регистрация в Forge EventBus
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("[GTEMCAddon] Мод инициализирован. Версия: {}", MOD_ID);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[GTEMCAddon] Common setup - регистрация EMC маппера");
        GTEMCMapper.register();
    }
}
