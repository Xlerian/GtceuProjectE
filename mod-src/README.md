# 🔨 GTCe Modern EMC Addon - Полная инструкция по компиляции

## 📋 Что вам понадобится

### Обязательные компоненты:

1. **Java Development Kit (JDK) 17**
   - Скачать: https://adoptium.net/temurin/releases/?version=17
   - Или: https://www.oracle.com/java/technologies/downloads/#java17
   - Проверка: `java -version` (должно показать 17.x.x)

2. **Git** (опционально)
   - https://git-scm.com/downloads

3. **IDE** (рекомендуется для разработки):
   - IntelliJ IDEA (рекомендуется) - https://www.jetbrains.com/idea/download/
   - Eclipse - https://www.eclipse.org/downloads/
   - VS Code - https://code.visualstudio.com/

---

## 🚀 Быстрая компиляция (5 минут)

### Шаг 1: Подготовка

```bash
# Создайте папку для мода
mkdir gtcemc-addon
cd gtcemc-addon

# Скопируйте ВСЕ файлы из папки mod-src/ в текущую директорию
```

### Шаг 2: Обновите зависимости

Откройте `build.gradle` и **обязательно** замените `<ВАШ_ID>` на актуальные CurseMaven ID:

```gradle
dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.2.0'

    // ProjectE - найдите ID на https://www.curseforge.com/minecraft/mc-mods/projecte
    implementation fg.deobf('curse.maven:projecte-226410:4860000')  // ← замените 4860000

    // GregTech CEu Modern - найдите ID на CurseForge
    implementation fg.deobf('curse.maven:gtceu-226410:5100000')    // ← замените 5100000
    
    // CodeChickenLib
    implementation fg.deobf('curse.maven:codechickenlib-242818:4750000')
    
    // LDLib
    implementation fg.deobf('curse.maven:ldlib-626668:5000000')
}
```

**Как найти актуальный FILE_ID:**
1. Перейдите на CurseForge страницу мода
2. Откройте вкладку "Files"
3. Выберите версию для Minecraft 1.20.1
4. В URL будет ID: `curseforge.com/.../files/`**`4860000`**

### Шаг 3: Сборка

```bash
# Windows:
gradlew.bat build

# Linux/Mac:
./gradlew build
```

**Первая сборка займёт 5-15 минут** (скачивание зависимостей).

### Шаг 4: Готово!

JAR файл будет в:
```
build/libs/gtcemodern-emc-addon-1.0.0.jar
```

---

## 📖 Подробная инструкция

### 1. Установка JDK 17

**Windows:**
1. Скачайте JDK 17 с https://adoptium.net/
2. Установите (например, в `C:\Program Files\Java\jdk-17`)
3. Установите переменную среды:
   ```cmd
   setx JAVA_HOME "C:\Program Files\Java\jdk-17"
   setx PATH "%JAVA_HOME%\bin;%PATH%"
   ```
4. Перезапустите терминал
5. Проверьте: `java -version`

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

**Mac (Homebrew):**
```bash
brew install openjdk@17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### 2. Структура проекта

Убедитесь, что структура такая:

```
gtcemc-addon/
├── build.gradle                    ✅
├── gradle.properties               ✅
├── settings.gradle                 ✅
├── src/main/
│   ├── java/com/gtemc/
│   │   ├── GTEMCAddon.java        ✅
│   │   ├── config/
│   │   │   └── GTEMCConfig.java   ✅
│   │   └── emc/
│   │       ├── GTEMCMapper.java   ✅
│   │       ├── GTEMCIntegration.java ✅
│   │       ├── FluidEMCRegistry.java ✅
│   │       ├── RecipeWalker.java  ✅
│   │       └── EMCCalculator.java ✅
│   └── resources/
│       ├── META-INF/mods.toml     ✅
│       └── pack.mcmeta            ✅
```

### 3. Gradle Wrapper

Если у вас нет `gradlew` и `gradlew.bat`, создайте файл `gradle/wrapper/gradle-wrapper.properties`:

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.1.1-bin.zip
networkTimeout=10000
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

Или скачайте готовый Gradle Wrapper из Forge MDK:
https://github.com/MinecraftForge/MinecraftForge

### 4. Сборка

```bash
# Очистка (если нужно)
./gradlew clean

# Сборка
./gradlew build

# Сборка с подробным выводом (для отладки)
./gradlew build --info

# Сборка без тестов (быстрее)
./gradlew build -x test
```

**Ожидаемый вывод:**
```
> Task :compileJava
> Task :processResources
> Task :classes
> Task :jar
> Task :reobfJar
> Task :build

BUILD SUCCESSFUL in 5m 23s
```

### 5. Проверка JAR

```bash
# Список файлов
ls build/libs/

# Должен быть:
# gtcemodern-emc-addon-1.0.0.jar

# Проверка содержимого (Linux/Mac)
jar tf build/libs/gtcemodern-emc-addon-1.0.0.jar

