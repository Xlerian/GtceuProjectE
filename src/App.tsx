import { useState } from 'react'

type Tab = 'overview' | 'architecture' | 'code' | 'kubejs' | 'build' | 'install'

function App() {
  const [activeTab, setActiveTab] = useState<Tab>('overview')

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100">
      {/* Header */}
      <header className="bg-gradient-to-r from-purple-900 via-gray-900 to-emerald-900 border-b border-gray-800">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-gradient-to-br from-purple-400 to-pink-500 rounded-xl flex items-center justify-center text-2xl shadow-lg shadow-purple-500/20">
              ⚙️
            </div>
            <div>
              <h1 className="text-2xl font-bold bg-gradient-to-r from-purple-300 to-pink-300 bg-clip-text text-transparent">
                GT Upgrade Slots
              </h1>
              <p className="text-sm text-gray-400">
                Дополнительные слоты для апгрейдов-ускорителей в машинах GregTech Modern | Minecraft 1.20.1
              </p>
            </div>
          </div>
        </div>
      </header>

      {/* Tabs */}
      <div className="max-w-7xl mx-auto px-4 pt-4">
        <div className="flex gap-2 flex-wrap">
          {[
            { id: 'overview' as Tab, label: '📋 Обзор' },
            { id: 'architecture' as Tab, label: '🏗️ Архитектура' },
            { id: 'code' as Tab, label: '💻 Java код' },
            { id: 'kubejs' as Tab, label: '📜 KubeJS' },
            { id: 'build' as Tab, label: '🔨 Сборка' },
            { id: 'install' as Tab, label: '📦 Установка' },
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                activeTab === tab.id
                  ? 'bg-purple-500/20 text-purple-300 border border-purple-500/30'
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
        {activeTab === 'architecture' && <ArchitectureTab />}
        {activeTab === 'code' && <CodeTab />}
        {activeTab === 'kubejs' && <KubeJSTab />}
        {activeTab === 'build' && <BuildTab />}
        {activeTab === 'install' && <InstallTab />}
      </div>
    </div>
  )
}

function OverviewTab() {
  return (
    <div className="space-y-6">
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-purple-300 mb-4">🎯 Что делает мод</h2>
        <p className="text-gray-300 leading-relaxed mb-4">
          <strong className="text-white">GT Upgrade Slots</strong> добавляет систему дополнительных слотов для апгрейдов-ускорителей 
          во все машины GregTech CEu Modern. Ускорители динамически влияют на скорость работы машин и потребление энергии.
        </p>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
          <FeatureCard
            icon="⚡"
            title="Ускорение крафтов"
            description="Каждый ускоритель уменьшает время крафта на 25% (настраиваемо)"
          />
          <FeatureCard
            icon="🔋"
            title="Баланс энергии"
            description="Ускорители увеличивают потребление энергии на 40% за каждый"
          />
          <FeatureCard
            icon="🎮"
            title="KubeJS интеграция"
            description="Полный доступ к API из серверных скриптов для кастомной логики"
          />
          <FeatureCard
            icon="🔧"
            title="Универсальность"
            description="Работает со всеми машинами GT, включая кастомные из KubeJS"
          />
        </div>
      </div>

      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-emerald-300 mb-4">📊 Формулы расчёта</h2>
        <div className="space-y-4">
          <div className="bg-gray-950 rounded-lg p-4">
            <h3 className="text-sm font-semibold text-gray-400 mb-2">Модификатор скорости:</h3>
            <code className="text-emerald-300 text-sm">
              duration_modifier = 1.0 - (accelerator_count × 0.25)<br/>
              new_duration = base_duration × duration_modifier<br/>
              min_duration = 1 тик (защита от отрицательных значений)
            </code>
          </div>
          <div className="bg-gray-950 rounded-lg p-4">
            <h3 className="text-sm font-semibold text-gray-400 mb-2">Модификатор энергии:</h3>
            <code className="text-cyan-300 text-sm">
              energy_modifier = 1.0 + (accelerator_count × 0.40)<br/>
              new_eut = base_eut × energy_modifier
            </code>
          </div>
          <div className="bg-gray-950 rounded-lg p-4">
            <h3 className="text-sm font-semibold text-gray-400 mb-2">Пример:</h3>
            <div className="text-sm text-gray-300">
              <p>Рецепт: 100 тиков, 120 EU/t</p>
              <p>3 ускорителя:</p>
              <p className="text-emerald-300">• Длительность: 100 × (1 - 3×0.25) = 100 × 0.25 = <strong>25 тиков</strong></p>
              <p className="text-cyan-300">• Энергия: 120 × (1 + 3×0.40) = 120 × 2.2 = <strong>264 EU/t</strong></p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

function ArchitectureTab() {
  return (
    <div className="space-y-6">
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-purple-300 mb-4">🏗️ Архитектура мода</h2>
        <div className="bg-gray-950 rounded-lg p-4 font-mono text-xs text-gray-300 overflow-x-auto">
          <pre>{`
┌─────────────────────────────────────────────────────────────┐
│                    GTUpgradeSlotsMod (@Mod)                  │
│              Главный класс, регистрация мода                 │
└────────────────────────┬────────────────────────────────────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
┌─────────────┐  ┌─────────────┐  ┌──────────────┐
│   Config    │  │   Mixins    │  │   KubeJS     │
│   System    │  │   System    │  │   Plugin     │
└─────────────┘  └──────┬──────┘  └──────┬───────┘
                        │                │
                        ▼                ▼
              ┌─────────────────┐  ┌──────────────┐
              │MetaTileEntity   │  │UpgradeSlots  │
              │Mixin            │  │API           │
              │                 │  │              │
              │• Добавляет      │  │• getExtra    │
              │  слоты          │  │  Slots()     │
              │• Считает        │  │• getSpeed    │
              │  ускорители     │  │  Modifier()  │
              └────────┬────────┘  └──────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │ExtraUpgrade     │
              │SlotsHandler     │
              │                 │
              │• isAccelerator()│
              │• countAccel()   │
              │• calcModifiers()│
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │RecipeLogic      │
              │Mixin            │
              │                 │
              │• Модифицирует   │
              │  duration       │
              │• Модифицирует   │
              │  EU/t           │
              └─────────────────┘
          `}</pre>
        </div>
      </div>

      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-cyan-300 mb-4">🔧 Ключевые компоненты</h2>
        <div className="space-y-3">
          <ComponentCard
            name="MetaTileEntityMixin"
            description="Перехватывает инициализацию инвентаря машин GT, добавляет дополнительные слоты"
            color="purple"
          />
          <ComponentCard
            name="ExtraUpgradeSlotsHandler"
            description="Логика проверки ускорителей и расчёта модификаторов скорости/энергии"
            color="emerald"
          />
          <ComponentCard
            name="RecipeLogicMixin"
            description="Применяет модификаторы к длительности и энергии рецептов"
            color="cyan"
          />
          <ComponentCard
            name="UpgradeSlotsAPI"
            description="Публичный API для доступа к слотам из KubeJS и других модов"
            color="pink"
          />
          <ComponentCard
            name="GTUpgradeSlotsKubeJSPlugin"
            description="Регистрирует API в KubeJS для использования в серверных скриптах"
            color="yellow"
          />
        </div>
      </div>
    </div>
  )
}

function CodeTab() {
  return (
    <div className="space-y-6">
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-purple-300 mb-4">💻 Основные Java классы</h2>
        
        <div className="space-y-4">
          <CodeBlock
            title="GTUpgradeSlotsMod.java"
            description="Главный класс мода"
            code={`@Mod(GTUpgradeSlotsMod.MOD_ID)
public class GTUpgradeSlotsMod {
    public static final String MOD_ID = "gtupgradeslots";
    
    public GTUpgradeSlotsMod() {
        // Регистрация конфигурации
        ModLoadingContext.get().registerConfig(
            ModConfig.Type.COMMON, 
            UpgradeSlotsConfig.SPEC
        );
        
        // Регистрация KubeJS плагина
        if (isKubeJSLoaded()) {
            GTUpgradeSlotsKubeJSPlugin.register();
        }
    }
}`}
          />
          
          <CodeBlock
            title="MetaTileEntityMixin.java"
            description="Mixin для добавления слотов"
            code={`@Mixin(value = MetaTileEntity.class, remap = false)
public abstract class MetaTileEntityMixin {
    
    private NotifiableItemStackHandler extraUpgradeSlotsHandler;
    
    @Inject(method = "initializeInventory", at = @At("TAIL"))
    private void afterInitializeInventory(CallbackInfo ci) {
        int extraSlots = UpgradeSlotsConfig.EXTRA_UPGRADE_SLOTS.get();
        if (extraSlots > 0) {
            extraUpgradeSlotsHandler = new NotifiableItemStackHandler(
                self, extraSlots, 
                ExtraUpgradeSlotsHandler::isAccelerator
            );
        }
    }
    
    public int getAcceleratorCount() {
        return ExtraUpgradeSlotsHandler.countAccelerators(
            extraUpgradeSlotsHandler
        );
    }
}`}
          />
          
          <CodeBlock
            title="ExtraUpgradeSlotsHandler.java"
            description="Логика расчёта модификаторов"
            code={`public class ExtraUpgradeSlotsHandler {
    
    public static boolean isAccelerator(ItemStack stack) {
        String acceleratorId = UpgradeSlotsConfig.ACCELERATOR_ITEM_ID.get();
        ResourceLocation acceleratorRL = new ResourceLocation(acceleratorId);
        return ForgeRegistries.ITEMS.getKey(stack.getItem())
            .equals(acceleratorRL);
    }
    
    public static float calculateSpeedModifier(int count) {
        double multiplier = UpgradeSlotsConfig.SPEED_MULTIPLIER.get();
        float modifier = 1.0f - (float)(count * multiplier);
        return Math.max(0.1f, modifier); // Минимум 10%
    }
    
    public static float calculateEnergyModifier(int count) {
        double multiplier = UpgradeSlotsConfig.ENERGY_MULTIPLIER.get();
        return 1.0f + (float)(count * multiplier);
    }
}`}
          />
        </div>
      </div>
    </div>
  )
}

function KubeJSTab() {
  return (
    <div className="space-y-6">
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-yellow-300 mb-4">📜 KubeJS интеграция</h2>
        <p className="text-gray-300 mb-4">
          Мод предоставляет полный API для использования в серверных скриптах KubeJS.
        </p>
        
        <CodeBlock
          title="Пример серверного скрипта"
          description="kubejs/server_scripts/upgrade_slots_example.js"
          code={`// Импорт API
const UpgradeSlotsAPI = Java.loadClass(
    'com.gtupgrades.api.UpgradeSlotsAPI'
);

// Получение информации о ускорителях
events.listen('gtceu.recipe', event => {
    const machine = event.machine;
    const acceleratorCount = UpgradeSlotsAPI
        .getAcceleratorCount(machine);
    
    if (acceleratorCount > 0) {
        const speedMod = UpgradeSlotsAPI
            .getSpeedModifier(machine);
        const energyMod = UpgradeSlotsAPI
            .getEnergyModifier(machine);
        
        console.log(\`Ускорителей: \${acceleratorCount}\`);
        console.log(\`Скорость: x\${(1/speedMod).toFixed(2)}\`);
        console.log(\`Энергия: x\${energyMod.toFixed(2)}\`);
    }
});

// Кастомная логика для конкретного мультиблока
events.listen('gtceu.recipe', event => {
    const machine = event.machine;
    
    if (machine.id.includes('large_chemical_reactor')) {
        const count = UpgradeSlotsAPI
            .getAcceleratorCount(machine);
        
        // Бонус к выходу при 2+ ускорителях
        if (count >= 2) {
            const bonus = 1.0 + (count - 1) * 0.1;
            event.recipe.outputs.forEach(output => {
                if (output.count) {
                    output.count = Math.ceil(output.count * bonus);
                }
            });
        }
    }
});`}
        />
        
        <div className="mt-6 bg-gray-950 rounded-lg p-4">
          <h3 className="text-sm font-semibold text-gray-400 mb-3">Доступные методы API:</h3>
          <div className="space-y-2 text-sm">
            <APIMethod name="getExtraUpgradeSlots(machine)" returns="NotifiableItemStackHandler" desc="Получить обработчик слотов" />
            <APIMethod name="getAcceleratorCount(machine)" returns="int" desc="Количество ускорителей" />
            <APIMethod name="setAccelerator(machine, slot, stack)" returns="boolean" desc="Установить ускоритель" />
            <APIMethod name="clearAccelerators(machine)" returns="void" desc="Очистить все слоты" />
            <APIMethod name="getSpeedModifier(machine)" returns="float" desc="Модификатор скорости" />
            <APIMethod name="getEnergyModifier(machine)" returns="float" desc="Модификатор энергии" />
          </div>
        </div>
      </div>
    </div>
  )
}

function BuildTab() {
  return (
    <div className="space-y-6">
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-emerald-300 mb-4">🔨 Сборка мода</h2>
        
        <div className="space-y-4">
          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">1. Требования</h3>
            <ul className="text-sm text-gray-400 space-y-1">
              <li>• JDK 17 — <a href="https://adoptium.net/" className="text-emerald-400 hover:underline">adoptium.net</a></li>
              <li>• Gradle 8.1+ (используется wrapper)</li>
              <li>• Minecraft 1.20.1 + Forge 47.2.0+</li>
            </ul>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">2. Зависимости в build.gradle</h3>
            <pre className="bg-gray-950 rounded p-3 text-xs text-gray-300 font-mono overflow-x-auto">
{`dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.2.0'
    
    // GregTech CEu Modern
    implementation fg.deobf('curse.maven:gtceu-226410:5100000')
    
    // KubeJS
    implementation fg.deobf('dev.latvian.mods:kubejs-forge:2001.6.5-build.16')
    implementation fg.deobf('dev.latvian.mods:rhino:2001.2.3-build.10')
    implementation fg.deobf('dev.latvian.mods:architectury-forge:9.2.14')
    
    // Mixin
    annotationProcessor 'org.spongepowered:mixin:0.8.5:processor'
}`}
            </pre>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">3. Команды сборки</h3>
            <pre className="bg-gray-950 rounded p-3 text-sm text-emerald-300 font-mono">
{`# Очистка
gradlew clean

# Сборка
gradlew build

# Результат
build/libs/gtupgradeslots-1.0.0.jar`}
            </pre>
          </div>
        </div>
      </div>
    </div>
  )
}

function InstallTab() {
  return (
    <div className="space-y-6">
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-pink-300 mb-4">📦 Установка</h2>
        
        <div className="space-y-4">
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
              <li>• <strong className="text-white">GregTech CEu Modern</strong> (обязательно)</li>
              <li>• <strong className="text-white">KubeJS</strong> (опционально, для скриптов)</li>
              <li>• CodeChickenLib</li>
              <li>• LDLib</li>
              <li>• Architectury</li>
              <li>• Rhino</li>
            </ul>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-200 mb-2">3. Настройка (опционально):</h3>
            <p className="text-sm text-gray-400 mb-2">
              Конфигурация в файле <code className="bg-gray-800 px-1 rounded">config/gtupgradeslots-common.toml</code>:
            </p>
            <pre className="bg-gray-950 rounded p-3 text-xs text-gray-300 font-mono">
{`# Количество дополнительных слотов
extraUpgradeSlots = 4

# Множитель скорости (0.25 = 25% ускорение)
speedMultiplier = 0.25

# Множитель энергии (0.40 = 40% увеличение)
energyMultiplier = 0.40

# Минимальное время рецепта (тики)
minRecipeDuration = 1

# ID предмета-ускорителя
acceleratorItemId = "gtupgradeslots:accelerator_plate"`}
            </pre>
          </div>
        </div>
      </div>
    </div>
  )
}

// Компоненты UI
function FeatureCard({ icon, title, description }: { icon: string; title: string; description: string }) {
  return (
    <div className="bg-gray-800/50 rounded-lg p-4 border border-gray-700/50">
      <div className="flex items-center gap-2 mb-2">
        <span className="text-xl">{icon}</span>
        <h3 className="font-semibold text-gray-200">{title}</h3>
      </div>
      <p className="text-sm text-gray-400">{description}</p>
    </div>
  )
}

function ComponentCard({ name, description, color }: { name: string; description: string; color: string }) {
  return (
    <div className="bg-gray-800/50 rounded-lg p-4 border border-gray-700/50">
      <div className="flex items-center gap-2 mb-1">
        <div className={`w-2 h-2 bg-${color}-400 rounded-full`}></div>
        <code className={`text-${color}-300 text-sm font-semibold`}>{name}</code>
      </div>
      <p className="text-sm text-gray-400 ml-4">{description}</p>
    </div>
  )
}

function CodeBlock({ title, description, code }: { title: string; description: string; code: string }) {
  return (
    <div className="bg-gray-950 rounded-lg overflow-hidden">
      <div className="px-4 py-2 bg-gray-900 border-b border-gray-800">
        <h3 className="text-sm font-semibold text-gray-200">{title}</h3>
        <p className="text-xs text-gray-500">{description}</p>
      </div>
      <pre className="p-4 text-xs text-gray-300 font-mono overflow-x-auto">
        <code>{code}</code>
      </pre>
    </div>
  )
}

function APIMethod({ name, returns, desc }: { name: string; returns: string; desc: string }) {
  return (
    <div className="flex items-start gap-2 text-xs">
      <code className="text-emerald-300 font-mono shrink-0">{name}</code>
      <span className="text-gray-500">→</span>
      <code className="text-cyan-300 font-mono shrink-0">{returns}</code>
      <span className="text-gray-400">— {desc}</span>
    </div>
  )
}

export default App
