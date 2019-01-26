// @flow

import {ConfigurationBinder as cfgBinder} from "@scm-manager/ui-components";
import BranchWPsContainer from "./BranchWPsContainer";

cfgBinder.bindRepository(
  "/branchwp",
  "scm-branchwp-plugin.nav-link",
  "branchWpConfig",
  BranchWPsContainer
);
