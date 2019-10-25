import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { LabelWithHelpIcon, Notification } from "@scm-manager/ui-components";
import { BranchWP } from "../types/BranchWP";
import BranchWPRow from "./BranchWPRow";

type Props = WithTranslation & {
  permissions: BranchWP[];
  onDelete: (p: BranchWP) => void;
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
        <table className="card-table table is-hoverable is-fullwidth">
          <thead>
            <tr>
              <th>
                <LabelWithHelpIcon
                  label={t("scm-branchwp-plugin.table.name")}
                  helpText={t("scm-branchwp-plugin.table.nameHelpText")}
                />
              </th>
              <th>{t("scm-branchwp-plugin.table.branch")}</th>
              <th>{t("scm-branchwp-plugin.table.permission")}</th>
              <th />
            </tr>
          </thead>
          <tbody>{tableRows}</tbody>
        </table>
      );
    }
    return <Notification type="info">{t("scm-branchwp-plugin.noPermissions")}</Notification>;
  }
}

export default withTranslation("plugins")(BranchWPTable);
