package chat.model;

import com.google.gson.Gson;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public record MessagePacket(
    Type type,
    String sender,
    String content,
    String timestamp,
    int onlineCount
) {
    public enum Type {
        LOGIN, REGISTER, AUTH_OK, AUTH_FAIL,
        CHAT, SYSTEM, HISTORY, ONLINE
    }

    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("HH:mm");

    public MessagePacket(Type type, String sender, String content) {
        this(type, sender, content, now(), 0);
    }

    public static String now() { return LocalDateTime.now().format(DF); }
    public String toJson() { return GSON.toJson(this); }
    public static MessagePacket fromJson(String j) { return GSON.fromJson(j, MessagePacket.class); }

    public MessagePacket withOnlineCount(int count) {
        return new MessagePacket(type, sender, content, timestamp, count);
    }
}
