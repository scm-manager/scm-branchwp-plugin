// @flow
import React from "react";
import {Configuration, Title} from "@scm-manager/ui-components";
import {translate} from "react-i18next";
import type {Repository} from "@scm-manager/ui-types";
import BranchWPsForm from "./BranchWPsForm";

type Props = {
  repository: Repository,
  link: string,
  t: string => string
};

class BranchWPsContainer extends React.Component<Props> {

  constructor(props: Props) {
    super(props);
  };

  render() {
    const {t, link} = this.props;
    return (
      <>
        <Title title={t("scm-branchwp-plugin.form.header")} />
        <br/>
        <Configuration link={link} render={props => <BranchWPsForm {...props} />}/>
      </>
    );
  };
}

export default translate("plugins")(BranchWPsContainer);
