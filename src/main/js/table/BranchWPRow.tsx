/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
