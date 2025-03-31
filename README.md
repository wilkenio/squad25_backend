# Financial Management API

This is the backend application for a financial management system that helps users track and manage their expenses effectively.

## Overview

The application provides a secure REST API that allows users to:
- Register and authenticate users
- Manage financial transactions
- Track expenses and financial history

## Technologies

### Core
- Java 17
- Spring Boot 3.4.4
- PostgreSQL Database

### Spring Dependencies 🌱
- **Spring Boot Starter Web**: For building the REST API
- **Spring Boot Starter Security**: For authentication and security
- **Spring Boot Starter Data JPA**: For database operations and ORM
- **Spring Boot Starter Validation**: For data validation
- **Spring Boot DevTools**: For development productivity
- **PostgreSQL Driver**: For database connectivity
- **Lombok**: For reducing boilerplate code
- **Java JWT (Auth0)**: For JWT token authentication
- **Spring Boot Starter Test**: For unit and integration testing

## Getting Started

### Prerequisites
- Java 17 or higher
- PostgreSQL installed and running
- Maven installed (or use the included Maven wrapper)

### Database Configuration
1. Create a PostgreSQL database named `financeiro`
2. Update the database configuration in `application.properties` if needed:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/financeiro
spring.datasource.username=your_username
spring.datasource.password=your_password
```
### Running the Application

1. Clone the repository
```bash
git clone https://github.com/your-username/squad25_backend.git
```
2. Navigate to the project directory
   ```bash
   cd squad25_backend\api
   ```
3. Build the project using Maven wrapper
   ```bash
    mvnw.cmd clean install
   ```
4. Run the application
   ```bash
    mvnw.cmd spring-boot:run
   ```
