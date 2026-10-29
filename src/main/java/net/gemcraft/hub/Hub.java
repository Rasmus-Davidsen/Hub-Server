package net.gemcraft.hub;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import net.gemcraft.hub.server.Server;
import net.gemcraft.hub.server.config.Config;
import net.gemcraft.hub.server.console.Console;
import net.gemcraft.hub.server.console.LogManager;
import net.gemcraft.hub.server.console.Logger;
import net.gemcraft.hub.utils.DateUtils;
import net.gemcraft.hub.world.event.EventManager;
import net.gemcraft.hub.world.event.handler.PlayerJoinHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Hub {
    private static final @NotNull LogManager LOG_MANAGER = new LogManager();
    private static final @NotNull Logger LOGGER = new Logger();
    private static final @NotNull Console CONSOLE = new Console();

    private static @Nullable Hub instance;
    private static boolean isRunning;

    private final @NotNull EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    private final @NotNull EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    private final @NotNull Set<@NotNull ScheduledFuture<?>> tasks = new HashSet<>();
    private final @NotNull Thread shutdownHook;
    private final @NotNull Server server;

    public static void main() {
        final Date date = DateUtils.getDate();

        if(instance != null) {
            return;
        }

        LOG_MANAGER.createFile();

        try {
            new Hub();
        } catch(final Exception ex) {
            LOGGER.warn("Unable to proceed: " + ex);
            instance.shutdown();
            return;
        }

        CONSOLE.initInputHandler();
        logStartupDone(date);
        LOG_MANAGER.flush();
    }

    private Hub() throws Exception {
        instance = this;

        LOGGER.info("Starting server on " +  Config.ADDRESS);
        shutdownHook = createShutdownHook();

        server = new Server();
        registerEvents();

        isRunning = true;
    }

    private void registerEvents() {
        LOGGER.info("Registering events");

        // PlayerJoinEvent
        EventManager.register(
                new PlayerJoinHandler()
        );
    }

    private static void logStartupDone(final @NotNull Date initDate) {
        final DecimalFormat format = new DecimalFormat("0.###");
        final long msUsed = DateUtils.msSince(initDate);

        final String formattedTime = format.format(
                msUsed / 1_000.0d
        );

        LOGGER.info(String.format(
                "Startup is done (%ss)", formattedTime
        ));
    }

    public static @NotNull Hub getInstance() {
        return Objects.requireNonNull(
                instance, "Instance was used before being initialized"
        );
    }

    public @NotNull Server getServer() {
        return server;
    }

    public @NotNull EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public @NotNull EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public void createDelayedTask(final @NotNull Runnable runnable, final int delay, final @NotNull TimeUnit timeUnit) {
        if(workerGroup.isShutdown()) {
            return;
        }

        final ScheduledFuture<?> task = workerGroup.schedule(
                runnable, delay, timeUnit
        );

        tasks.add(task);
    }

    public @Nullable ScheduledFuture<?> createRepeatingTask(final @NotNull Runnable runnable, final int delay, final @NotNull TimeUnit timeUnit) {
        if(workerGroup.isShutdown()) {
            return null;
        }

        final ScheduledFuture<?> task = workerGroup.scheduleAtFixedRate(
                runnable, 0, delay, timeUnit
        );

        tasks.add(task);

        return task;
    }

    private @NotNull Thread createShutdownHook() {
        final Thread shutdownHook = new Thread(
                this::shutdown
        );

        Runtime.getRuntime().addShutdownHook(
                shutdownHook
        );

        LOGGER.info("Created shutdown hook");

        return shutdownHook;
    }

    public void shutdown() {
        // First priority is to prevent multiple shutdowns
        removeShutdownHook();

        LOGGER.info("Shutting down...");

        // Disconnect players
        if(isRunning) {
            isRunning = false;

            LOGGER.info("Disconnecting all players");

            server.disconnectAll(
                    "Hub has been closed"
            );
        }

        cancelTasks();
        shutdownThreadGroups();

        LOGGER.info("Shutdown is done");

        // Save and close console/log
        LOG_MANAGER.flushAndClose();
        CONSOLE.close();

        System.exit(0);
    }

    private void removeShutdownHook() {
        try {
            final Runtime runtime = Runtime.getRuntime();

            runtime.removeShutdownHook(
                    shutdownHook
            );
        } catch(final IllegalStateException _) {}
    }

    private void cancelTasks() {
        LOGGER.info("Cancelling scheduled tasks");

        for(final ScheduledFuture<?> task : tasks) {
            task.cancel(true);
        }
    }

    private void shutdownThreadGroups() {
        LOGGER.info("Closing thread groups");

        // Boss group
        bossGroup.shutdownGracefully(
                0, 250, TimeUnit.MILLISECONDS
        );

        // Worker group
        workerGroup.shutdownGracefully(
                0, 250, TimeUnit.MILLISECONDS
        );
    }

    public static @NotNull Console getConsole() {
        return CONSOLE;
    }

    public static @NotNull Logger getLogger() {
        return LOGGER;
    }

    public static @NotNull LogManager getLogManager() {
        return LOG_MANAGER;
    }

    public static boolean isRunning() {
        return isRunning;
    }
}