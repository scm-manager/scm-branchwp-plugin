//@flow
import React from "react";
import {translate} from "react-i18next";
import type {BranchWPs} from "./BranchWP";
import {Button, Checkbox} from "@scm-manager/ui-components";
import BranchWPComponent from "./BranchWPComponent";


type Props = {
  initialConfiguration: BranchWPs,
  readOnly: boolean,
  onConfigurationChange: (BranchWPs, boolean) => void,
  // context prop
  t: (string) => string
};

type State = BranchWPs & {};

class BranchWPsForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {...props.initialConfiguration};
  };

  isValid() {
    const {permissions} = this.state;
    let valid = true;
    permissions.map((branchWP) => {
      valid = valid && branchWP.name.trim() != "" && branchWP.branch.trim() != "" && branchWP.type.trim() != "";
    });
    return valid;
  }

  updateBranchWPs(permissions) {
    this.setState({permissions}, () => this.props.onConfigurationChange(this.state, this.isValid()));
  }

  onDelete = (deletedBranchWP) => {
    const {permissions} = this.state;
    let index = permissions.indexOf(deletedBranchWP);
    permissions.splice(index, 1);
    this.updateBranchWPs(permissions);
  };

  onChange = (changedBranchWP, index) => {
    const {permissions} = this.state;
    permissions[index] = changedBranchWP;
    this.updateBranchWPs(permissions);
  };

  onChangeEnabled = (isEnabled) => {
    this.setState({enabled: isEnabled}
    , () => this.props.onConfigurationChange(this.state, this.isValid()))
  };

  render() {
    const {permissions, enabled} = this.state;
    const {t, readOnly} = this.props;
    let defaultUserBranWP = {
      branch: "",
      name: "",
      group: false,
      type: "ALLOW"
    };
    let defaultGroupBranWP = {
      branch: "",
      name: "",
      group: true,
      type: "ALLOW"
    };

    const buttons = (<article className="media">
      <Button disabled={readOnly}
              label={t("scm-branchwp-plugin.add-user-permission")}
              action={() => {
                permissions.push(defaultUserBranWP);
                this.updateBranchWPs(permissions);
              }
              }/>
      <Button disabled={readOnly}
              label={t("scm-branchwp-plugin.add-group-permission")}
              action={() => {
                permissions.push(defaultGroupBranWP);
                this.updateBranchWPs(permissions);
              }
              }/>
    </article>);


    const form = permissions.map((branchWP, index) => {
      return <BranchWPComponent
        branchWP={branchWP}
        readOnly={readOnly}
        onDelete={this.onDelete}
        onChange={(changedBranchWP) => this.onChange(changedBranchWP, index)}
      />
    }) ;


    return (
      <>
        <Checkbox checked={enabled} onChange={this.onChangeEnabled}
                  label={t("scm-branchwp-plugin.is-enabled")}
                  helpText={t("scm-branchwp-plugin.is-enabled-help-text")}/>
        {enabled ? (form): ("")}
        {enabled ? (buttons): ("")}
      </>
    );
  }
}

export default translate("plugins")(BranchWPsForm);
