# AirBridge ⚡
### Direct Offline-First Android-to-Laptop Backup & High-Speed File Transfer

**AirBridge** is a direct, zero-cloud backup and file-transfer application that enables local synchronization between your Android smartphone and laptop over Wi-Fi or USB data cables.

With AirBridge, your phone hosts its own embedded web server and desktop portal. You don't need to install any client software, drivers, or companion apps on your laptop—simply open any web browser (Chrome, Safari, Edge, Firefox) to access your files, initiate incremental backups, or transfer files bidirectionally.

---

## 🌟 Key Highlights

- **100% Offline & Private:** Zero cloud servers, zero internet access required. All data remains within your local network or physical USB cable.
- **Zero-Install Desktop Portal:** Full-featured desktop web application served directly from the phone.
- **Incremental Backup Engine:** Syncs only new or modified files, drastically reducing transfer time and bandwidth.
- **Resumable Transfers:** Supports HTTP Range requests (`bytes=start-end`) to resume interrupted transfers without starting over.
- **SHA-256 Integrity Verification:** Computes cryptographic hashes before and after transfer to guarantee zero data loss or corruption.
- **End-to-End Encryption:** Optional AES-256-GCM encrypted transfer streams.
- **Secure Device Pairing:** 6-digit cryptographic PIN verification protects your device from unauthorized local network access.
- **Biometric App Lock:** Safeguards file access and pairing approvals with fingerprint, facial recognition, or PIN lock.
- **Dual Connectivity:** Supports both Wi-Fi Local Area Networks and USB Cable / Tethering connections.
- **Adaptive UI & Dark Mode:** Material 3 design with System, Dark, AMOLED Pitch Black, and Light themes across mobile and desktop.

---

## 🚀 Connectivity Modes

### 1. Wi-Fi Local Area Network (Wireless)
- Phone and laptop connect to the same Wi-Fi router or mobile hotspot.
- AirBridge automatically discovers your local IP (e.g., `http://192.168.1.50:8888`).
- Scan the generated on-screen QR code with your laptop camera or type the address into your browser.

### 2. USB Data Cable (Wired & Tethering)
- **USB Tethering Mode:** Connect your phone via USB cable and enable **USB Tethering** in Android Settings. AirBridge automatically detects tethering interfaces (`rndis0`, `usb0`) and provides the direct IP link.
- **ADB Port Forwarding:** For developer and power-user workflows:
  ```bash
  adb forward tcp:8888 tcp:8888
  ```
  Then open `http://localhost:8888` on your laptop for maximum wired transfer speeds.

---

## 🖥️ Zero-Install Desktop Web Portal

The embedded web server serves a modern desktop interface directly to your laptop browser:

| Feature | Description |
| :--- | :--- |
| **Authentication Modal** | Prompts for the 6-digit PIN displayed on your phone screen before granting access. |
| **Incremental Sync Hero** | 1-click button to download an archive containing only new or modified files since your last backup. |
| **Download All (.ZIP)** | Bundles your entire selected media library into a single compressed archive on the fly. |
| **Categorized Explorer** | Filter photos, videos, documents, audio, and downloads with live search and preview. |
| **Individual Downloads** | Download single files with browser resume support and SHA-256 checksum tags. |
| **Drag-and-Drop Uploader** | Drag files from your computer into the browser to upload them directly into your phone's storage. |
| **Live Transfer HUD** | Floating real-time monitor displaying active transfer speeds (MB/s), ETA countdown, and progress bars. |
| **Dark / Light Toggle** | Instant theme switching matching your desktop environment. |

---

## 📱 Mobile App Screens & Architecture

### 1. Dashboard (`DashboardScreen`)
- **Connection Engine Card:** Server status indicator, Wi-Fi/USB toggle, IP address pill with 1-tap clipboard copy, and QR code trigger.
- **Incremental Sync Card:** Shows count and size of unbacked-up items with a 1-tap "Sync" action.
- **Active Transfer HUD:** Real-time speed gauge, ETA counter, active file progress, and Pause/Resume/Cancel controls.
- **Media Storage Breakdown:** Quick-glance counters for photos, videos, and documents.

