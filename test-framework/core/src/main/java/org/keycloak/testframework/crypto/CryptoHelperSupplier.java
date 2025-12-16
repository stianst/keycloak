package org.keycloak.testframework.crypto;

import java.util.List;

import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.crypto.FipsMode;
import org.keycloak.crypto.def.DefaultCryptoProvider;
import org.keycloak.testframework.annotations.InjectCryptoHelper;
import org.keycloak.testframework.config.Config;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;

public class CryptoHelperSupplier implements Supplier<CryptoHelper, InjectCryptoHelper> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<CryptoHelper, InjectCryptoHelper> instanceContext) {
        return DependenciesBuilder.none();
    }

    @Override
    public CryptoHelper getValue(InstanceContext<CryptoHelper, InjectCryptoHelper> instanceContext) {
        if (!CryptoIntegration.isInitialised()) {
            CryptoIntegration.setProvider(new DefaultCryptoProvider());
        }
        FipsMode fips = Config.getValueTypeConfig(CryptoHelper.class, "fips", FipsMode.DISABLED.name(), FipsMode.class);
        return new CryptoHelper(fips);
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    @Override
    public boolean compatible(InstanceContext<CryptoHelper, InjectCryptoHelper> a, RequestedInstance<CryptoHelper, InjectCryptoHelper> b) {
        return true;
    }

}
