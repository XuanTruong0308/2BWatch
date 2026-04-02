# 2BShop - E-commerce Platform

Watch shop management system built with Spring Boot 3.

## 📋 Prerequisites

- Java 21+
- Maven 3.8+
- SQL Server 2019+
- Google OAuth2 Credentials (optional)
- VNPay Account (optional for payment)

## 🚀 Quick Start

## 🐳 Run with Docker (No SSMS Required)

### 1. Prepare environment file

Windows (PowerShell):
```powershell
Copy-Item .env.example .env
```

Linux/macOS:
```bash
cp .env.example .env
```

Then edit `.env` and set a strong `MSSQL_SA_PASSWORD`.

### 2. Build and run app + SQL Server

```bash
docker compose up -d --build
```

App URL: `http://localhost:8080`

Swagger URL:
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/swagger-ui/index.html`

### 3. Stop services

```bash
docker compose down
```

### 4. Stop and remove volumes (reset all data)

```bash
docker compose down -v
```

## 📦 Deploy to another machine without source code

Goal: build once, run anywhere with Docker only.

### Step A - Build image once on your machine

```bash
docker compose build app
```

### Step B - Export web image to a single file

```bash
docker save -o 2bshop-web-1.0.0.tar 2bshop-web:1.0.0
```

### Step C - On another machine

1) Copy these files to target machine:
- `2bshop-web-1.0.0.tar`
- `docker-compose.deploy.yml`
- `.env` (or `.env.example` then rename, can edit `APP_IMAGE` if needed)

2) Load image:

```bash
docker load -i 2bshop-web-1.0.0.tar
```

3) Start:

```bash
docker compose -f docker-compose.deploy.yml up -d
```

Note:
- SQL Server image (`mcr.microsoft.com/mssql/server:2022-latest`) will be pulled automatically if internet is available.
- If target machine is offline, export/import SQL Server image too:

```bash
docker pull mcr.microsoft.com/mssql/server:2022-latest
docker save -o mssql-2022.tar mcr.microsoft.com/mssql/server:2022-latest
docker load -i mssql-2022.tar
```

### 1. Clone the repository
```bash
git clone <repository-url>
cd 2BShop
```

### 2. Configure application.properties
```bash
# Copy example config
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Edit with your settings
# Update: database credentials, email config, OAuth2 keys
```

### 3. Setup Database
- Create database: `BShopDB`
- Run schema: `src/main/resources/db/schema.sql`
- (Optional) Run seed data: See `DataSeeder.java`

### 4. Run Application
```bash
mvn clean install
mvn spring-boot:run
```

Application will start at: http://localhost:8080

## 🔐 Security Configuration

### OAuth2 Login (Google)
1. Create OAuth2 credentials at Google Cloud Console
2. Add Client ID and Secret to `application.properties`
3. Set redirect URI: `http://localhost:8080/login/oauth2/code/google`

See: `knowledged/OAUTH2_SETUP_GUIDE.md` (if available)

### Email Configuration
- Use Gmail App Password (not regular password)
- Enable 2FA on Google account first

### VNPay Payment
- Register at VNPay Sandbox
- Get TMN Code and Hash Secret
- Update `application.properties`

## 📚 Features

### User Features
- Form Login / Google OAuth2 Login
- Email verification (form login)
- Phone verification (OAuth2 required before checkout)
- Product browsing, search, filter
- Shopping cart
- Checkout (COD, VNPay, Banking)
- Order tracking
- Profile management
- Invoice download (Word/PDF)

### Admin Features
- Dashboard
- Product management
- Order management
- User management
- Payment tracking
- Ban system

## 📁 Project Structure

```
2BShop/
├── src/main/
│   ├── java/boiz/shop/_2BShop/
│   │   ├── config/          # Security, OAuth2 config
│   │   ├── controller/      # REST & MVC controllers
│   │   ├── service/         # Business logic
│   │   ├── respository/     # Data access
│   │   ├── entity/          # JPA entities
│   │   └── dto/             # Data Transfer Objects
│   └── resources/
│       ├── templates/       # Thymeleaf views
│       ├── static/          # CSS, JS, images
│       ├── db/              # SQL scripts
│       └── application.properties
└── knowledged/              # Documentation (not in repo)
```

## 🛠️ Technologies

- **Backend**: Spring Boot 3.2.3
- **Security**: Spring Security + OAuth2 Client
- **Database**: SQL Server + JPA/Hibernate
- **Template**: Thymeleaf
- **Payment**: VNPay Gateway
- **Email**: JavaMailSender (Gmail)
- **Build**: Maven

## 📖 Documentation

See `/knowledged` folder for detailed guides:
- `USER_FEATURES.md` - User functionality documentation
- `ADMIN_BACKEND_GUIDE.md` - Admin features guide
- `OAUTH2_SETUP_GUIDE.md` - Google OAuth2 setup
- `PHONE_VERIFICATION_GUIDE.md` - Phone verification workflow
- `VNPAY_INTEGRATION_GUIDE.md` - Payment integration
- `TESTING_GUIDE.md` - Testing scenarios

## 🔧 Development

### Run in development mode
```bash
mvn spring-boot:run
```

### Build for production
```bash
mvn clean package
java -jar target/2BShop-0.0.1-SNAPSHOT.jar
```

### Enable debug logging
Edit `application.properties`:
```properties
logging.level.boiz.shop._2BShop=DEBUG
spring.jpa.show-sql=true
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📝 License

This project is for educational purposes.

## 👥 Authors

- **BoizTheDev** - Initial work

## 🙏 Acknowledgments

- Spring Boot Team
- Google OAuth2 Team
- VNPay Vietnam
