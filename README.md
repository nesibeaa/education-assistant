# 🤖 AI Helper - Akıllı Eğitim Asistanı

Modern teknolojiler kullanılarak geliştirilmiş, yapay zeka destekli eğitim asistanı uygulaması.

## 🚀 Özellikler

- **AI Destekli Sohbet:** Gemini AI entegrasyonu ile akıllı yanıtlar
- **Web Arama Entegrasyonu:** Google arama sonuçları ile zenginleştirilmiş yanıtlar
- **Çok Dilli Destek:** Türkçe ve İngilizce dil desteği
- **Kişiselleştirilmiş Öğrenme:** Kullanıcı seviyesine uygun yanıtlar
- **Sohbet Geçmişi:** Tüm konuşmaların kayıt altına alınması
- **Güvenli Kimlik Doğrulama:** JWT tabanlı güvenlik sistemi

## 🏗️ Mimari

### Frontend (Angular 18)
- Modern Angular framework
- Material Design bileşenleri
- Responsive tasarım
- Lazy loading ile performans optimizasyonu

### Backend (Spring Boot 3.3.2)
- Java 21
- Spring Security
- JPA/Hibernate
- PostgreSQL veritabanı
- Redis cache

### AI Entegrasyonu
- Google Gemini 1.5 Flash
- Yapılandırılmış JSON yanıtlar
- Konu bazlı kategorizasyon
- Zorluk seviyesi belirleme

## 🛠️ Teknoloji Stack

- **Frontend:** Angular 18, TypeScript, SCSS, Angular Material
- **Backend:** Spring Boot 3.3.2, Java 21, Maven
- **Veritabanı:** PostgreSQL 16, Redis
- **AI:** Google Gemini API
- **Arama:** Serper.dev (Google Search API)
- **Container:** Docker, Docker Compose

## 📋 Gereksinimler

- Java 21+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 16
- Redis 7

## 🚀 Kurulum

### 1. Repository'yi Klonlayın
```bash
git clone https://github.com/nesibeaa/ai-helper.git
cd ai-helper
```

### 2. Backend Kurulumu
```bash
cd bim25
./mvnw clean install
```

### 3. Frontend Kurulumu
```bash
cd aihelper-ui
npm install
```

### 4. Docker ile Veritabanı Başlatma
```bash
cd bim25
docker-compose up -d
```

### 5. Uygulamayı Başlatma

**Backend:**
```bash
cd bim25
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd aihelper-ui
npm start
```

## 🔧 Konfigürasyon

### Environment Variables

Backend için `bim25/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/aihelper
spring.datasource.username=app
spring.datasource.password=apppass

# AI Provider
ai.provider=gemini
gemini.api.key=YOUR_GEMINI_API_KEY
gemini.model=gemini-1.5-flash

# Search
serper.api.key=YOUR_SERPER_API_KEY
search.topN=3
```

### Docker Compose

Veritabanı portları:
- PostgreSQL: 5432
- PgAdmin: 5051
- Redis: 6379

## 📱 Kullanım

1. **Kayıt Olun:** Yeni hesap oluşturun
2. **Giriş Yapın:** Mevcut hesabınızla giriş yapın
3. **Sohbet Başlatın:** AI ile sohbet etmeye başlayın
4. **Geçmişi Görüntüleyin:** Önceki sohbetlerinizi inceleyin

## 🔒 Güvenlik

- JWT tabanlı kimlik doğrulama
- Şifre hashleme (BCrypt)
- Role-based access control
- CORS konfigürasyonu

## 📊 Veri Modeli

### User
- ID, Email, Password Hash, Role, Created At

### Conversation
- ID, User ID, Title, Created At, Updated At

### ChatMessage
- ID, User ID, Request, Reply, Status, Topic, Difficulty, Next Step, Confidence, Language, Resources JSON

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📝 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 👨‍💻 Geliştirici

**Nesibe Alataş** - [@nesibeaa](https://github.com/nesibeaa)

## 🙏 Teşekkürler

- Google Gemini AI
- Serper.dev
- Spring Boot Team
- Angular Team
- PostgreSQL Community

## 📞 İletişim

- GitHub: [@nesibeaa](https://github.com/nesibeaa)
- Proje Linki: [https://github.com/nesibeaa/ai-helper](https://github.com/nesibeaa/ai-helper)

---

⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!
