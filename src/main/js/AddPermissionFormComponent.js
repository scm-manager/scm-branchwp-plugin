// @flow
import React from "react";
import {
  Autocomplete,
  Button,
  InputField,
  DropDown
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import type { BranchWP } from "./BranchWP";
import type { SelectValue } from "@scm-manager/ui-types";
import LabelWithHelpIcon from "@scm-manager/ui-components/src/forms/LabelWithHelpIcon";

type Props = {
  userAutocompleteLink: string,
  groupAutocompleteLink: string,
  readOnly: boolean,
  onAdd: BranchWP => void,
  // Context props
  t: string => string
};
type State = {
  branchProtectionPermission: BranchWP,
  selectedValue: SelectValue
};

const defaultState = {
  branchProtectionPermission: {
    name: "",
    type: "ALLOW",
    branch: "",
    group: false
  },
  selectedValue: {
    label: "",
    value: { id: "", displayName: "" }
  }
};

class AddPermissionFormComponent extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = defaultState;
  }

  loadUserSuggestions = (inputValue: string) => {
    return this.loadAutocompletion(this.props.userAutocompleteLink, inputValue);
  };

  loadGroupSuggestions = (inputValue: string) => {
    return this.loadAutocompletion(
      this.props.groupAutocompleteLink,
      inputValue
    );
  };

  loadAutocompletion = (url: string, inputValue: string) => {
    const link = url + "?q=";
    return fetch(link + inputValue)
      .then(response => response.json())
      .then(json => {
        return json.map(element => {
          const label = element.displayName
            ? `${element.displayName} (${element.id})`
            : element.id;
          return {
            value: element,
            label
          };
        });
      });
  };

  handleDropDownChange = (type: string) => {
    this.setState({
      ...this.state,
      branchProtectionPermission: {
        ...this.state.branchProtectionPermission,
        type
      }
    });
  };

  selectName = (selection: SelectValue) => {
    this.setState({
      ...this.state,
      branchProtectionPermission: {
        ...this.state.branchProtectionPermission,
        name: selection.value.id
      },
      selectedValue: selection
    });
  };

  handleBranchExpressionChange = (branch: string) => {
    this.setState({
      ...this.state,
      branchProtectionPermission: {
        ...this.state.branchProtectionPermission,
        branch
      }
    });
  };

  permissionScopeChanged = event => {
    const group = event.target.value === "GROUP_PERMISSION";
    this.setState({
      ...this.state,
      branchProtectionPermission: {
        ...this.state.branchProtectionPermission,
        group
      }
    });
  };

  render() {
    const { t, readOnly } = this.props;
    const { branchProtectionPermission } = this.state;
    const { branch } = branchProtectionPermission;
    return (
      <>
        <h1 class="subtitle">{t("scm-branchwp-plugin.form.add-permission")}</h1>
          <div className="control">
            <LabelWithHelpIcon
              label={t("scm-branchwp-plugin.form.permission-type")}
              helpText={t("scm-branchwp-plugin.form.permission-type-help-text")}
            />
            <label className="radio">
              <input
                type="radio"
                name="permission_scope"
                checked={!this.state.branchProtectionPermission.group}
                value="USER_PERMISSION"
                onChange={this.permissionScopeChanged}
              />
              {t("scm-branchwp-plugin.form.user-permission")}
            </label>
            <label className="radio">
              <input
                type="radio"
                name="permission_scope"
                value="GROUP_PERMISSION"
                checked={this.state.branchProtectionPermission.group}
                onChange={this.permissionScopeChanged}
              />
              {t("scm-branchwp-plugin.form.group-permission")}
            </label>
          </div>
        <InputField
          name={"branch"}
          placeholder={t("scm-branchwp-plugin.form.branch")}
          label={t("scm-branchwp-plugin.form.branch")}
          helpText={t("scm-branchwp-plugin.form.branch-help-text")}
          value={branch}
          onChange={this.handleBranchExpressionChange}
          disabled={readOnly}
        />
        <div class="columns">
          <div class="column is-two-thirds">{this.renderAutocomplete()}</div>

          <div class="column is-one-third">
            <LabelWithHelpIcon
              label={t("scm-branchwp-plugin.form.permission")}
              helpText={t("scm-branchwp-plugin.form.permission-help-text")}
            />
            <DropDown
              options={["ALLOW", "DENY"]}
              optionSelected={this.handleDropDownChange}
              preselectedOption={this.state.branchProtectionPermission.type}
              disabled={readOnly}
            />
          </div>
        </div>
        <Button
          label={t("scm-branchwp-plugin.form.add")}
          disabled={this.props.readOnly}
          action={() => {
            this.props.onAdd(this.state.branchProtectionPermission);
            this.setState({ ...defaultState });
          }}
        />
      </>
    );
  }

  renderAutocomplete = () => {
    const { t } = this.props;
    const group = this.state.branchProtectionPermission.group;
    const label = group
      ? t("scm-branchwp-plugin.form.group-name")
      : t("scm-branchwp-plugin.form.user-name");
    const helpText = group
      ? t("scm-branchwp-plugin.form.group-name-help-text")
      : t("scm-branchwp-plugin.form.user-name-help-text");
    const placeholder = group
      ? t("scm-branchwp-plugin.form.group-name")
      : t("scm-branchwp-plugin.form.user-name");
    const loadSuggestions = group
      ? this.loadGroupSuggestions
      : this.loadUserSuggestions;
    return (
      <Autocomplete
        label={label}
        loadSuggestions={loadSuggestions}
        helpText={helpText}
        valueSelected={this.selectName}
        value={this.state.selectedValue}
        placeholder={placeholder}
      />
    );
  };
}

export default translate("plugins")(AddPermissionFormComponent);
