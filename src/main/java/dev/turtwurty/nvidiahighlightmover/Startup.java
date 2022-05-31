package dev.turtwurty.nvidiahighlightmover;

import java.awt.AWTException;
import java.io.IOException;

public class Startup {
    public static void main(String[] args) throws IOException, InterruptedException, AWTException {
        new NvidiaHighlightMover().run();
    }
}
