# Dilution Manager

Учебный проект для расчёта и управления сериями химических разбавлений. Поддерживает графический (JavaFX) и консольный интерфейсы, хранение данных в PostgreSQL и разграничение прав пользователей.

##  Возможности
-  Регистрация и авторизация пользователей
-  Создание серий разбавлений и добавление шагов
-  Автоматический расчёт концентраций
-  Контроль прав: редактирование только владельцем
-  Надёжное хранение в PostgreSQL
-  Два интерфейса: GUI (JavaFX) и CLI

## Требования
- Java 17+
- PostgreSQL 15+
- Maven 3.8+

## Запуск

### 1. Подготовка базы данных
Выполните в pgAdmin или psql:
```sql
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    login VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE dilution_source_type AS ENUM ('SAMPLE', 'SOLUTION');
CREATE TYPE final_quantity_unit AS ENUM ('ML', 'L', 'G', 'MG');

CREATE TABLE IF NOT EXISTS dilution_series (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    source_type dilution_source_type NOT NULL,
    source_id BIGINT NOT NULL,
    owner_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dilution_steps (
    id BIGINT PRIMARY KEY,
    series_id BIGINT REFERENCES dilution_series(id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL CHECK (step_number > 0),
    factor DOUBLE PRECISION NOT NULL CHECK (factor > 0),
    final_quantity DOUBLE PRECISION NOT NULL CHECK (final_quantity > 0),
    final_unit final_quantity_unit NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);




### 2. Настройка подключения
В файле `src/main/resources/database.properties` укажите свои данные:
```properties
db.url=jdbc:postgresql://localhost:5432/postgres
db.username=postgres
db.password=ваш_пароль
```

### 3. Сборка и запуск
```bash

mvn javafx:run          # Графическая версия
# или
mvn exec:java -Dexec.mainClass=ru.itmo.anya.mark.Main  # Консольная версия
```

##  Использование (CLI)
```bash
help                    # Список команд
register <логин> <пароль>
login <логин> <пароль>
dil_series_create "Название" SAMPLE 1
dil_step_add <id_серии> <шаг> <фактор> <объём> <единица>
dil_calc <id_серии> <начальная_концентрация>
dil_export <id_серии>
exit
```

##  Структура проекта
```
src/main/java/ru/itmo/anya/mark/
├── model/      # Модели данных
├── storage/    # Работа с БД
├── service/    # Бизнес-логика
├── ui/         # JavaFX интерфейс
└── command/    # CLI команды
```

##  Авторы
разрабыы: mazzyha, anutaf

