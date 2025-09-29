# Bank Backend API

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://docker.com)

Backend API для управления банковскими операциями, клиентами и депозитами. Spring Boot с полной контейнеризацией через Docker.

## 🚀 Как запустить проект? 🚀

### Требования:
- Docker & Docker Compose
- Git

### Запуск проекта (API):
```bash
# Клонировать репозиторий
git clone https://github.com/HXTSUKX6E/bank-backend
cd bank-backend

# Запустить приложение (из корня проекта)
docker-compose up --build
```
## 🎯 API Endpoints

### Клиенты

| Метод | Endpoint | Описание | Параметры |
|-------|----------|-----------|-----------|
| **GET** | `/api/clients` | Получить всех клиентов | `name`, `shortName`, `address`, `legalForm`, `sortBy`, `direction` |
| **GET** | `/api/clients/{id}` | Получить клиента по ID | - |
| **POST** | `/api/clients` | Создать нового клиента | `name*`, `shortName`, `address`, `legalForm*` |
| **PUT** | `/api/clients/{id}` | Обновить клиента | `name*`, `shortName`, `address`, `legalForm*` |
| **DELETE** | `/api/clients/{id}` | Удалить клиента | - |

### Банки

| Метод | Endpoint | Описание | Параметры |
|-------|----------|-----------|-----------|
| **GET** | `/api/banks` | Получить все банки | `name`, `bik`, `sortBy`, `direction` |
| **GET** | `/api/banks/{id}` | Получить банк по ID | - |
| **POST** | `/api/banks` | Создать новый банк | `name*`, `bik*` |
| **PUT** | `/api/banks/{id}` | Обновить банк | `name*`, `bik*` |
| **DELETE** | `/api/banks/{id}` | Удалить банк | - |

### Депозиты

| Метод | Endpoint | Описание | Параметры |
|-------|----------|-----------|-----------|
| **GET** | `/api/deposits` | Получить все депозиты | `clientId`, `bankId`, `openingDateFrom`, `openingDateTo`, `minPercentage`, `maxPercentage`, `minTerm`, `maxTerm`, `sortBy`, `direction` |
| **GET** | `/api/deposits/{id}` | Получить депозит по ID | - |
| **POST** | `/api/deposits` | Создать новый депозит | `clientId*`, `bankId*`, `percentage*`, `termMonths*` |
| **PUT** | `/api/deposits/{id}` | Обновить депозит | `clientId*`, `bankId*`, `openingDate*`, `percentage*`, `termMonths*` |
| **DELETE** | `/api/deposits/{id}` | Удалить депозит | - |
