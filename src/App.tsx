import { useState } from 'react'

type Tab = 'overview' | 'approach' | 'code' | 'compile' | 'result'

function App() {
  const [activeTab, setActiveTab] = useState<Tab>('overview')

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100">
      {/* Header */}
      <header className="bg-gradient-to-r from-emerald-900 via-gray-900 to-purple-900 border-b border-gray-800">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-gradient-to-br from-emerald-400 to-cyan-500 rounded-xl flex items-center justify-center text-2xl shadow-lg shadow-emerald-500/20">
              ⚡
            </div>
            <div>
              <h1 className="text-2xl font-bold bg-gradient-to-r from-emerald-300 to-cyan-300 bg-clip-text text-transparent">
                GTCe Modern EMC Addon
              </h1>
              <p className="text-sm text-gray-400">
                Minecraft 1.20.1 (Forge) — ProjectE + GregTech CEu Modern
              </p>
            </div>
          </div>
        </div>
      </header>

      {/* Tabs */}
      <div className="max-w-7xl mx-auto px-4 pt-4">
        <div className="flex gap-2 flex-wrap">
          {[
            { id: 'overview' as Tab, label: '📋 Обзор', color: 'emerald' },
            { id: 'approach' as Tab, label: '🎯 Правильный подход', color: 'cyan' },
            { id: 'code' as Tab, label: '💻 Код', color: 'purple' },
            { id: 'compile' as Tab, label: '🔨 Компиляция', color: 'yellow' },
            { id: 'result' as Tab, label: '📁 Результат', color: 'pink' },
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                activeTab === tab.id
                  ? `bg-${tab.color}-500/20 text-${tab.color}-300 border border-${tab.color}-500/30`
                  : 'text-gray-400 hover:text-gray-200 hover:bg-gray-800/50'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 py-6">
        {activeTab === 'overview' && <OverviewTab />}
        {activeTab === 'approach' && <ApproachTab />}
        {activeTab === 'code' && <CodeTab />}
        {activeTab === 'compile' && <CompileTab />}
        {activeTab === 'result' && <ResultTab />}
      </div>
    </div>
  )
}

function OverviewTab() {
  return (
    <div className="space-y-6">
      <div className="bg-red-900/20 border border-red-700/50 rounded-xl p-6">
        <h2 className="text-xl font-bold text-red-300 mb-3">⚠️ Почему предыдущий код не работал</h2>
        <div className="space-y-3 text-sm text-gray-300">
          <p>
            <strong className="text-red-300">Проблема 1:</strong> Нейросеть использовала устаревший API из старых версий GregTech (1.12.2).
            В GregTech CEu Modern 1.20.1 API полностью переписан.
          </p>
          <p>
            <strong className="text-red-300">Проблема 2:</strong> Методы <code className="bg-gray-800 px-1 rounded">getInputs()</code>, <code className="bg-gray-800 px-1 rounded">getOutputs()</code>, <code className="bg-gray-800 px-1 rounded">getFluidInputs()</code> — <strong>НЕ СУЩЕСТВУЮТ</strong> в современном API.
          </p>
          <p>
            <strong className="text-red-300">Проблема 3:</strong> <code className="bg-gray-800 px-1 rounded">ProjectEAPI.getEMCRegistrationEvent()</code> — тоже не существует в ProjectE для 1.20.1.
          </p>
        </div>
      </div>

      <div className="bg-emerald-900/20 border border-emerald-700/50 rounded-xl p-6">
        <h2 className="text-xl font-bold text-emerald-300 mb-3">✅ Решение: Датапак вместо Java API</h2>
        <p className="text-gray-300 mb-4">
          ProjectE для 1.20.1 использует <strong className="text-emerald-300">датапаки</strong> для регистрации EMC значений.
          Это самый надёжный и правильный способ.
        </p>
        <div className="bg-gray-950 rounded-lg p-4 font-mono text-sm">
          <div className="text-gray-500">// ProjectE загружает EMC из:</div>
          <div className="text-emerald-300">data/gtcemcaddon/pe_custom_conversions/*.json</div>
        </div>
      </div>

      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-cyan-300 mb-4">🏗️ Новая архитектура</h2>
        <div className="bg-gray-950 rounded-lg p-4 font-mono text-xs text-gray-300">
          <pre>{`
┌──────────────────────────────────────────────┐
│           GTEMCAddon.java (@Mod)             │
│  При загрузке → вызывает генератор датапака  │
└──────────────────┬───────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────┐
│        GTDatapackGenerator.java              │
│  Читает базовые EMC материалов GT            │
│  Генерирует JSON файлы для ProjectE          │
└──────────────────┬───────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────┐
│     Датапак в .minecraft/datapacks/          │
│  └── gtcemcaddon/                            │
│      ├── pack.mcmeta                         │
│      └── data/gtcemcaddon/                   │
│          └── pe_custom_conversions/          │
│              ├── base_values.json            │
│              ├── material_forms.json         │
│              └── fluid_conversions.json      │
└──────────────────┬───────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────┐
│              ProjectE                        │
│  Загружает JSON → рассчитывает EMC           │
│  для всех предметов и жидкостей GT           │
└──────────────────────────────────────────────┘
          `}</pre>
        </div>
      </div>
    </div>
  )
}

function ApproachTab() {
  return (
    <div className="space-y-6">
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-emerald-300 mb-4">🎯 Почему датапак — правильный подход</h2>
        
        <div className="space-y-4">
          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">1. ProjectE сам рассчитывает EMC</h3>
            <p className="text-sm text-gray-400">
              Вам не нужно писать калькулятор! ProjectE сам рассчитывает EMC на основе конверсий.
              Вы просто указываете базовые значения и рецепты — остальное делает ProjectE.
            </p>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">2. Не зависит от API</h3>
            <p className="text-sm text-gray-400">
              Датапак — это просто JSON файлы. Не нужно разбираться со сложным API GT или ProjectE.
              Работает с любой версией модов.
            </p>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">3. Поддержка жидкостей из коробки</h3>
            <p className="text-sm text-gray-400">
              ProjectE поддерживает жидкости через префикс <code className="bg-gray-800 px-1 rounded">FLUID|</code>.
              Формат: <code className="bg-gray-800 px-1 rounded">FLUID|gtceu:iron</code> для жидкого железа.
            </p>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">4. Легко модифицировать</h3>
            <p className="text-sm text-gray-400">
              Можно вручную редактировать JSON файлы для тонкой настройки EMC значений.
              Не нужно перекомпилировать мод.
            </p>
          </div>
        </div>
      </div>

      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-purple-300 mb-4">📐 Формат JSON для ProjectE</h2>
        
        <div className="space-y-4">
          <div className="bg-gray-950 rounded-lg p-4">
            <h3 className="text-sm font-semibold text-gray-400 mb-2">Базовые значения:</h3>
            <pre className="text-xs text-emerald-300 font-mono overflow-x-auto">
{`{
  "values": {
    "before": {
      "gtceu:iron_ingot": 256,
      "gtceu:gold_ingot": 2048,
      "gtceu:diamond_gem": 8192
    }
  }
}`}
            </pre>
          </div>

          <div className="bg-gray-950 rounded-lg p-4">
            <h3 className="text-sm font-semibold text-gray-400 mb-2">Конверсии (рецепты):</h3>
            <pre className="text-xs text-cyan-300 font-mono overflow-x-auto">
{`{
  "groups": {
    "plates": {
      "conversions": [
        {
          "output": "gtceu:iron_plate",
          "count": 1,
          "ingredients": {
            "gtceu:iron_ingot": 0.25
          }
        }
      ]
    }
  }
}`}
            </pre>
          </div>

          <div className="bg-gray-950 rounded-lg p-4">
            <h3 className="text-sm font-semibold text-gray-400 mb-2">Жидкости:</h3>
            <pre className="text-xs text-purple-300 font-mono overflow-x-auto">
{`{
  "conversions": [
    {
      "output": "FLUID|gtceu:iron",
      "count": 1000,
      "ingredients": {
        "gtceu:iron_ingot": 7
      }
    }
  ]
}`}
            </pre>
          </div>
        </div>
      </div>

      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-yellow-300 mb-4">🔑 Ключевые моменты</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">📦 Предметы</h3>
            <code className="text-xs text-emerald-300">gtceu:iron_ingot</code>
            <p className="text-xs text-gray-500 mt-1">Формат: namespace:item_id</p>
          </div>
          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">💧 Жидкости</h3>
            <code className="text-xs text-cyan-300">FLUID|gtceu:iron</code>
            <p className="text-xs text-gray-500 mt-1">Префикс FLUID|</p>
          </div>
          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">🏷️ Теги</h3>
            <code className="text-xs text-purple-300">#forge:ingots/iron</code>
            <p className="text-xs text-gray-500 mt-1">Префикс #</p>
          </div>
          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">🎭 Фейковые</h3>
            <code className="text-xs text-yellow-300">FAKE|single_emc</code>
            <p className="text-xs text-gray-500 mt-1">Префикс FAKE|</p>
          </div>
        </div>
      </div>
    </div>
  )
}

function CodeTab() {
  return (
    <div className="space-y-6">
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-emerald-300 mb-4">📁 Структура проекта</h2>
        <div className="bg-gray-950 rounded-lg p-4 font-mono text-xs text-gray-300">
          <pre>{`gtcemc-addon/
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
            └── mods.toml                ← Forge mod descriptor`}</pre>
        </div>
      </div>

      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-cyan-300 mb-4">💻 GTEMCAddon.java</h2>
        <pre className="bg-gray-950 rounded-lg p-4 text-xs text-gray-300 font-mono overflow-x-auto">
{`package com.gtemc;

import com.gtemc.datapack.GTDatapackGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LOGGER.info("[GTEMCAddon] Генерация датапака для ProjectE");
        event.enqueueWork(() -> {
            try {
                GTDatapackGenerator.generateDatapack();
                LOGGER.info("[GTEMCAddon] Датапак успешно сгенерирован");
            } catch (Exception e) {
                LOGGER.error("[GTEMCAddon] Ошибка при генерации датапака", e);
            }
        });
    }
}`}
        </pre>
      </div>

      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-purple-300 mb-4">💻 GTDatapackGenerator.java</h2>
        <p className="text-sm text-gray-400 mb-4">
          Этот класс генерирует JSON файлы для ProjectE. Полный код в файле <code className="bg-gray-800 px-1 rounded">mod-src/src/main/java/com/gtemc/datapack/GTDatapackGenerator.java</code>
        </p>
        <div className="bg-gray-950 rounded-lg p-4 text-xs text-gray-300 font-mono">
          <div className="text-gray-500">// Ключевые методы:</div>
          <div className="text-emerald-300 mt-2">generateDatapack() — главная функция</div>
          <div className="text-cyan-300 mt-1">generateBaseValues() — базовые EMC материалов</div>
          <div className="text-purple-300 mt-1">generateMaterialForms() — конверсии для форм</div>
          <div className="text-yellow-300 mt-1">generateFluidConversions() — EMC жидкостей</div>
        </div>
      </div>
    </div>
  )
}

function CompileTab() {
  return (
    <div className="space-y-6">
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-emerald-300 mb-4">🔨 Компиляция мода</h2>
        
        <div className="space-y-4">
          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">1. Требования</h3>
            <ul className="text-sm text-gray-400 space-y-1">
              <li>• JDK 17 — <a href="https://adoptium.net/" className="text-emerald-400 hover:underline">adoptium.net</a></li>
              <li>• Проверка: <code className="bg-gray-800 px-1 rounded">java -version</code> → должно показать 17.x.x</li>
            </ul>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">2. Скопируйте файлы</h3>
            <p className="text-sm text-gray-400">
              Скопируйте содержимое <code className="bg-gray-800 px-1 rounded">mod-src/</code> в ваш проект
            </p>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">3. Сборка</h3>
            <pre className="bg-gray-950 rounded p-3 text-sm text-emerald-300 font-mono mt-2">
{`# Windows:
gradlew.bat build

# Linux/Mac:
./gradlew build`}
            </pre>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">4. Результат</h3>
            <p className="text-sm text-gray-400">
              JAR файл: <code className="bg-gray-800 px-1 rounded">build/libs/gtcemodern-emc-addon-1.0.0.jar</code>
            </p>
          </div>
        </div>
      </div>

      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-yellow-300 mb-4">📦 Установка</h2>
        <div className="space-y-3">
          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">1. Поместите JAR в папку mods:</h3>
            <pre className="bg-gray-950 rounded p-3 text-xs text-gray-300 font-mono">
{`Windows: %APPDATA%\\.minecraft\\mods\\
Linux:   ~/.minecraft/mods/
Mac:     ~/Library/Application Support/minecraft/mods/`}
            </pre>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">2. Необходимые моды:</h3>
            <ul className="text-sm text-gray-400 space-y-1">
              <li>• ProjectE (для 1.20.1)</li>
              <li>• GregTech CEu Modern (для 1.20.1)</li>
              <li>• CodeChickenLib</li>
              <li>• LDLib</li>
            </ul>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">3. Запустите Minecraft с Forge</h3>
            <p className="text-sm text-gray-400">
              Мод автоматически сгенерирует датапак при первом запуске.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

function ResultTab() {
  return (
    <div className="space-y-6">
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-emerald-300 mb-4">📁 Сгенерированные файлы</h2>
        
        <div className="space-y-4">
          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">📂 Путь к датапаку:</h3>
            <pre className="bg-gray-950 rounded p-3 text-xs text-emerald-300 font-mono">
{`.minecraft/datapacks/gtcemcaddon/
├── pack.mcmeta
└── data/gtcemcaddon/pe_custom_conversions/
    ├── base_values.json
    ├── material_forms.json
    └── fluid_conversions.json`}
            </pre>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">📄 base_values.json</h3>
            <p className="text-sm text-gray-400 mb-2">Базовые EMC для 60+ материалов GT:</p>
            <pre className="bg-gray-950 rounded p-3 text-xs text-gray-300 font-mono overflow-x-auto">
{`{
  "values": {
    "before": {
      "gtceu:iron_ingot": 256,
      "gtceu:gold_ingot": 2048,
      "gtceu:copper_ingot": 128,
      "gtceu:titanium_ingot": 4096,
      "gtceu:naquadah_ingot": 16384,
      "gtceu:neutronium_ingot": 262144,
      // ... и т.д.
    }
  }
}`}
            </pre>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">📄 material_forms.json</h3>
            <p className="text-sm text-gray-400 mb-2">Конверсии для 30+ форм каждого материала:</p>
            <pre className="bg-gray-950 rounded p-3 text-xs text-gray-300 font-mono overflow-x-auto">
{`{
  "groups": {
    "forms_iron": {
      "conversions": [
        {
          "output": "gtceu:iron_plate",
          "ingredients": { "gtceu:iron_ingot": 0.25 }
        },
        {
          "output": "gtceu:iron_gear",
          "ingredients": { "gtceu:iron_ingot": 4 }
        },
        {
          "output": "gtceu:iron_wire",
          "ingredients": { "gtceu:iron_ingot": 0.25 }
        },
        // ... plate, rod, bolt, screw, ring, foil, gear, etc.
      ]
    }
  }
}`}
            </pre>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">📄 fluid_conversions.json</h3>
            <p className="text-sm text-gray-400 mb-2">EMC для жидкостей (металлы + химия):</p>
            <pre className="bg-gray-950 rounded p-3 text-xs text-gray-300 font-mono overflow-x-auto">
{`{
  "groups": {
    "fluids": {
      "conversions": [
        {
          "output": "FLUID|gtceu:iron",
          "count": 1000,
          "ingredients": { "gtceu:iron_ingot": 7 }
        },
        {
          "output": "FLUID|gtceu:sulfuric_acid",
          "count": 1000,
          "ingredients": { "FAKE|single_emc": 128 }
        },
        // ... все жидкие металлы и химикаты
      ]
    }
  }
}`}
            </pre>
          </div>
        </div>
      </div>

      <div className="bg-emerald-900/20 border border-emerald-700/50 rounded-xl p-6">
        <h2 className="text-xl font-bold text-emerald-300 mb-3">✅ Что получает пользователь</h2>
        <ul className="space-y-2 text-sm text-gray-300">
          <li>• <strong>60+ материалов</strong> с EMC значениями</li>
          <li>• <strong>30+ форм</strong> для каждого материала (ingot, dust, plate, gear, wire...)</li>
          <li>• <strong>50+ жидкостей</strong> (металлы + химикаты)</li>
          <li>• <strong>2000+ предметов</strong> с автоматически рассчитанным EMC</li>
          <li>• ProjectE сам рассчитывает EMC для всех комбинаций</li>
          <li>• Можно вручную редактировать JSON для тонкой настройки</li>
        </ul>
      </div>
    </div>
  )
}

export default App
