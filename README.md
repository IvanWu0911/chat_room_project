# JavaFX 多人聊天室系統 (Railway 雲端版)

這是一個極簡化的 JavaFX 聊天室系統，專為 **Railway** 雲端部署與 **NetBeans** 開發環境優化。

## 🚀 專案架構
- **Server**: 部署於 Railway，連接 PostgreSQL 資料庫。
- **Client**: 於學校電腦使用 NetBeans 執行，透過 Socket 連接雲端 Server。
- **技術棧**: Java 17, JavaFX 21, Maven, PostgreSQL, Gson, jBCrypt.

---

## ☁️ 雲端部署 (Railway)

1. **GitHub 連動**: 將此專案推送到 GitHub。
2. **建立 Service**: 在 Railway 建立一個新的 Web Service 並連結 GitHub 倉庫。
3. **資料庫**: 建立一個 PostgreSQL Instance。
4. **環境變數**: 在 Railway 設定以下變數：
   - `DB_URL`: 資料庫連線字串。
   - `DB_USER`: `postgres`
   - `DB_PASS`: 資料庫密碼。
   - `PORT`: `12345` (或其他指定 Port)。

---

## 💻 學校 Demo 執行 (NetBeans)

1. **開啟專案**: 在學校電腦開啟 NetBeans，選擇 `Open Project` 並選取本資料夾（NetBeans 會自動識別 Maven 專案）。
2. **設定 Server 位址**: 
   - 找到 `src/main/java/chat/client/ChatClient.java`。
   - 修改 `HOST` 與 `PORT` 變數為你的 Railway TCP 位址，或是在 NetBeans 的專案屬性中設定環境變數：
     - `SERVER_HOST`: 你的 Railway 網址。
     - `SERVER_PORT`: 你的 Railway Port。
3. **執行**: 
   - 右鍵點擊專案 -> `Run`。
   - 或者使用 NetBeans 的 Maven 指令：`javafx:run`。

---

## 🛠️ 開發備註
- **代碼簡潔**: 已全面使用 Java 17 特性 (`var`, `record`, `switch expressions`)。
- **打包**: 執行 `mvn package` 會在 `target/` 生成 `ChatServer.jar` (雲端用) 與 `ChatClient.jar` (單機執行用)。

---