### 2. File Explorer (`FileExplorerScreen`)
- Multi-category tabs: **All**, **Photos**, **Videos**, **Documents**, **Audio**, and **Downloads**.
- **"Only Unbacked Up"** toggle to isolate modified or newly created files.
- Search filter for filenames and file extensions.
- Multi-select action bar with batch selection and custom backup triggers.
- Per-item cards displaying size, timestamp, SHA-256 hash chip, and sync badges (`Synced`, `New`, `Modified`, `Failed`).

### 3. Laptop Portal & Pairing (`ConnectPortalScreen`)
- High-contrast QR code generated dynamically via an offline Kotlin matrix generator.
- Large, accessible 6-digit pairing PIN display.
- Step-by-step USB cable tethering and ADB forwarding instructions.
- **Paired Laptops Manager:** Lists paired computers, client IP addresses, pairing timestamps, with one-tap device revocation.

### 4. Transfer Activity & Retry Queue (`TransferActivityScreen`)
- Live HUD tracking running transfers.
- **Failure Queue:** Captures interrupted transfers with explicit error causes (e.g., connection reset, cancelled). Includes **"Retry All"** and individual retry actions.
- **Transfer Audit Logs:** Historical record of past transfers with timestamp, direction (phone-to-laptop or laptop-to-phone), bytes transferred, and cryptographic SHA-256 validation stamps.

### 5. Security & Appearance Settings (`SecuritySettingsScreen`)
- **Biometric / PIN Lock:** Toggle fingerprint/face authentication or 4-digit PIN lock before vault access.
- **End-to-End Encryption:** Active AES-256-GCM cipher payload indicator.
- **Theme Selection:** System Default, Dark Mode, AMOLED Pitch Black Mode, and Light Mode.
- **Server Configuration:** Configurable local port (default: `8888`), HTTP Range resumable status, and SHA-256 enforcement toggles.

---

## 🔒 Security & Cryptography

1. **Cryptographic PIN Authentication:** Every new session generates a cryptographically random 6-digit PIN (`java.security.SecureRandom`). Unpaired clients cannot access file endpoints without passing this handshake.
2. **Session Tokens:** Successful pairings receive an isolated, cryptographically generated bearer token stored in browser memory.
3. **AES-256-GCM Encryption:** Payloads can be encrypted on-the-fly using AES in Galois/Counter Mode with unique 96-bit initialization vectors (IV).
4. **SHA-256 Integrity Verification:** Stream-calculated hash guarantees that zero bits were flipped or corrupted in transit.

---

## 🛠️ Technology Stack

- **Platform:** Android (Kotlin, Jetpack Compose, Material Design 3)
- **Local Persistence:** Room Database with KSP (`AppDatabase`, `BackupRecordDao`, `PairedDeviceDao`, `TransferLogDao`)
- **Network Engine:** Embedded zero-dependency socket server (`ServerSocket`, buffered I/O, coroutine dispatchers)
- **Image & Thumbnail Loading:** Coil Compose
- **Concurrency & State:** Kotlin Coroutines (`StateFlow`, `collectAsStateWithLifecycle`)
- **Testing:** Local JVM and Robolectric unit tests (`ExampleRobolectricTest`)

---

## 📋 Permissions

AirBridge uses least-privilege permissions configured in `AndroidManifest.xml`:
- `INTERNET` & `ACCESS_NETWORK_STATE`: For local LAN and USB socket communication.
- `ACCESS_WIFI_STATE` & `CHANGE_WIFI_STATE`: To detect network SSID and local IP addresses.
- `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`: To index media on Android 13+.
- `READ_EXTERNAL_STORAGE`: Backward-compatibility fallback for Android 12 and below.
- `USE_BIOMETRIC`: For on-device biometric security.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_DATA_SYNC`: To ensure large backups complete smoothly when the app is backgrounded.
