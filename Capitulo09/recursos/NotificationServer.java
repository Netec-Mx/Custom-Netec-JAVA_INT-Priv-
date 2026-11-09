import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servidor WebSocket de notificaciones para la Práctica 9 (Cliente HTTP asíncrono y comunicación WebSocket).
 *
 * Implementa el protocolo WebSocket (RFC 6455) solo con la biblioteca estándar de Java 11, sin dependencias.
 * Ejecución (lanzamiento de archivo fuente de Java 11):
 *
 *     java NotificationServer.java          (puerto 8090)
 *     java NotificationServer.java 9090     (otro puerto)
 *
 * Endpoint: ws://localhost:8090/ws/notifications
 *  - Al conectarse, el cliente recibe un mensaje de bienvenida.
 *  - Cada 3 segundos envía una notificación a todos los clientes conectados.
 *  - Responde con un eco a cada mensaje de texto recibido.
 *  - Responde a ping con pong y completa el cierre ordenado (close).
 * Detener el servidor con Ctrl+C permite probar la reconexión automática del cliente.
 *
 * Material de capacitación: no usar en producción.
 */
public class NotificationServer {

    private static final String PATH = "/ws/notifications";
    private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final Set<Client> CLIENTS = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8090;

        AtomicInteger counter = new AtomicInteger();
        ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "notificaciones");
            t.setDaemon(true);
            return t;
        });
        ticker.scheduleAtFixedRate(() -> {
            int n = counter.incrementAndGet();
            String msg = "{\"tipo\":\"notificacion\",\"id\":" + n
                    + ",\"mensaje\":\"Pedido " + (1000 + n) + " actualizado\"}";
            for (Client c : CLIENTS) {
                c.sendText(msg);
            }
        }, 3, 3, TimeUnit.SECONDS);

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[Servidor] WebSocket escuchando en ws://localhost:" + port + PATH);
            System.out.println("[Servidor] Presiona Ctrl+C para detener.");
            while (true) {
                Socket socket = server.accept();
                Thread t = new Thread(() -> handle(socket), "cliente-" + socket.getPort());
                t.setDaemon(true);
                t.start();
            }
        }
    }

    private static void handle(Socket socket) {
        Client client = null;
        try (socket) {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            String[] lines = readHttpHeaders(in).split("\r\n");
            String key = null;
            for (String line : lines) {
                int i = line.indexOf(':');
                if (i > 0 && line.substring(0, i).trim().equalsIgnoreCase("Sec-WebSocket-Key")) {
                    key = line.substring(i + 1).trim();
                }
            }
            if (!lines[0].startsWith("GET " + PATH) || key == null) {
                out.write(("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n")
                        .getBytes(StandardCharsets.US_ASCII));
                return;
            }

            String accept = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1")
                    .digest((key + GUID).getBytes(StandardCharsets.US_ASCII)));
            out.write(("HTTP/1.1 101 Switching Protocols\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Sec-WebSocket-Accept: " + accept + "\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
            out.flush();

            client = new Client(out);
            CLIENTS.add(client);
            System.out.println("[Servidor] Cliente conectado: " + socket.getRemoteSocketAddress());
            client.sendText("{\"tipo\":\"bienvenida\",\"mensaje\":\"Conectado al servicio de notificaciones\"}");

            ByteArrayOutputStream message = new ByteArrayOutputStream();
            while (true) {
                int b0 = in.read();
                if (b0 < 0) {
                    break;
                }
                int b1 = readByte(in);
                boolean fin = (b0 & 0x80) != 0;
                int opcode = b0 & 0x0F;
                boolean masked = (b1 & 0x80) != 0;
                long length = b1 & 0x7F;
                if (length == 126) {
                    length = (readByte(in) << 8) | readByte(in);
                } else if (length == 127) {
                    length = 0;
                    for (int i = 0; i < 8; i++) {
                        length = (length << 8) | readByte(in);
                    }
                }
                byte[] mask = new byte[4];
                if (masked) {
                    readFully(in, mask);
                }
                byte[] payload = new byte[(int) length];
                readFully(in, payload);
                if (masked) {
                    for (int i = 0; i < payload.length; i++) {
                        payload[i] ^= mask[i % 4];
                    }
                }

                if (opcode == 0x8) {                 // close
                    client.sendFrame(0x8, payload);
                    System.out.println("[Servidor] El cliente cerró la conexión.");
                    break;
                } else if (opcode == 0x9) {          // ping
                    client.sendFrame(0xA, payload);
                } else if (opcode == 0x1 || opcode == 0x0) {  // texto o continuación
                    message.write(payload);
                    if (fin) {
                        String text = message.toString(StandardCharsets.UTF_8);
                        message.reset();
                        System.out.println("[Servidor] Recibido: " + text);
                        client.sendText("{\"tipo\":\"eco\",\"mensaje\":\""
                                + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Servidor] Conexión terminada: " + e.getMessage());
        } finally {
            if (client != null) {
                CLIENTS.remove(client);
            }
        }
    }

    private static String readHttpHeaders(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int matched = 0;
        byte[] end = {'\r', '\n', '\r', '\n'};
        while (matched < 4) {
            int b = readByte(in);
            buffer.write(b);
            matched = (b == end[matched]) ? matched + 1 : (b == '\r' ? 1 : 0);
            if (buffer.size() > 16_384) {
                throw new IOException("Encabezados demasiado grandes");
            }
        }
        return buffer.toString(StandardCharsets.US_ASCII);
    }

    private static int readByte(InputStream in) throws IOException {
        int b = in.read();
        if (b < 0) {
            throw new EOFException("Fin del flujo");
        }
        return b;
    }

    private static void readFully(InputStream in, byte[] data) throws IOException {
        int offset = 0;
        while (offset < data.length) {
            int n = in.read(data, offset, data.length - offset);
            if (n < 0) {
                throw new EOFException("Fin del flujo");
            }
            offset += n;
        }
    }

    /** Conexión de un cliente; los envíos se sincronizan porque varios hilos escriben en el mismo socket. */
    private static final class Client {
        private final OutputStream out;

        Client(OutputStream out) {
            this.out = out;
        }

        void sendText(String text) {
            sendFrame(0x1, text.getBytes(StandardCharsets.UTF_8));
        }

        synchronized void sendFrame(int opcode, byte[] payload) {
            try {
                out.write(0x80 | opcode);
                if (payload.length < 126) {
                    out.write(payload.length);
                } else if (payload.length < 65_536) {
                    out.write(126);
                    out.write(payload.length >>> 8);
                    out.write(payload.length & 0xFF);
                } else {
                    out.write(127);
                    for (int i = 7; i >= 0; i--) {
                        out.write((int) (((long) payload.length >>> (8 * i)) & 0xFF));
                    }
                }
                out.write(payload);
                out.flush();
            } catch (IOException e) {
                CLIENTS.remove(this);
            }
        }
    }
}
