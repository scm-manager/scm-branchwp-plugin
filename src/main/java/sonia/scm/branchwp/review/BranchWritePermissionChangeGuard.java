package sonia.scm.branchwp.review;

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
