package dev.turtwurty.nvidiahighlightmover;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

public class NvidiaHighlightMover {
    private static final Path CLIPS = Path.of("C:\\Users\\TurtyWurty\\AppData\\Local\\Temp\\Highlights\\Battlefield V");
    private static final Path DEST = Path.of("F:\\Battlefield V");
    private final WatchService watchService;
    
    private final SystemTray tray;
    private final Image icon;
    private final TrayIcon trayIcon;
    private boolean isWindows;

    public NvidiaHighlightMover() throws IOException, AWTException {
        this.watchService = CLIPS.getFileSystem().newWatchService();
        
        CLIPS.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE);
        
        if (SystemTray.isSupported()) {
            this.tray = SystemTray.getSystemTray();
            this.icon = Toolkit.getDefaultToolkit().createImage("");
            this.trayIcon = new TrayIcon(this.icon);
            this.trayIcon.setImageAutoSize(true);
            this.tray.add(this.trayIcon);
        } else {
            this.tray = null;
            this.icon = null;
            this.trayIcon = null;
        }
        
        this.isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public void displayMessage(String title, String description, MessageType type, Runnable onClick) {
        if (this.tray != null) {
            this.trayIcon.displayMessage(title, description, type);
            this.trayIcon.addActionListener(event -> onClick.run());
        }
    }

    public void run() throws InterruptedException {
        WatchKey key;
        while (true) {
            key = this.watchService.poll(10, TimeUnit.SECONDS);
            if (key != null) {
                Thread.sleep(5000);
                key.pollEvents().stream().filter(event -> event.context() instanceof Path)
                    .map(event -> (Path) event.context()).forEach(path -> {
                        if (path.toFile().getName().endsWith(".DVR.mp4")) {
                            try {
                                Files.move(Path.of(CLIPS.toFile().getPath(), path.toFile().getName()),
                                    Path.of(DEST.toFile().getPath(), path.toFile().getName()));
                            } catch (final IOException exception) {
                                exception.printStackTrace();
                                displayMessage("Nvidia Highlight Mover", "There has been an error moving a highlight!",
                                    MessageType.WARNING, () -> {
                                        if (this.isWindows) {
                                            try {
                                                Runtime.getRuntime().exec("explorer.exe /select,"
                                                    + CLIPS.toFile().getPath() + "/" + path.toFile().getName());
                                            } catch (final IOException e2) {
                                                displayMessage("Nvidia Highlight Mover",
                                                    "There has been an error trying to open the highlights folder",
                                                    MessageType.ERROR, () -> {
                                                    });
                                            }
                                        }
                                    });
                            }
                        }
                    });

                key.reset();
            }
            
            if (System.currentTimeMillis() == Long.MAX_VALUE) {
                break;
            }
        }
    }
}
