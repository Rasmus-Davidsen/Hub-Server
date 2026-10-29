package net.gemcraft.hub.server.console;

import net.gemcraft.hub.Hub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

public class LogManager {
    private static final String ARCHIVE_DATE_FORMAT = "yyyy'-'MM'-'dd";
    private static final String LATEST_FILE_NAME = "latest";
    private static final String FOLDER = "logs";

    private @Nullable BufferedOutputStream out;

    public void createFile() {
        archiveFile();

        final String fileName = LATEST_FILE_NAME + ".log";
        Hub.getLogger().info("Created " + fileName);

        final File logFile = new File(
                getDirectory(), fileName
        );

        try {
            // Create the file if it doesn't exist otherwise clear the previous content
            final OutputStream stream = Files.newOutputStream(
                    logFile.toPath(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            out = new BufferedOutputStream(
                    stream
            );
        } catch(final IOException ex) {
            throw new RuntimeException(
                    "Couldn't create log output stream", ex
            );
        }
    }

    private void archiveFile() {
        final File logFile = getLatestFile();

        if(!logFile.exists()) {
            return;
        }

        final File archiveFile = getArchiveFile();

        final boolean succeeded = archiveFile(
                logFile, archiveFile
        );

        if(succeeded) {
            Hub.getLogger().info(String.format(
                    "Archived %s as %s", logFile.getName(),
                    archiveFile.getName()
            ));
        }
    }

    private @NotNull File getLatestFile() {
        final String fileName = LATEST_FILE_NAME + ".log";

        return new File(
                getDirectory(), fileName
        );
    }

    private @NotNull File getArchiveFile() {
        final SimpleDateFormat dataFormat = new SimpleDateFormat(ARCHIVE_DATE_FORMAT);
        final String date = dataFormat.format(new Date());

        final File dir = getDirectory();

        File file;
        int i = 0;

        do {
            String fileName = date;

            if(i != 0) {
                fileName += "-" + i;
            }

            file = new File(dir, fileName + ".log.gz");
            i++;
        } while(file.exists());

        return file;
    }

    private boolean archiveFile(final @NotNull File file, final @NotNull File archiveFile) {
        try(final FileInputStream in = new FileInputStream(file);
                final FileOutputStream out = new FileOutputStream(archiveFile);
                final GZIPOutputStream gzipOut = new GZIPOutputStream(out))
        {
            final byte[] buf = new byte[1024];

            int len;

            while((len = in.read(buf)) > 0) {
                gzipOut.write(
                        buf, 0, len
                );
            }
        } catch(final IOException ex) {
            Hub.getLogger().warn(String.format(
                    "Couldn't archive %s as %s", file.getName(),
                    archiveFile.getName()
            ));

            return false;
        }

        return true;
    }

    public void writeLn(@NotNull String msg) {
        if(out == null) {
            return;
        }

        msg += '\n';

        try {
            out.write(msg.getBytes());
        } catch(final IOException _) {}
    }

    public void flush() {
        if(out == null) {
            return;
        }

        try {
            out.flush();
        } catch(final IOException ex) {
            Hub.getLogger().warn("Couldn't flush log output: " + ex);
        }
    }

    public void flushAndClose() {
        flush();

        if(out == null) {
            return;
        }

        try {
            out.close();
        } catch(final IOException ex) {
            Hub.getLogger().warn("Couldn't close log output: " + ex);
        }
    }

    private @NotNull File getDirectory() {
        final File folder = new File(FOLDER);

        if(folder.mkdirs()) {
            Hub.getLogger().info("Created logs folder");
        }

        return folder;
    }
}
