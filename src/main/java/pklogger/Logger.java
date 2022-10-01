package pklogger;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.TextFormat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Logger extends Thread {

    private File logFile;
    private final String logPath;
    private boolean shutdown;
    private boolean isShutdown;
    private final ConcurrentLinkedQueue<String> logBuffer = new ConcurrentLinkedQueue<>();

    public Logger(String logFile) {
        logPath = logFile;
        initialize();
        start();
    }

    public void shutdown() {
        synchronized (this) {
            shutdown = true;
            interrupt();
            while (!isShutdown) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public void print(boolean send, String player, DataPacket pk) {
        logBuffer.add(new SimpleDateFormat("y-M-d HH:mm:ss [").format(new Date()) + (send ? "Send]" : "Receive]") + " [" + player + "] " + pk.toString());
    }

    @Override
    public void run() {
        do {
            waitForMessage();
            flushBuffer(logFile);
        } while (!shutdown);
        flushBuffer(logFile);
        synchronized (this) {
            isShutdown = true;
            notify();
        }
    }

    private void initialize() {
        logFile = new File(logPath);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException ignored) {}
        }
    }

    private void waitForMessage() {
        while (logBuffer.isEmpty()) {
            try {
                synchronized (this) {
                    wait(25000);
                }
                Thread.sleep(5);
            } catch (InterruptedException ignore) {}
        }
    }

    private void flushBuffer(File logFile) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), StandardCharsets.UTF_8), 1024)) {
            while (!logBuffer.isEmpty()) {
                String message = logBuffer.poll();
                if (message != null) {
                    writer.write(TextFormat.clean(message));
                    writer.write("\r\n");
                }
            }
            writer.flush();
        } catch (Exception ignored) {
        }
    }
}
