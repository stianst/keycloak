package org.keycloak.quarkus.runtime;

import org.keycloak.BasicParamConverterProvider;
import org.keycloak.authorization.AuthorizationService;
import org.keycloak.authorization.admin.PolicyResourceService;
import org.keycloak.authorization.admin.PolicyService;
import org.keycloak.authorization.admin.PolicyTypeService;
import org.keycloak.authorization.admin.ResourceServerService;
import org.keycloak.authorization.admin.ResourceSetService;
import org.keycloak.authorization.admin.ScopeService;
import org.keycloak.authorization.protection.ProtectionService;
import org.keycloak.authorization.protection.permission.PermissionTicketService;
import org.keycloak.authorization.protection.policy.UserManagedPermissionService;
import org.keycloak.authorization.protection.resource.ResourceService;
import org.keycloak.broker.oidc.KeycloakOIDCIdentityProvider;
import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.jose.jwe.JWEHeader;
import org.keycloak.jose.jwk.ECPublicJWK;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.RSAPublicJWK;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.json.StringListMapDeserializer;
import org.keycloak.json.StringOrArrayDeserializer;
import org.keycloak.json.StringOrArraySerializer;
import org.keycloak.protocol.oidc.OIDCLoginProtocolService;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpoint;
import org.keycloak.protocol.oidc.endpoints.LoginStatusIframeEndpoint;
import org.keycloak.protocol.oidc.endpoints.LogoutEndpoint;
import org.keycloak.protocol.oidc.endpoints.ThirdPartyCookiesIframeEndpoint;
import org.keycloak.protocol.oidc.endpoints.TokenEndpoint;
import org.keycloak.protocol.oidc.endpoints.UserInfoEndpoint;
import org.keycloak.protocol.oidc.grants.ciba.endpoints.BackchannelAuthenticationCallbackEndpoint;
import org.keycloak.protocol.oidc.grants.ciba.endpoints.CibaRootEndpoint;
import org.keycloak.protocol.oidc.grants.device.endpoints.DeviceEndpoint;
import org.keycloak.protocol.oidc.par.endpoints.ParEndpoint;
import org.keycloak.protocol.oidc.par.endpoints.ParRootEndpoint;
import org.keycloak.protocol.oidc.representations.MTLSEndpointAliases;
import org.keycloak.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.keycloak.quarkus.runtime.benchmark.BenchmarkResource;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.AddressClaimSet;
import org.keycloak.representations.AuthorizationDetailsJSONRepresentation;
import org.keycloak.representations.ClaimsRepresentation;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.LogoutToken;
import org.keycloak.representations.OAuth2DeviceAuthorizationResponse;
import org.keycloak.representations.UserInfo;
import org.keycloak.representations.VersionRepresentation;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.adapters.config.BaseAdapterConfig;
import org.keycloak.representations.adapters.config.BaseRealmConfig;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig;
import org.keycloak.representations.docker.DockerAccess;
import org.keycloak.representations.docker.DockerError;
import org.keycloak.representations.docker.DockerErrorResponseToken;
import org.keycloak.representations.docker.DockerResponse;
import org.keycloak.representations.docker.DockerResponseToken;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.representations.idm.ClientPolicyConditionRepresentation;
import org.keycloak.representations.idm.ClientPolicyExecutorRepresentation;
import org.keycloak.representations.idm.ClientProfilesRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.keycloak.representations.idm.PublishedRealmRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.authorization.Permission;
import org.keycloak.representations.idm.authorization.PermissionRequest;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.oidc.TokenMetadataRepresentation;
import org.keycloak.services.clientregistration.AdapterInstallationClientRegistrationProvider;
import org.keycloak.services.clientregistration.ClientRegistrationService;
import org.keycloak.services.clientregistration.DefaultClientRegistrationProvider;
import org.keycloak.services.clientregistration.oidc.OIDCClientRegistrationProvider;
import org.keycloak.services.resources.AbstractSecuredLocalService;
import org.keycloak.services.resources.ClientsManagementService;
import org.keycloak.services.resources.IdentityBrokerService;
import org.keycloak.services.resources.JsResource;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.services.resources.PublicRealmResource;
import org.keycloak.services.resources.RealmsResource;
import org.keycloak.services.resources.RobotsResource;
import org.keycloak.services.resources.ThemeResource;
import org.keycloak.services.resources.WelcomeResource;
import org.keycloak.services.resources.account.AccountConsole;
import org.keycloak.services.resources.account.AccountCredentialResource;
import org.keycloak.services.resources.account.AccountLoader;
import org.keycloak.services.resources.account.AccountRestService;
import org.keycloak.services.resources.account.CorsPreflightService;
import org.keycloak.services.resources.account.LinkedAccountsResource;
import org.keycloak.services.resources.account.SessionResource;
import org.keycloak.services.resources.account.resources.ResourcesService;
import org.keycloak.services.resources.admin.AdminConsole;
import org.keycloak.services.resources.admin.AdminCorsPreflightService;
import org.keycloak.services.resources.admin.AdminRoot;
import org.keycloak.services.resources.admin.AttackDetectionResource;
import org.keycloak.services.resources.admin.AuthenticationManagementResource;
import org.keycloak.services.resources.admin.ClientAttributeCertificateResource;
import org.keycloak.services.resources.admin.ClientInitialAccessResource;
import org.keycloak.services.resources.admin.ClientProfilesResource;
import org.keycloak.services.resources.admin.ClientRegistrationPolicyResource;
import org.keycloak.services.resources.admin.ClientResource;
import org.keycloak.services.resources.admin.ClientRoleMappingsResource;
import org.keycloak.services.resources.admin.ClientScopeEvaluateResource;
import org.keycloak.services.resources.admin.ClientScopeEvaluateScopeMappingsResource;
import org.keycloak.services.resources.admin.ClientScopeResource;
import org.keycloak.services.resources.admin.ClientScopesResource;
import org.keycloak.services.resources.admin.ClientsResource;
import org.keycloak.services.resources.admin.ComponentResource;
import org.keycloak.services.resources.admin.GroupResource;
import org.keycloak.services.resources.admin.GroupsResource;
import org.keycloak.services.resources.admin.IdentityProviderResource;
import org.keycloak.services.resources.admin.IdentityProvidersResource;
import org.keycloak.services.resources.admin.KeyResource;
import org.keycloak.services.resources.admin.ProtocolMappersResource;
import org.keycloak.services.resources.admin.RealmAdminResource;
import org.keycloak.services.resources.admin.RealmLocalizationResource;
import org.keycloak.services.resources.admin.RealmsAdminResource;
import org.keycloak.services.resources.admin.RoleByIdResource;
import org.keycloak.services.resources.admin.RoleContainerResource;
import org.keycloak.services.resources.admin.RoleMapperResource;
import org.keycloak.services.resources.admin.ScopeMappedClientResource;
import org.keycloak.services.resources.admin.ScopeMappedResource;
import org.keycloak.services.resources.admin.UserProfileResource;
import org.keycloak.services.resources.admin.UserResource;
import org.keycloak.services.resources.admin.UsersResource;
import org.keycloak.services.resources.admin.info.ServerInfoAdminResource;

