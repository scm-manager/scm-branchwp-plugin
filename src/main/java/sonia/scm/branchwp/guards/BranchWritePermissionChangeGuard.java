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
package sonia.scm.branchwp.guards;

import com.cloudogu.scm.editor.ChangeGuard;
import com.cloudogu.scm.editor.ChangeObstacle;
import org.apache.shiro.SecurityUtils;
import sonia.scm.branchwp.service.BranchWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.user.User;

import javax.inject.Inject;
import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

@Extension(requires = "scm-editor-plugin")
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
