# 🚀 Инструкция по компиляции GTCe Modern EMC Addon

## 📋 Требования

Перед началом убедитесь, что у вас установлены:

1. **Java Development Kit (JDK) 17** - обязательно!
   - Скачать: https://adoptium.net/temurin/releases/?version=17
   - Или: https://www.oracle.com/java/technologies/downloads/#java17
   
2. **Git** (опционально, для клонирования репозитория)
   - https://git-scm.com/downloads

3. **IDE** (рекомендуется):
   - IntelliJ IDEA (рекомендуется) - https://www.jetbrains.com/idea/download/
   - Eclipse - https://www.eclipse.org/downloads/
   - VS Code - https://code.visualstudio.com/

---

## 🔧 Пошаговая инструкция

### Шаг 1: Подготовка проекта

```bash
# Создайте папку для мода
mkdir gtcemc-addon
cd gtcemc-addon

# Скопируйте все файлы из папки mod-src/ в текущую директорию
# Структура должна быть:
# gtcemc-addon/
# ├── build.gradle
# ├── gradle.properties
# └── src/
#     └── main/
#         ├── java/com/gtemc/...
#         └── resources/...
```

### Шаг 2: Установка Gradle Wrapper

Создайте файл `gradle/wrapper/gradle-wrapper.properties`:

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.1.1-bin.zip
networkTimeout=10000
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

Или скачайте готовый Gradle Wrapper:

```bash
# Если у вас установлен Gradle глобально:
gradle wrapper --gradle-version 8.1.1

# Или скачайте gradlew скрипты вручную:
# Windows: gradlew.bat
# Linux/Mac: gradlew (chmod +x gradlew)
```

### Шаг 3: Обновление зависимостей

Откройте `build.gradle` и **обязательно** обновите CurseMaven ID для актуальных версий:

```gradle
dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.2.0'

    // ProjectE - найдите актуальный ID на https://www.curseforge.com/minecraft/mc-mods/projecte
    // Формат: curse.maven:projecte-226410:<FILE_ID>
    implementation fg.deobf('curse.maven:projecte-226410:4860000')

    // GregTech CEu Modern - найдите актуальный ID
    implementation fg.deobf('curse.maven:gtceu-226410:5100000')
    
    // CodeChickenLib
    implementation fg.deobf('curse.maven:codechickenlib-242818:4750000')
    
    // LDLib
    implementation fg.deobf('curse.maven:ldlib-626668:5000000')
}
```

**Как найти актуальные ID файлов:**

1. Перейдите на CurseForge страницу мода
2. Откройте вкладку "Files"
3. Выберите нужную версию для 1.20.1
4. В URL будет ID файла, например: `https://www.curseforge.com/minecraft/mc-mods/projecte/files/4860000`
   - Здесь `4860000` - это FILE_ID

### Шаг 4: Проверка Java версии

```bash
java -version
# Должно показать: java version "17.x.x" или openjdk version "17.x.x"

javac -version
# Должно показать: javac 17.x.x
```

Если версия не 17, установите JAVA_HOME:

**Windows:**
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
```

**Linux/Mac:**
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

### Шаг 5: Сборка мода

```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

Первая сборка займёт 5-15 минут (скачивание зависимостей).

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

### Шаг 6: Поиск JAR файла

После успешной сборки JAR файл будет в:

```
build/libs/gtcemodern-emc-addon-1.0.0.jar
```

---

## 🎮 Установка мода в Minecraft

1. Установите Minecraft 1.20.1 с Forge 47.2.0+
2. Скопируйте JAR файл в папку `mods/`:
   ```
   .minecraft/mods/gtcemodern-emc-addon-1.0.0.jar
   ```
3. Убедитесь, что также установлены:
   - ProjectE (для 1.20.1)
   - GregTech CEu Modern (для 1.20.1)
   - CodeChickenLib
   - LDLib
4. Запустите Minecraft

---

## 🐛 Решение проблем

### Ошибка: "Could not resolve all files for configuration"

**Причина:** Неверные CurseMaven ID

**Решение:**
- Проверьте актуальные ID файлов на CurseForge
- Убедитесь, что версии совместимы с 1.20.1

### Ошибка: "Unsupported class file major version 61"

**Причина:** Используется Java версия ниже 17

