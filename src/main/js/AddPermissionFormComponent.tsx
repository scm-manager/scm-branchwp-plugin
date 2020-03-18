/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { SelectValue } from "@scm-manager/ui-types";
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
import { BranchWP } from "./types/BranchWP";

type Props = WithTranslation & {
  userAutocompleteLink: string;
  groupAutocompleteLink: string;
  readOnly: boolean;
  onAdd: (p: BranchWP) => void;
};

type State = {
  branchProtectionPermission: BranchWP;
  selectedValue?: SelectValue;
};

const defaultState = {
  branchProtectionPermission: {
    name: "",
    type: "ALLOW",
    branch: "",
    group: false
  },
  selectedValue: undefined
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

  changeToUserPermissionScope = (value: boolean) => {
    if (value) {
      this.permissionScopeChanged(false);
    }
  }

  changeToGroupPermissionScope = (value: boolean) => {
    if (value) {
      this.permissionScopeChanged(true);
    }
  }

  permissionScopeChanged = (group: boolean) => {
    this.setState({
      ...this.state,
      branchProtectionPermission: {
        ...this.state.branchProtectionPermission,
        group
      },
      selectedValue: undefined
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
            <label className="label">{t("scm-branchwp-plugin.form.permissionType")}</label>
            <div className="field is-grouped">
              <div className="control">
                <Radio
                  label={t("scm-branchwp-plugin.form.userPermission")}
                  name="permission_scope"
                  value="USER_PERMISSION"
                  checked={!this.state.branchProtectionPermission.group}
                  onChange={this.changeToUserPermissionScope}
                />
                <Radio
                  label={t("scm-branchwp-plugin.form.groupPermission")}
                  name="permission_scope"
                  value="GROUP_PERMISSION"
                  checked={this.state.branchProtectionPermission.group}
                  onChange={this.changeToGroupPermissionScope}
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
          <div className="column">{this.renderAutocomplete()}</div>
          <div className="column">
            <div className="columns">
              <div className="column">
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
                    this.props.readOnly || !branch || !(this.state.selectedValue && this.state.selectedValue.label)
                  }
                  action={() => {
                    this.props.onAdd(this.state.branchProtectionPermission);
                    this.setState({
                      ...defaultState
                    });
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
    const group = this.state.branchProtectionPermission.group;
    if (group) {
      return (
        <GroupAutocomplete
          autocompleteLink={this.props.groupAutocompleteLink}
          valueSelected={this.selectName}
          value={this.state.selectedValue ? this.state.selectedValue : ""}
        />
      );
    }
    return (
      <UserAutocomplete
        autocompleteLink={this.props.userAutocompleteLink}
        valueSelected={this.selectName}
        value={this.state.selectedValue ? this.state.selectedValue : ""}
      />
    );
  };
}

export default withTranslation("plugins")(AddPermissionFormComponent);
