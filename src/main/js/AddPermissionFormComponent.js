// @flow
import React from "react";
import { translate } from "react-i18next";
import type { SelectValue } from "@scm-manager/ui-types";
import {
  Button,
  InputField,
  Radio,
  DropDown,
  Subtitle,
  LabelWithHelpIcon,
  GroupAutocomplete,
  UserAutocomplete
} from "@scm-manager/ui-components";
import type { BranchWP } from "./types/BranchWP";

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
      },
      selectedValue: null
    });
  };

  render() {
    const { t, readOnly } = this.props;
    const { branchProtectionPermission } = this.state;
    const { branch } = branchProtectionPermission;
    return (
      <>
        <hr />
        <Subtitle subtitle={t("scm-branchwp-plugin.addSubtitle")} />
        <div className="columns is-multiline">
          <div className="column is-full">
            <label className="label">
              {t("scm-branchwp-plugin.form.permissionType")}
            </label>
            <div className="field is-grouped">
              <div className="control">
                <Radio
                  label={t("scm-branchwp-plugin.form.userPermission")}
                  name="permission_scope"
                  value="USER_PERMISSION"
                  checked={!this.state.branchProtectionPermission.group}
                  onChange={this.permissionScopeChanged}
                />
                <Radio
                  label={t("scm-branchwp-plugin.form.groupPermission")}
                  name="permission_scope"
                  value="GROUP_PERMISSION"
                  checked={this.state.branchProtectionPermission.group}
                  onChange={this.permissionScopeChanged}
                />
              </div>
            </div>
          </div>
          <div className="column is-full">
            <InputField
              name={"branch"}
              placeholder={t("scm-branchwp-plugin.form.branch")}
              label={t("scm-branchwp-plugin.form.branch")}
              helpText={t("scm-branchwp-plugin.form.branchHelpText")}
              value={branch}
              onChange={this.handleBranchExpressionChange}
              disabled={readOnly}
            />
          </div>
          <div className="column is-three-fifths">
            {this.renderAutocomplete()}
          </div>
          <div className="column is-two-fifths">
            <div className="columns">
              <div className="column is-one-third">
                <LabelWithHelpIcon
                  label={t("scm-branchwp-plugin.form.permission")}
                  helpText={t("scm-branchwp-plugin.form.permissionHelpText")}
                />
                <DropDown
                  options={["ALLOW", "DENY"]}
                  optionSelected={this.handleDropDownChange}
                  preselectedOption={this.state.branchProtectionPermission.type}
                  disabled={readOnly}
                />
              </div>
              <div className="column">
                <Button
                  label={t("scm-branchwp-plugin.form.add")}
                  disabled={
                    this.props.readOnly ||
                    !branch ||
                    !(
                      this.state.selectedValue && this.state.selectedValue.label
                    )
                  }
                  action={() => {
                    this.props.onAdd(this.state.branchProtectionPermission);
                    this.setState({ ...defaultState });
                  }}
                  className="label-icon-spacing"
                />
              </div>
            </div>
          </div>
        </div>
      </>
    );
  }

  renderAutocomplete = () => {
    const group = this.state.pathProtectionPermission.group;
    if (group) {
      return (
        <GroupAutocomplete
          groupAutocompleteLink={this.props.groupAutocompleteLink}
          valueSelected={this.selectName}
          value={this.state.selectedValue}
        />
      );
    }
    return (
      <UserAutocomplete
        userAutocompleteLink={this.props.userAutocompleteLink}
        valueSelected={this.selectName}
        value={this.state.selectedValue}
      />
    );
  };
}

export default translate("plugins")(AddPermissionFormComponent);
