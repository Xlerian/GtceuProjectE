@echo off
REM Скрипт для быстрого обновления файлов мода
REM Запустите этот скрипт из папки с обновлёнными файлами

echo ========================================
echo  Обновление файлов GTCe Modern EMC Addon
echo ========================================
echo.

REM Путь к вашему проекту (измените если нужно)
set PROJECT_PATH=C:\Users\Xlerian\Desktop\gt

echo Целевая папка: %PROJECT_PATH%
echo.

REM Проверка существования папки
if not exist "%PROJECT_PATH%" (
    echo ОШИБКА: Папка проекта не найдена!
    echo Измените PROJECT_PATH в этом скрипте
    pause
    exit /b 1
)

REM Копирование файлов
echo Копирование файлов...
echo.

REM Java файлы
xcopy /Y /E "src\main\java\com\gtemc\*.*" "%PROJECT_PATH%\src\main\java\com\gtemc\"
if errorlevel 1 (
    echo ОШИБКА при копировании Java файлов!
) else (
    echo [OK] Java файлы скопированы
)

REM Resources
xcopy /Y /E "src\main\resources\*.*" "%PROJECT_PATH%\src\main\resources\"
if errorlevel 1 (
    echo ОШИБКА при копировании ресурсов!
) else (
    echo [OK] Ресурсы скопированы
)

REM Build файлы
copy /Y "build.gradle" "%PROJECT_PATH%\"
copy /Y "gradle.properties" "%PROJECT_PATH%\"
copy /Y "settings.gradle" "%PROJECT_PATH%\"
if errorlevel 1 (
    echo ОШИБКА при копировании build файлов!
) else (
    echo [OK] Build файлы скопированы
)

echo.
echo ========================================
echo  Обновление завершено!
echo ========================================
echo.
echo Теперь выполните сборку:
echo   cd %PROJECT_PATH%
echo   gradlew.bat build
echo.
pause