@io.quarkus.runtime.annotations.RegisterForReflection(targets = {
        AccessToken.class,
        AccessTokenResponse.class,
        AdapterConfig.class,
        AddressClaimSet.class,
        AuthorizationDetailsJSONRepresentation.class,
        BaseAdapterConfig.class,
        BaseRealmConfig.class,
        ClaimsRepresentation.class,
        ClientPolicyConditionConfigurationRepresentation.class,
        ClientPolicyConditionRepresentation.class,
        ClientPolicyExecutorRepresentation.class,
        ClientProfilesRepresentation.class,
        DockerAccess.class,
        DockerError.class,
        DockerErrorResponseToken.class,
        DockerResponse.class,
        DockerResponseToken.class,
        ECPublicJWK.class,
        IDToken.class,
        JSONWebKeySet.class,
        JsonWebToken.class,
        JWEHeader.class,
        JWK.class,
        JWSHeader.class,
        LogoutToken.class,
        MTLSEndpointAliases.class,
        OAuth2DeviceAuthorizationResponse.class,
        OAuth2ErrorRepresentation.class,
        OIDCConfigurationRepresentation.class,
        Permission.class,
        PermissionRequest.class,
        PolicyEnforcerConfig.class,
        PublishedRealmRepresentation.class,
        RealmRepresentation.class,
        ResourceRepresentation.class,
        RSAPublicJWK.class,
        TokenMetadataRepresentation.class,
        UserInfo.class,
        VersionRepresentation.class,
        ErrorRepresentation.class,

        AbstractSecuredLocalService.class,
        AccountConsole.class,
        AccountCredentialResource.class,
        AccountLoader.class,
        AccountRestService.class,
        AdapterInstallationClientRegistrationProvider.class,
        AdminConsole.class,
        AdminCorsPreflightService.class,
        AdminRoot.class,
        AuthenticationManagementResource.class,
        AuthorizationEndpoint.class,
        AuthorizationService.class,
        BackchannelAuthenticationCallbackEndpoint.class,
        CibaRootEndpoint.class,
        ClientAttributeCertificateResource.class,
        ClientInitialAccessResource.class,
        ClientRegistrationPolicyResource.class,
        ClientRegistrationService.class,
        ClientResource.class,
        ClientRoleMappingsResource.class,
        ClientScopeEvaluateResource.class,
        ClientScopeEvaluateScopeMappingsResource.class,
        ClientScopeResource.class,
        ClientScopesResource.class,
        ClientsManagementService.class,
        ClientsResource.class,
        ComponentResource.class,
        CorsPreflightService.class,
        DefaultClientRegistrationProvider.class,
        DeviceEndpoint.class,
        GroupResource.class,
        GroupsResource.class,
        IdentityBrokerService.class,
        IdentityProviderResource.class,
        IdentityProvidersResource.class,
        JsResource.class,
        KeycloakOIDCIdentityProvider.class,
        LinkedAccountsResource.class,
        LoginActionsService.class,
        LoginStatusIframeEndpoint.class,
        LogoutEndpoint.class,
        OIDCClientRegistrationProvider.class,
        OIDCIdentityProvider.class,
        OIDCLoginProtocolService.class,
        ParEndpoint.class,
        ParRootEndpoint.class,
        PermissionTicketService.class,
        PolicyResourceService.class,
        PolicyService.class,
        PolicyTypeService.class,
        ProtectionService.class,
        ProtocolMappersResource.class,
        PublicRealmResource.class,
        RealmAdminResource.class,
        RealmLocalizationResource.class,
        RealmsAdminResource.class,
        RealmsResource.class,
        ResourceServerService.class,
        ResourceService.class,
        ResourceSetService.class,
        ResourcesService.class,
        RobotsResource.class,
        RoleByIdResource.class,
        RoleContainerResource.class,
        RoleMapperResource.class,
        ScopeMappedClientResource.class,
        ScopeMappedResource.class,
        ScopeService.class,
        SessionResource.class,
        ThemeResource.class,
        ThirdPartyCookiesIframeEndpoint.class,
        TokenEndpoint.class,
        UserInfoEndpoint.class,
        UserManagedPermissionService.class,
        UserResource.class,
        UsersResource.class,
        WelcomeResource.class,
        ServerInfoAdminResource.class,
        AdminConsole.WhoAmI.class,
        UserProfileResource.class,
        SessionResource.class,
        KeyResource.class,
        ClientProfilesResource.class,
        AttackDetectionResource.class,

        org.keycloak.admin.ui.rest.AdminExtResource.class,
        org.keycloak.admin.ui.rest.AdminExtProvider.class,
        org.keycloak.admin.ui.rest.AuthenticationManagementResource.class,
        org.keycloak.admin.ui.rest.BruteForceUsersResource.class,
        org.keycloak.admin.ui.rest.EffectiveRoleMappingResource.class,
        org.keycloak.admin.ui.rest.GroupsResource.class,
        org.keycloak.admin.ui.rest.RoleMappingResource.class,
        org.keycloak.admin.ui.rest.SessionsResource.class,

        org.keycloak.email.freemarker.beans.AdminEventBean.class,
        org.keycloak.email.freemarker.beans.EventBean.class,
        org.keycloak.email.freemarker.beans.ProfileBean.class,
        org.keycloak.forms.login.freemarker.model.ClientBean.class,
        org.keycloak.forms.login.freemarker.model.CodeBean.class,
        org.keycloak.forms.login.freemarker.model.FrontChannelLogoutBean.class,
        org.keycloak.forms.login.freemarker.model.LogoutConfirmBean.class,
        org.keycloak.forms.login.freemarker.model.OAuthGrantBean.class,
        org.keycloak.forms.login.freemarker.model.RealmBean.class,
        org.keycloak.forms.login.freemarker.model.RecoveryAuthnCodeInputLoginBean.class,
        org.keycloak.forms.login.freemarker.model.RecoveryAuthnCodesBean.class,
        org.keycloak.forms.login.freemarker.model.TotpLoginBean.class,
        org.keycloak.forms.login.freemarker.model.IdentityProviderBean.class,
        org.keycloak.forms.login.freemarker.model.AbstractUserProfileBean.class,
        org.keycloak.forms.login.freemarker.model.EmailBean.class,
        org.keycloak.forms.login.freemarker.model.IdpReviewProfileBean.class,
        org.keycloak.forms.login.freemarker.model.LoginBean.class,
        org.keycloak.forms.login.freemarker.model.ProfileBean.class,
        org.keycloak.forms.login.freemarker.model.RegisterBean.class,
        org.keycloak.forms.login.freemarker.model.TotpBean.class,
        org.keycloak.forms.login.freemarker.model.VerifyProfileBean.class,
        org.keycloak.forms.login.freemarker.model.X509ConfirmBean.class,
        org.keycloak.forms.login.freemarker.model.AuthenticationContextBean.class,
        org.keycloak.forms.login.freemarker.model.UrlBean.class,
        org.keycloak.theme.beans.LocaleBean.class,
        org.keycloak.theme.beans.MessageBean.class,
        org.keycloak.theme.beans.MessagesPerFieldBean.class,

        org.keycloak.models.credential.dto.OTPSecretData.class,
        org.keycloak.models.credential.dto.PasswordCredentialData.class,
        org.keycloak.models.credential.dto.PasswordSecretData.class,
        org.keycloak.models.credential.dto.RecoveryAuthnCodesCredentialData.class,
        org.keycloak.models.credential.dto.RecoveryAuthnCodesSecretData.class,
        org.keycloak.models.credential.dto.OTPCredentialData.class,
        org.keycloak.models.credential.dto.RecoveryAuthnCodeRepresentation.class,

        org.keycloak.protocol.oidc.rar.model.IntermediaryScopeRepresentation.class,
        org.keycloak.authorization.protection.resource.UmaResourceRepresentation.class,

        BasicParamConverterProvider.class,

        StringOrArraySerializer.class,
        StringListMapDeserializer.class,
        StringOrArrayDeserializer.class,

        BenchmarkResource.class
})
public class RegisterForReflection {
}
