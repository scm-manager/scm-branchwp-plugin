// @flow
import React from "react";
import { translate } from "react-i18next";
import { Notification } from "@scm-manager/ui-components";
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
    const { permissions, onDelete, t } = this.props;

    if (permissions && permissions[0]) {
      const tableRows = permissions.map(branchWP => {
        return (
          <>
            <BranchWPRow
              permission={branchWP}
              onDelete={permission => {
                onDelete(permission);
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
    return (
      <Notification type="info">
        {t("scm-branchwp-plugin.noPermissions")}
      </Notification>
    );
  }
}

export default translate("plugins")(BranchWPTable);
