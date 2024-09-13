package org.keycloak.encoding;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.keycloak.utils.SafePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class GzipResourceEncodingProvider implements ResourceEncodingProvider {

    private static final Logger logger = Logger.getLogger(ResourceEncodingProvider.class);

    private final File cacheDir;

    public GzipResourceEncodingProvider(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public InputStream getEncodedStream(InputStream is, String... path) {
        StringBuilder sb = new StringBuilder();
        sb.append(cacheDir.getAbsolutePath());
        for (String p : path) {
            sb.append(File.separatorChar);
            sb.append(p);
        }
        sb.append(".gz");

        String filePath = sb.toString();

        File encodedFile = SafePath.resolve(cacheDir, new File(filePath), false);
        if (encodedFile == null) {
            return null;
        }

        try {
            if (!encodedFile.exists()) {
                if (!createEncodedFile(is, encodedFile)) {
                    return null;
                }
            }

            return new FileInputStream(encodedFile);
        } catch (Exception e) {
            logger.warn("Failed to encode resource", e);
            return null;
        }
    }

    private boolean createEncodedFile(InputStream is, File encodedFile) throws IOException {
        File parent = encodedFile.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                logger.warnf("Fail to create directory %s", parent);
                return false;
            }
        }
        File tmpEncodedFile = File.createTempFile(
                encodedFile.getName(),
                "tmp",
                parent);

        FileOutputStream fos = new FileOutputStream(tmpEncodedFile);
        GZIPOutputStream gos = new GZIPOutputStream(fos);
        IOUtils.copy(is, gos);
        gos.close();
        is.close();
        try {
            Files.move(
                    tmpEncodedFile.toPath(),
                    encodedFile.toPath(),
                    REPLACE_EXISTING);
        } catch (IOException io) {
            logger.warnf("Fail to move %s  %s", tmpEncodedFile.toString(), io);
            return false;
        }

        return true;
    }

    public String getEncoding() {
        return "gzip";
    }

}
