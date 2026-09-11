// Пример серверного скрипта KubeJS для использования дополнительных слотов апгрейдов
// Путь: kubejs/server_scripts/upgrade_slots_example.js

// Импортируем API мода
const UpgradeSlotsAPI = Java.loadClass('com.gtupgrades.api.UpgradeSlotsAPI');
const ExtraUpgradeSlotsHandler = Java.loadClass('com.gtupgrades.handler.ExtraUpgradeSlotsHandler');

// Пример 1: Получение информации о ускорителях в машине
events.listen('gtceu.recipe', event => {
    // Получаем машину, выполняющую рецепт
    const machine = event.machine;
    
    // Получаем количество ускорителей
    const acceleratorCount = UpgradeSlotsAPI.getAcceleratorCount(machine);
    
    if (acceleratorCount > 0) {
        console.log(`Машина ${machine.id} имеет ${acceleratorCount} ускорителей`);
        
        // Получаем модификаторы
        const speedModifier = UpgradeSlotsAPI.getSpeedModifier(machine);
        const energyModifier = UpgradeSlotsAPI.getEnergyModifier(machine);
        
        console.log(`Модификатор скорости: ${speedModifier}`);
        console.log(`Модификатор энергии: ${energyModifier}`);
    }
});

// Пример 2: Кастомная логика для конкретного мультиблока
events.listen('gtceu.recipe', event => {
    const machine = event.machine;
    
    // Проверяем тип машины
    if (machine.id.includes('large_chemical_reactor')) {
        const acceleratorCount = UpgradeSlotsAPI.getAcceleratorCount(machine);
        
        // Если есть хотя бы 2 ускорителя, добавляем бонус
        if (acceleratorCount >= 2) {
            // Увеличиваем выход продукта на 10% за каждый дополнительный ускоритель
            const bonus = 1.0 + (acceleratorCount - 1) * 0.1;
            
            // Модифицируем выходы рецепта
            event.recipe.outputs.forEach(output => {
                if (output.count) {
                    output.count = Math.ceil(output.count * bonus);
                }
            });
            
            console.log(`Large Chemical Reactor: бонус к выходу x${bonus}`);
        }
    }
});

// Пример 3: Ограничение использования ускорителей для определённых рецептов
events.listen('gtceu.recipe', event => {
    const machine = event.machine;
    const recipe = event.recipe;
    
    // Запрещаем ускорители для рецептов с большим потреблением энергии
    if (recipe.EUt > 10000) {
        const acceleratorCount = UpgradeSlotsAPI.getAcceleratorCount(machine);
        
        if (acceleratorCount > 0) {
            console.warn(`Рецепт с высоким EU/t (${recipe.EUt}) не может использовать ускорители!`);
            // Очищаем слоты ускорителей
            UpgradeSlotsAPI.clearAccelerators(machine);
            event.cancel(); // Отменяем рецепт
        }
    }
});

// Пример 4: Динамическое изменение параметров в зависимости от типа ускорителя
events.listen('gtceu.recipe', event => {
    const machine = event.machine;
    const handler = UpgradeSlotsAPI.getExtraUpgradeSlots(machine);
    
    if (handler) {
        // Проверяем каждый слот
        for (let i = 0; i < handler.slots; i++) {
            const stack = handler.getStackInSlot(i);
            
            if (!stack.isEmpty()) {
                // Проверяем NBT данные ускорителя
                const nbt = stack.nbt;
                
                if (nbt && nbt.contains('tier')) {
                    const tier = nbt.getInt('tier');
                    
                    // Разные уровни ускорителей дают разные бонусы
                    switch (tier) {
                        case 1:
                            // Базовый ускоритель: стандартные модификаторы
                            break;
                        case 2:
                            // Продвинутый ускоритель: улучшенные модификаторы
                            console.log(`Используется продвинутый ускоритель в слоте ${i}`);
                            break;
                        case 3:
                            // Элитный ускоритель: максимальные модификаторы
                            console.log(`Используется элитный ускоритель в слоте ${i}`);
                            break;
                    }
                }
            }
        }
    }
});

// Пример 5: Команды для управления ускорителями
events.listen('command.gtupgrades', event => {
    // Команда: /gtupgrades info
    if (event.args[0] === 'info') {
        const player = event.player;
        const machine = player.getMachineLookingAt(); // Псевдо-метод
        
        if (machine) {
            const acceleratorCount = UpgradeSlotsAPI.getAcceleratorCount(machine);
            player.tell(`Машина имеет ${acceleratorCount} ускорителей`);
        }
    }
    
    // Команда: /gtupgrades clear
    if (event.args[0] === 'clear') {
        const player = event.player;
        const machine = player.getMachineLookingAt();
        
        if (machine) {
            UpgradeSlotsAPI.clearAccelerators(machine);
            player.tell('Слоты ускорителей очищены');
        }
    }
});

// Пример 6: Интеграция с системой квестов FTB Quests
events.listen('ftbquests.completed', event => {
    const player = event.player;
    const quest = event.quest;
    
    // Если выполнен определённый квест, даём бонусные ускорители
    if (quest.id === 'advanced_manufacturing') {
        // Даём игроку 4 ускорителя
        const accelerator = Item.of('gtupgradeslots:accelerator_plate', 4);
        player.give(accelerator);
        
        player.tell('§aВы получили 4 платы-ускорителя за выполнение квеста!');
    }
});

// Пример 7: Кастомный RecipeModifier для мультиблоков
ServerEvents.recipes(event => {
    // Модифицируем все рецепты в Large Chemical Reactor
    event.modifyRecipe({type: 'gtceu:large_chemical_reactor'}, recipe => {
        // Добавляем шанс получения бонусного выхода при использовании ускорителей
        recipe.customData.putBoolean('supports_accelerators', true);
        recipe.customData.putInt('bonus_chance_per_accelerator', 5); // 5% за каждый ускоритель
    });
});

// Пример 8: Логирование использования ускорителей
events.listen('gtceu.recipe.start', event => {
    const machine = event.machine;
    const recipe = event.recipe;
    const acceleratorCount = UpgradeSlotsAPI.getAcceleratorCount(machine);
    
    if (acceleratorCount > 0) {
        const speedMod = UpgradeSlotsAPI.getSpeedModifier(machine);
        const energyMod = UpgradeSlotsAPI.getEnergyModifier(machine);
        
        console.log(`[GTUpgradeSlots] Рецепт ${recipe.id} запущен с ${acceleratorCount} ускорителями`);
        console.log(`  Скорость: x${(1/speedMod).toFixed(2)}, Энергия: x${energyMod.toFixed(2)}`);
    }
});

console.log('[GTUpgradeSlots] Серверный скрипт загружен');
