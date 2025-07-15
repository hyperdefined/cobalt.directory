package lol.hyper.cobaltdirectory.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUtil {

    private static final Logger logger = LogManager.getLogger(FileUtil.class);

    /**
     * Write contents of an object to a file.
     *
     * @param content The content to write.
     * @param file    The file to write to.
     */
    public static void writeFile(Object content, File file) {
        logger.info("Writing to file {}", file.getAbsolutePath());
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content.toString());
            writer.close();
        } catch (IOException exception) {
            logger.error("Unable to write file {}", file, exception);
        }
    }

    /**
     * Read contents of a file.
     *
     * @param file The file to read.
     * @return The contents. NULL if something went wrong.
     */
    public static String readFile(File file) {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(file.toPath());
        } catch (IOException exception) {
            logger.error("Unable to read file {}", file, exception);
            return null;
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    /**
     * Read the "raw" files. Mainly for instances, removed_instances, and test_urls.
     *
     * @param file The file to read.
     * @return The contents. If the size is zero, it was not able to read it.
     */
    public static List<String> readRawFile(File file) {
        List<String> contents = new ArrayList<>();
        try (LineIterator it = FileUtils.lineIterator(file, "UTF-8")) {
            while (it.hasNext()) {
                String line = it.next();
                // skip lines that have comment/whitespace
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                contents.add(line);

            }
        } catch (IOException exception) {
            logger.error("Unable to read contents of instances file!", exception);
            return Collections.emptyList();
        }
        return contents;
    }
}