# Должны быть:
# - META-INF/mods.toml
# - com/gtemc/*.class
# - pack.mcmeta
```

---

## 🎮 Установка в Minecraft

### 1. Установите Minecraft с Forge

1. Установите Minecraft 1.20.1
2. Установите Forge 47.2.0+: https://files.minecraftforge.net/
3. Запустите Minecraft с Forge хотя бы один раз

### 2. Установите зависимости

Скачайте и поместите в папку `mods/`:

- **ProjectE** (для 1.20.1)
  - https://www.curseforge.com/minecraft/mc-mods/projecte
  
- **GregTech CEu Modern** (для 1.20.1)
  - https://www.curseforge.com/minecraft/mc-mods/gregtechceu-modern
  
- **CodeChickenLib**
  - https://www.curseforge.com/minecraft/mc-mods/codechicken-lib
  
- **LDLib**
  - https://www.curseforge.com/minecraft/mc-mods/ldlib

### 3. Установите ваш мод

Скопируйте `build/libs/gtcemodern-emc-addon-1.0.0.jar` в папку `mods/`:

**Windows:**
```
%APPDATA%\.minecraft\mods\
```

**Linux:**
```
~/.minecraft/mods/
```

**Mac:**
```
~/Library/Application Support/minecraft/mods/
```

### 4. Запустите Minecraft

Запустите Minecraft с Forge профилем и проверьте:
- Мод загружается без ошибок
- В логах есть: `[GTEMCAddon] Мод инициализирован`
- В логах есть: `[GTEMCAddon] Расчёт EMC завершён`

---

## 🐛 Решение проблем

### Ошибка: "Could not resolve all files for configuration"

**Причина:** Неверные CurseMaven ID

**Решение:**
1. Проверьте актуальные ID файлов на CurseForge
2. Убедитесь, что версии совместимы с 1.20.1
3. Проверьте, что CurseMaven репозиторий добавлен:
   ```gradle
   repositories {
       maven {
           name = 'CurseMaven'
           url = 'https://www.cursemaven.com'
       }
   }
   ```

### Ошибка: "Unsupported class file major version 61"

**Причина:** Используется Java версия ниже 17

**Решение:**
```bash
java -version
# Должно показать: 17.x.x

# Если нет, установите JDK 17 и проверьте JAVA_HOME
echo $JAVA_HOME  # Linux/Mac
echo %JAVA_HOME% # Windows
```

### Ошибка: "package com.gregtechceu.gtceu does not exist"

**Причина:** GTCEu не загружен корректно

**Решение:**
1. Проверьте CurseMaven ID для GTCEu
2. Убедитесь, что используете версию для 1.20.1
3. Очистите кэш: `./gradlew clean`
4. Обновите зависимости: `./gradlew --refresh-dependencies`

### Ошибка: "Could not find net.minecraftforge:forge:1.20.1-47.2.0"

**Причина:** Проблема с репозиториями Forge

**Решение:**
Добавьте в `build.gradle`:
```gradle
repositories {
    mavenCentral()
    maven {
        name = 'MinecraftForge'
        url = 'https://maven.minecraftforge.net/'
    }
    maven {
        name = 'CurseMaven'
        url = 'https://www.cursemaven.com'
    }
}
```

### Ошибка: "gradlew: Permission denied" (Linux/Mac)

**Решение:**
```bash
chmod +x gradlew
```

### Ошибка: "Could not determine java version"

**Причина:** Gradle не может определить версию Java

**Решение:**
1. Обновите Gradle Wrapper до версии 8.1.1+
2. Убедитесь, что JAVA_HOME установлен корректно
3. Проверьте, что в PATH указан путь к JDK 17

### Ошибка: "Execution failed for task ':compileJava'"

**Причина:** Ошибки компиляции Java кода

**Решение:**
1. Проверьте логи: `build/reports/`
2. Убедитесь, что все импорты корректны
3. Проверьте совместимость API ProjectE и GTCEu

---

## 🛠️ Полезные команды

```bash
# Очистка предыдущей сборки
./gradlew clean

# Сборка без тестов (быстрее)
./gradlew build -x test

# Сборка с подробным выводом
./gradlew build --info

# Запуск клиента для тестирования
./gradlew runClient

# Запуск сервера для тестирования
./gradlew runServer

# Генерация файлов для IntelliJ IDEA
./gradlew idea

# Генерация файлов для Eclipse
./gradlew eclipse

# Обновление зависимостей
./gradlew --refresh-dependencies

# Просмотр всех задач
./gradlew tasks
```

---

## 📚 Полезные ссылки

- **ProjectE:** https://www.curseforge.com/minecraft/mc-mods/projecte
- **GregTech CEu Modern:** https://www.curseforge.com/minecraft/mc-mods/gregtechceu-modern
- **Forge MDK:** https://github.com/MinecraftForge/MinecraftForge
- **ProjectE API:** https://github.com/sinkillerj/ProjectE
- **GTCEu Modern:** https://github.com/GregTechCEu/GregTech-Modern

---

## ✅ Чек-лист перед запуском

- [ ] JDK 17 установлен (`java -version` показывает 17)
- [ ] CurseMaven ID обновлены в build.gradle
- [ ] Команда `./gradlew build` выполняется без ошибок
- [ ] JAR файл создан в `build/libs/`
- [ ] ProjectE установлен в папке mods
- [ ] GregTech CEu Modern установлен в папке mods
- [ ] CodeChickenLib установлен в папке mods
- [ ] LDLib установлен в папке mods
- [ ] Minecraft 1.20.1 с Forge 47.2.0+ установлен

---

## 🎯 Итоговая структура

После сборки у вас должно быть:

```
gtcemc-addon/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew / gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── src/main/...
└── build/libs/
    └── gtcemodern-emc-addon-1.0.0.jar  ← Ваш мод!
```

---

**Удачи с разработкой! 🚀**

Если возникли проблемы, проверьте логи в `build/reports/` и `.minecraft/logs/latest.log`.
