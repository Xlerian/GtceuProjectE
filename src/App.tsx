import { useState } from 'react'
import SyntaxHighlighter from 'react-syntax-highlighter/dist/esm/prism-light'
import java from 'react-syntax-highlighter/dist/esm/languages/prism/java'
import groovy from 'react-syntax-highlighter/dist/esm/languages/prism/groovy'
import bash from 'react-syntax-highlighter/dist/esm/languages/prism/bash'
import vscDarkPlus from 'react-syntax-highlighter/dist/esm/styles/prism/vsc-dark-plus'

SyntaxHighlighter.registerLanguage('java', java)
SyntaxHighlighter.registerLanguage('groovy', groovy)
SyntaxHighlighter.registerLanguage('bash', bash)

type Tab = 'overview' | 'compile' | 'mapper' | 'fluid' | 'walker' | 'calculator' | 'integration' | 'config' | 'build'

interface FileContent {
  id: Tab
  name: string
  path: string
  language: string
  description: string
}

const files: FileContent[] = [
  { id: 'overview', name: '📋 Обзор', path: '', language: 'markdown', description: 'Общая документация мода' },
  { id: 'compile', name: '🔨 Компиляция', path: '', language: 'bash', description: 'Инструкция по сборке' },
  { id: 'mapper', name: 'GTEMCMapper', path: 'com/gtemc/emc/GTEMCMapper.java', language: 'java', description: 'Основной EMC-маппер' },
  { id: 'fluid', name: 'FluidEMCRegistry', path: 'com/gtemc/emc/FluidEMCRegistry.java', language: 'java', description: 'Реестр EMC жидкостей' },
  { id: 'walker', name: 'RecipeWalker', path: 'com/gtemc/emc/RecipeWalker.java', language: 'java', description: 'Обходчик рецептов GT' },
  { id: 'calculator', name: 'EMCCalculator', path: 'com/gtemc/emc/EMCCalculator.java', language: 'java', description: 'Калькулятор EMC' },
  { id: 'integration', name: 'GTEMCIntegration', path: 'com/gtemc/emc/GTEMCIntegration.java', language: 'java', description: 'Интеграция с ProjectE' },
  { id: 'config', name: 'GTEMCConfig', path: 'com/gtemc/config/GTEMCConfig.java', language: 'java', description: 'Конфигурация мода' },
  { id: 'build', name: 'build.gradle', path: 'build.gradle', language: 'groovy', description: 'Скрипт сборки' },
]

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
                Автоматический расчёт EMC для GregTech CEu Modern + ProjectE | Minecraft 1.20.1 (Forge)
              </p>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 py-6 flex gap-6">
        {/* Sidebar */}
        <nav className="w-64 shrink-0">
          <div className="sticky top-6">
            <h2 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3 px-3">
              Файлы мода
            </h2>
            <ul className="space-y-1">
              {files.map(file => (
                <li key={file.id}>
                  <button
                    onClick={() => setActiveTab(file.id)}
                    className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all ${
                      activeTab === file.id
                        ? 'bg-emerald-500/10 text-emerald-300 border border-emerald-500/30'
                        : 'text-gray-400 hover:text-gray-200 hover:bg-gray-800/50'
                    }`}
                  >
                    <span className="font-medium">{file.name}</span>
                    {file.path && (
                      <span className="block text-xs text-gray-600 mt-0.5 truncate">{file.path}</span>
                    )}
                  </button>
                </li>
              ))}
            </ul>

            <div className="mt-6 p-4 bg-gray-900 rounded-xl border border-gray-800">
              <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">
                Зависимости
              </h3>
              <ul className="text-xs space-y-1 text-gray-400">
                <li className="flex items-center gap-2">
                  <span className="w-2 h-2 bg-green-400 rounded-full"></span>
                  Minecraft 1.20.1
                </li>
                <li className="flex items-center gap-2">
                  <span className="w-2 h-2 bg-green-400 rounded-full"></span>
                  Forge 47.2.0+
                </li>
                <li className="flex items-center gap-2">
                  <span className="w-2 h-2 bg-blue-400 rounded-full"></span>
                  ProjectE
                </li>
                <li className="flex items-center gap-2">
                  <span className="w-2 h-2 bg-orange-400 rounded-full"></span>
                  GregTech CEu Modern
                </li>
              </ul>
            </div>
          </div>
        </nav>

        {/* Main Content */}
        <main className="flex-1 min-w-0">
          <FileContent tab={activeTab} />
        </main>
      </div>
    </div>
  )
}

function FileContent({ tab }: { tab: Tab }) {
  const content = getFileContent(tab)
  
  if (tab === 'overview') {
    return <OverviewContent />
  }
  
  if (tab === 'compile') {
    return <CompileContent />
  }

  return (
    <div className="bg-gray-900 rounded-xl border border-gray-800 overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-800 flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold text-gray-100">
            {files.find(f => f.id === tab)?.name}
          </h2>
          <p className="text-sm text-gray-500 mt-0.5">
            src/main/java/{files.find(f => f.id === tab)?.path}
          </p>
        </div>
        <span className="px-2 py-1 text-xs bg-gray-800 rounded text-gray-400">
          {files.find(f => f.id === tab)?.language}
        </span>
      </div>
      <div className="overflow-x-auto">
        <SyntaxHighlighter
          language={files.find(f => f.id === tab)?.language || 'java'}
          style={vscDarkPlus}
          customStyle={{ margin: 0, padding: '1.5rem', fontSize: '0.8rem', background: 'transparent' }}
          showLineNumbers
          wrapLines
        >
          {content}
        </SyntaxHighlighter>
      </div>
    </div>
  )
}

function OverviewContent() {
  return (
    <div className="space-y-6">
      {/* Описание */}
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-emerald-300 mb-4">📖 Описание мода</h2>
        <p className="text-gray-300 leading-relaxed mb-4">
          <strong className="text-white">GTCe Modern EMC Addon</strong> — мод-аддон для Minecraft 1.20.1 (Forge), 
          который автоматически рассчитывает и назначает EMC-стоимость (ProjectE) для всех предметов и жидкостей 
          из мода <strong className="text-orange-300">GregTech CEu Modern</strong>.
        </p>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
          <FeatureCard
            icon="🔄"
            title="Рекурсивный расчёт"
            description="Обходит рецепты GT машин (Assembler, Bender, Wiremill, Chemical Reactor и др.) и рассчитывает EMC на основе ингредиентов"
          />
          <FeatureCard
            icon="💧"
            title="Поддержка жидкостей"
            description="Работает с жидкостями GT: 1000 mB = базовая EMC. Пропорциональный расчёт для рецептов"
          />
          <FeatureCard
            icon="🛡️"
            title="Защита от циклов"
            description="Лимит итераций (до 20), игнорирование chanced outputs < 100%, детектор циклических рецептов"
          />
          <FeatureCard
            icon="⚙️"
            title="Настраиваемость"
            description="Полная конфигурация: лимит итераций, порог шанса, базовые EMC жидкостей, debug-логирование"
          />
        </div>
      </div>

      {/* Архитектура */}
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-cyan-300 mb-4">🏗️ Архитектура</h2>
        <div className="bg-gray-950 rounded-lg p-4 font-mono text-sm text-gray-300 overflow-x-auto">
          <pre>{`
┌─────────────────────────────────────────────────────────────────┐
│                    GTEMCIntegration                              │
│              (Точка входа, координатор)                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐     │
│  │FluidEMCReg.  │  │RecipeWalker  │  │ EMCCalculator     │     │
│  │(базовые      │  │(обход GT     │  │ (финальный        │     │
│  │ жидкости)    │  │ рецептов)    │  │  расчёт)          │     │
│  └──────┬───────┘  └──────┬───────┘  └────────┬──────────┘     │
│         │                 │                     │                │
│         └─────────────────┼─────────────────────┘                │
│                           │                                      │
│                    ┌──────▼───────┐                              │
│                    │  GTEMCMapper │                              │
│                    │ (ProjectE    │                              │
│                    │  API bridge) │                              │
│                    └──────┬───────┘                              │
│                           │                                      │
├───────────────────────────┼──────────────────────────────────────┤
│                    ┌──────▼───────┐                              │
│                    │   ProjectE   │  ← EMC Registration Event    │
│                    │   API        │                              │
│                    └──────────────┘                              │
└─────────────────────────────────────────────────────────────────┘
          `}</pre>
        </div>
      </div>

      {/* Алгоритм */}
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-purple-300 mb-4">🧮 Алгоритм расчёта</h2>
        <div className="space-y-3">
          <Step number={1} title="Инициализация базовых EMC">
            <p>Собираются EMC из ванильных предметов (ProjectE стандарт) и базовых материалов GT (iron=256, gold=2048, diamond=8192 и т.д.).</p>
            <p>Для каждого материала регистрируются все формы (ingot, dust, plate, gear, wire...) с пропорциональным EMC.</p>
          </Step>
          <Step number={2} title="Инициализация жидкостей">
            <p>Регистрируются базовые EMC для жидкостей GT: металлы, кислоты, полимеры, газы.</p>
            <p>Соотношение: 1000 mB (1 ведро) = EMC слитка × 7 (т.к. 1 слиток = 144 mB в GT).</p>
          </Step>
          <Step number={3} title="Итеративный обход рецептов">
            <p>Для каждого типа GT машины (до 30+ типов) обходятся все рецепты.</p>
            <p>Если ВСЕ ингредиенты имеют EMC → результат получает EMC = Σ(вход) / количество_выхода.</p>
            <p>Для жидкостей: EMC_за_1000mB = (Σ(вход) × 1000) / amount_mB.</p>
          </Step>
          <Step number={4} title="Повторение до стабилизации">
            <p>Цикл повторяется до 20 итераций или пока новые EMC не перестанут назначаться.</p>
            <p>Это позволяет разрешить глубокие цепочки (микросхемы → платы → процессоры → наноматериалы).</p>
          </Step>
          <Step number={5} title="Валидация и применение">
            <p>Проверка на разумность значений (не отрицательные, не &gt; 10 млрд).</p>
            <p>Применение к ProjectE через NSSItem/NSSFluid маппер.</p>
          </Step>
        </div>
      </div>

      {/* Формулы */}
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-yellow-300 mb-4">📐 Формулы расчёта</h2>
        <div className="space-y-4">
          <div className="bg-gray-950 rounded-lg p-4">
            <h3 className="text-sm font-semibold text-gray-400 mb-2">Предметный рецепт:</h3>
            <code className="text-emerald-300 text-sm">
              EMC(output) = Σ(EMC(ingredient_i) × count_i) / output_count
            </code>
          </div>
          <div className="bg-gray-950 rounded-lg p-4">
            <h3 className="text-sm font-semibold text-gray-400 mb-2">Жидкость как ингредиент:</h3>
            <code className="text-cyan-300 text-sm">
              EMC_contribution = (EMC_per_bucket × fluid_amount_mB) / 1000
            </code>
          </div>
          <div className="bg-gray-950 rounded-lg p-4">
            <h3 className="text-sm font-semibold text-gray-400 mb-2">Жидкость как выход:</h3>
            <code className="text-purple-300 text-sm">
              EMC_per_bucket(output_fluid) = (Σ(input_EMC) × 1000) / output_amount_mB
            </code>
          </div>
          <div className="bg-gray-950 rounded-lg p-4">
            <h3 className="text-sm font-semibold text-gray-400 mb-2">Формы материала:</h3>
            <code className="text-orange-300 text-sm">
              EMC(form) = EMC(ingot) × multiplier<br/>
              plate=0.25, gear=4.0, wire=0.25, bolt=0.125, block=9.0, nugget=0.111...
            </code>
          </div>
        </div>
      </div>

      {/* Структура файлов */}
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-pink-300 mb-4">📁 Структура проекта</h2>
        <div className="bg-gray-950 rounded-lg p-4 font-mono text-sm text-gray-300">
          <pre>{`gtcemodern-emc-addon/
├── build.gradle                          # Скрипт сборки (Forge Gradle)
├── gradle.properties                     # Свойства сборки
└── src/main/
    ├── java/com/gtemc/
    │   ├── GTEMCAddon.java              # @Mod - главный класс
    │   ├── config/
    │   │   └── GTEMCConfig.java         # ForgeConfigSpec
    │   └── emc/
    │       ├── GTEMCMapper.java         # Основной маппер (ProjectE API)
    │       ├── GTEMCIntegration.java    # Координатор расчёта
    │       ├── FluidEMCRegistry.java    # Реестр EMC жидкостей
    │       ├── RecipeWalker.java        # Обходчик рецептов GT
    │       └── EMCCalculator.java       # Калькулятор + валидация
    └── resources/
        ├── META-INF/mods.toml           # Forge mod descriptor
        └── pack.mcmeta                  # Resource pack metadata`}</pre>
        </div>
      </div>

      {/* Установка */}
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-green-300 mb-4">🚀 Установка и сборка</h2>
        <div className="space-y-3">
          <div className="bg-gray-950 rounded-lg p-4">
            <code className="text-sm text-gray-300">
              <span className="text-gray-500"># 1. Клонировать и настроить</span><br/>
              git clone &lt;repo&gt; gtcemc-addon<br/>
              cd gtcemc-addon<br/><br/>
              <span className="text-gray-500"># 2. Обновить версии зависимостей в build.gradle</span><br/>
              <span className="text-gray-500"># (CurseMaven ID для ProjectE и GTCEu)</span><br/><br/>
              <span className="text-gray-500"># 3. Собрать</span><br/>
              ./gradlew build<br/><br/>
              <span className="text-gray-500"># 4. JAR будет в build/libs/</span><br/>
              <span className="text-gray-500"># Поместить в папку mods вместе с ProjectE и GTCEu</span>
            </code>
          </div>
        </div>
      </div>
    </div>
  )
}

function CompileContent() {
  return (
    <div className="space-y-6">
      {/* Требования */}
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-emerald-300 mb-4">📋 Требования для компиляции</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-gray-800/50 rounded-lg p-4 border border-gray-700/50">
            <div className="text-2xl mb-2">☕</div>
            <h3 className="font-semibold text-gray-200 mb-1">JDK 17</h3>
            <p className="text-sm text-gray-400">
              <a href="https://adoptium.net/temurin/releases/?version=17" target="_blank" className="text-emerald-400 hover:underline">
                Скачать Adoptium Temurin
              </a>
            </p>
          </div>
          <div className="bg-gray-800/50 rounded-lg p-4 border border-gray-700/50">
            <div className="text-2xl mb-2">📦</div>
            <h3 className="font-semibold text-gray-200 mb-1">Gradle 8.1+</h3>
            <p className="text-sm text-gray-400">
              Используется Gradle Wrapper (gradlew)
            </p>
          </div>
          <div className="bg-gray-800/50 rounded-lg p-4 border border-gray-700/50">
            <div className="text-2xl mb-2">💻</div>
            <h3 className="font-semibold text-gray-200 mb-1">IDE (опционально)</h3>
            <p className="text-sm text-gray-400">
              IntelliJ IDEA / Eclipse / VS Code
            </p>
          </div>
        </div>
      </div>

      {/* Быстрый старт */}
      <div className="bg-gradient-to-br from-emerald-900/30 to-cyan-900/30 rounded-xl border border-emerald-700/50 p-6">
        <h2 className="text-xl font-bold text-emerald-300 mb-4">⚡ Быстрый старт (TL;DR)</h2>
        <div className="bg-gray-950 rounded-lg p-4 font-mono text-sm">
          <div className="text-gray-500 mb-2"># 1. Проверьте Java</div>
          <div className="text-emerald-300">java -version</div>
          <div className="text-gray-500 mt-3 mb-2"># 2. Скопируйте файлы мода в папку</div>
          <div className="text-gray-500"># 3. Обновите CurseMaven ID в build.gradle</div>
          <div className="text-gray-500 mt-3 mb-2"># 4. Соберите мод</div>
          <div className="text-emerald-300">./gradlew build</div>
          <div className="text-gray-500 mt-3 mb-2"># 5. JAR будет в build/libs/</div>
          <div className="text-emerald-300">ls build/libs/</div>
        </div>
      </div>

      {/* Пошаговая инструкция */}
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-cyan-300 mb-4">🔧 Пошаговая инструкция</h2>
        <div className="space-y-4">
          
          <Step number={1} title="Подготовка проекта">
            <div className="bg-gray-950 rounded-lg p-3 font-mono text-xs text-gray-300">
              <div className="text-gray-500"># Создайте папку и скопируйте файлы мода</div>
              <div>mkdir gtcemc-addon && cd gtcemc-addon</div>
              <div className="text-gray-500 mt-2"># Скопируйте содержимое mod-src/ сюда</div>
              <div className="text-gray-500"># Структура:</div>
              <div className="text-gray-500"># ├── build.gradle</div>
              <div className="text-gray-500"># ├── gradle.properties</div>
              <div className="text-gray-500"># └── src/main/java/com/gtemc/...</div>
            </div>
          </Step>

          <Step number={2} title="Установка Gradle Wrapper">
            <div className="bg-gray-950 rounded-lg p-3 font-mono text-xs text-gray-300">
              <div className="text-gray-500"># Создайте файл gradle/wrapper/gradle-wrapper.properties:</div>
              <div className="mt-2">distributionBase=GRADLE_USER_HOME</div>
              <div>distributionPath=wrapper/dists</div>
              <div>distributionUrl=https\://services.gradle.org/distributions/gradle-8.1.1-bin.zip</div>
              <div>zipStoreBase=GRADLE_USER_HOME</div>
              <div>zipStorePath=wrapper/dists</div>
            </div>
            <div className="mt-2 text-sm text-gray-400">
              Или используйте готовый gradlew из Forge MDK: <br/>
              <a href="https://github.com/MinecraftForge/MinecraftForge" target="_blank" className="text-cyan-400 hover:underline">
                github.com/MinecraftForge/MinecraftForge
              </a>
            </div>
          </Step>

          <Step number={3} title="Обновление зависимостей (ВАЖНО!)">
            <p className="text-sm text-gray-400 mb-2">
              Откройте <code className="text-emerald-300 bg-gray-800 px-1 rounded">build.gradle</code> и обновите CurseMaven ID 
              на актуальные версии для 1.20.1:
            </p>
            <div className="bg-gray-950 rounded-lg p-3 font-mono text-xs text-gray-300">
              <div className="text-gray-500">// build.gradle - секция dependencies</div>
              <div className="mt-1">dependencies {'{'}</div>
              <div className="pl-4">minecraft 'net.minecraftforge:forge:1.20.1-47.2.0'</div>
              <div className="pl-4 mt-2 text-gray-500">// Найдите актуальный FILE_ID на CurseForge!</div>
              <div className="pl-4 text-yellow-300">implementation fg.deobf('curse.maven:projecte-226410:<span className="text-red-400">ВАШ_ID</span>')</div>
              <div className="pl-4 text-yellow-300">implementation fg.deobf('curse.maven:gtceu-226410:<span className="text-red-400">ВАШ_ID</span>')</div>
              <div className="pl-4">implementation fg.deobf('curse.maven:codechickenlib-242818:<span className="text-red-400">ВАШ_ID</span>')</div>
              <div className="pl-4">implementation fg.deobf('curse.maven:ldlib-626668:<span className="text-red-400">ВАШ_ID</span>')</div>
              <div>{'}'}</div>
            </div>
            <div className="mt-3 p-3 bg-yellow-900/20 border border-yellow-700/50 rounded-lg">
              <p className="text-sm text-yellow-200">
                <strong>⚠️ Как найти FILE_ID:</strong> Перейдите на CurseForge → Files → выберите версию для 1.20.1 → 
                ID в URL (например: curseforge.com/.../files/<span className="text-emerald-300">4860000</span>)
              </p>
            </div>
          </Step>

          <Step number={4} title="Проверка Java версии">
            <div className="bg-gray-950 rounded-lg p-3 font-mono text-xs text-gray-300">
              <div>java -version</div>
              <div className="text-gray-500"># Должно показать: openjdk version "17.x.x"</div>
              <div className="mt-2">javac -version</div>
              <div className="text-gray-500"># Должно показать: javac 17.x.x</div>
            </div>
            <div className="mt-2 text-sm text-gray-400">
              Если версия не 17, установите <code className="text-emerald-300 bg-gray-800 px-1 rounded">JAVA_HOME</code>:
            </div>
            <div className="bg-gray-950 rounded-lg p-3 font-mono text-xs text-gray-300 mt-2">
              <div className="text-gray-500"># Windows:</div>
              <div>set JAVA_HOME=C:\Program Files\Java\jdk-17</div>
              <div className="mt-2 text-gray-500"># Linux/Mac:</div>
              <div>export JAVA_HOME=/usr/lib/jvm/java-17-openjdk</div>
            </div>
          </Step>

          <Step number={5} title="Сборка мода">
            <div className="bg-gray-950 rounded-lg p-3 font-mono text-xs text-gray-300">
              <div className="text-gray-500"># Windows:</div>
              <div className="text-emerald-300">gradlew.bat build</div>
              <div className="mt-2 text-gray-500"># Linux/Mac:</div>
              <div className="text-emerald-300">./gradlew build</div>
            </div>
            <div className="mt-2 p-3 bg-blue-900/20 border border-blue-700/50 rounded-lg">
              <p className="text-sm text-blue-200">
                <strong>💡 Первая сборка</strong> займёт 5-15 минут (скачивание зависимостей). 
                Последующие сборки будут быстрее.
              </p>
            </div>
            <div className="mt-2 text-sm text-gray-400">Ожидаемый результат:</div>
            <div className="bg-gray-950 rounded-lg p-3 font-mono text-xs text-gray-300 mt-1">
              <div className="text-green-400">BUILD SUCCESSFUL in 5m 23s</div>
            </div>
          </Step>

          <Step number={6} title="Установка JAR в Minecraft">
            <div className="bg-gray-950 rounded-lg p-3 font-mono text-xs text-gray-300">
              <div className="text-gray-500"># JAR файл будет здесь:</div>
              <div className="text-emerald-300">build/libs/gtcemodern-emc-addon-1.0.0.jar</div>
              <div className="mt-2 text-gray-500"># Скопируйте в папку mods:</div>
              <div className="text-gray-500"># Windows: %APPDATA%/.minecraft/mods/</div>
              <div className="text-gray-500"># Linux: ~/.minecraft/mods/</div>
              <div className="text-gray-500"># Mac: ~/Library/Application Support/minecraft/mods/</div>
            </div>
            <div className="mt-3 p-3 bg-purple-900/20 border border-purple-700/50 rounded-lg">
              <p className="text-sm text-purple-200">
                <strong>🎮 Также установите в папку mods:</strong>
              </p>
              <ul className="text-sm text-purple-200 mt-1 space-y-1">
                <li>• ProjectE (для 1.20.1)</li>
                <li>• GregTech CEu Modern (для 1.20.1)</li>
                <li>• CodeChickenLib</li>
                <li>• LDLib</li>
              </ul>
            </div>
          </Step>
        </div>
      </div>

      {/* Полезные команды */}
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-purple-300 mb-4">🛠️ Полезные команды</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <CommandBlock cmd="./gradlew clean" desc="Очистка предыдущей сборки" />
          <CommandBlock cmd="./gradlew build -x test" desc="Сборка без тестов (быстрее)" />
          <CommandBlock cmd="./gradlew build --info" desc="Сборка с подробным выводом" />
          <CommandBlock cmd="./gradlew runClient" desc="Запуск клиента для тестирования" />
          <CommandBlock cmd="./gradlew runServer" desc="Запуск сервера для тестирования" />
          <CommandBlock cmd="./gradlew idea" desc="Генерация файлов для IntelliJ" />
          <CommandBlock cmd="./gradlew eclipse" desc="Генерация файлов для Eclipse" />
          <CommandBlock cmd="./gradlew --refresh-dependencies" desc="Обновить зависимости" />
        </div>
      </div>

      {/* Решение проблем */}
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-red-300 mb-4">🐛 Решение проблем</h2>
        <div className="space-y-3">
          <ProblemBlock
            error="Could not resolve all files for configuration"
            solution="Неверные CurseMaven ID. Проверьте актуальные ID файлов на CurseForge для 1.20.1"
          />
          <ProblemBlock
            error="Unsupported class file major version 61"
            solution="Используется Java ниже 17. Установите JDK 17 и проверьте JAVA_HOME"
          />
          <ProblemBlock
            error="package com.gregtechceu.gtceu does not exist"
            solution="GTCEu не загружен. Проверьте CurseMaven ID и версию для 1.20.1"
          />
          <ProblemBlock
            error="Could not find net.minecraftforge:forge:1.20.1-47.2.0"
            solution="Добавьте репозиторий Forge в repositories: maven { url = 'https://maven.minecraftforge.net/' }"
          />
          <ProblemBlock
            error="gradlew: Permission denied (Linux/Mac)"
            solution="Выполните: chmod +x gradlew"
          />
        </div>
      </div>

      {/* Структура проекта */}
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-xl font-bold text-yellow-300 mb-4">📁 Итоговая структура проекта</h2>
        <div className="bg-gray-950 rounded-lg p-4 font-mono text-xs text-gray-300">
          <pre>{`gtcemc-addon/
├── build.gradle                    # Скрипт сборки
├── gradle.properties               # Свойства сборки
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew                         # Linux/Mac wrapper
├── gradlew.bat                     # Windows wrapper
├── settings.gradle                 # Настройки проекта
├── src/main/
│   ├── java/com/gtemc/
│   │   ├── GTEMCAddon.java        # @Mod - главный класс
│   │   ├── config/
│   │   │   └── GTEMCConfig.java   # Конфигурация
│   │   └── emc/
│   │       ├── GTEMCMapper.java   # EMC маппер
│   │       ├── GTEMCIntegration.java
│   │       ├── FluidEMCRegistry.java
│   │       ├── RecipeWalker.java
│   │       └── EMCCalculator.java
│   └── resources/
│       ├── META-INF/mods.toml     # Forge mod descriptor
│       └── pack.mcmeta
└── build/libs/                     # ← Результат сборки
    └── gtcemodern-emc-addon-1.0.0.jar`}</pre>
        </div>
      </div>
    </div>
  )
}

function CommandBlock({ cmd, desc }: { cmd: string; desc: string }) {
  return (
    <div className="bg-gray-950 rounded-lg p-3 border border-gray-700/50">
      <code className="text-emerald-300 text-xs font-mono">{cmd}</code>
      <p className="text-xs text-gray-500 mt-1">{desc}</p>
    </div>
  )
}

function ProblemBlock({ error, solution }: { error: string; solution: string }) {
  return (
    <div className="bg-gray-800/50 rounded-lg p-3 border border-gray-700/50">
      <div className="flex items-start gap-2">
        <span className="text-red-400 text-sm">❌</span>
        <div>
          <code className="text-red-300 text-xs font-mono">{error}</code>
          <p className="text-sm text-gray-400 mt-1">
            <span className="text-green-400">✅</span> {solution}
          </p>
        </div>
      </div>
    </div>
  )
}

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

function Step({ number, title, children }: { number: number; title: string; children: React.ReactNode }) {
  return (
    <div className="flex gap-4">
      <div className="w-8 h-8 shrink-0 bg-emerald-500/20 border border-emerald-500/30 rounded-full flex items-center justify-center text-sm font-bold text-emerald-300">
        {number}
      </div>
      <div className="flex-1">
        <h3 className="font-semibold text-gray-200 mb-1">{title}</h3>
        <div className="text-sm text-gray-400 space-y-1">{children}</div>
      </div>
    </div>
  )
}

function getFileContent(tab: Tab): string {
  switch (tab) {
    case 'mapper': return MAPPER_CODE
    case 'fluid': return FLUID_CODE
    case 'walker': return WALKER_CODE
    case 'calculator': return CALCULATOR_CODE
    case 'integration': return INTEGRATION_CODE
    case 'config': return CONFIG_CODE
    case 'build': return BUILD_CODE
    default: return ''
  }
}

// Код файлов (хранится как строки для отображения)
const MAPPER_CODE = `package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Основной EMC-маппер для GregTech CEu Modern.
 * Регистрируется через ProjectE API и рассчитывает EMC-стоимость
 * для всех предметов и жидкостей GT на основе рецептов машин.
 * 
 * Алгоритм:
 * 1. Собирает базовые EMC из ванильных/уже определённых значений
 * 2. Итеративно обходит рецепты GT машин
 * 3. Если все ингредиенты имеют EMC - рассчитывает EMC для выхода
 * 4. Повторяет до стабилизации или достижения лимита итераций
 */
public class GTEMCMapper {

    // Хранилище рассчитанных EMC для предметов (ResourceLocation -> EMC за 1 шт.)
    private static final Map<ResourceLocation, Long> itemEMC = new HashMap<>();
    
    // Хранилище рассчитанных EMC для жидкостей (ResourceLocation -> EMC за 1000 mB)
    private static final Map<ResourceLocation, Long> fluidEMC = new HashMap<>();
    
    // Множество уже обработанных рецептов (предотвращение зацикливания)
    private static final Set<String> processedRecipes = new HashSet<>();
    
    // Флаг регистрации
    private static boolean registered = false;

    /**
     * Регистрация маппера в системе ProjectE.
     * Вызывается при инициализации мода.
     */
    public static void register() {
        if (registered) return;
        registered = true;
        
        GTEMCAddon.LOGGER.info("[GTEMCAddon] EMC маппер зарегистрирован");
        
        // Регистрируем наш конвертер через ProjectE API
        moze_intel.projecte.api.ProjectEAPI.getEMCRegistrationEvent().addListener(event -> {
            addMappings(event.getMappingCollector());
        });
    }

    /**
     * Основной метод добавления EMC-маппингов.
     * Вызывается ProjectE при сборке EMC-карты.
     */
    public static <T extends NormalizedSimpleStack<T, ?>> void addMappings(
            IMappingCollector<T, Long> mapper) {
        
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Начинается расчёт EMC для GregTech...");
        long startTime = System.currentTimeMillis();
        
        // Шаг 1: Собираем базовые EMC из ванильных предметов
        collectBaseEMC(mapper);
        
        // Шаг 2: Инициализируем FluidEMCRegistry базовыми жидкостями
        FluidEMCRegistry.initialize(mapper);
        
        // Шаг 3: Итеративный обход рецептов GT
        int maxIterations = GTEMCConfig.MAX_ITERATIONS.get();
        int iteration = 0;
        int totalMapped = 0;
        
        do {
            iteration++;
            int mappedThisIteration = 0;
            
            if (GTEMCConfig.CALCULATE_ITEMS.get()) {
                mappedThisIteration += processItemRecipes(mapper);
            }
            if (GTEMCConfig.CALCULATE_FLUIDS.get()) {
                mappedThisIteration += processFluidRecipes(mapper);
            }
            
            totalMapped += mappedThisIteration;
            
            if (mappedThisIteration == 0) {
                GTEMCAddon.LOGGER.info("[GTEMCAddon] Стабилизация на итерации {}", iteration);
                break;
            }
        } while (iteration < maxIterations);
        
        long elapsed = System.currentTimeMillis() - startTime;
        GTEMCAddon.LOGGER.info("[GTEMCAddon] Завершено за {} мс. Итераций: {}, Назначено: {} предметов, {} жидкостей",
                elapsed, iteration, itemEMC.size(), fluidEMC.size());
    }

    /**
     * Сбор базовых EMC из ванильных предметов.
     */
    private static <T extends NormalizedSimpleStack<T, ?>> void collectBaseEMC(
            IMappingCollector<T, Long> mapper) {
        
        Map<ItemStack, Long> vanillaEMC = getVanillaBaseEMC();
        for (Map.Entry<ItemStack, Long> entry : vanillaEMC.entrySet()) {
            ItemStack stack = entry.getKey();
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (rl != null) {
                itemEMC.put(rl, entry.getValue());
            }
        }
        
        collectMaterialBaseEMC();
    }

    /**
     * Базовые ванильные EMC значения.
     */
    private static Map<ItemStack, Long> getVanillaBaseEMC() {
        Map<ItemStack, Long> base = new HashMap<>();
        base.put(new ItemStack(Items.COBBLESTONE), 1L);
        base.put(new ItemStack(Items.STONE), 1L);
        base.put(new ItemStack(Items.DIRT), 1L);
        base.put(new ItemStack(Items.COAL), 64L);
        base.put(new ItemStack(Items.IRON_INGOT), 256L);
        base.put(new ItemStack(Items.GOLD_INGOT), 2048L);
        base.put(new ItemStack(Items.DIAMOND), 8192L);
        base.put(new ItemStack(Items.EMERALD), 8192L);
        base.put(new ItemStack(Items.REDSTONE), 32L);
        base.put(new ItemStack(Items.QUARTZ), 128L);
        base.put(new ItemStack(Items.OBSIDIAN), 64L);
        base.put(new ItemStack(Items.BLAZE_ROD), 768L);
        base.put(new ItemStack(Items.ENDER_PEARL), 1024L);
        // ... и т.д.
        return base;
    }

    /**
     * Расчёт EMC для предметных выходов рецептов.
     */
    private static <T extends NormalizedSimpleStack<T, ?>> int processItemRecipes(
            IMappingCollector<T, Long> mapper) {
        int newMappings = 0;
        
        for (GTRecipeType recipeType : getGTRecipeTypes()) {
            for (GTRecipe recipe : recipeType.getRecipes()) {
                String recipeId = recipe.getId().toString();
                if (processedRecipes.contains(recipeId)) continue;
                
                long inputEMC = calculateInputEMC(recipe);
                if (inputEMC < 0) continue;
                
                List<ItemStack> outputs = getRecipeOutputs(recipe);
                if (outputs.isEmpty()) continue;
                
                long emcPerOutput = inputEMC / outputs.size();
                if (emcPerOutput <= 0) continue;
                
                for (ItemStack output : outputs) {
                    ResourceLocation outputRL = ForgeRegistries.ITEMS.getKey(output.getItem());
                    if (outputRL == null) continue;
                    if (outputRL.getNamespace().equals("minecraft")) continue;
                    
                    if (!itemEMC.containsKey(outputRL)) {
                        itemEMC.put(outputRL, emcPerOutput);
                        newMappings++;
                    }
                }
                processedRecipes.add(recipeId);
            }
        }
        return newMappings;
    }

    /**
     * Расчёт EMC для жидкостных выходов рецептов.
     */
    private static <T extends NormalizedSimpleStack<T, ?>> int processFluidRecipes(
            IMappingCollector<T, Long> mapper) {
        int newMappings = 0;
        
        for (GTRecipeType recipeType : getGTRecipeTypes()) {
            for (GTRecipe recipe : recipeType.getRecipes()) {
                String recipeId = recipe.getId().toString() + "_fluid";
                if (processedRecipes.contains(recipeId)) continue;
                
                long inputEMC = calculateTotalInputEMC(recipe);
                if (inputEMC < 0) continue;
                
                List<FluidStack> fluidOutputs = getRecipeFluidOutputs(recipe);
                for (FluidStack fluidOutput : fluidOutputs) {
                    ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidOutput.getFluid());
                    if (fluidRL == null) continue;
                    
                    int amount = fluidOutput.getAmount();
                    long emcPerBucket = (inputEMC * 1000L) / amount;
                    
                    if (emcPerBucket > 0 && !fluidEMC.containsKey(fluidRL)) {
                        fluidEMC.put(fluidRL, emcPerBucket);
                        newMappings++;
                    }
                }
                processedRecipes.add(recipeId);
            }
        }
        return newMappings;
    }

    /**
     * Расчёт EMC всех ингредиентов (предметы).
     * Возвращает -1 если хоть один ингредиент не имеет EMC.
     */
    private static long calculateInputEMC(GTRecipe recipe) {
        long totalEMC = 0;
        List<Content> inputs = recipe.getInputs();
        for (Content input : inputs) {
            if (input.getContent() instanceof ItemStack stack) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl == null) return -1;
                Long emc = itemEMC.get(rl);
                if (emc == null) return -1;
                totalEMC += emc * stack.getCount();
            }
        }
        return totalEMC;
    }

    /**
     * Расчёт полного EMC входа (предметы + жидкости).
     * Формула для жидкости: EMC = (emcPerBucket * amount) / 1000
     */
    private static long calculateTotalInputEMC(GTRecipe recipe) {
        long totalEMC = 0;
        
        // Предметные входы
        for (Content input : recipe.getInputs()) {
            if (input.getContent() instanceof ItemStack stack) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl == null) return -1;
                Long emc = itemEMC.get(rl);
                if (emc == null) return -1;
                totalEMC += emc * stack.getCount();
            }
        }
        
        // Жидкостные входы
        for (Content fluidInput : recipe.getFluidInputs()) {
            if (fluidInput.getContent() instanceof FluidStack fluidStack) {
                ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
                if (fluidRL == null) return -1;
                Long emcPerBucket = fluidEMC.get(fluidRL);
                if (emcPerBucket == null) return -1;
                int amount = fluidStack.getAmount(); // mB
                totalEMC += (emcPerBucket * amount) / 1000L;
            }
        }
        return totalEMC;
    }

    /**
     * Получение выходов с учётом chanced outputs.
     * Игнорирует побочные продукты с шансом < 100%.
     */
    private static List<ItemStack> getRecipeOutputs(GTRecipe recipe) {
        List<ItemStack> outputs = new ArrayList<>();
        
        // Основные выходы (100% шанс)
        for (Content output : recipe.getOutputs()) {
            if (output.getContent() instanceof ItemStack stack) {
                outputs.add(stack.copy());
            }
        }
        
        // Chanced outputs - проверяем порог
        if (GTEMCConfig.MIN_CHANCE_THRESHOLD.get() < 100) {
            List<Content> chanced = recipe.getChancedOutputs();
            if (chanced != null) {
                for (Content chancedOutput : chanced) {
                    int chance = chancedOutput.getChance(); // 0-10000
                    int threshold = GTEMCConfig.MIN_CHANCE_THRESHOLD.get() * 100;
                    if (chance >= threshold && chancedOutput.getContent() instanceof ItemStack stack) {
                        outputs.add(stack.copy());
                    }
                }
            }
        }
        return outputs;
    }

    // ... вспомогательные методы (getGTRecipeTypes, getRecipeFluidOutputs и т.д.)
}`

const FLUID_CODE = `package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * Реестр EMC для жидкостей GregTech.
 * 
 * Базовое соотношение: 1000 mB (1 ведро) = базовая EMC стоимость.
 * При расчёте рецептов: EMC = (emcPerBucket * recipeAmount) / 1000
 */
public class FluidEMCRegistry {

    // EMC за 1000 mB для каждой жидкости
    private static final Map<ResourceLocation, Long> fluidEMCValues = new HashMap<>();
    
    // Приоритетные жидкости (не перезаписываются калькулятором)
    private static final Map<ResourceLocation, Long> priorityFluids = new HashMap<>();

    /**
     * Инициализация реестра базовыми значениями.
     */
    public static <T extends NormalizedSimpleStack<T, ?>> void initialize(
            IMappingCollector<T, Long> mapper) {
        
        registerBaseFluids();
        registerGTMaterialFluids();
        applyToMapper(mapper);
    }

    /**
     * Базовые ванильные жидкости.
     */
    private static void registerBaseFluids() {
        registerFluid("minecraft:water", 1L);
        registerFluid("minecraft:lava", 64L);
        registerFluid("minecraft:milk", 128L);
        setPriority("minecraft:water", 1L);
        setPriority("minecraft:lava", 64L);
    }

    /**
     * Жидкости GregTech материалов.
     * Формула: EMC жидкости = EMC слитка * (1000 / 144) ≈ EMC слитка * 7
     * (т.к. 1 слиток в GT = 144 mB)
     */
    private static void registerGTMaterialFluids() {
        // Металлы
        registerFluid("gtceu:iron", 256L * 7);           // 1792
        registerFluid("gtceu:gold", 2048L * 7);          // 14336
        registerFluid("gtceu:copper", 128L * 7);         // 896
        registerFluid("gtceu:tin", 64L * 7);             // 448
        registerFluid("gtceu:bronze", 96L * 7);          // 672
        registerFluid("gtceu:steel", 512L * 7);          // 3584
        registerFluid("gtceu:titanium", 4096L * 7);      // 28672
        registerFluid("gtceu:tungsten", 2048L * 7);      // 14336
        registerFluid("gtceu:iridium", 8192L * 7);       // 57344
        registerFluid("gtceu:naquadah", 16384L * 7);     // 114688
        registerFluid("gtceu:neutronium", 262144L * 7);  // 1835008
        
        // Химические жидкости
        registerFluid("gtceu:sulfuric_acid", 128L);
        registerFluid("gtceu:hydrochloric_acid", 96L);
        registerFluid("gtceu:nitric_acid", 128L);
        registerFluid("gtceu:hydrogen", 16L);
        registerFluid("gtceu:oxygen", 16L);
        registerFluid("gtceu:nitrogen", 16L);
        registerFluid("gtceu:chlorine", 32L);
        registerFluid("gtceu:methane", 64L);
        registerFluid("gtceu:ethylene", 96L);
        registerFluid("gtceu:benzene", 256L);
        registerFluid("gtceu:polyethylene", 192L);
        registerFluid("gtceu:polyvinyl_chloride", 256L);
        registerFluid("gtceu:polytetrafluoroethylene", 512L);
        registerFluid("gtceu:polybenzimidazole", 2048L);
        registerFluid("gtceu:epoxid", 384L);
        registerFluid("gtceu:rubber", 128L);
        registerFluid("gtceu:creosote", 8L);
        registerFluid("gtceu:seed_oil", 16L);
        registerFluid("gtceu:lubricant", 64L);
        registerFluid("gtceu:concrete", 4L);
        registerFluid("gtceu:uranium_hexafluoride", 8192L);
    }

    private static void registerFluid(String fluidId, long emcPerBucket) {
        ResourceLocation rl = new ResourceLocation(fluidId);
        fluidEMCValues.put(rl, emcPerBucket);
    }

    private static void setPriority(String fluidId, long emcPerBucket) {
        ResourceLocation rl = new ResourceLocation(fluidId);
        priorityFluids.put(rl, emcPerBucket);
        fluidEMCValues.put(rl, emcPerBucket);
    }

    /**
     * Применение к мапперу ProjectE.
     */
    private static <T extends NormalizedSimpleStack<T, ?>> void applyToMapper(
            IMappingCollector<T, Long> mapper) {
        for (Map.Entry<ResourceLocation, Long> entry : fluidEMCValues.entrySet()) {
            ResourceLocation fluidRL = entry.getKey();
            long emcPerBucket = entry.getValue();
            
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidRL);
            if (fluid == null || fluid == Fluids.EMPTY) continue;
            
            try {
                NSSFluid nssFluid = NSSFluid.createFluid(fluidRL);
                mapper.setValueBefore(nssFluid, emcPerBucket);
            } catch (Exception e) {
                GTEMCAddon.LOGGER.warn("[FluidEMC] Ошибка для {}: {}", fluidRL, e.getMessage());
            }
        }
    }

    /**
     * Получение EMC для FluidStack (пропорционально количеству mB).
     */
    public static long getFluidStackEMC(FluidStack fluidStack) {
        ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
        if (fluidRL == null) return 0;
        
        long emcPerBucket = fluidEMCValues.getOrDefault(fluidRL, 0L);
        if (emcPerBucket <= 0) return 0;
        
        // Пропорционально: (emcPerBucket * amount) / 1000
        return (emcPerBucket * fluidStack.getAmount()) / 1000L;
    }

    /**
     * Регистрация нового EMC (используется калькулятором).
     * Не перезаписывает приоритетные жидкости.
     */
    public static boolean registerFluidEMC(ResourceLocation fluidRL, long emcPerBucket) {
        if (priorityFluids.containsKey(fluidRL)) return false;
        if (fluidEMCValues.containsKey(fluidRL)) return false;
        fluidEMCValues.put(fluidRL, emcPerBucket);
        return true;
    }

    public static Map<ResourceLocation, Long> getAllFluidEMC() {
        return new HashMap<>(fluidEMCValues);
    }
}`

const WALKER_CODE = `package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Обходчик рецептов GregTech.
 * Реализует итеративный алгоритм обхода рецептов для расчёта EMC.
 * 
 * Защита от зацикливания:
 * - Ограничение по количеству итераций (configurable)
 * - Множество уже обработанных рецептов
 * - Игнорирование chanced outputs с шансом < 100%
 * - Детектор циклических рецептов (вход == выход)
 */
public class RecipeWalker {

    private final Set<String> processedRecipes = new HashSet<>();
    private int totalProcessed = 0;
    private int totalAssigned = 0;

    /**
     * Запуск полного обхода рецептов.
     */
    public <T extends NormalizedSimpleStack<T, ?>> int walkAllRecipes(
            IMappingCollector<T, Long> mapper,
            Map<ResourceLocation, Long> itemEMC,
            Map<ResourceLocation, Long> fluidEMC) {
        
        int maxIterations = GTEMCConfig.MAX_ITERATIONS.get();
        int totalNew = 0;
        
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            int newThisIteration = walkOnce(mapper, itemEMC, fluidEMC);
            totalNew += newThisIteration;
            
            GTEMCAddon.LOGGER.info("[RecipeWalker] Итерация {}/{}: +{} EMC",
                    iteration + 1, maxIterations, newThisIteration);
            
            // Стабилизация
            if (newThisIteration == 0) {
                GTEMCAddon.LOGGER.info("[RecipeWalker] Стабилизация на итерации {}", iteration + 1);
                break;
            }
        }
        return totalNew;
    }

    /**
     * Один проход по всем рецептам.
     */
    private <T extends NormalizedSimpleStack<T, ?>> int walkOnce(
            IMappingCollector<T, Long> mapper,
            Map<ResourceLocation, Long> itemEMC,
            Map<ResourceLocation, Long> fluidEMC) {
        
        int newMappings = 0;
        
        for (GTRecipeType recipeType : getAllRecipeTypes()) {
            Collection<GTRecipe> recipes = recipeType.getRecipes();
            if (recipes == null) continue;
            
            for (GTRecipe recipe : recipes) {
                String recipeId = recipeType.getRegistryName() + ":" + recipe.getId();
                if (processedRecipes.contains(recipeId)) continue;
                
                // Проверка на цикл
                if (isCircularRecipe(recipe, itemEMC)) {
                    processedRecipes.add(recipeId);
                    continue;
                }
                
                processedRecipes.add(recipeId);
                totalProcessed++;
                
                // Расчёт EMC входов
                long inputEMC = calculateInputEMC(recipe, itemEMC, fluidEMC);
                if (inputEMC < 0) continue;
                
                // Предметные выходы
                newMappings += processItemOutputs(recipe, inputEMC, itemEMC);
                
                // Жидкостные выходы
                if (GTEMCConfig.CALCULATE_FLUIDS.get()) {
                    newMappings += processFluidOutputs(recipe, inputEMC, fluidEMC);
                }
            }
        }
        totalAssigned += newMappings;
        return newMappings;
    }

    /**
     * Расчёт EMC всех входов (предметы + жидкости).
     * Возвращает -1 если хоть один ингредиент не имеет EMC.
     */
    private long calculateInputEMC(GTRecipe recipe, 
                                    Map<ResourceLocation, Long> itemEMC,
                                    Map<ResourceLocation, Long> fluidEMC) {
        long totalEMC = 0;
        
        // Предметные входы
        for (Content input : recipe.getInputs()) {
            if (input.getContent() instanceof ItemStack stack) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl == null) return -1;
                Long emc = itemEMC.get(rl);
                if (emc == null) return -1;
                totalEMC += emc * stack.getCount();
            }
        }
        
        // Жидкостные входы
        // Формула: EMC = (emcPerBucket * amount) / 1000
        for (Content fluidInput : recipe.getFluidInputs()) {
            if (fluidInput.getContent() instanceof FluidStack fluidStack) {
                ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
                if (fluidRL == null) return -1;
                Long emcPerBucket = fluidEMC.get(fluidRL);
                if (emcPerBucket == null) return -1;
                int amount = fluidStack.getAmount(); // mB
                totalEMC += (emcPerBucket * amount) / 1000L;
            }
        }
        return totalEMC;
    }

    /**
     * Обработка предметных выходов.
     */
    private int processItemOutputs(GTRecipe recipe, long inputEMC, 
                                    Map<ResourceLocation, Long> itemEMC) {
        int newMappings = 0;
        List<ItemStack> outputs = getValidOutputs(recipe);
        if (outputs.isEmpty()) return 0;
        
        long emcPerUnit = inputEMC / outputs.size();
        if (emcPerUnit <= 0) return 0;
        
        for (ItemStack output : outputs) {
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(output.getItem());
            if (rl == null || rl.getNamespace().equals("minecraft")) continue;
            
            if (!itemEMC.containsKey(rl)) {
                itemEMC.put(rl, emcPerUnit);
                newMappings++;
            }
        }
        return newMappings;
    }

    /**
     * Обработка жидкостных выходов.
     * EMC за 1000 mB = (inputEMC * 1000) / outputAmount
     */
    private int processFluidOutputs(GTRecipe recipe, long inputEMC,
                                     Map<ResourceLocation, Long> fluidEMC) {
        int newMappings = 0;
        
        for (Content output : recipe.getFluidOutputs()) {
            if (output.getContent() instanceof FluidStack fluidStack) {
                ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
                if (fluidRL == null) continue;
                
                int amount = fluidStack.getAmount(); // mB
                long emcPerBucket = (inputEMC * 1000L) / amount;
                
                if (emcPerBucket > 0 && !fluidEMC.containsKey(fluidRL)) {
                    fluidEMC.put(fluidRL, emcPerBucket);
                    newMappings++;
                }
            }
        }
        return newMappings;
    }

    /**
     * Получение валидных выходов (игнорирует chanced с низким шансом).
     */
    private List<ItemStack> getValidOutputs(GTRecipe recipe) {
        List<ItemStack> outputs = new ArrayList<>();
        
        // Основные выходы (100%)
        for (Content output : recipe.getOutputs()) {
            if (output.getContent() instanceof ItemStack stack) {
                outputs.add(stack.copy());
            }
        }
        
        // Chanced outputs
        int minChance = GTEMCConfig.MIN_CHANCE_THRESHOLD.get();
        if (minChance < 100) {
            for (Content chanced : recipe.getChancedOutputs()) {
                int chance = chanced.getChance(); // 0-10000
                if (chance >= minChance * 100 && chanced.getContent() instanceof ItemStack stack) {
                    outputs.add(stack.copy());
                }
            }
        }
        return outputs;
    }

    /**
     * Детектор циклических рецептов.
     */
    private boolean isCircularRecipe(GTRecipe recipe, Map<ResourceLocation, Long> itemEMC) {
        Set<ResourceLocation> inputs = new HashSet<>();
        Set<ResourceLocation> outputs = new HashSet<>();
        
        for (Content input : recipe.getInputs()) {
            if (input.getContent() instanceof ItemStack stack) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl != null) inputs.add(rl);
            }
        }
        for (Content output : recipe.getOutputs()) {
            if (output.getContent() instanceof ItemStack stack) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl != null) outputs.add(rl);
            }
        }
        return !outputs.isEmpty() && inputs.containsAll(outputs);
    }

    /**
     * Все типы рецептов GT для обхода.
     */
    private List<GTRecipeType> getAllRecipeTypes() {
        return List.of(
            GTRecipeTypes.FURNACE_RECIPES,
            GTRecipeTypes.ALLOY_SMELTER_RECIPES,
            GTRecipeTypes.ASSEMBLER_RECIPES,
            GTRecipeTypes.BENDER_RECIPES,
            GTRecipeTypes.WIREMILL_RECIPES,
            GTRecipeTypes.CHEMICAL_RECIPES,
            GTRecipeTypes.MIXER_RECIPES,
            GTRecipeTypes.BLAST_RECIPES,
            GTRecipeTypes.CENTRIFUGE_RECIPES,
            GTRecipeTypes.ELECTROLYZER_RECIPES,
            GTRecipeTypes.CHEMICAL_BATH_RECIPES,
            GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES,
            GTRecipeTypes.FLUID_HEATER_RECIPES,
            GTRecipeTypes.DISTILLATION_RECIPES,
            GTRecipeTypes.LASER_ENGRAVER_RECIPES,
            GTRecipeTypes.AUTOCLAVE_RECIPES,
            GTRecipeTypes.COMPRESSOR_RECIPES,
            GTRecipeTypes.EXTRACTOR_RECIPES,
            GTRecipeTypes.CUTTER_RECIPES,
            GTRecipeTypes.LATHE_RECIPES,
            GTRecipeTypes.MACERATOR_RECIPES,
            GTRecipeTypes.ARC_FURNACE_RECIPES,
            GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
            GTRecipeTypes.FORMING_PRESS_RECIPES,
            GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES
        );
    }
}`

const CALCULATOR_CODE = `package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Калькулятор EMC для GregTech CEu Modern.
 * Финализирует расчёт и применяет значения к ProjectE.
 * 
 * Ответственности:
 * 1. Применение рассчитанных EMC к мапперу ProjectE
 * 2. Разрешение конфликтов (минимальное EMC при нескольких рецептах)
 * 3. Финальная валидация значений
 */
public class EMCCalculator {

    private final Map<ResourceLocation, Long> itemEMC = new HashMap<>();
    private final Map<ResourceLocation, Long> fluidEMC = new HashMap<>();
    private final Map<ResourceLocation, List<Long>> itemEMCCandidates = new HashMap<>();
    private final Map<ResourceLocation, List<Long>> fluidEMCCandidates = new HashMap<>();

    /**
     * Добавление кандидата EMC для предмета.
     * Выбирается минимальное значение (защита от завышения).
     */
    public void addItemEMCCandidate(ResourceLocation itemRL, long emc) {
        if (emc <= 0) return;
        itemEMCCandidates.computeIfAbsent(itemRL, k -> new ArrayList<>()).add(emc);
        
        long currentBest = itemEMC.getOrDefault(itemRL, Long.MAX_VALUE);
        if (emc < currentBest) {
            itemEMC.put(itemRL, emc);
        }
    }

    /**
     * Добавление кандидата EMC для жидкости.
     */
    public void addFluidEMCCandidate(ResourceLocation fluidRL, long emcPerBucket) {
        if (emcPerBucket <= 0) return;
        fluidEMCCandidates.computeIfAbsent(fluidRL, k -> new ArrayList<>()).add(emcPerBucket);
        
        long currentBest = fluidEMC.getOrDefault(fluidRL, Long.MAX_VALUE);
        if (emcPerBucket < currentBest) {
            fluidEMC.put(fluidRL, emcPerBucket);
        }
    }

    /**
     * Расчёт EMC предмета на основе рецепта.
     * 
     * Поддерживает смешанные входы (предметы + жидкости).
     * Формула жидкости: EMC = (emcPerBucket * amount) / 1000
     */
    public long calculateItemEMCFromRecipe(List<Object> inputs, ItemStack output) {
        long totalInputEMC = 0;
        
        for (Object input : inputs) {
            if (input instanceof ItemStack itemInput) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(itemInput.getItem());
                if (rl == null) return -1;
                Long emc = itemEMC.get(rl);
                if (emc == null) return -1;
                totalInputEMC += emc * itemInput.getCount();
                
            } else if (input instanceof FluidStack fluidInput) {
                ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidInput.getFluid());
                if (fluidRL == null) return -1;
                Long emcPerBucket = fluidEMC.get(fluidRL);
                if (emcPerBucket == null) return -1;
                // Пропорционально количеству mB
                totalInputEMC += (emcPerBucket * fluidInput.getAmount()) / 1000L;
            }
        }
        return totalInputEMC / output.getCount();
    }

    /**
     * Расчёт EMC жидкости на основе рецепта.
     * Возвращает EMC за 1000 mB.
     */
    public long calculateFluidEMCFromRecipe(List<Object> inputs, FluidStack output) {
        long totalInputEMC = 0;
        
        for (Object input : inputs) {
            if (input instanceof ItemStack itemInput) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(itemInput.getItem());
                if (rl == null) return -1;
                Long emc = itemEMC.get(rl);
                if (emc == null) return -1;
                totalInputEMC += emc * itemInput.getCount();
                
            } else if (input instanceof FluidStack fluidInput) {
                ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluidInput.getFluid());
                if (fluidRL == null) return -1;
                Long emcPerBucket = fluidEMC.get(fluidRL);
                if (emcPerBucket == null) return -1;
                totalInputEMC += (emcPerBucket * fluidInput.getAmount()) / 1000L;
            }
        }
        // EMC за 1000 mB выходной жидкости
        return (totalInputEMC * 1000L) / output.getAmount();
    }

    /**
     * Применение всех значений к мапперу ProjectE.
     */
    public <T extends NormalizedSimpleStack<T, ?>> void applyToMapper(
            IMappingCollector<T, Long> mapper) {
        
        int appliedItems = 0, appliedFluids = 0, errors = 0;
        
        // Предметы
        for (Map.Entry<ResourceLocation, Long> entry : itemEMC.entrySet()) {
            try {
                NSSItem nssItem = NSSItem.createItem(entry.getKey());
                mapper.setValueBefore(nssItem, entry.getValue());
                appliedItems++;
            } catch (Exception e) {
                errors++;
            }
        }
        
        // Жидкости
        for (Map.Entry<ResourceLocation, Long> entry : fluidEMC.entrySet()) {
            try {
                NSSFluid nssFluid = NSSFluid.createFluid(entry.getKey());
                mapper.setValueBefore(nssFluid, entry.getValue());
                appliedFluids++;
            } catch (Exception e) {
                errors++;
            }
        }
        
        GTEMCAddon.LOGGER.info("[EMCCalculator] Применено: {} предметов, {} жидкостей, {} ошибок",
                appliedItems, appliedFluids, errors);
    }

    /**
     * Валидация значений.
     */
    public void validate() {
        long maxEMC = 10_000_000_000L;
        
        itemEMC.entrySet().removeIf(e -> e.getValue() <= 0);
        itemEMC.replaceAll((k, v) -> Math.min(v, maxEMC));
        
        fluidEMC.entrySet().removeIf(e -> e.getValue() <= 0);
        fluidEMC.replaceAll((k, v) -> Math.min(v, maxEMC));
    }

    public Map<ResourceLocation, Long> getItemEMC() {
        return Collections.unmodifiableMap(itemEMC);
    }

    public Map<ResourceLocation, Long> getFluidEMC() {
        return Collections.unmodifiableMap(fluidEMC);
    }

    public int getTotalCandidates() {
        int total = 0;
        for (List<Long> c : itemEMCCandidates.values()) total += c.size();
        for (List<Long> c : fluidEMCCandidates.values()) total += c.size();
        return total;
    }
}`

const INTEGRATION_CODE = `package com.gtemc.emc;

import com.gtemc.GTEMCAddon;
import com.gtemc.config.GTEMCConfig;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Интеграционный слой между GTEMCMapper и ProjectE API.
 * Управляет полным циклом расчёта EMC для GregTech CEu Modern.
 * 
 * Координирует:
 * - FluidEMCRegistry (базовые жидкости)
 * - RecipeWalker (обход рецептов)
 * - EMCCalculator (финальный расчёт и применение)
 */
public class GTEMCIntegration {

    private static GTEMCIntegration instance;
    private final EMCCalculator calculator;
    private final RecipeWalker recipeWalker;
    private boolean initialized = false;

    private GTEMCIntegration() {
        this.calculator = new EMCCalculator();
        this.recipeWalker = new RecipeWalker();
    }

    public static GTEMCIntegration getInstance() {
        if (instance == null) {
            instance = new GTEMCIntegration();
        }
        return instance;
    }

    /**
     * Основной метод интеграции с ProjectE.
     */
    public <T extends NormalizedSimpleStack<T, ?>> void processMappings(
            IMappingCollector<T, Long> mapper) {
        
        if (initialized) return;
        
        long startTime = System.currentTimeMillis();
        GTEMCAddon.LOGGER.info("[GTEMCIntegration] === Начало расчёта EMC ===");
        
        try {
            // Шаг 1: Базовые EMC
            initializeBaseEMC(mapper);
            
            // Шаг 2: Обход рецептов
            int newMappings = recipeWalker.walkAllRecipes(
                    mapper, calculator.getItemEMC(), calculator.getFluidEMC());
            
            // Шаг 3: Валидация
            calculator.validate();
            
            // Шаг 4: Применение
            calculator.applyToMapper(mapper);
            
            initialized = true;
        } catch (Exception e) {
            GTEMCAddon.LOGGER.error("[GTEMCIntegration] Критическая ошибка!", e);
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        GTEMCAddon.LOGGER.info("[GTEMCIntegration] === Завершено за {} мс ===", elapsed);
    }

    /**
     * Инициализация базовых EMC значений.
     */
    private <T extends NormalizedSimpleStack<T, ?>> void initializeBaseEMC(
            IMappingCollector<T, Long> mapper) {
        
        // Ванильные EMC
        Map<String, Long> baseItems = new LinkedHashMap<>();
        baseItems.put("minecraft:cobblestone", 1L);
        baseItems.put("minecraft:iron_ingot", 256L);
        baseItems.put("minecraft:gold_ingot", 2048L);
        baseItems.put("minecraft:diamond", 8192L);
        baseItems.put("minecraft:emerald", 8192L);
        baseItems.put("minecraft:coal", 64L);
        baseItems.put("minecraft:redstone", 32L);
        baseItems.put("minecraft:quartz", 128L);
        baseItems.put("minecraft:obsidian", 64L);
        baseItems.put("minecraft:blaze_rod", 768L);
        baseItems.put("minecraft:ender_pearl", 1024L);
        // ...
        
        for (Map.Entry<String, Long> entry : baseItems.entrySet()) {
            calculator.addItemEMCCandidate(new ResourceLocation(entry.getKey()), entry.getValue());
        }
        
        // Жидкости
        FluidEMCRegistry.initialize(mapper);
        for (Map.Entry<ResourceLocation, Long> entry : FluidEMCRegistry.getAllFluidEMC().entrySet()) {
            calculator.addFluidEMCCandidate(entry.getKey(), entry.getValue());
        }
        
        // GT материалы
        registerBaseGTMaterials();
    }

    /**
     * Регистрация базовых материалов GT.
     */
    private void registerBaseGTMaterials() {
        Map<String, Long> materials = new LinkedHashMap<>();
        materials.put("iron", 256L);
        materials.put("gold", 2048L);
        materials.put("copper", 128L);
        materials.put("tin", 64L);
        materials.put("steel", 512L);
        materials.put("titanium", 4096L);
        materials.put("tungsten", 2048L);
        materials.put("iridium", 8192L);
        materials.put("naquadah", 16384L);
        materials.put("neutronium", 262144L);
        // ... все материалы
        
        for (Map.Entry<String, Long> entry : materials.entrySet()) {
            registerAllForms(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Регистрация всех форм материала.
     */
    private void registerAllForms(String materialName, long ingotEMC) {
        String ns = "gtceu";
        
        // Множители форм относительно слитка
        Map<String, Double> forms = Map.ofEntries(
            Map.entry("ingot", 1.0),
            Map.entry("dust", 1.0),
            Map.entry("small_dust", 0.25),
            Map.entry("tiny_dust", 0.111),
            Map.entry("nugget", 0.111),
            Map.entry("block", 9.0),
            Map.entry("plate", 0.25),
            Map.entry("double_plate", 0.5),
            Map.entry("rod", 0.5),
            Map.entry("long_rod", 1.0),
            Map.entry("bolt", 0.125),
            Map.entry("screw", 0.125),
            Map.entry("ring", 0.25),
            Map.entry("gear", 4.0),
            Map.entry("small_gear", 2.0),
            Map.entry("wire", 0.25),
            Map.entry("fine_wire", 0.125),
            Map.entry("spring", 0.5),
            Map.entry("frame", 2.0),
            Map.entry("rotor", 4.25),
            Map.entry("ore", 2.0),
            Map.entry("gem", 1.0),
            Map.entry("lens", 0.75)
        );
        
        for (Map.Entry<String, Double> form : forms.entrySet()) {
            long emc = Math.max(1, (long)(ingotEMC * form.getValue()));
            ResourceLocation rl = new ResourceLocation(ns, materialName + "_" + form.getKey());
            calculator.addItemEMCCandidate(rl, emc);
        }
    }
}`

const CONFIG_CODE = `package com.gtemc.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Конфигурация мода GTEMCAddon.
 * Файл: config/gtcemcaddon-common.toml
 */
public class GTEMCConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_ITERATIONS;
    public static final ForgeConfigSpec.IntValue MIN_CHANCE_THRESHOLD;
    public static final ForgeConfigSpec.IntValue DEFAULT_FLUID_EMC_PER_BUCKET;
    public static final ForgeConfigSpec.BooleanValue CALCULATE_FLUIDS;
    public static final ForgeConfigSpec.BooleanValue CALCULATE_ITEMS;
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING;

    static {
        BUILDER.comment("GTCe Modern EMC Addon Configuration")
               .push("general");

        MAX_ITERATIONS = BUILDER
                .comment("Максимальное количество итераций калькулятора EMC.",
                         "Увеличьте для сложных цепочек (микросхемы, наноматериалы).",
                         "Рекомендуется: 15-25")
                .defineInRange("maxIterations", 20, 5, 50);

        MIN_CHANCE_THRESHOLD = BUILDER
                .comment("Минимальный шанс выхода (%) для учёта побочного продукта.",
                         "Рецепты с шансом ниже этого значения игнорируются.",
                         "100 = учитывать только 100% выходы (рекомендуется)")
                .defineInRange("minChanceThreshold", 100, 0, 100);

        DEFAULT_FLUID_EMC_PER_BUCKET = BUILDER
                .comment("Базовое EMC за 1000 mB жидкости, если не определено из рецептов.",
                         "0 = не назначать EMC жидкостям без рецептов")
                .defineInRange("defaultFluidEMCPerBucket", 0, 0, 1000000);

        CALCULATE_FLUIDS = BUILDER
                .comment("Включить автоматический расчёт EMC для жидкостей GregTech.")
                .define("calculateFluids", true);

        CALCULATE_ITEMS = BUILDER
                .comment("Включить автоматический расчёт EMC для предметов GregTech.")
                .define("calculateItems", true);

        DEBUG_LOGGING = BUILDER
                .comment("Включить подробное логирование процесса расчёта.",
                         "ВНИМАНИЕ: генерирует много вывода в лог!")
                .define("debugLogging", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}`

const BUILD_CODE = `plugins {
    id 'java'
    id 'eclipse'
    id 'idea'
    id 'net.minecraftforge.gradle' version '[6.0,6.2)'
}

version = '1.0.0'
group = 'com.gtemc'
archivesBaseName = 'gtcemodern-emc-addon'

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

minecraft {
    mappings channel: 'official', version: '1.20.1'
    
    runs {
        client {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'REGISTRIES'
            property 'forge.logging.console.level', 'debug'
        }
        server {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'REGISTRIES'
            property 'forge.logging.console.level', 'debug'
        }
    }
}

repositories {
    mavenCentral()
    maven {
        name = 'CurseMaven'
        url = 'https://www.cursemaven.com'
    }
    maven {
        name = 'JitPack'
        url = 'https://jitpack.io'
    }
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.2.0'

    // ProjectE for Minecraft 1.20.1
    // Найдите актуальный file ID на curseforge.com
    implementation fg.deobf('curse.maven:projecte-226410:4860000')

    // GregTech CEu Modern for 1.20.1
    implementation fg.deobf('curse.maven:gtceu-226410:5100000')
    
    // CodeChickenLib (dependency of GTCEu)
    implementation fg.deobf('curse.maven:codechickenlib-242818:4750000')
    
    // LDLib (dependency of GTCEu Modern)
    implementation fg.deobf('curse.maven:ldlib-626668:5000000')
}

jar {
    manifest {
        attributes([
            "Specification-Title"     : "gtcemodern-emc-addon",
            "Specification-Vendor"    : "GTEMCAddon",
            "Specification-Version"   : "1",
            "Implementation-Title"    : project.name,
            "Implementation-Version"  : project.version,
            "Implementation-Vendor"   : "GTEMCAddon",
        ])
    }
}`

export default App