**Решение:**
- Установите JDK 17
- Проверьте `java -version`
- Установите JAVA_HOME

### Ошибка: "Could not find net.minecraftforge:forge:1.20.1-47.2.0"

**Причина:** Проблема с репозиториями Forge

**Решение:**
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

### Ошибка компиляции: "package com.gregtechceu.gtceu does not exist"

**Причина:** GTCEu не загружен корректно

**Решение:**
- Проверьте CurseMaven ID для GTCEu
- Убедитесь, что используете правильную версию для 1.20.1
- Попробуйте очистить кэш: `gradlew clean`

### Ошибка: "Could not determine java version"

**Причина:** Gradle не может определить версию Java

**Решение:**
- Обновите Gradle Wrapper до версии 8.1.1+
- Убедитесь, что JAVA_HOME установлен корректно

---

## 📦 Альтернативные способы сборки

### Способ 1: Через IntelliJ IDEA

1. Откройте IntelliJ IDEA
2. File → Open → выберите папку проекта
3. Дождитесь импорта Gradle проекта
4. В панели Gradle (справа) → Tasks → build → build
5. Или используйте терминал: `gradlew build`

### Способ 2: Через Eclipse

1. Откройте Eclipse
2. File → Import → Gradle → Existing Gradle Project
3. Выберите папку проекта
4. Дождитесь импорта
5. Правый клик на проекте → Run As → Gradle Build
6. Введите задачу: `build`

### Способ 3: Через командную строку (без Gradle Wrapper)

Если у вас установлен Gradle глобально:

```bash
# Установите Gradle 8.1.1
# https://gradle.org/install/

# Запустите сборку
gradle build
```

---

## 🔍 Проверка сборки

После сборки проверьте:

```bash
# Список файлов в build/libs/
ls build/libs/

# Должен быть файл:
# gtcemodern-emc-addon-1.0.0.jar
```

Проверьте содержимое JAR:

```bash
# Windows (через 7-Zip или WinRAR)
# Откройте JAR как архив

# Linux/Mac
jar tf build/libs/gtcemodern-emc-addon-1.0.0.jar

# Должны быть:
# - META-INF/mods.toml
# - com/gtemc/*.class
# - pack.mcmeta
```

---

## 📝 Полезные команды

```bash
# Очистка предыдущей сборки
gradlew clean

# Сборка без тестов (быстрее)
gradlew build -x test

# Сборка с подробным выводом
gradlew build --info

# Сборка и запуск клиента для тестирования
gradlew runClient

# Сборка и запуск сервера для тестирования
gradlew runServer

# Генерация файлов для IDE
gradlew eclipse    # Для Eclipse
gradlew idea       # Для IntelliJ IDEA
```

---

## 🎯 Быстрый старт (TL;DR)

```bash
# 1. Установите JDK 17
# 2. Скопируйте файлы мода в папку
# 3. Обновите CurseMaven ID в build.gradle
# 4. Запустите:
gradlew build
# 5. Заберите JAR из build/libs/
# 6. Положите в .minecraft/mods/
```

---

## 📞 Поддержка

Если возникли проблемы:

1. Проверьте логи сборки в `build/reports/`
2. Проверьте логи Minecraft в `.minecraft/logs/latest.log`
3. Убедитесь, что все зависимости установлены
4. Проверьте совместимость версий модов

**Полезные ссылки:**
- ProjectE: https://www.curseforge.com/minecraft/mc-mods/projecte
- GregTech CEu Modern: https://www.curseforge.com/minecraft/mc-mods/gregtechceu-modern
- Forge MDK: https://github.com/MinecraftForge/MinecraftForge

---

## ✅ Чек-лист перед запуском

- [ ] JDK 17 установлен (`java -version` показывает 17)
- [ ] CurseMaven ID обновлены в build.gradle
- [ ] Команда `gradlew build` выполняется без ошибок
- [ ] JAR файл создан в `build/libs/`
- [ ] ProjectE установлен в папке mods
- [ ] GregTech CEu Modern установлен в папке mods
- [ ] CodeChickenLib установлен в папке mods
- [ ] LDLib установлен в папке mods
- [ ] Minecraft 1.20.1 с Forge 47.2.0+ установлен

---

**Удачи с разработкой! 🚀**
