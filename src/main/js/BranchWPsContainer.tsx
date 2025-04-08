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
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { Configuration } from "@scm-manager/ui-components";
import { Subtitle, useDocumentTitleForRepository } from "@scm-manager/ui-core";
import BranchWPsForm from "./BranchWPsForm";

type Props = {
  repository: Repository;
  link: string;
  indexLinks: object;
};

const BranchWPsContainer: React.FC<Props> = ({ repository, link, indexLinks }) => {
  const { t } = useTranslation("plugins");
  useDocumentTitleForRepository(repository, t("scm-branchwp-plugin.subtitle"));

  const userAutoCompleteLink = indexLinks.autocomplete.find((link) => link.name === "users")?.href;
  const groupsAutoCompleteLink = indexLinks.autocomplete.find((link) => link.name === "groups")?.href;

  return (
    <>
      <Subtitle subtitle={t("scm-branchwp-plugin.subtitle")} />
      <Configuration
        link={link}
        render={(props) => (
          <BranchWPsForm
            {...props}
            userAutocompleteLink={userAutoCompleteLink}
            groupAutocompleteLink={groupsAutoCompleteLink}
          />
        )}
      />
    </>
  );
};

export default BranchWPsContainer;
