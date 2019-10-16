// @flow
import React from "react";
import { translate } from "react-i18next";
import styled from "styled-components";
import { confirmAlert, Icon } from "@scm-manager/ui-components";
import type { BranchWP } from "../types/BranchWP";

type Props = {
  permission: BranchWP,
  onDelete: BranchWP => void,

  // context props
  t: string => string
};

const VCenteredTd = styled.td`
  display: table-cell;
  vertical-align: middle !important;
`;

class BranchWPRow extends React.Component<Props> {
  confirmDelete = () => {
    const { t, onDelete, permission } = this.props;
    confirmAlert({
      title: t("scm-branchwp-plugin.confirmDeleteAlert.title"),
      message: t("scm-branchwp-plugin.confirmDeleteAlert.message"),
      buttons: [
        {
          label: t("scm-branchwp-plugin.confirmDeleteAlert.submit"),
          onClick: () => onDelete(permission)
        },
        {
          label: t("scm-branchwp-plugin.confirmDeleteAlert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  render() {
    const { permission, t } = this.props;

    const iconType =
      permission && permission.group ? (
        <Icon
          title={t("scm-branchwp-plugin.table.group")}
          name="user-friends"
        />
      ) : (
        <Icon title={t("scm-branchwp-plugin.table.user")} name="user" />
      );

    return (
      <tr>
        <VCenteredTd>
          {iconType} {permission.name}
        </VCenteredTd>
        <td>{permission.branch}</td>
        <td>{permission.type}</td>
        <VCenteredTd className="is-darker">
          <a
            className="level-item"
            onClick={this.confirmDelete}
            title={t("scm-branchwp-plugin.table.delete")}
          >
            <span className="icon is-small">
              <Icon name="trash" color="inherit" />
            </span>
          </a>
        </VCenteredTd>
      </tr>
    );
  }
}

export default translate("plugins")(BranchWPRow);
