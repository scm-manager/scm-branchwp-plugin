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

package sonia.scm.branchwp.guards;

import com.cloudogu.scm.editor.ChangeGuard;
import com.cloudogu.scm.editor.ChangeObstacle;
import org.apache.shiro.SecurityUtils;
import sonia.scm.branchwp.service.BranchWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.user.User;

import jakarta.inject.Inject;
import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

@Extension
@Requires("scm-editor-plugin")
public class BranchWritePermissionChangeGuard implements ChangeGuard {

  private final BranchWritePermissionService service;

  @Inject
  public BranchWritePermissionChangeGuard(BranchWritePermissionService service) {
    this.service = service;
  }

  @Override
  public Collection<ChangeObstacle> getObstacles(NamespaceAndName namespaceAndName, String branch, Changes changes) {
    User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    if (service.isPrivileged(user, namespaceAndName, branch)) {
      return emptyList();
    } else {
      return singleton(new ChangeObstacle() {
        @Override
        public String getMessage() {
          return "The user has no privileges to write to branch " + branch;
        }

        @Override
        public String getKey() {
          return "scm-branchwp-plugin.obstacle";
        }
      });
    }
  }
}
