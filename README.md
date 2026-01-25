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

```bash
# Клонирование репозитория
git clone https://github.com/urasha/dream-marketplace.git
cd dream-marketplace

# Запуск всех сервисов
docker-compose up -d

# Отдельно также запустить backend (./gradlew bootRun) и frontend (npm run dev)
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

---

⭐️ Если проект вам понравился, поставьте звездочку на GitHub!

**Made with ❤️ and ☕ in 2026**
