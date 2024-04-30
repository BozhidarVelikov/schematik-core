package org.schematik.util.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

public class FileResourceUtil {
    static Logger logger = LoggerFactory.getLogger(FileResourceUtil.class);

    public static InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = FileResourceUtil.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }

    public static File getFileFromResource(String fileName) {
        ClassLoader classLoader = FileResourceUtil.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            logger.error("File not found: " + fileName);
            return null;
        }
        // failed if files have whitespaces or special characters
        // return new File(resource.getFile());

        try {
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String readFile(File file) {
        InputStream inputStream = null;
        String content;
        try {
            inputStream = new FileInputStream(file);
            content = readFromInputStream(inputStream);
        } catch (IOException e) {
            logger.error(
                    String.format("Error while reading file %s", file.getName()),
                    e
            );
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error(
                            String.format("Error while closing file input stream for file %s", file.getName()),
                            e
                    );
                }
            }
        }

        return content;
    }

    private static String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
