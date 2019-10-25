import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import BranchWPsContainer from "./BranchWPsContainer";

cfgBinder.bindRepositorySetting("/branchwp", "scm-branchwp-plugin.navLink", "branchWpConfig", BranchWPsContainer);
