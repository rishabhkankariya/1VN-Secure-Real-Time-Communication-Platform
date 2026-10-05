# 1VN — Installation & User Guide

1VN is a secure, real-time communication platform designed to work out of the box on Windows with **zero external prerequisites**.

---

## Quick Start (For End Users)

### Option 1: One-Click Installer (Recommended)
1. Download **`1VN-Setup.exe`** from the latest release.
2. Double-click the installer and follow the standard Windows setup wizard.
3. Launch **1VN** from your Desktop shortcut or Start Menu.
4. The embedded communication server and local encrypted database start automatically.
5. Create your account with a username and password, then start chatting!

**Zero Dependencies Required:**
- ❌ No Java or JDK installation needed (custom Java 25 runtime is bundled).
- ❌ No Maven installation needed.
- ❌ No MySQL server or Docker needed (embedded SQLite with AES-256-GCM encryption is used).
- ❌ No environment variables or terminal commands required.

---

### Option 2: Portable Distribution
1. Download **`1VN-v1.0.0-windows-portable.zip`**.
2. Extract the folder anywhere on your computer (e.g. `C:\Users\<You>\1VN` or a USB drive).
3. Double-click **`1VN.exe`**.
4. The application boots immediately with local storage in `%LOCALAPPDATA%\1VN\data\1vn.db`.

---

## Desktop Architecture

```
                 1VN.exe
                    │
          ┌─────────┴─────────┐
          │                   │
      JavaFX Client       Embedded Server (Port 5000)
                              │
                         Embedded SQLite
                              │
                    Local Storage (1vn.db)
```

- **Client**: Modern JavaFX presentation layer with responsive dark design system, presence indicators, and message bubble threads.
- **Server**: Multi-threaded TCP socket engine running as an internal background daemon.
- **Security**: Passwords hashed with BCrypt; all messages encrypted at rest using AES-256-GCM with automatically generated device master keys.

---

## Enterprise / Centralized Server Mode (Optional)

If deploying 1VN in a multi-user corporate network where clients connect to a central server:

1. **Central Server with MySQL:**
   ```powershell
   # Set environment variables
   $env:DB_HOST="mysql-server.local"
   $env:DB_USER="onevn"
   $env:DB_PASSWORD="your-secure-password"
   $env:MESSAGE_ENCRYPTION_KEY="your-base64-32byte-key"

   # Run server
   .\mvnw.cmd -pl server exec:java
   ```

2. **Client Connection:**
   The JavaFX client connects to the central server IP/port automatically or can be configured via `ChatService`.
