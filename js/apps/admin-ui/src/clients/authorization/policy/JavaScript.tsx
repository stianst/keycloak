import { HelpItem } from "@keycloak/keycloak-ui-shared";
import { Checkbox, FormGroup } from "@patternfly/react-core";
import { Controller, useFormContext } from "react-hook-form";
import { useTranslation } from "react-i18next";
import CodeEditor from "../../../components/form/CodeEditor";

export const JavaScript = (props: {
  supportedCapabilities?: string[];
  isScriptingProvider?: boolean;
}) => {
  const { t } = useTranslation();
  const { control } = useFormContext();
  const isVerifiable = props.supportedCapabilities?.includes("verifiable");
  const isRuntimeDeployable =
    props.supportedCapabilities?.includes("runtime_deployment");

  const codeFieldName = props.isScriptingProvider ? "config.code" : "code";

  return (
    <>
      <FormGroup
        label={t("code")}
        labelIcon={
          <HelpItem helpText={t("policyCodeHelp")} fieldLabelId="code" />
        }
        fieldId="code"
        isRequired
      >
        <Controller
          name={codeFieldName}
          defaultValue=""
          control={control}
          render={({ field }) => (
            <CodeEditor
              id="code"
              data-testid="code"
              readOnly={!isRuntimeDeployable}
              value={field.value}
              onChange={(value) => field.onChange(value)}
              language={isRuntimeDeployable ? "plaintext" : "js"}
              height={600}
            />
          )}
        />
      </FormGroup>
      {isVerifiable && (
        <FormGroup
          label={t("validateAgainstSchema")}
          labelIcon={
            <HelpItem
              helpText={t("validateAgainstSchemaHelp")}
              fieldLabelId="validateAgainstSchema"
            />
          }
          fieldId="validateSchema"
        >
          <Controller
            name="config.validateSchema"
            defaultValue="false"
            control={control}
            render={({ field }) => (
              <Checkbox
                id="validateSchema"
                data-testid="validateSchema"
                label={t("validateAgainstSchemaLabel")}
                isChecked={field.value === "true"}
                onChange={(_event, checked) =>
                  field.onChange(checked ? "true" : "false")
                }
              />
            )}
          />
        </FormGroup>
      )}
    </>
  );
};
