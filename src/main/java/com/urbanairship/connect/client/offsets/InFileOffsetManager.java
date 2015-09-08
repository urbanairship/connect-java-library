/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.offsets;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicReference;

public class InFileOffsetManager implements OffsetManager {

    private static final Logger logger = LogManager.getLogger(InFileOffsetManager.class);

    private final File offsetFile;
    private final AtomicReference<String> offset = new AtomicReference<>(null);

    public InFileOffsetManager(String appKey) {
        offsetFile = new File(System.getProperty("user.dir"), "." + appKey + "-offsets");
        final Optional<String> loadedOffset = loadOffset();
        if (loadedOffset.isPresent()) {
            this.offset.set(loadedOffset.get());
        }
    }

    @Override
    public Optional<String> getLastOffset() {
        return Optional.fromNullable(offset.get());
    }

    @Override
    public void update(String offset) {
        this.offset.set(offset);
        try {
            saveOffset();
        } catch (IOException e) {
            throw new RuntimeException("Failed to update offset in file: " + offsetFile.getName(), e);
        }
    }

    private synchronized Optional<String> loadOffset() {
        if (!offsetFile.exists()) {
            logger.debug("No apps file exists: " + offsetFile.getAbsolutePath());
            return Optional.absent();
        }
        try {
            offset.set(Files.toString(offsetFile, Charsets.UTF_8));
        } catch (IOException e) {
            logger.warn("Failed to read file: " + offsetFile.getName(), e);
        }
        return Optional.fromNullable(offset.get());
    }

    private synchronized void saveOffset() throws IOException {
        Writer writer = Files.newWriter(offsetFile, Charsets.UTF_8);
        writer.write(offset.get());
        writer.flush();
        writer.close();
    }



}
