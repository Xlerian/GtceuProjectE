# ⚡ GTCe Modern EMC Addon

**Автоматический расчёт EMC для GregTech CEu Modern через ProjectE**

## 🎯 Что делает этот мод?

Мод генерирует **датапак** для ProjectE с EMC значениями для всех предметов и жидкостей GregTech CEu Modern.

### Почему датапак, а не Java API?

ProjectE для Minecraft 1.20.1 использует **датапаки** для регистрации EMC значений. Это самый надёжный и правильный способ, потому что:

1. ✅ **ProjectE сам рассчитывает EMC** — не нужно писать калькулятор
2. ✅ **Не зависит от API** — работает с любой версией модов
3. ✅ **Поддержка жидкостей** через префикс `FLUID|`
4. ✅ **Легко модифицировать** — просто редактируйте JSON файлы

## 📦 Установка

1. Установите **JDK 17**: https://adoptium.net/
2. Скопируйте файлы из `mod-src/` в ваш проект
3. Запустите сборку:
   ```bash
   # Windows:
   gradlew.bat build
   
   # Linux/Mac:
   ./gradlew build
   ```
4. Поместите JAR из `build/libs/` в папку `.minecraft/mods/`
5. Убедитесь, что установлены:
   - ProjectE (для 1.20.1)
   - GregTech CEu Modern (для 1.20.1)
   - CodeChickenLib
   - LDLib

## 🏗️ Архитектура

```
GTEMCAddon.java (@Mod)
    ↓
GTDatapackGenerator.java
    ↓
Генерирует датапак:
  .minecraft/datapacks/gtcemcaddon/
    ├── pack.mcmeta
    └── data/gtcemcaddon/pe_custom_conversions/
        ├── base_values.json        ← Базовые EMC материалов
        ├── material_forms.json     ← Конверсии для форм
        └── fluid_conversions.json  ← EMC жидкостей
    ↓
ProjectE загружает JSON → рассчитывает EMC
```

## 📊 Что включено

### Базовые материалы (60+):
- Металлы: iron, gold, copper, tin, steel, titanium, tungsten, naquadah, neutronium...
- Драгоценные камни: diamond, emerald, ruby, sapphire, opal, amethyst...
- Химикаты: sulfur, carbon, silicon, rubber...
- Полимеры: polyethylene, PVC, PTFE, PBI, epoxid...

### Формы материалов (30+):
- ingot, dust, small_dust, tiny_dust, nugget
- block, plate, double_plate, dense_plate
- rod, long_rod, bolt, screw, ring
- gear, small_gear, wire, fine_wire
- spring, frame, rotor, ore, gem, lens...

### Жидкости (50+):
- Жидкие металлы: iron, gold, steel, titanium, naquadah...
- Кислоты: sulfuric_acid, hydrochloric_acid, nitric_acid...
- Газы: hydrogen, oxygen, nitrogen, chlorine, methane...
- Полимеры: polyethylene, PVC, PTFE, PBI...
- Другие: rubber, creosote, seed_oil, lubricant, concrete...

## 🔧 Ручная настройка

Вы можете редактировать JSON файлы в `.minecraft/datapacks/gtcemcaddon/data/gtcemcaddon/pe_custom_conversions/`:

### Изменить EMC материала:
```json
{
  "values": {
    "before": {
      "gtceu:iron_ingot": 512  // было 256
    }
  }
}
```

### Добавить новую конверсию:
```json
{
  "groups": {
    "custom": {
      "conversions": [
        {
          "output": "gtceu:custom_item",
          "ingredients": {
            "gtceu:iron_ingot": 2,
            "gtceu:gold_ingot": 1
          }
        }
      ]
    }
  }
}
```

### Формат ProjectE JSON:
- **Предметы**: `gtceu:iron_ingot`
- **Жидкости**: `FLUID|gtceu:iron`
- **Теги**: `#forge:ingots/iron`
- **Фейковые**: `FAKE|single_emc`

## 📁 Структура проекта

```
gtcemc-addon/
├── build.gradle
├── gradle.properties
├── settings.gradle
└── src/main/
    ├── java/com/gtemc/
    │   ├── GTEMCAddon.java              ← Главный класс
    │   └── datapack/
    │       └── GTDatapackGenerator.java ← Генератор датапака
    └── resources/
        └── META-INF/
            └── mods.toml
```

## 🐛 Решение проблем

### Датапак не генерируется
- Проверьте логи Minecraft в `.minecraft/logs/latest.log`
- Убедитесь, что мод загружается без ошибок

### EMC не рассчитываются
- Проверьте, что датапак активирован в мире
- Перезапустите Minecraft после генерации датапака
- Проверьте JSON файлы на синтаксические ошибки

### Ошибки компиляции
- Убедитесь, что используете JDK 17
- Проверьте версии зависимостей в `build.gradle`
- Очистите кэш: `gradlew clean`

## 📚 Полезные ссылки

- **ProjectE**: https://www.curseforge.com/minecraft/mc-mods/projecte
- **GregTech CEu Modern**: https://www.curseforge.com/minecraft/mc-mods/gregtechceu-modern
- **ProjectE Custom Conversions**: https://github.com/sinkillerj/ProjectE/blob/mc1.20.x/example_custom_conversion.json

## 📝 Лицензия

MIT License

---

**Создано для Minecraft 1.20.1 с Forge**
