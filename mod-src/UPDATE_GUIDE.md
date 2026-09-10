# 🔄 Быстрое обновление файлов мода

## Способ 1: Автоматический (рекомендуется)

### Шаг 1: Запустите скрипт обновления

```cmd
cd C:\Users\Xlerian\Desktop\gt\mod-src
update_files.bat
```

Скрипт автоматически скопирует все обновлённые файлы в ваш проект.

### Шаг 2: Соберите мод

```cmd
cd C:\Users\Xlerian\Desktop\gt
gradlew.bat build
```

---

## Способ 2: Ручное копирование

### Быстрая команда для копирования всех файлов:

```cmd
REM Из папки mod-src выполните:
xcopy /Y /E "src\main\java\com\gtemc\*" "C:\Users\Xlerian\Desktop\gt\src\main\java\com\gtemc\"
xcopy /Y /E "src\main\resources\*" "C:\Users\Xlerian\Desktop\gt\src\main\resources\"
copy /Y "build.gradle" "C:\Users\Xlerian\Desktop\gt\"
copy /Y "gradle.properties" "C:\Users\Xlerian\Desktop\gt\"
copy /Y "settings.gradle" "C:\Users\Xlerian\Desktop\gt\"
```

---

## Способ 3: Через проводник Windows

1. Откройте папку `mod-src\src\main\java\com\gtemc\`
2. Выделите все файлы (Ctrl+A)
3. Скопируйте (Ctrl+C)
4. Откройте `C:\Users\Xlerian\Desktop\gt\src\main\java\com\gtemc\`
5. Вставьте с заменой (Ctrl+V → "Заменить файлы")

---

## Способ 4: PowerShell (одна команда)

```powershell
# Из папки mod-src:
robocopy src\main C:\Users\Xlerian\Desktop\gt\src\main /E /IS /IT
```

---

## ⚡ Быстрая проверка после обновления

```cmd
cd C:\Users\Xlerian\Desktop\gt
gradlew.bat clean
gradlew.bat build
```

---

## 📋 Что было исправлено

### Ошибка 1: `var` в параметрах методов
**Было:**
```java
private static long calculateInputEMC(var recipe) {
```

**Стало:**
```java
private static long calculateInputEMC(GTRecipe recipe) {
```

### Ошибка 2: `NormalizedSimpleStack<T, ?>`
**Было:**
```java
public static <T extends NormalizedSimpleStack<T, ?>> void initialize(
    IMappingCollector<T, Long> mapper) {
```

**Стало:**
```java
public static void initialize(
    IMappingCollector<NormalizedSimpleStack, Long> mapper) {
```

---

## 🎯 Итоговая команда для полной пересборки

```cmd
cd C:\Users\Xlerian\Desktop\gt\mod-src
update_files.bat
cd ..
gradlew.bat clean build
```

Готово! JAR файл будет в `build\libs\`
