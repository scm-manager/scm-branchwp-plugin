// @flow
import React from "react";
import { translate } from "react-i18next";
import type { BranchWP } from "../types/BranchWP";
import BranchWPRow from "./BranchWPRow";

type Props = {
  permissions: BranchWP[],
  onDelete: BranchWP => void,

  // context prop
  t: string => string
};

class BranchWPTable extends React.Component<Props> {
  render() {
    const { permissions, t } = this.props;

    const tableRows = permissions.map(branchWP => {
      return (
        <>
          <BranchWPRow
            permission={branchWP}
            onDelete={permission => {
              this.props.onDelete(permission);
            }}
          />
        </>
      );
    });

    return (
      <table className="has-background-light table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th>{t("scm-branchwp-plugin.table.name")}</th>
            <th>{t("scm-branchwp-plugin.table.branch")}</th>
            <th>{t("scm-branchwp-plugin.table.permission")}</th>
            <th />
          </tr>
        </thead>
        <tbody>{tableRows}</tbody>
      </table>
    );
  }
}

export default translate("plugins")(BranchWPTable);
