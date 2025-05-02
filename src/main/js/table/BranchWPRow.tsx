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

import React, { FC, useState } from "react";
import styled from "styled-components";
import { BranchWP } from "../types/BranchWP";
import { IconButton, Icon, Dialog, Button } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";

type Props = {
  permission: BranchWP;
  onDelete: (p: BranchWP) => void;
};

const VCenteredTd = styled.td`
  display: table-cell;
  vertical-align: middle !important;
`;

const BranchWPRow: FC<Props> = ({ permission, onDelete }) => {
  const [t] = useTranslation("plugins");
  const [isOpen, setIsOpen] = useState(false);

  const confirmDelete = () => {
    onDelete(permission);
    setIsOpen(false);
  }

  const iconType = permission && permission.group ? (
      <Icon>user-friends</Icon>
    ) : (
      <Icon>user</Icon>
    );
  return (
    <tr>
      <VCenteredTd>
        {iconType} {permission.name}
      </VCenteredTd>
      <VCenteredTd>{permission.branch}</VCenteredTd>
      <VCenteredTd>{permission.type}</VCenteredTd>
      <VCenteredTd className="is-darker">
        <Dialog
          trigger={
            <IconButton title={t("scm-branchwp-plugin.table.delete")}>
              <Icon>trash</Icon>
            </IconButton>
          }
          title={t("scm-branchwp-plugin.confirmDeleteAlert.title")}
          footer={[
            <Button onClick={confirmDelete}>
              {t("scm-branchwp-plugin.confirmDeleteAlert.submit")}
            </Button>,
            <Button variant="primary" autoFocus onClick={() => setIsOpen(false)}>
              {t("scm-branchwp-plugin.confirmDeleteAlert.cancel")}
            </Button>
          ]}
          open={isOpen}
          onOpenChange={setIsOpen}
        >
          {t("scm-branchwp-plugin.confirmDeleteAlert.message")}
        </Dialog>
      </VCenteredTd>
    </tr>
  );
};

export default BranchWPRow;
