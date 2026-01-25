# 🌙 Dream Marketplace

> Экспериментальная платформа для обмена, визуализации и монетизации снов

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black.svg)](https://kafka.apache.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 📋 О проекте

**Dream Marketplace** — это платформа, объединяющая технологии искусственного интеллекта и экономику для создания уникального пространства, где пользователи могут делиться своими снами, получать их AI-визуализации и монетизировать уникальный контент.

### ✨ Ключевые возможности

- 🎨 **AI-визуализация снов** — автоматическая генерация изображений на основе текстового описания через Midjourney
- ⚡ **Асинхронная обработка** — масштабируемая архитектура на базе Apache Kafka
- 🛍️ **Маркетплейс** — покупка и продажа уникальных снов с внутренним балансом
- 🔐 **OAuth 2.0** — безопасная аутентификация через Яндекс ID с JWT-токенами
- 📊 **Административная панель** — управление платформой с использованием хранимых процедур PostgreSQL
- 🔔 **Система уведомлений** — отслеживание активности и транзакций в реальном времени
- 💾 **S3-хранилище** — надежное хранение медиа-контента

## 🚀 Технологический стек

### Backend
- **Java 17+** — основной язык разработки
- **Spring Boot 3.x** — фреймворк для создания enterprise-приложений
- **Spring Security** — безопасность и OAuth 2.0
- **Spring Data JPA** — работа с базой данных

### Базы данных и хранилища
- **PostgreSQL 15+** — основная реляционная БД
- **Flyway** — миграции схемы базы данных
- **MinIO S3** — объектное хранилище для изображений

### Интеграции и очереди
- **Apache Kafka** — обработка асинхронных задач
- **Яндекс ID** — OAuth 2.0 провайдер
- **GenAPI (Midjourney)** — AI-генерация изображений
- **ЮKassa** — платежный шлюз (тестовый режим)

## 🏗️ Архитектура

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   Client    │─────▶│  Spring Boot │─────▶│ PostgreSQL  │
│  (Web/API)  │      │   Backend    │      │   Database  │
└─────────────┘      └──────┬───────┘      └─────────────┘
                            │
                            ├─────────────▶ MinIO S3
                            │
                            ├─────────────▶ Apache Kafka
                            │                    │
                            │                    ▼
                            │              ┌──────────┐
                            ├─────────────▶│  GenAPI  │
                            │              │Midjourney│
                            │              └──────────┘
                            │
                            ├─────────────▶ Яндекс ID
                            │
                            └─────────────▶ ЮKassa
```

## 📦 Установка и запуск

### Быстрый старт с Docker Compose

```bash
# Клонирование репозитория
git clone https://github.com/urasha/dream-marketplace.git
cd dream-marketplace

# Запуск всех сервисов
docker-compose up -d

# Приложение будет доступно по адресу http://localhost:8080
```

### Ручная установка

1. **Клонирование репозитория**
```bash
git clone https://github.com/urasha/dream-marketplace.git
cd dream-marketplace
```

2. **Настройка переменных окружения**
```bash
cp .env.example .env
# Отредактируйте .env файл с вашими настройками
```

3. **Настройка базы данных**
```bash
# Создайте базу данных PostgreSQL
createdb dream_marketplace

# Миграции будут применены автоматически при запуске приложения
```

4. **Запуск Kafka**
```bash
# Используйте Docker
docker run -d --name kafka -p 9092:9092 apache/kafka:latest
```

5. **Запуск MinIO**
```bash
docker run -d --name minio \
  -p 9000:9000 -p 9001:9001 \
  -e "MINIO_ROOT_USER=admin" \
  -e "MINIO_ROOT_PASSWORD=password" \
  minio/minio server /data --console-address ":9001"
```

6. **Сборка и запуск приложения**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

## ⚙️ Конфигурация

Основные параметры настраиваются в `application.yml` или через переменные окружения:

```yaml
# Database
spring.datasource.url: ${DB_URL:jdbc:postgresql://localhost:5432/dream_marketplace}
spring.datasource.username: ${DB_USERNAME:postgres}
spring.datasource.password: ${DB_PASSWORD:password}

# Kafka
spring.kafka.bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}

# MinIO S3
minio.endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
minio.access-key: ${MINIO_ACCESS_KEY:admin}
minio.secret-key: ${MINIO_SECRET_KEY:password}

# Yandex ID OAuth
oauth.yandex.client-id: ${YANDEX_CLIENT_ID}
oauth.yandex.client-secret: ${YANDEX_CLIENT_SECRET}

# GenAPI (Midjourney)
genapi.api-key: ${GENAPI_KEY}

# ЮKassa
yookassa.shop-id: ${YOOKASSA_SHOP_ID}
yookassa.secret-key: ${YOOKASSA_SECRET_KEY}
```

## 📚 Документация

- [Архитектура системы](docs/ARCHITECTURE.md)
- [API Reference](docs/API.md)
- [Руководство разработчика](docs/DEVELOPMENT.md)
- [Deployment Guide](docs/DEPLOYMENT.md)

## 🤝 Вклад в проект

Мы приветствуем вклад в развитие проекта! Пожалуйста, ознакомьтесь с [руководством по внесению вклада](CONTRIBUTING.md).

1. Форкните репозиторий
2. Создайте ветку для вашей функции (`git checkout -b feature/AmazingFeature`)
3. Закоммитьте изменения (`git commit -m 'Add some AmazingFeature'`)
4. Запушьте в ветку (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

## 📝 Roadmap

- [ ] Мобильное приложение (iOS/Android)
- [ ] Интеграция с дополнительными AI-моделями (DALL-E, Stable Diffusion)
- [ ] NFT-функционал для уникальных снов
- [ ] Социальные функции (комментарии, лайки, подписки)
- [ ] Расширенная аналитика снов
- [ ] Мультиязычная поддержка
- [ ] GraphQL API

## 📄 Лицензия

Этот проект распространяется под лицензией MIT. Подробности в файле [LICENSE](LICENSE).

## 👥 Авторы

- **urasha** - *Initial work* - [@urasha](https://github.com/urasha)

## 🙏 Благодарности

- Команда Spring за отличный фреймворк
- Yandex за OAuth 2.0 интеграцию
- Midjourney за AI-генерацию изображений
- Всем контрибьюторам проекта

## 📞 Контакты

- GitHub Issues: [dream-marketplace/issues](https://github.com/urasha/dream-marketplace/issues)
- Email: support@dream-marketplace.io
- Telegram: [@dream_marketplace](https://t.me/dream_marketplace)

---

⭐️ Если проект вам понравился, поставьте звездочку на GitHub!

**Made with ❤️ and ☕ in 2026**
