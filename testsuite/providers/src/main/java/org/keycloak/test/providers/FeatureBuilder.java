package org.keycloak.test.providers;

import org.keycloak.common.Profile;

import java.util.HashMap;
import java.util.Map;

public class FeatureBuilder {

    private TestCloakBuilder parent;
    private HashMap<Profile.Feature, Boolean> features;

    FeatureBuilder(TestCloakBuilder parent) {
        this.parent = parent;
        Profile defaultProfile = Profile.defaults();
        this.features = new HashMap<>();
        this.features.putAll(defaultProfile.getFeatures());
    }

    public TestCloakBuilder enable(Profile.Feature... features) {
        for (Profile.Feature f : features) {
            this.features.put(f, true);
        }
        return parent;
    }

    public TestCloakBuilder disable(Profile.Feature... features) {
        for (Profile.Feature f : features) {
            this.features.put(f, false);
        }
        return parent;
    }

    Map<Profile.Feature, Boolean> getFeatures() {
        return features;
    }

}
