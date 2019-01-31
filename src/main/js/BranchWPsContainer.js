// @flow
import React from "react";
import { Configuration, Title } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import type { Link, Repository } from "@scm-manager/ui-types";
import BranchWPsForm from "./BranchWPsForm";
import { connect } from "react-redux";

type Props = {
  repository: Repository,
  link: string,
  userAutocompleteLink: string,
  groupAutocompleteLink: string,
  t: string => string
};

class BranchWPsContainer extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
  }

  render() {
    const { userAutocompleteLink, groupAutocompleteLink, t, link } = this.props;
    return (
      <>
        <Title title={t("scm-branchwp-plugin.form.header")} />
        <br />
        <Configuration
          link={link}
          render={props => (
            <BranchWPsForm
              {...props}
              userAutocompleteLink={userAutocompleteLink}
              groupAutocompleteLink={groupAutocompleteLink}
            />
          )}
        />
      </>
    );
  }
}

function getUserAutoCompleteLink(state: Object): string {
  const link = getLinkCollection(state, "autocomplete").find(
    i => i.name === "users"
  );
  if (link) {
    return link.href;
  }
  return "";
}
function getGroupAutoCompleteLink(state: Object): string {
  const link = getLinkCollection(state, "autocomplete").find(
    i => i.name === "groups"
  );
  if (link) {
    return link.href;
  }
  return "";
}

function getLinkCollection(state: Object, name: string): Link[] {
  if (state.indexResources.links && state.indexResources.links[name]) {
    return state.indexResources.links[name];
  }
  return [];
}

const mapStateToProps = state => {
  const userAutocompleteLink = getUserAutoCompleteLink(state);
  const groupAutocompleteLink = getGroupAutoCompleteLink(state);
  return {
    userAutocompleteLink,
    groupAutocompleteLink
  };
};

export default connect(mapStateToProps)(
  translate("plugins")(BranchWPsContainer)
);
