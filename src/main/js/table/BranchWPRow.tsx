/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import styled from "styled-components";
import { confirmAlert, Icon } from "@scm-manager/ui-components";
import { BranchWP } from "../types/BranchWP";

type Props = WithTranslation & {
  permission: BranchWP;
  onDelete: (p: BranchWP) => void;
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
          className: "is-info",
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
        <Icon title={t("scm-branchwp-plugin.table.group")} name="user-friends" />
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
          <a className="level-item" onClick={this.confirmDelete} title={t("scm-branchwp-plugin.table.delete")}>
            <span className="icon is-small">
              <Icon name="trash" color="inherit" />
            </span>
          </a>
        </VCenteredTd>
      </tr>
    );
  }
}

export default withTranslation("plugins")(BranchWPRow);
