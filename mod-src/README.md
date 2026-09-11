# ⚙️ GT Upgrade Slots

**Мод для Minecraft 1.20.1 (Forge) — Дополнительные слоты для апгрейдов-ускорителей в машинах GregTech CEu Modern**

## 🎯 Что делает мод?

GT Upgrade Slots добавляет систему дополнительных слотов для апгрейдов-ускорителей во все машины GregTech Modern. Ускорители динамически влияют на скорость работы машин и потребление энергии.

### Основные возможности:

- ⚡ **Ускорение крафтов** — каждый ускоритель уменьшает время крафта на 25% (настраиваемо)
- 🔋 **Баланс энергии** — ускорители увеличивают потребление энергии на 40% за каждый
- 🎮 **KubeJS интеграция** — полный доступ к API из серверных скриптов
- 🔧 **Универсальность** — работает со всеми машинами GT, включая кастомные из KubeJS

## 📊 Формулы расчёта

### Модификатор скорости:
```
duration_modifier = 1.0 - (accelerator_count × 0.25)
new_duration = base_duration × duration_modifier
min_duration = 1 тик (защита от отрицательных значений)
```

### Модификатор энергии:
```
energy_modifier = 1.0 + (accelerator_count × 0.40)
new_eut = base_eut × energy_modifier
```

### Пример:
Рецепт: 100 тиков, 120 EU/t  
3 ускорителя:
- Длительность: 100 × (1 - 3×0.25) = 100 × 0.25 = **25 тиков**
- Энергия: 120 × (1 + 3×0.40) = 120 × 2.2 = **264 EU/t**

## 🏗️ Архитектура

```
GTUpgradeSlotsMod (@Mod)
    ↓
├── Config System (ForgeConfigSpec)
├── Mixins System
│   ├── MetaTileEntityMixin (добавляет слоты)
│   └── RecipeLogicMixin (модифицирует рецепты)
├── ExtraUpgradeSlotsHandler (логика ускорителей)
├── UpgradeSlotsAPI (публичный API)
└── KubeJS Plugin (интеграция со скриптами)
```

## 📦 Установка

### Требования:
- JDK 17 — https://adoptium.net/
- Minecraft 1.20.1
- Forge 47.2.0+
- GregTech CEu Modern (обязательно)
- KubeJS (опционально, для скриптов)

### Команды сборки:
```bash
# Очистка
gradlew clean

# Сборка
gradlew build

# Результат
build/libs/gtupgradeslots-1.0.0.jar
```

### Установка в Minecraft:
1. Поместите JAR в папку `.minecraft/mods/`
2. Убедитесь, что установлены необходимые моды
3. Запустите Minecraft с Forge

## 📜 KubeJS интеграция

### Пример серверного скрипта:
```javascript
// kubejs/server_scripts/upgrade_slots_example.js

const UpgradeSlotsAPI = Java.loadClass('com.gtupgrades.api.UpgradeSlotsAPI');

events.listen('gtceu.recipe', event => {
    const machine = event.machine;
    const acceleratorCount = UpgradeSlotsAPI.getAcceleratorCount(machine);
    
    if (acceleratorCount > 0) {
        const speedMod = UpgradeSlotsAPI.getSpeedModifier(machine);
        const energyMod = UpgradeSlotsAPI.getEnergyModifier(machine);
        
        console.log(`Ускорителей: ${acceleratorCount}`);
        console.log(`Скорость: x${(1/speedMod).toFixed(2)}`);
        console.log(`Энергия: x${energyMod.toFixed(2)}`);
    }
});
```

### Доступные методы API:
- `getExtraUpgradeSlots(machine)` → `NotifiableItemStackHandler`
- `getAcceleratorCount(machine)` → `int`
- `setAccelerator(machine, slot, stack)` → `boolean`
- `clearAccelerators(machine)` → `void`
- `getSpeedModifier(machine)` → `float`
- `getEnergyModifier(machine)` → `float`

## ⚙️ Конфигурация

Файл: `config/gtupgradeslots-common.toml`

```toml
# Количество дополнительных слотов
extraUpgradeSlots = 4

# Множитель скорости (0.25 = 25% ускорение)
speedMultiplier = 0.25

# Множитель энергии (0.40 = 40% увеличение)
energyMultiplier = 0.40

# Минимальное время рецепта (тики)
minRecipeDuration = 1

# ID предмета-ускорителя
acceleratorItemId = "gtupgradeslots:accelerator_plate"
```

## 📁 Структура проекта

```
gtupgradeslots/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── src/main/
│   ├── java/com/gtupgrades/
│   │   ├── GTUpgradeSlotsMod.java          # Главный класс
│   │   ├── config/
│   │   │   └── UpgradeSlotsConfig.java     # Конфигурация
│   │   ├── mixin/
│   │   │   ├── MetaTileEntityMixin.java    # Mixin для слотов
│   │   │   └── RecipeLogicMixin.java       # Mixin для рецептов
│   │   ├── handler/
│   │   │   └── ExtraUpgradeSlotsHandler.java # Логика ускорителей
│   │   ├── api/
│   │   │   └── UpgradeSlotsAPI.java        # Публичный API
│   │   └── kubejs/
│   │       └── GTUpgradeSlotsKubeJSPlugin.java # KubeJS плагин
│   └── resources/
│       ├── META-INF/
│       │   └── mods.toml
│       └── gtupgradeslots.mixins.json
└── kubejs_examples/
    └── server_scripts/
        └── upgrade_slots_example.js
```

## 🔧 Разработка

### Зависимости в build.gradle:
```gradle
dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.2.0'
    
    // GregTech CEu Modern
    implementation fg.deobf('curse.maven:gtceu-226410:5100000')
    
    // KubeJS
    implementation fg.deobf('dev.latvian.mods:kubejs-forge:2001.6.5-build.16')
    implementation fg.deobf('dev.latvian.mods:rhino:2001.2.3-build.10')
    implementation fg.deobf('dev.latvian.mods:architectury-forge:9.2.14')
    
    // Mixin
    annotationProcessor 'org.spongepowered:mixin:0.8.5:processor'
}
```

### Полезные команды:
```bash
# Сборка без тестов
gradlew build -x test

# Сборка с подробным выводом
gradlew build --info

# Запуск клиента для тестирования
gradlew runClient

# Генерация файлов для IntelliJ IDEA
gradlew idea
```

## 🐛 Решение проблем

### Ошибка: "Cannot find symbol"
- Убедитесь, что все зависимости загружены
- Проверьте версии CurseMaven ID в build.gradle
- Выполните `gradlew --refresh-dependencies`

### Mixin не применяется
- Проверьте `gtupgradeslots.mixins.json`
- Убедитесь, что `MixinConfigs` указан в MANIFEST.MF
- Проверьте совместимость версий GTCEu

### KubeJS скрипты не работают
- Убедитесь, что KubeJS установлен
- Проверьте логи на наличие ошибок регистрации плагина
- Используйте абсолютные пути к классам: `Java.loadClass('com.gtupgrades.api.UpgradeSlotsAPI')`

## 📚 Полезные ссылки

- **GregTech CEu Modern**: https://www.curseforge.com/minecraft/mc-mods/gregtechceu-modern
- **KubeJS**: https://www.curseforge.com/minecraft/mc-mods/kubejs
- **Forge MDK**: https://github.com/MinecraftForge/MinecraftForge
- **Mixin Documentation**: https://github.com/SpongePowered/Mixin

## 📝 Лицензия

MIT License

---

**Создано для Minecraft 1.20.1 с Forge**
