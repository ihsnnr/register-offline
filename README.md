# Register Offline — Technical Test Mobile Developer

Aplikasi Android native (Kotlin + Jetpack Compose) untuk pendaftaran member secara **offline-first**. Data disimpan ke database lokal terlebih dahulu, kemudian disinkronisasi ke server saat koneksi internet tersedia.

## 📱 Fitur

### A. Autentikasi & Profil
- **Login** — email & password, JWT token disimpan di DataStore (encrypted preferences)
- **Profile** — menampilkan nama lengkap & email user
- **Logout** — hapus session, kembali ke Login

### B. Form Pendaftaran Member (Offline Mode)
- **Data Identitas** — Nama, NIK (16 digit), Telepon, Tempat & Tanggal Lahir, Jenis Kelamin, Status, Pekerjaan
- **Alamat KTP** — Alamat Lengkap, Provinsi, Kota/Kab, Kecamatan, Kelurahan, Kode Pos
- **Alamat Domisili** — Checkbox "sama dengan KTP" atau isi manual
- **Foto KTP** — Primary & Secondary via kamera, dengan kompresi otomatis (max 1MB)
- **Save Draft** — data disimpan ke Room DB lokal dengan status "Draft"

### C. List Member & Sinkronisasi
- **Tab Draft** — list data member berstatus "Draft" dari DB lokal
- **Tab Sudah Di-Upload** — list data yang sudah berhasil disinkronisasi
- **Upload Satuan** — upload satu member tertentu
- **Upload Semua (Bulk Sync)** — loop upload semua draft satu-per-satu dengan progress indicator
- **Edit** — buka kembali form untuk edit data draft

## 🏗️ Arsitektur

### Clean Architecture + MVVM

```
┌─────────────────────────────────────────────────────┐
│                  Presentation Layer                  │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────┐  │
│  │  Screen   │  │ ViewModel │  │  Navigation      │  │
│  │ (Compose) │──│  (State)  │  │  (NavGraph)      │  │
│  └──────────┘  └─────┬─────┘  └──────────────────┘  │
│                      │                               │
├──────────────────────┼───────────────────────────────┤
│                Domain│Layer                          │
│  ┌───────────┐  ┌────┴────┐  ┌────────────────┐     │
│  │   Model   │  │ UseCase │  │  Repository    │     │
│  │ (Member)  │  │  (Logic)│  │  (Interface)   │     │
│  └───────────┘  └─────────┘  └────────────────┘     │
│                                                      │
├──────────────────────────────────────────────────────┤
│                    Data Layer                        │
│  ┌────────────────┐      ┌─────────────────────┐    │
│  │  Remote (API)  │      │  Local (Room DB)    │    │
│  │  ┌──────────┐  │      │  ┌──────────┐       │    │
│  │  │ ApiService│  │      │  │ MemberDao│       │    │
│  │  │ DTOs     │  │      │  │ Entity   │       │    │
│  │  └──────────┘  │      │  └──────────┘       │    │
│  └────────────────┘      └─────────────────────┘    │
│                                                      │
├──────────────────────────────────────────────────────┤
│                    Core Layer                        │
│  ┌──────┐ ┌────────────┐ ┌───────────┐ ┌─────────┐ │
│  │  DI  │ │  Network   │ │TokenManager│ │Compressor│ │
│  │(Hilt)│ │(Interceptor│ │(DataStore) │ │ (Image) │ │
│  └──────┘ └────────────┘ └───────────┘ └─────────┘ │
└─────────────────────────────────────────────────────┘
```

### Tech Stack

| Komponen | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Local DB | Room |
| Network | Retrofit + OkHttp |
| Image Loading | Coil |
| Image Compression | Zelory Compressor |
| State Management | StateFlow + ViewModel |
| Navigation | Navigation Compose |
| Token Storage | DataStore Preferences |
| Camera | CameraX + FileProvider |
| Permissions | Accompanist Permissions |

## 📂 Struktur Folder

