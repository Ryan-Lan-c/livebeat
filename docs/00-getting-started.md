# 00 — 啟動指南

> [← 返回總覽](../PROJECT_PLAN.md)

> 本文件適用對象：所有加入本專案的開發人員，包含初次接觸本專案的工程師。  
> 請依序完成每個步驟，若遇到問題請先查閱文件末尾的 [常見問題](#常見問題-faq)。

---

## 目錄

1. [環境需求總覽](#一環境需求總覽)
2. [安裝前置環境](#二安裝前置環境)
3. [IDE 安裝與外掛設定](#三ide-安裝與外掛設定)
4. [SonarQube 設定（後端代碼品質）](#四sonarqube-設定後端代碼品質)
5. [ESLint 設定（前端代碼品質）](#五eslint-設定前端代碼品質)
6. [取得專案](#六取得專案)
7. [啟動依賴服務（Docker Compose）](#七啟動依賴服務docker-compose)
8. [啟動後端（Spring Boot）](#八啟動後端spring-boot)
9. [啟動使用者前台 Web](#九啟動使用者前台-web)
10. [啟動後台管理 Web](#十啟動後台管理-web)
11. [啟動 Flutter App](#十一啟動-flutter-app)
12. [各服務 Port 對照表](#十二各服務-port-對照表)
13. [常見問題 FAQ](#十三常見問題-faq)

---

## 一、環境需求總覽

開始之前，請確認以下工具都已安裝並符合版本需求：

| 工具 | 最低版本 | 用途 | 下載 |
|---|---|---|---|
| Git | 2.x | 版本控制 | https://git-scm.com |
| JDK | **25** | 後端開發 | https://adoptium.net |
| Node.js | **22 LTS** | 前端開發 | https://nodejs.org |
| Flutter SDK | **3.x** | App 開發 | https://flutter.dev |
| Docker Desktop | Latest | 本機依賴服務 | https://www.docker.com/products/docker-desktop |
| IntelliJ IDEA | 2024.x+ | 後端 IDE（推薦 Ultimate）| https://www.jetbrains.com/idea |
| VS Code | Latest | 前端 / Flutter IDE | https://code.visualstudio.com |

> **Mac M4（Apple Silicon）使用者注意：**
> 下載 JDK、Docker Desktop 時請選擇 **Apple Silicon / ARM64** 版本，不要下載 x86_64 版本。

> **IntelliJ IDEA 版本說明：**
> - **Ultimate（付費）**：有完整 Spring Boot 支援、資料庫工具，推薦使用
> - **Community（免費）**：基本 Java 開發可用，但部分 Spring Boot 功能受限
> - 學生可透過 [JetBrains Student Program](https://www.jetbrains.com/community/education/) 免費取得 Ultimate

---

## 二、安裝前置環境

### 2.1 安裝 Git

**macOS**
```bash
# 方法一：使用 Homebrew（推薦）
brew install git

# 方法二：安裝 Xcode Command Line Tools（會順帶安裝 Git）
xcode-select --install
```

**Windows**
前往 https://git-scm.com/download/win 下載安裝程式，安裝時選擇「Git Bash」選項。

**Linux（Ubuntu / Debian）**
```bash
sudo apt update && sudo apt install git -y
```

**Linux（Fedora / RHEL）**
```bash
sudo dnf install git -y
```

驗證安裝：
```bash
git --version
# 預期輸出：git version 2.x.x
```

---

### 2.2 安裝 JDK 25

前往 https://adoptium.net/temurin/releases/?version=25 下載。

**macOS（Apple Silicon M1/M2/M3/M4）**
選擇 `macOS` + `aarch64` + `JDK` + `tar.gz`，或使用 Homebrew：
```bash
brew install --cask temurin@25
```

**macOS（Intel）**
選擇 `macOS` + `x64` + `JDK` + `pkg`

**Windows**
選擇 `Windows` + `x64` + `JDK` + `msi`，執行安裝程式，**記得勾選「Set JAVA_HOME variable」**。

**Linux（Ubuntu / Debian）**
```bash
# 先下載 .tar.gz，解壓縮到 /opt/jdk-25
sudo tar -xzf OpenJDK25U-jdk_x64_linux_hotspot_*.tar.gz -C /opt
# 設定環境變數（加到 ~/.bashrc 或 ~/.zshrc）
export JAVA_HOME=/opt/jdk-25.x.x+x
export PATH=$JAVA_HOME/bin:$PATH
source ~/.bashrc
```

驗證安裝：
```bash
java -version
# 預期輸出：openjdk version "25" ...
```

---

### 2.3 安裝 Node.js 22 LTS

推薦使用 **nvm**（Node Version Manager）管理 Node.js 版本，方便日後切換。

**macOS / Linux**
```bash
# 安裝 nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash

# 重新載入 shell
source ~/.bashrc  # 或 source ~/.zshrc

# 安裝並使用 Node.js 22
nvm install 22
nvm use 22
nvm alias default 22
```

**Windows**
前往 https://github.com/coreybutler/nvm-windows/releases 下載 `nvm-setup.exe` 安裝，之後在 **命令提示字元（以系統管理員身分執行）**：
```cmd
nvm install 22
nvm use 22
```

驗證安裝：
```bash
node --version   # 預期：v22.x.x
npm --version    # 預期：10.x.x
```

---

### 2.4 安裝 Flutter SDK

**macOS（Apple Silicon）**
```bash
# 方法一：使用 Homebrew（推薦）
brew install --cask flutter

# 方法二：手動下載
# 前往 https://flutter.dev/docs/get-started/install/macos
# 下載 Apple Silicon 版本的 .tar.xz
# 解壓到 ~/development/flutter
# 加到 ~/.zshrc：export PATH="$HOME/development/flutter/bin:$PATH"
```

**Windows**
前往 https://flutter.dev/docs/get-started/install/windows 下載 `.zip`，解壓到 `C:\src\flutter`，並將 `C:\src\flutter\bin` 加入系統環境變數 `PATH`。

**Linux**
```bash
# 使用 snap（Ubuntu）
sudo snap install flutter --classic

# 或手動下載，解壓到 ~/development/flutter
tar xf flutter_linux_*.tar.xz -C ~/development
echo 'export PATH="$HOME/development/flutter/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

驗證安裝並檢查環境：
```bash
flutter --version
flutter doctor
# flutter doctor 會提示缺少的工具，依提示安裝即可
```

> **iOS 開發限制：** 只有 macOS 可以編譯 iOS App（需安裝 Xcode）。Windows / Linux 只能開發 Android。

---

### 2.5 安裝 Docker Desktop

**macOS（Apple Silicon）**
前往 https://www.docker.com/products/docker-desktop 下載，選擇 **Apple Silicon** 版本（`.dmg`）。

**macOS（Intel）**
同上，選擇 **Intel Chip** 版本。

**Windows**
前往 https://www.docker.com/products/docker-desktop 下載 Windows 安裝程式。  
安裝完成後需重新開機，並確認 WSL2 已啟用（安裝程式通常會自動引導）。

**Linux**
Linux 安裝的是 **Docker Engine**（不是 Docker Desktop）：
```bash
# Ubuntu / Debian
sudo apt update
sudo apt install docker.io docker-compose-plugin -y
sudo systemctl enable --now docker
# 讓目前使用者不需要 sudo 執行 docker
sudo usermod -aG docker $USER
newgrp docker
```

驗證安裝：
```bash
docker --version          # 預期：Docker version 2x.x.x
docker compose version    # 預期：Docker Compose version v2.x.x
```

---

## 三、IDE 安裝與外掛設定

### 3.1 IntelliJ IDEA（後端開發）

安裝完成後，安裝以下外掛（File → Settings → Plugins → 搜尋名稱）：

| 外掛名稱 | 用途 |
|---|---|
| **SonarLint** | 即時代碼品質分析，可連接 SonarQube Server |
| **Lombok** | 支援 Lombok 注解（@Getter、@Builder 等）|
| **EnvFile** | 從 `.env` 檔讀取環境變數到 Run Configuration |
| **Docker** | 在 IDE 內管理 Docker 容器（IntelliJ Ultimate 內建）|
| **Gradle** | Gradle 任務管理（通常已內建）|

> 安裝外掛後需重啟 IntelliJ IDEA 才會生效。

---

### 3.2 VS Code（前端 / Flutter 開發）

安裝以下 Extension（Ctrl+Shift+X / Cmd+Shift+X 開啟市集）：

**前端開發必裝**
| Extension | 用途 |
|---|---|
| **ESLint**（Microsoft 出品）| 即時顯示 ESLint 錯誤與警告 |
| **Volar**（Vue - Official）| Vue 3 語法支援、型別檢查 |
| **Prettier - Code formatter** | 統一代碼格式 |
| **Tailwind CSS IntelliSense** | TailwindCSS 類名自動補全 |
| **Path Intellisense** | 路徑自動補全 |

**Flutter 開發必裝**
| Extension | 用途 |
|---|---|
| **Flutter**（Dart 團隊出品）| Flutter 開發支援（會自動安裝 Dart）|
| **Dart** | Dart 語言支援 |

---

## 四、SonarQube 設定（後端代碼品質）

SonarQube 提供靜態代碼分析，幫助開發者在提交前發現潛在的 Bug、Security 問題、Code Smell。

### 選項 A：SonarLint 單機模式（推薦開發者本機使用）

不需要啟動 SonarQube Server，SonarLint 在 IntelliJ 內直接分析，即裝即用：

1. 確認 IntelliJ 已安裝 **SonarLint** 外掛（見 [3.1](#31-intellij-idea後端開發)）
2. 開啟任一 Java 檔案，SonarLint 會自動在下方面板顯示問題
3. 快捷鍵：`Alt+Shift+S`（Windows/Linux）/ `Option+Shift+S`（macOS）可立即分析當前檔案

### 選項 B：SonarQube Server（完整分析，CI/CD 使用）

> SonarQube 需要較多記憶體（約 2GB），若記憶體不足請先使用選項 A。

**啟動 SonarQube（使用獨立的 Docker Compose）：**

```bash
# 在專案根目錄執行
docker compose -f infrastructure/docker-compose.sonarqube.yml up -d
```

等待約 1 分鐘後，開啟瀏覽器：http://localhost:9090  
預設帳號：`admin` / 預設密碼：`admin`（首次登入會要求修改密碼）

**連接 IntelliJ SonarLint 到 SonarQube Server：**
1. IntelliJ → File → Settings → Tools → SonarLint
2. 點選「Add」→ 選擇「SonarQube」
3. Server URL 填入：`http://localhost:9000`
4. 依指示生成 Token 並輸入
5. 選擇對應的 Project Key

**執行 Sonar 分析（Terminal）：**

macOS / Linux：
```bash
cd backend/livebeat
./gradlew sonarqube
```

Windows：
```cmd
cd backend\livebeat
gradlew.bat sonarqube
```

---

## 五、ESLint 設定（前端代碼品質）

ESLint 已在前端專案的 `package.json` 中設定好，安裝依賴後即可使用。

### VS Code 即時顯示

確認已安裝 **ESLint** Extension（見 [3.2](#32-vs-code前端--flutter-開發)），VS Code 開啟前端資料夾後即會自動啟用。

### 手動執行 ESLint

```bash
# 進入前台 Web 資料夾
cd frontend/user-web

# 檢查所有檔案
npm run lint

# 自動修正可修正的問題
npm run lint:fix
```

後台管理 Web 同樣操作，切換到 `frontend/admin-web` 資料夾執行。

---

## 六、取得專案

```bash
# Clone 專案
git clone <your-repository-url> livebeat
cd livebeat
```

Clone 完成後，目錄結構如下：
```
livebeat/
├── PROJECT_PLAN.md       ← 專案總覽（你現在在看的）
├── docs/                 ← 所有技術文件
├── backend/
│   └── livebeat/         ← Spring Boot 後端
├── frontend/
│   ├── user-web/         ← 使用者前台 Web（Vue 3）
│   └── admin-web/        ← 後台管理 Web（Vue 3）
├── mobile/
│   └── livebeat_flutter/ ← Flutter App
└── infrastructure/
    ├── docker-compose.yml ← 本機依賴服務
    └── nginx/
```

**複製環境變數設定檔：**

macOS / Linux：
```bash
cp backend/livebeat/.env.example backend/livebeat/.env
cp frontend/user-web/.env.example frontend/user-web/.env.local
cp frontend/admin-web/.env.example frontend/admin-web/.env.local
```

Windows（命令提示字元）：
```cmd
copy backend\livebeat\.env.example backend\livebeat\.env
copy frontend\user-web\.env.example frontend\user-web\.env.local
copy frontend\admin-web\.env.example frontend\admin-web\.env.local
```

> 複製完成後，開啟 `.env` 檔案，確認各設定值正確（通常本機開發用預設值即可）。

---

## 七、啟動依賴服務（Docker Compose）

後端啟動之前，必須先讓 Docker Compose 把所有依賴服務跑起來（PostgreSQL、Redis、RabbitMQ 等）。

**確認 Docker Desktop 已在背景執行（桌面工具列應有 Docker 圖示）。**

```bash
# 在專案根目錄執行
cd infrastructure

# 啟動所有依賴服務（背景執行）
docker compose up -d
```

Windows（命令提示字元 / PowerShell）：
```cmd
cd infrastructure
docker compose up -d
```

**確認所有服務都已啟動：**
```bash
docker compose ps
```

應看到以下服務都是 `running` 狀態：

| 服務 | 狀態確認 |
|---|---|
| PostgreSQL | `running` |
| Redis | `running` |
| RabbitMQ | `running` |
| MinIO | `running` |
| Nginx | `running` |
| Mailpit | `running` |
| Jaeger | `running` |

### 各服務說明

| 服務 | 功能說明 |
|---|---|
| **PostgreSQL 16** | 主資料庫。存所有永久資料：演唱會、場次、訂單、使用者帳號、付款紀錄。分成 6 個獨立 Schema（auth / concert / order / payment / notification / admin），對應 6 個後端模組 |
| **Redis 7** | 快取 + 分散式鎖。搶票核心：庫存數量存放於 Redis，以 Lua Script 原子扣減防止超賣；同時快取演唱會資訊，避免熱門場次每次請求都打 DB |
| **RabbitMQ 3** | 訊息佇列。搶票時不直接寫 DB，而是將「訂單建立」事件投入 Queue，Consumer 非同步寫入 PostgreSQL，DB 不承受瞬間高流量。亦負責 Email / LINE Bot 推播任務與 WebSocket 多實例 STOMP relay |
| **MinIO** | 本機版 S3 物件儲存。存放演唱會圖片、票券 PDF、座位圖 SVG。API 完全相容 AWS S3，上線時只換 endpoint 與金鑰，程式碼不需修改 |
| **Mailpit** | 本機 Email 測試工具。Spring Boot 發出的所有 Email（驗證信、訂票確認信）都會被攔截並顯示在 Web UI（port 8025），不會真正寄出 |
| **Jaeger** | 分散式追蹤。記錄一個請求從前端進入，經過 Spring Boot → Redis → PostgreSQL → RabbitMQ 的完整路徑與各段耗時，用於效能瓶頸排查 |
| **Nginx** | 反向代理。統一入口：`/api/` 導至 Spring Boot（8080）、`/` 導至使用者前台（5173）、`/admin/` 導至後台（5174）。本機開發多直接存取各自 Port，上線後 Nginx 為唯一對外入口並負責 SSL 終止 |

**停止所有服務：**
```bash
docker compose down
```

**停止並清除所有資料（完全重置）：**
```bash
docker compose down -v
```

> ⚠️ `down -v` 會刪除資料庫資料，請謹慎使用。

---

## 八、啟動後端（Spring Boot）

> 確認 Docker Compose 服務已全部啟動後再進行此步驟。

### 方法一：使用 IntelliJ IDEA 播放鍵（推薦）

1. 用 IntelliJ IDEA 開啟 `backend/livebeat` 資料夾
   - File → Open → 選擇 `backend/livebeat`
2. 等待 IntelliJ 完成 **Gradle 同步**（右下角進度條結束）
3. 設定 Project SDK：
   - File → Project Structure → Project → SDK → 選擇 **JDK 25**
4. 設定 Run Configuration：
   - 右上角點選 `Edit Configurations...`
   - 點選左上角 `+` → 選擇 `Spring Boot`
   - **Main class**：`com.livebeat.LiveBeatApplication`
   - **Environment variables**：點選右側圖示，載入 `.env` 檔（需安裝 EnvFile 外掛）
   - 點選 `OK` 儲存
5. 點選右上角 **綠色播放按鈕 ▶** 啟動

啟動成功後，Console 會出現：
```
Started LiveBeatApplication in x.xxx seconds
```

### 方法二：使用 Terminal

**macOS / Linux：**
```bash
cd backend/livebeat

# 首次執行需給予執行權限
chmod +x gradlew

# 啟動
./gradlew bootRun
```

**Windows（命令提示字元）：**
```cmd
cd backend\livebeat
gradlew.bat bootRun
```

**Windows（PowerShell）：**
```powershell
cd backend\livebeat
.\gradlew.bat bootRun
```

啟動成功後可以用瀏覽器開啟 API 文件：  
http://localhost:8080/swagger-ui.html

---

## 九、啟動使用者前台 Web

> 此為使用者訂票的主要網站（Vue 3 + Element Plus）。

### 安裝依賴套件

macOS / Linux：
```bash
cd frontend/user-web
npm install
```

Windows：
```cmd
cd frontend\user-web
npm install
```

> 首次安裝會花幾分鐘，後續不需要重複執行（除非 `package.json` 有更動）。

### 啟動開發伺服器

macOS / Linux：
```bash
npm run dev
```

Windows：
```cmd
npm run dev
```

啟動成功後，Terminal 會顯示：
```
VITE v5.x.x  ready in xxx ms
➜  Local:   http://localhost:5173/
```

開啟瀏覽器前往 http://localhost:5173

### 使用 VS Code 啟動（選用）

1. 用 VS Code 開啟 `frontend/user-web` 資料夾
2. 開啟 Terminal（Ctrl+` / Cmd+`）
3. 執行 `npm install`，再執行 `npm run dev`

---

## 十、啟動後台管理 Web

> 此為票務公司員工使用的管理系統（Vue 3 + Element Plus + ECharts）。

步驟與使用者前台相同，資料夾改為 `frontend/admin-web`，Port 為 **5174**：

macOS / Linux：
```bash
cd frontend/admin-web
npm install
npm run dev
```

Windows：
```cmd
cd frontend\admin-web
npm install
npm run dev
```

啟動成功後開啟：http://localhost:5174

---

## 十一、啟動 Flutter App

> App 支援 Android 與 iOS。iOS 只能在 macOS 上開發。

### 安裝依賴套件

macOS / Linux：
```bash
cd mobile/livebeat_flutter
flutter pub get
```

Windows：
```cmd
cd mobile\livebeat_flutter
flutter pub get
```

### 確認可用的裝置 / 模擬器

```bash
flutter devices
```

輸出範例：
```
2 connected devices:
iPhone 16 Pro (simulator)  • ios  • com.apple.CoreSimulator.SimDeviceType.iPhone-16-Pro
Android SDK (emulator)     • android-x64 • Android 15
```

若沒有顯示任何裝置：
- **Android**：開啟 Android Studio → AVD Manager → 建立並啟動模擬器
- **iOS**（macOS 限定）：開啟 Xcode → Window → Devices and Simulators → 啟動模擬器

### 使用 VS Code 啟動（推薦）

1. 用 VS Code 開啟 `mobile/livebeat_flutter` 資料夾
2. 右下角點選裝置選擇器，選擇目標裝置
3. 按 **F5** 或 Run → Start Debugging

### 使用 Terminal 啟動

```bash
# 啟動到預設裝置
flutter run

# 啟動到特定裝置（使用上方 flutter devices 顯示的 device id）
flutter run -d <device-id>
```

> 首次編譯時間較長（約 2-5 分鐘），之後 Hot Reload 很快（按 `r` 即可）。

---

## 十二、各服務 Port 對照表

所有服務啟動後，以下是各服務的存取位址：

| 服務 | Port / URL | 說明 |
|---|---|---|
| **後端 API** | http://localhost:8080 | Spring Boot 主程式 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | API 文件 |
| **使用者前台 Web** | http://localhost:5173 | Vue 3 使用者介面 |
| **後台管理 Web** | http://localhost:5174 | Vue 3 管理介面 |
| **PostgreSQL** | localhost:5432 | 資料庫（user: postgres, db: livebeat）|
| **Redis** | localhost:6379 | 快取 / 分散鎖 |
| **RabbitMQ Management** | http://localhost:15672 | 訊息佇列管理介面（livebeat / livebeat_dev）|
| **MinIO Console** | http://localhost:9001 | 物件儲存管理介面 |
| **Mailpit** | http://localhost:8025 | 本機 Email 收件匣（測試用）|
| **Jaeger UI** | http://localhost:16686 | 分散式追蹤 |
| **SonarQube** | http://localhost:9090 | 代碼品質（需另外啟動）|

---

## 十三、常見問題 FAQ

### Q1：`./gradlew: Permission denied`

**macOS / Linux** 需要先給予執行權限：
```bash
chmod +x backend/livebeat/gradlew
```

---

### Q2：Docker Compose 啟動失敗，提示 port 被佔用

先找出佔用 port 的程式：

**macOS / Linux：**
```bash
# 查詢占用 5432 port 的程式（以 PostgreSQL 為例）
lsof -i :5432
# 找到 PID 後停止該程式
kill -9 <PID>
```

**Windows（PowerShell）：**
```powershell
netstat -ano | findstr :5432
# 找到最後一欄的 PID，然後
taskkill /PID <PID> /F
```

---

### Q3：`npm install` 速度很慢

切換到台灣或亞洲的 npm mirror：
```bash
npm config set registry https://registry.npmmirror.com
```

若要還原官方 registry：
```bash
npm config set registry https://registry.npmjs.org
```

---

### Q4：`flutter doctor` 顯示缺少工具

依照 `flutter doctor` 的提示逐一安裝缺少的工具。常見問題：
- **Android toolchain**：需要安裝 Android Studio 並執行 `flutter doctor --android-licenses` 同意授權
- **Xcode（macOS）**：需安裝 Xcode 並執行 `sudo xcode-select --switch /Applications/Xcode.app`
- **CocoaPods（macOS / iOS）**：`sudo gem install cocoapods`

---

### Q5：IntelliJ 無法識別 Gradle 專案

1. 確認 SDK 已設定為 JDK 25（File → Project Structure → Project）
2. 在 `build.gradle` 所在目錄右鍵 → `Link Gradle Project`
3. 或使用 View → Tool Windows → Gradle → 點選重新整理圖示

---

### Q6：後端啟動時出現 `Connection refused` 錯誤（無法連接 DB / Redis）

表示 Docker Compose 服務尚未啟動。回到[步驟七](#七啟動依賴服務docker-compose)先啟動依賴服務。

---

### Q7：Mac M4 Docker 拉不到某個 Image

部分舊版 image 可能只有 `amd64` 版本。嘗試：
```bash
docker pull --platform linux/arm64 <image-name>
# 或強制使用 amd64（會慢一點，透過模擬執行）
docker pull --platform linux/amd64 <image-name>
```

---

### Q8：RabbitMQ Management UI 無法登入

預設帳密為 `guest` / `guest`，只允許從 `localhost` 登入。  
若在容器內或遠端無法登入，需在 docker-compose.yml 中設定額外使用者。

---

### Q9：MinIO 整合測試（`MinioStorageAdapterTest`）一直 SKIPPED，不是 PASSED

**症狀**

執行 `./gradlew test` 後，`MinioStorageAdapterTest` 兩個測試都顯示 `SKIPPED`，Gradle `--info` log 出現：

```
Could not find a valid Docker environment.
UnixSocketClientProviderStrategy: failed with exception
  BadRequestException (Status 400: {"ID":"","Containers":0,...})
```

---

**根本原因**

Docker Desktop **4.40+（Docker Engine 29.x）** 把 Docker API 最低支援版本從 1.26 提高到了 **1.40**。

Testcontainers 內建的 shaded docker-java 預設使用 **API 1.32** 做初始連線協商，對代理 socket 發出的請求是 `/v1.32/_ping`，Docker Desktop 一律回傳 HTTP 400，Testcontainers 因此判斷 Docker 不可用，將所有測試標記為 SKIPPED。

> **版本說明**：目前已知 Docker Desktop 4.40+（Docker Engine 29.x）與 Testcontainers 1.21.3 內建的 `api.version=1.32` 搭配會觸發此問題。  
> 後續 Docker Desktop 或 Testcontainers 版本仍可能改變，此文件只記錄當時的排查結果。

---

**踩雷過程**

1. `docker info` 正常，排除 Docker 本身沒有在跑的問題
2. `docker context ls` 看到 active context 是 `desktop-linux`（socket 路徑 `~/.docker/run/docker.sock`），與 Testcontainers 預設連線的 `/var/run/docker.sock` 不一致
3. 嘗試設定 `DOCKER_HOST`、`DOCKER_API_VERSION` 環境變數——無效（Gradle daemon 有環境隔離，且這些 env var 不是 shaded docker-java 實際讀取的設定路徑）
4. 嘗試升級 Testcontainers 版本（1.20.6 → 1.21.3）——無效，問題相同
5. 用 curl 直接打代理 socket，確認是 API 版本被拒的問題：
   ```bash
   curl --unix-socket /var/run/docker.sock http://localhost/v1.32/_ping  # → HTTP 400 ✗
   curl --unix-socket /var/run/docker.sock http://localhost/v1.47/_ping  # → HTTP 200 ✓
   ```
6. 反編譯 Testcontainers jar 內的 shaded `DefaultDockerClientConfig`，發現它從 classpath 上的 `/docker-java.properties` 讀取設定，property key 是 `api.version`（不是常見的 `DOCKER_API_VERSION` env var）

---

**修復方式**

在 `backend/livebeat/src/test/resources/docker-java.properties` 加入：

```properties
api.version=1.47
```

Testcontainers 的 shaded docker-java 讀取此檔後改用 `/v1.47/_ping` 做偵測，成功通過，測試由 SKIPPED 變為 PASSED。

---

> 若遇到本文件未列出的問題，請在 GitHub Issue 回報，並附上錯誤訊息與作業系統版本。
