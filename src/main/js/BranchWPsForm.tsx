import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Checkbox, Subtitle } from "@scm-manager/ui-components";
import { BranchWPs, BranchWP } from "./types/BranchWP";
import BranchWPTable from "./table/BranchWPTable";
import AddPermissionFormComponent from "./AddPermissionFormComponent";

type Props = WithTranslation & {
  initialConfiguration: BranchWPs;
  readOnly: boolean;
  onConfigurationChange: (p1: BranchWPs, p2: boolean) => void;
  userAutocompleteLink: string;
  groupAutocompleteLink: string;
};

type State = BranchWPs & {};

class BranchWPsForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  isValid() {
    const { permissions } = this.state;
    let valid = true;
    permissions.map(branchWP => {
      valid = valid && branchWP.name.trim() !== "" && branchWP.branch.trim() !== "" && branchWP.type.trim() !== "";
    });
    return valid;
  }

  updateBranchWPs(permissions) {
    this.setState(
      {
        permissions
      },
      () => this.props.onConfigurationChange(this.state, this.isValid())
    );
  }

  onDelete = deletedBranchWP => {
    const { permissions } = this.state;
    const index = permissions.indexOf(deletedBranchWP);
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
    this.setState(
      {
        enabled: isEnabled
      },
      () => {
        this.props.onConfigurationChange(this.state, this.isValid());
      }
    );
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
          label={t("scm-branchwp-plugin.enable")}
          helpText={t("scm-branchwp-plugin.enableHelpText")}
        />
        {enabled ? (
          <>
            <hr />
            <Subtitle subtitle={t("scm-branchwp-plugin.editSubtitle")} />
            <BranchWPTable permissions={this.state.permissions} onDelete={this.onDelete} />
            {this.renderAddUserFormComponent()}
          </>
        ) : null}
      </>
    );
  }
}

export default withTranslation("plugins")(BranchWPsForm);