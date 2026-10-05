# 1VN — Secure Real-Time Communication Platform

[![Release](https://img.shields.io/badge/Release-v1.0.0-blue.svg)](https://github.com/rishabhkankariya/1VN-Secure-Real-Time-Communication-Platform/releases)
[![Platform](https://img.shields.io/badge/Platform-Windows%20x64-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-25%20LTS-orange.svg)]()
[![Security](https://img.shields.io/badge/Encryption-AES--256--GCM-red.svg)]()
[![License](https://img.shields.io/badge/License-MIT-green.svg)]()

**1VN** is a secure, high-performance real-time messaging application engineered with **JavaFX** for its desktop client, a custom multi-threaded **Java TCP socket server**, and dual-storage capability (**Embedded SQLite** for zero-dependency desktop distribution and **MySQL** for centralized enterprise infrastructure).

---

## Highlights

- **Zero-Configuration Desktop Experience**: Distributed with a bundled, minimal Java 25 runtime and embedded SQLite database. Users simply install and run—no Java, Maven, Docker, or MySQL setup required.
- **Enterprise-Grade Security**:
  - **BCrypt** password hashing with adaptive salt rounds to protect user credentials.
  - **AES-256-GCM** authenticated message encryption at rest with 128-bit integrity tags.
  - Automatic cryptographic device master-key generation for desktop mode; environment-based key injection for enterprise servers.
- **Real-Time Communication Engine**:
  - Full-duplex TCP socket architecture supporting concurrent client connections.
  - Direct 1-on-1 private messaging and channel broadcast capabilities.
  - Real-time presence indicators (online/offline status).
  - Chronological conversation history persistence and instant retrieval.
- **Modern UI & Design System**:
  - Clean JavaFX presentation layer with a custom dark-mode theme (`chat.css`).
  - Distinct message bubble threads with timestamps and sender identifiers.
  - Inline input validation and responsive status monitoring.

---

## Architecture Overview

### Desktop Distribution Mode (Self-Contained)

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

In desktop distribution mode, `1VN.exe` automatically checks for a local server, boots the embedded daemon on `127.0.0.1:5000`, initializes local encrypted SQLite storage at `%LOCALAPPDATA%\1VN\data\1vn.db`, and opens the JavaFX GUI immediately.

### Enterprise Client-Server Mode

```
Presentation Layer (JavaFX Desktop Client)
      ↓
Business Logic (ChatService & UserService)
      ↓
Network Layer (ChatClient TCP Sockets)
      ↓ [TCP Socket Protocol]
Server Daemon (ChatServer & ClientHandler)
      ↓
Data Access Layer (Repository Pattern)
      ↓
Storage Layer (MySQL / SQLite with AES-256-GCM Encryption)
```

---

## Tech Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 25 (LTS) | Modern Java features, virtual threads, and pattern matching |
| **UI Framework** | OpenJFX 25 | Modular JavaFX controls with hardware-accelerated rendering |
| **Networking** | Java TCP Sockets | Low-latency, full-duplex client-server socket protocol |
| **Desktop Storage** | SQLite 3 (via Xerial JDBC) | Embedded, zero-configuration local database |
| **Server Storage** | MySQL 8.4+ | Enterprise persistent storage for multi-user deployments |
| **Authentication** | jBCrypt 0.4 | Adaptive salted password hashing |
| **Cryptography** | AES-256-GCM (Java Cryptography) | Authenticated encryption at rest for messages |
| **Packaging** | `jlink` + `jpackage` + Inno Setup 6 | Minimal bundled JRE and Windows setup installer |

---

## Quick Start

### For End Users (Windows)
1. Download **`1VN-Setup.exe`** from [Releases](https://github.com/rishabhkankariya/1VN-Secure-Real-Time-Communication-Platform/releases).
2. Run the installer and launch **1VN** from the Desktop shortcut.
3. Register an account and begin chatting immediately.

*For complete setup options including portable archive usage, refer to [INSTALL.md](INSTALL.md).*

### For Developers

```powershell
# Clone the repository
git clone https://github.com/rishabhkankariya/1VN-Secure-Real-Time-Communication-Platform.git
cd 1VN-Secure-Real-Time-Communication-Platform

# Build all modules
.\mvnw.cmd clean package

# Run client with JavaFX plugin
.\mvnw.cmd -pl client javafx:run

# Run standalone server
.\mvnw.cmd -pl server exec:java

# Build Windows installer and bundled runtime
.\scripts\build-release.ps1
```

---

## Security Model

- **Authentication**: Passwords are never stored in plaintext. Passwords are salted and hashed using BCrypt before database persistence.
- **Data at Rest**: Every message body is encrypted with AES-256-GCM before writing to the database. The 128-bit authentication tag guarantees message integrity and prevents tampering.
- **Input Sanitization**: All database interactions use prepared statements to eliminate SQL injection vulnerabilities.

---

## Project Structure

```
1VN/
├── client/              # JavaFX client module (UI, networking, models)
├── server/              # Server module (sockets, auth, repositories, encryption)
├── installer/           # Inno Setup 6 installer script (1vn-setup.iss)
├── scripts/             # Build and packaging automation scripts
├── pom.xml              # Maven parent multi-module definition
├── INSTALL.md           # End-user installation guide
└── README.md            # Project overview and documentation
```

---

## License

This project is licensed under the MIT License — see the repository for details.

Developed by **Rishabh Kankariya**.
