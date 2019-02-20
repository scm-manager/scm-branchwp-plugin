//@flow
import React from "react";
import { translate } from "react-i18next";
import type { BranchWPs, BranchWP } from "./BranchWP";
import { Checkbox, Subtitle } from "@scm-manager/ui-components";
import BranchWPTable from "./BranchWPTable";
import AddPermissionFormComponent from "./AddPermissionFormComponent";

type Props = {
  initialConfiguration: BranchWPs,
  readOnly: boolean,
  onConfigurationChange: (BranchWPs, boolean) => void,
  userAutocompleteLink: string,
  groupAutocompleteLink: string,
  // context prop
  t: string => string
};

type State = BranchWPs & {};

class BranchWPsForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { ...props.initialConfiguration };
  }

  isValid() {
    const { permissions } = this.state;
    let valid = true;
    permissions.map(branchWP => {
      valid =
        valid &&
        branchWP.name.trim() !== "" &&
        branchWP.branch.trim() !== "" &&
        branchWP.type.trim() !== "";
    });
    return valid;
  }

  updateBranchWPs(permissions) {
    this.setState({ permissions }, () =>
      this.props.onConfigurationChange(this.state, this.isValid())
    );
  }

  onDelete = deletedBranchWP => {
    const { permissions } = this.state;
    let index = permissions.indexOf(deletedBranchWP);
    permissions.splice(index, 1);
    this.updateBranchWPs(permissions);
  };

  onChange = (changedBranchWP, index) => {
    const { permissions } = this.state;
    permissions[index] = changedBranchWP;
    this.updateBranchWPs(permissions);
  };

  userBranchPermissionAdded = (permission: BranchWP) => {
    this.setState(
      {
        ...this.state,
        permissions: [...this.state.permissions, permission]
      },
      () => {
        this.props.onConfigurationChange(this.state, this.isValid());
      }
    );
  };

  onChangeEnabled = isEnabled => {
    this.setState({ enabled: isEnabled }, () => {
      this.props.onConfigurationChange(this.state, this.isValid());
    });
  };

  renderAddUserFormComponent = () => {
    const { readOnly } = this.props;
    if (this.props.userAutocompleteLink) {
      return (
        <AddPermissionFormComponent
          userAutocompleteLink={this.props.userAutocompleteLink}
          groupAutocompleteLink={this.props.groupAutocompleteLink}
          onAdd={this.userBranchPermissionAdded}
          readOnly={readOnly}
        />
      );
    } else return null;
  };

  render() {
    const { enabled } = this.state;
    const { t } = this.props;

    return (
      <>
        <Checkbox
          checked={enabled}
          onChange={this.onChangeEnabled}
          label={t("scm-branchwp-plugin.is-enabled")}
          helpText={t("scm-branchwp-plugin.is-enabled-help-text")}
        />
        {enabled ? (
          <>
            <hr />
            <Subtitle subtitle={t("scm-branchwp-plugin.form.title")} />
            <BranchWPTable
              permissions={this.state.permissions}
              onDelete={this.onDelete}
            />
            {this.renderAddUserFormComponent()}
          </>
        ) : null}
      </>
    );
  }
}

export default translate("plugins")(BranchWPsForm);
