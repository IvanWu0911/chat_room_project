package chat.server;

import chat.db.DatabaseManager;
import chat.model.MessagePacket;
import chat.model.MessagePacket.Type;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "12345"));
    private final ConcurrentHashMap<String, Handler> clients = new ConcurrentHashMap<>();
    private DatabaseManager db;

    public static void main(String[] args) { new ChatServer().start(); }

    public void start() {
        try {
            db = new DatabaseManager();
            try (var ss = new ServerSocket(PORT)) {
                System.out.println("[Server] Running on port " + PORT);
                while (true) new Thread(new Handler(ss.accept())).start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    class Handler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private String user;

        Handler(Socket s) { this.socket = s; }

        @Override
        public void run() {
            try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                String line;
                while ((line = in.readLine()) != null) handle(MessagePacket.fromJson(line));
            } catch (IOException ignored) {
            } finally {
                if (user != null) {
                    clients.remove(user);
                    broadcast(new MessagePacket(Type.SYSTEM, "System", user + " left").withOnlineCount(clients.size()));
                }
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void handle(MessagePacket p) {
            switch (p.type()) {
                case LOGIN, REGISTER -> {
                    boolean ok = (p.type() == Type.LOGIN) ? db.authenticate(p.sender(), p.content()) : db.register(p.sender(), p.content());
                    if (ok) {
                        user = p.sender();
                        clients.put(user, this);
                        send(new MessagePacket(Type.AUTH_OK, "System", "Welcome"));
                        db.getRecentMessages(50).forEach(this::send);
                        send(new MessagePacket(Type.SYSTEM, "System", "--- History Messages Above ---"));
                        broadcast(new MessagePacket(Type.SYSTEM, "System", user + " joined").withOnlineCount(clients.size()));
                    } else send(new MessagePacket(Type.AUTH_FAIL, "System", "Auth failed"));
                }
                case CHAT -> {
                    if (user != null) {
                        broadcast(new MessagePacket(Type.CHAT, user, p.content()));
                        db.saveMessage(user, p.content());
                    }
                }
            }
        }
        void send(MessagePacket p) { out.println(p.toJson()); }
    }

    private void broadcast(MessagePacket p) { clients.values().forEach(h -> h.send(p)); }
}
