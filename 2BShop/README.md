# 2BShop - E-commerce Platform

Watch shop management system built with Spring Boot 3.

## 📋 Prerequisites

- Java 21+
- Maven 3.8+
- SQL Server 2019+
- Google OAuth2 Credentials (optional)
- VNPay Account (optional for payment)

## 🚀 Quick Start

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
