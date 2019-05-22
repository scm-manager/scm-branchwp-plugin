// @flow
import React from "react";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import classNames from "classnames";
import { confirmAlert } from "@scm-manager/ui-components";
import type { BranchWP } from "./types/BranchWP";

type Props = {
  permission: BranchWP,
  onDelete: BranchWP => void,

  // context props
  classes: Object,
  t: string => string
};

const styles = {
  iconColor: {
    color: "#9a9a9a"
  },
  centerMiddle: {
    display: "table-cell",
    verticalAlign: "middle !important"
  },
  columnWidth: {
    width: "100%"
  }
};

class BranchWPTableRow extends React.Component<Props> {
  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("scm-branchwp-plugin.confirmDeleteAlert.title"),
      message: t("scm-branchwp-plugin.confirmDeleteAlert.message"),
      buttons: [
        {
          label: t("scm-branchwp-plugin.confirmDeleteAlert.submit"),
          onClick: () => this.props.onDelete(this.props.permission)
        },
        {
          label: t("scm-branchwp-plugin.confirmDeleteAlert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  render() {
    const { permission, classes, t } = this.props;

    const iconType =
      permission && permission.group ? (
        <i
          title={t("scm-branchwp-plugin.table.group")}
          className={classNames("fas fa-user-friends", classes.iconColor)}
        />
      ) : (
        <i
          title={t("scm-branchwp-plugin.table.user")}
          className={classNames("fas fa-user", classes.iconColor)}
        />
      );

    return (
      <tr className={classes.columnWidth}>
        <td className={classes.centerMiddle}>
          {iconType} {permission.name}
        </td>
        <td>{permission.branch}</td>
        <td>{permission.type}</td>
        <td>
          <a
            className="level-item"
            onClick={this.confirmDelete}
            title={t("scm-branchwp-plugin.table.delete")}
          >
            <span className="icon is-small">
              <i className="fas fa-trash" />
            </span>
          </a>
        </td>
      </tr>
    );
  }
}

export default translate("plugins")(injectSheet(styles)(BranchWPTableRow));
