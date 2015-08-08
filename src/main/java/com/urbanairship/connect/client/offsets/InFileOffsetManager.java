package com.urbanairship.connect.client.offsets;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class InFileOffsetManager implements OffsetManager {

    private static final Logger logger = LogManager.getLogger(InFileOffsetManager.class);

    private final File offsetFile;
    private final AtomicReference<Long> offset = new AtomicReference<>(null);

    private static InFileOffsetManager instance;

    private InFileOffsetManager(String appName) {
        offsetFile = new File(System.getProperty("user.dir"), "." + appName + "offsets");
        if (loadOffset().isPresent()) {
            this.offset.set(loadOffset().get());
        }
    }

    public static InFileOffsetManager getInstance(String appName) {
        if (instance == null) {
            instance = new InFileOffsetManager(appName);
        }

        return instance;
    }

    @Override
    public Optional<Long> getLastOffset() {
        return Optional.ofNullable(offset.get());
    }

    @Override
    public void update(Long offset) {
        this.offset.set(offset);
        try {
            saveOffset();
        } catch (IOException e) {
            logger.warn("Failed to update offset in file: " + offsetFile.getName(), e);
        }
    }

    private synchronized Optional<Long> loadOffset() {
        if (!offsetFile.exists()) {
            logger.debug("No apps file exists: " + offsetFile.getAbsolutePath());
            return Optional.empty();
        }
        try {
            offset.set(Long.parseLong(Files.toString(offsetFile, Charsets.UTF_8)));
        } catch (IOException e) {
            logger.warn("Failed to read file: " + offsetFile.getName(), e);
        }
        return Optional.ofNullable(offset.get());
    }

    private synchronized void saveOffset() throws IOException {
        Writer writer = Files.newWriter(offsetFile, Charsets.UTF_8);
        writer.write(offset.get().byteValue());
        writer.flush();
        writer.close();
    }



}
