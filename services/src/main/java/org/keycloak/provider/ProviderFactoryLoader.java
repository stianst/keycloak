package org.keycloak.provider;

import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ProviderFactoryLoader {

    private static final Logger logger = Logger.getLogger(ProviderManager.class);

    private ClassLoader classLoader;

    private Set<String> excludedProviderFactoryClassNames;

    public ProviderFactoryLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.excludedProviderFactoryClassNames = getClassNames("excluded-provider-factories");
    }

    public List<ProviderFactory> loadFactories(Spi spi) {
        List<ProviderFactory> list = new LinkedList<>();

        Set<String> providerFactoryClassNames = getClassNames(spi.getProviderFactoryClass().getName());

        for (String providerFactoryClassName : providerFactoryClassNames) {
            if (!excludedProviderFactoryClassNames.contains(providerFactoryClassName)) {
                ProviderFactory providerFactory = loadProviderFactory(providerFactoryClassName);
                list.add(providerFactory);
            } else {
                logger.debugv("Skipping provider {0}", providerFactoryClassName);
            }
        }

        return list;
    }

    private ProviderFactory loadProviderFactory(String providerFactoryClassName) {
        try {
            return (ProviderFactory) classLoader.loadClass(providerFactoryClassName).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> getClassNames(String filename) {
        Set<String> classNames = new HashSet<>();
        Enumeration<URL> resources = getUrlEnumeration(filename, classLoader);

        while (resources.hasMoreElements()) {
            try (InputStream is = resources.nextElement().openStream()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                for (String l = br.readLine(); l != null; l = br.readLine()) {
                    l = l.trim();
                    if (!l.startsWith("#") && !l.isEmpty()) {
                        classNames.add(l);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return classNames;
    }

    private Enumeration<URL> getUrlEnumeration(String filename, ClassLoader classLoader) {
        try {
            return classLoader.getResources("META-INF/services/" + filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
