package chat.db;

import chat.model.MessagePacket;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private final String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/chatroom");
    private final String user = System.getenv().getOrDefault("DB_USER", "postgres");
    private final String pass = System.getenv().getOrDefault("DB_PASS", "password");
    private Connection conn;

    public DatabaseManager() throws SQLException {
        conn = DriverManager.getConnection(url, user, pass);
        try (var st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, username TEXT UNIQUE, password TEXT)");
            st.execute("CREATE TABLE IF NOT EXISTS messages (id SERIAL PRIMARY KEY, sender TEXT, content TEXT, sent_at TIMESTAMP DEFAULT NOW())");
        }
    }

    public boolean authenticate(String u, String p) {
        try (var ps = conn.prepareStatement("SELECT password FROM users WHERE username = ?")) {
            ps.setString(1, u);
            var rs = ps.executeQuery();
            return rs.next() && BCrypt.checkpw(p, rs.getString(1));
        } catch (SQLException e) { return false; }
    }

    public boolean register(String u, String p) {
        try (var ps = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            ps.setString(1, u);
            ps.setString(2, BCrypt.hashpw(p, BCrypt.gensalt()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public void saveMessage(String s, String c) {
        try (var ps = conn.prepareStatement("INSERT INTO messages (sender, content) VALUES (?, ?)")) {
            ps.setString(1, s); ps.setString(2, c);
            ps.executeUpdate();
        } catch (SQLException ignored) { }
    }

    public List<MessagePacket> getRecentMessages(int limit) {
        var list = new ArrayList<MessagePacket>();
        try (var ps = conn.prepareStatement("SELECT sender, content, sent_at FROM messages ORDER BY sent_at DESC LIMIT ?")) {
            ps.setInt(1, limit);
            var rs = ps.executeQuery();
            while (rs.next()) {
                var timestamp = rs.getTimestamp("sent_at").toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                list.add(0, new MessagePacket(MessagePacket.Type.HISTORY, rs.getString("sender"), rs.getString("content"), timestamp, 0));
            }
        } catch (SQLException ignored) { }
        return list;
    }

    public void close() { try { if (conn != null) conn.close(); } catch (SQLException ignored) {} }
}
