// @flow

import React from "react";
import type { BranchWP } from "./BranchWP";
import BranchWPTableRow from "./BranchWPTableRow";
import { translate } from "react-i18next";

type Props = {
  permissions: BranchWP[],
  // permissionListChanged: (permissions: BranchWP[]) => void
  onDelete: BranchWP => void,
  // context prop
  t: string => string
};

class BranchWPTable extends React.Component<Props> {
  render() {
    const { t } = this.props;
    const tableRows = this.props.permissions.map(branchWP => {
      return (
        <>
          <BranchWPTableRow permission={branchWP} onDelete={permission => {
            this.props.onDelete(permission)
          }}/>
        </>
      );
    });
    return (
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
          <th>{t("scm-branchwp-plugin.table.branch")}</th>
          <th>{t("scm-branchwp-plugin.table.name")}</th>
          <th>{t("scm-branchwp-plugin.table.type")}</th>
          <th>{t("scm-branchwp-plugin.table.group")}</th>
          <th>{t("scm-branchwp-plugin.table.delete")}</th>
        </thead>
        {tableRows}
      </table>
    );
  }
}

export default translate("plugins")(BranchWPTable);
