package net.gemcraft.hub.server.console;

import net.gemcraft.hub.Hub;
import net.gemcraft.hub.utils.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Console {
    private static final @NotNull String ANSI_CLEAR_LINE = "[2K";
    private static final @NotNull String INPUT_PREFIX = "> ";

    private final @NotNull Terminal terminal;
    private final @NotNull LineReader reader;
    private final @NotNull Thread inputThread;
    private final @NotNull Thread outputThread;
    private final @NotNull BlockingQueue<String> outputQueue = new LinkedBlockingQueue<>();

    public Console() {
        terminal = createTerminal();

        reader = LineReaderBuilder
                .builder()
                .terminal(terminal)
                .build();

        inputThread = new Thread(
                this::inputListener
        );

        outputThread = createOutputThread();
    }

    private @NotNull Terminal createTerminal() {
        try {
            return TerminalBuilder
                    .builder()
                    .system(true)
                    .build();
        } catch(final IOException ex) {
            throw new RuntimeException(
                    "Couldn't build terminal", ex
            );
        }
    }

    private @NotNull Thread createOutputThread() {
        final Thread thread = new Thread(
                this::printQueue
        );

        thread.setDaemon(true);
        thread.start();

        return thread;
    }

    public void println(final @NotNull String msg) {
        outputQueue.add(
                msg
        );
    }

    private void printQueue() {
        try {
            String msg;

            while(!Thread.currentThread().isInterrupted()) {
                msg = outputQueue.take();

                if(!Hub.isRunning()) {
                    System.out.println(
                            msg
                    );

                    continue;
                }

                reader.printAbove(
                        msg
                );
            }
        } catch(final InterruptedException _) {}
    }

    public void initInputHandler() {
        if(inputThread.isAlive()) {
            return;
        }

        inputThread.setDaemon(true);
        inputThread.start();
    }

    private void inputListener() {
        while(!Thread.currentThread().isInterrupted() && Hub.isRunning()) {
            try {
                final String input = reader.readLine(
                        INPUT_PREFIX
                );

                if(input.isBlank()) {
                    preventNewLine();
                    continue;
                }

                Hub.getLogManager().writeLn(
                        INPUT_PREFIX + input
                );

                handleInput(
                        input
                );
            } catch(final UserInterruptException | EndOfFileException ex) {
                break;
            }
        }
    }

    private void preventNewLine() {
        terminal.puts(
                InfoCmp.Capability.cursor_up
        );

        terminal.writer().print(
                TextUtils.ANSI_ESC + ANSI_CLEAR_LINE
        );

        terminal.flush();
    }

    private void handleInput(final @NotNull String input) {
        switch(input.toLowerCase()) {
            case "stop", "shutdown" -> Hub.getInstance().shutdown();
            default -> Hub.getLogger().info(
                    "Unknown command"
            );
        }
    }

    public void close() {
        inputThread.interrupt();
        outputThread.interrupt();
    }
}
