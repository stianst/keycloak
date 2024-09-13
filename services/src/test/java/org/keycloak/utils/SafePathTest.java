package org.keycloak.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class SafePathTest {

    static String NONE = "../";
    static String SINGLE = "%2E%2E%2F";
    static String DOUBLE = "%252E%252E%252F";

    @Test
    public void testResources() {
        URL parent = SafePathTest.class.getClassLoader().getResource("dummy-resources/parent");

        resolve(parent, "dummy-resources/parent/myresource.css", true, true);
        resolve(parent, "dummy-resources/parent/" + NONE + "myresource.css", false, true);
        resolve(parent, "dummy-resources/parent/" + SINGLE + "myresource.css", false, false);
        resolve(parent, "dummy-resources/parent/" + DOUBLE + "myresource.css", false, false);
    }

    @Test
    public void testFiles() throws IOException {
        Path tempDirectory = Files.createTempDirectory("safepath-test");

        File parent = new File(tempDirectory.toFile(), "resources");
        Assert.assertTrue(parent.mkdir());

        new FileOutputStream(new File(tempDirectory.toFile(), "myresource.css")).close();
        new FileOutputStream(new File(parent, "myresource.css")).close();

        resolve(parent, "myresource.css", true, true);
        resolve(parent, NONE + "myresource.css", false, true);
        resolve(parent, SINGLE + "myresource.css", false, false);
        resolve(parent, DOUBLE + "myresource.css", false, false);
    }

    public void resolve(URL parent, String resource, boolean expectValid, boolean expectResourceToExist) {
        URL resourceUrl = SafePathTest.class.getClassLoader().getResource(resource);
        URL verified = SafePath.resolve(parent, resourceUrl);
        if (expectValid) {
            Assert.assertNotNull(verified);
        } else {
            Assert.assertNull(verified);
        }

        if (expectResourceToExist) {
            Assert.assertNotNull(resourceUrl);
        }
    }

    public void resolve(File parent, String resource, boolean expectValid, boolean expectFileToExist) {
        File resourceFile = new File(parent, resource);
        File verified = SafePath.resolve(parent, resourceFile);
        if (expectValid) {
            Assert.assertNotNull(verified);
        } else {
            Assert.assertNull(verified);
        }

        if (expectFileToExist) {
            Assert.assertTrue(resourceFile.isFile());
        }
    }

}
