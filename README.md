# Telegram-бот для опросов

Это Telegram-бот, который позволяет пользователям участвовать в опросах.

## Содержание

*   [Введение](#введение)
*   [Необходимые условия](#необходимые-условия)
*   [Сборка приложения](#сборка-приложения)
*   [Запуск приложения](#запуск-приложения)
*   [Команды бота](#команды-бота)
*   [Конфигурация](#конфигурация)



## Введение

Этот бот использует Spring Boot, Kotlin и Gradle (Kotlin DSL) для предоставления платформы опросов в Telegram.

## Необходимые условия

Перед началом работы убедитесь, что у вас установлено следующее:

*   **Java Development Kit (JDK):** Версия 17 или выше
*   **Gradle:** Версия 7.x или выше
*   **Docker:** Docker Engine и Docker Compose.
*   **Аккаунт Telegram:** Вам понадобится аккаунт Telegram для создания и взаимодействия с ботом.
*   **PostgreSQL:** Установленная и запущенная локально или удаленно база данных PostgreSQL.

## Сборка приложения

Приложение использует Gradle (Kotlin DSL) для управления зависимостями и сборки.

1.  **Клонируйте репозиторий:**

    ```bash
    git clone [your_repository_url]
    cd theSurveyTgBot
    ```

2.  **Соберите приложение:**

    ```bash
    ./gradlew clean build
    ```

    или

    ```bash
    gradle clean build
    ```

    Эта команда выполнит следующие действия:

    *   `clean`: Очистит директорию `build`, удалив все скомпилированные классы и ресурсы от предыдущих сборок.
    *   `build`: Скомпилирует Kotlin код, обработает ресурсы и создаст JAR-файл в директории `build/libs`.

    Результирующий JAR-файл будет находиться в директории `build/libs`,jar файл пример `theSurveyTgBot-0.0.1-SNAPSHOT.jar` скопируйте в корневую папку проекта (там же, где находятся build.gradle.kts, settings.gradle.kts, src и другие файлы проекта)

## Запуск приложения

Есть два основных способа запуска приложения: с помощью Docker Compose или напрямую. Docker Compose - рекомендуемый подход для большинства развертываний.

### Используя Docker Compose (Только для локальной разработки)

**ОСТОРОЖНО! Этот метод подходит только для локальной разработки и ни в коем случае не должен использоваться в production! Пароли и токен бота хранятся в открытом виде в `application.properties`, что является серьезной угрозой безопасности.**

1.  **Убедитесь, что файл `application.properties` содержит необходимые настройки:**

    ```properties
    spring.application.name=theSurveyTgBot
    bot.token =ВАШ_ТОКЕН_TELEGRAM_БОТА
    bot.username = ИМЯ_ПОЛЬЗОВАТЕЛЯ_ВАШЕГО_БОТА

    spring.datasource.url=jdbc:postgresql://localhost:5432/user_db
    spring.datasource.username= postgres
    spring.datasource.password= root
    spring.datasource.driver-class-name=org.postgresql.Driver
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    ```

    Замените `ВАШ_ТОКЕН_TELEGRAM_БОТА` и `ИМЯ_ПОЛЬЗОВАТЕЛЯ_ВАШЕГО_БОТА` на ваши фактические значения.  Убедитесь, что база данных PostgreSQL запущена локально.

2.  **Запустите Docker Compose:**

    ```bash
    docker-compose up --build
    ```

    Эта команда построит Docker-образ и запустит приложение вместе с базой данных PostgreSQL.  Обратите внимание, что Docker Compose будет использовать настройки из вашего `application.properties`.

3.  **Доступ к приложению:** Бот должен быть запущен и доступен в Telegram.

### Запуск напрямую (для разработки)

1.  **Убедитесь, что файл `application.properties` содержит необходимые настройки:**

    ```properties
    spring.application.name=theSurveyTgBot
    bot.token =ВАШ_ТОКЕН_TELEGRAM_БОТА
    bot.username = ИМЯ_ПОЛЬЗОВАТЕЛЯ_ВАШЕГО_БОТА

    spring.datasource.url=jdbc:postgresql://localhost:5432/user_db
    spring.datasource.username= postgres
    spring.datasource.password= root
    spring.datasource.driver-class-name=org.postgresql.Driver
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    ```

    Замените `ВАШ_ТОКЕН_TELEGRAM_БОТА` и `ИМЯ_ПОЛЬЗОВАТЕЛЯ_ВАШЕГО_БОТА` на ваши фактические значения.  Убедитесь, что база данных PostgreSQL запущена локально.

2.  **Запустите приложение:**

    ```bash
    java -jar theSurveyTgBot-0.0.1-SNAPSHOT.jar
    ```

    Замените `theSurveyTgBot-0.0.1-SNAPSHOT.jar` на фактическое имя вашего JAR-файла.

## Команды бота

Следующие команды доступны в боте:

*   `/start` - Запускает бота и отображает приветственное сообщение.
*   `/form` - запуск последовательного опроса (имя → email → оценка 1-10)
*   `/report` - генерация Word-документа с результатами опросов и отправка пользователю




## Конфигурация

**ВНИМАНИЕ! Для локальной разработки настройки (токен бота, имя пользователя, URL, имя пользователя и пароль базы данных) хранятся непосредственно в файле `application.properties`. Это ОПАСНО для production!**


