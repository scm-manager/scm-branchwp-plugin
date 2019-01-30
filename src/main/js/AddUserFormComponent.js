// @flow
import React from "react";
import {
  Autocomplete,
  Button,
  DropDown,
  InputField,
  LabelWithHelpIcon
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import type { BranchWP } from "./BranchWP";
import type { SelectValue } from "@scm-manager/ui-types";

type Props = {
  userAutocompleteLink: string,
  readOnly: boolean,
  onAdd: BranchWP => void,
  // Context props
  t: string => string
};
type State = BranchWP;

const defaultState = {
  name: "",
  type: "ALLOW",
  branch: "",
  group: false
};

class AddUserFormComponent extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = defaultState;
  }
  loadSuggestions = (inputValue: string) => {
    return this.loadAutocompletion(this.props.userAutocompleteLink, inputValue);
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
    this.setState({ ...this.state, type });
  };

  selectName = (selection: SelectValue) => {
    this.setState({ ...this.state, name: selection.label });
  };

  handleBranchExpressionChange = (branch: string) => {
    this.setState({ ...this.state, branch });
  };

  render() {
    const { t, readOnly } = this.props;
    const { name, branch } = this.state;
    return (
      <>
        <LabelWithHelpIcon
          label={t("scm-branchwp-plugin.form.user")}
          helpText={t("scm-branchwp-plugin.form.permission-help-text")}
        />
        <DropDown
          options={["ALLOW", "DENY"]}
          optionSelected={this.handleDropDownChange}
          preselectedOption={"ALLOW"}
          disabled={readOnly}
        />
        <Autocomplete
          label={t("scm-branchwp-plugin.form.user-name")}
          loadSuggestions={this.loadSuggestions}
          helpText={t("scm-branchwp-plugin.form.user-name-help-text")}
          valueSelected={this.selectName}
          value={name}
          placeholder={t("scm-branchwp-plugin.form.user-name")}
        />
        <InputField
          name={"branch"}
          placeholder={t("scm-branchwp-plugin.form.branch")}
          label={t("scm-branchwp-plugin.form.branch")}
          helpText={t("scm-branchwp-plugin.form.branch-help-text")}
          value={branch}
          onChange={this.handleBranchExpressionChange}
          disabled={readOnly}
        />
        <Button
          label={t("scm-branchwp-plugin.form.add")}
          disabled={this.props.readOnly}
          action={() => {
            this.props.onAdd(this.state);
            this.setState({ ...defaultState });
          }}
        />
      </>
    );
  }
}

export default translate("plugins")(AddUserFormComponent);
