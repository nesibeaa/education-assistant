# 🚀 AI Helper - Akıllı Eğitim Asistanı

<div align="center">

![AI Helper Logo](https://img.shields.io/badge/AI%20Helper-Smart%20Education%20Assistant-blue?style=for-the-badge&logo=robot)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-green?style=for-the-badge&logo=spring)
![Angular](https://img.shields.io/badge/Angular-18-red?style=for-the-badge&logo=angular)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)

</div>

## 📖 Proje Hakkında

AI Helper, yapay zeka destekli akıllı eğitim asistanıdır. Google Gemini AI ve web arama entegrasyonu ile öğrencilere kişiselleştirilmiş eğitim desteği sağlar.

## ✨ Özellikler

- 🤖 **Google Gemini AI Entegrasyonu** - Gelişmiş AI destekli eğitim
- 🔍 **Web Arama Entegrasyonu** - Güncel bilgilerle destek
- 💬 **Akıllı Sohbet Sistemi** - Doğal dil işleme
- 🔐 **JWT Tabanlı Güvenlik** - Güvenli kimlik doğrulama
- 📱 **Modern Web Arayüzü** - Angular 18 ile responsive tasarım
- 🐳 **Docker Desteği** - Kolay kurulum ve deployment
- 🗄️ **PostgreSQL Veritabanı** - Güvenilir veri saklama

## 🏗️ Teknoloji Stack

### Backend
- **Java 17+**
- **Spring Boot 3.3.2**
- **Spring Security + JWT**
- **Spring Data JPA**
- **PostgreSQL**

### Frontend
- **Angular 18**
- **TypeScript**
- **SCSS**
- **Responsive Design**

### DevOps & Tools
- **Docker & Docker Compose**
- **Maven**
- **Git**

## 🚀 Hızlı Başlangıç

### Gereksinimler
- Java 17+
- Maven 3.6+
- PostgreSQL 15+
- Node.js 18+ (Frontend için)
- Docker (Opsiyonel)

### Backend Kurulumu

1. **Repository'yi klonlayın**
```bash
git clone https://github.com/nesibeaa/smart-education-assistant.git
cd smart-education-assistant
```

2. **Veritabanını hazırlayın**
```bash
# PostgreSQL'de aihelper veritabanı oluşturun
createdb aihelper
```

3. **Konfigürasyon dosyasını oluşturun**
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

4. **API key'lerinizi ekleyin**
```properties
# Google Gemini API Key
gemini.api.key=YOUR_GEMINI_API_KEY_HERE

# Serper API Key (Web arama için)
serper.api.key=YOUR_SERPER_API_KEY_HERE

# JWT Secret (Güvenlik için değiştirin)
jwt.secret=YOUR_JWT_SECRET_HERE
```

5. **Uygulamayı çalıştırın**
```bash
mvn spring-boot:run
```

### Frontend Kurulumu

1. **Frontend klasörüne gidin**
```bash
cd aihelper-ui
```

2. **Bağımlılıkları yükleyin**
```bash
npm install
```

3. **Uygulamayı çalıştırın**
```bash
ng serve
```

4. **Tarayıcıda açın**
```
http://localhost:4200
```

### Docker ile Kurulum

```bash
# Tüm servisleri başlatın
docker-compose up -d

# Logları takip edin
docker-compose logs -f
```

## 🔧 Konfigürasyon

### Environment Variables

| Değişken | Açıklama | Varsayılan |
|-----------|----------|------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL bağlantı URL'i | `jdbc:postgresql://localhost:5432/aihelper` |
| `SPRING_DATASOURCE_USERNAME` | Veritabanı kullanıcı adı | `app` |
| `SPRING_DATASOURCE_PASSWORD` | Veritabanı şifresi | `apppass` |
| `GEMINI_API_KEY` | Google Gemini API Key | - |
| `SERPER_API_KEY` | Serper API Key | - |
| `JWT_SECRET` | JWT şifreleme anahtarı | - |

### Port Konfigürasyonu

- **Backend**: 8080
- **Frontend**: 4200
- **PostgreSQL**: 5432

## 📁 Proje Yapısı

```
smart-education-assistant/
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── api/          # REST API Controllers
│   │   │   ├── auth/         # Kimlik doğrulama
│   │   │   ├── chat/         # Sohbet servisleri
│   │   │   ├── config/       # Konfigürasyon
│   │   │   ├── domain/       # Domain modelleri
│   │   │   ├── repository/   # Veri erişim katmanı
│   │   │   └── search/       # Arama servisleri
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-docker.properties
├── aihelper-ui/              # Angular frontend
├── docker-compose.yml
├── pom.xml
└── README.md
```

## 🔐 Güvenlik

- JWT tabanlı kimlik doğrulama
- Spring Security entegrasyonu
- API key'ler environment variables ile
- CORS konfigürasyonu
- Input validation ve sanitization

## 🧪 Test

```bash
# Backend testleri
mvn test

# Frontend testleri
cd aihelper-ui
ng test
```

## 📊 API Dokümantasyonu

Uygulama çalıştıktan sonra Swagger UI'a erişin:
```
http://localhost:8080/swagger-ui.html
```

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📝 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 📞 İletişim

- **Proje Sahibi**: Nesibe Alataş
- **Email**: alatasn@mef.edu.tr
- **GitHub**: [@nesibeaa](https://github.com/nesibeaa)

## 🙏 Teşekkürler

- Google Gemini AI ekibine
- Spring Boot geliştiricilerine
- Angular ekibine
- Açık kaynak topluluğuna

---

<div align="center">

**⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın! ⭐**

</div>
