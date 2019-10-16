// @flow
import React from "react";
import type { Repository } from "@scm-manager/ui-types";
import { Configuration } from "@scm-manager/ui-components";
import BranchWPsForm from "./BranchWPsForm";

type Props = {
  repository: Repository,
  link: string,
  indexLinks: Object
};

class BranchWPsContainer extends React.Component<Props> {
  render() {
    const { link, indexLinks } = this.props;
    const userAutoCompleteLink = indexLinks.autocomplete.find(
      link => link.name === "users"
    ).href;
    const groupsAutoCompleteLink = indexLinks.autocomplete.find(
      link => link.name === "groups"
    ).href;
    return (
      <Configuration
        link={link}
        render={props => (
          <BranchWPsForm
            {...props}
            userAutocompleteLink={userAutoCompleteLink}
            groupAutocompleteLink={groupsAutoCompleteLink}
          />
        )}
      />
    );
  }
}

export default BranchWPsContainer;
