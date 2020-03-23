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
