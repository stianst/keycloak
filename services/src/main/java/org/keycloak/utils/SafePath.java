package org.keycloak.utils;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

public class SafePath {

    public static URL resolve(URL parent, URL resource) {
        if (parent == null || resource == null) {
            return null;
        }
        if (!resource.getPath().startsWith(parent.getPath())) {
            return null;
        }
        return resource;
    }

    public static File resolve(File parent, File file) {
        return resolve(parent, file, true);
    }

    public static File resolve(File parent, File file, boolean checkFileExists) {
        if (parent == null || file == null) {
            return null;
        }
        Path path = file.toPath().normalize();
        if (!path.startsWith(parent.toPath())) {
            return null;
        }
        file = path.toFile();
        if (checkFileExists) {
            file = file.isFile() ? file : null;
        }
        return file;
    }


}