```
app/src/main/java/com/registeroffline/
├── RegisterOfflineApp.kt              # Application class (@HiltAndroidApp)
├── core/
│   ├── di/AppModule.kt                # Hilt module (Network, DB, Repository)
│   ├── network/AuthInterceptor.kt     # JWT token auto-inject
│   ├── local/
│   │   ├── entity/MemberEntity.kt     # Room entity
│   │   ├── dao/MemberDao.kt           # Room DAO
│   │   └── db/AppDatabase.kt          # Room database
│   └── util/
│       ├── TokenManager.kt            # DataStore token + profile cache
│       ├── NetworkMonitor.kt          # Connectivity observer (Flow)
│       └── ImageCompressor.kt         # Zelory compressor wrapper
├── data/
│   ├── remote/
│   │   ├── api/ApiService.kt          # Retrofit interface
│   │   └── dto/Dtos.kt                # Request/Response DTOs
│   └── repository/
│       ├── Mappers.kt                 # Entity <-> Domain mappers
│       ├── AuthRepositoryImpl.kt      # Auth implementation
│       └── MemberRepositoryImpl.kt    # Member CRUD + multipart upload
├── domain/
│   ├── model/Models.kt                # Domain models (Member, UserProfile)
│   ├── repository/Repositories.kt     # Repository interfaces
│   └── usecase/UseCases.kt            # All use cases
└── presentation/
    ├── MainActivity.kt                # Entry point (@AndroidEntryPoint)
    ├── theme/Theme.kt                 # Compose theme (colors, typography)
    ├── navigation/
    │   ├── Route.kt                   # Route definitions
    │   └── AppNavGraph.kt             # Navigation graph
    ├── splash/SplashScreen.kt         # Splash screen (indigo)
    ├── auth/
    │   ├── AuthViewModel.kt           # Login/register state
    │   └── LoginScreen.kt             # Login UI
    ├── home/
    │   ├── HomeViewModel.kt           # Draft/Synced + bulk sync
    │   └── HomeScreen.kt              # Home with tabs
    ├── member/form/
    │   ├── MemberFormViewModel.kt     # Form state + save/upload
    │   └── MemberFormScreen.kt        # Complex form UI
    └── profile/
        ├── ProfileViewModel.kt        # Profile + logout
        └── ProfileScreen.kt           # Profile UI
```

## 🚀 Cara Menjalankan

### Prasyarat
- Android Studio Ladybug (2024.2.1) atau lebih baru
- JDK 17
- Android SDK 35
- Device/Emulator Android API 26+

### Steps
1. Clone repository:
   ```bash
   git clone <repo-url>
   cd RegisterOffline
   ```

2. Buka project di Android Studio

3. Sync Gradle dan tunggu dependencies terunduh

4. Run di emulator atau device:
   ```bash
   ./gradlew installDebug
   ```

### API Base URL
Default: `https://api-test.partaiperindo.com/api/v1/`
Bisa diubah di `app/build.gradle.kts` pada `buildConfigField`.

## 🔄 Alur Offline → Sync

1. **Input Offline** — User mengisi form dan tap "Simpan sebagai Draft"
2. **Data Tersimpan** — Room DB menyimpan dengan `syncStatus = "Draft"`
3. **List Draft** — HomeScreen tab "Draft" menampilkan data dari Room (observe Flow)
4. **Koneksi Aktif** — NetworkMonitor mendeteksi internet available
5. **Upload** — User tap "Upload" (satuan) atau "Upload Semua" (bulk)
6. **Multipart POST** — Image dikompresi → dikirim via multipart/form-data
7. **Sync Success** — `syncStatus` di-update ke "Synced", pindah ke tab "Sudah Di-Upload"

## 📝 Catatan

- **Image Compression** — Foto KTP dikompresi menggunakan Zelory Compressor (max 1MB, 1024x1024) sebelum upload untuk menghemat bandwidth
- **Offline-First** — Semua data WAJIB masuk Room dulu sebelum bisa di-upload
- **Token Security** — JWT disimpan di DataStore (bukan SharedPreferences biasa)
- **Error Handling** — Semua API call dibungkus Result<T> untuk handling error yang konsisten
