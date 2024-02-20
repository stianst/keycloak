Start Keycloak:
```
export KEYCLOAK_ADMIN=admin
export KEYCLOAK_ADMIN_PASSWORD=admin

./kc.sh start-dev --features=organization
```

Login with kcadm:
```
./kcadm.sh config credentials --server http://localhost:8080 --user admin --password admin --realm master
```

Create an organization:
```
./kcadm.sh create organizations -s name=myorg
```

List organizations:
```
./kcadm.sh get organizations
```

Get admin user id:
```
export ADMIN_ID=$(./kcadm.sh get users -q username=admin | jq -r .[0].id)
```

Add admin to organization:
```
./kcadm.sh update organizations/myorg/users/$ADMIN_ID
```

List members of organization:
```
./kcadm.sh get organizations/myorg/users
```

Get admin-cli client id:
```
export ADMIN_CLI_ID=$(./kcadm.sh get clients -q clientId=admin-cli | jq -r .[0].id)
```

Add organization protocol mapper to admin-cli:
```
./kcadm.sh create clients/$ADMIN_CLI_ID/protocol-mappers/models -f - << EOF
{
  "protocol": "openid-connect",
  "protocolMapper": "oidc-organization-membership-mapper",
  "name": "organization",
  "config": {
    "claim.name": "organization",
    "id.token.claim": "true",
    "access.token.claim": "true",
    "lightweight.claim": "false",
    "userinfo.token.claim": "true",
    "introspection.token.claim": "true"
  }
}
EOF
```

Download `https://github.com/stianst/keycloak-oidc-cli`

Create a context for the `admin-cli` client:
```
kc-oidc config set --context=admin-cli --issuer=http://localhost:8080/realms/master --flow=resource-owner --client-id=admin-cli --user=admin --user-password=admin
```

Get an access token and decode it to see the organization claim:
```
kc-oidc token --decode --context=admin-cli
```