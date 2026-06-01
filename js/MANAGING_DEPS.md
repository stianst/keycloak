Find vulnerable dependencies:

```shell
pnpm audit -P --json | jq '[ .advisories[] | { module_name: .module_name, patched_versions: .patched_versions, severity: .severity, findings: ( [ .findings[] | { version: .version, paths: .paths } ] ) } ]'
```

If `patched_versions` is `null` it means no fix is available.

From the paths you can identify which are direct dependencies, and which are transitive dependencies.

For example, `apps__create-keycloak-theme>simple-git` means `simple-git` is a direct dependency of `apps/create-keycloak-theme`, while `apps__account-ui>@patternfly/react-table>lodash` means `loadash` is a transitive dependency as a result of `apps/account-ui` having a dependency `@patternfly`.

When patching dependencies start with direct dependencies, then follow with transitive dependencies one by one.

## Fixing direct dependencies

When patching direct dependencies use `pnpm update <module name>` in the paths identified above.

For example, if the identified path is `apps__create-keycloak-theme>simple-git` do the following:

```shell
cd apps/create-keycloak-theme
pnpm update simple-git
```

## Fixing transitive dependencies

Patching indirect dependencies can be done by adding overrides in `pnpm-workspace.yaml`, which can be done using `pnpm audit -P --interactive` and selecting the dependencies to fix.

After updating `pnpm-workspace.yaml` either manually or through `pnpm audit` you need to run `pnpm install`.