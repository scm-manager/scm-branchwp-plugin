package sonia.scm.branchwp.review;

import com.cloudogu.scm.review.pullrequest.service.MergeGuard;
import com.cloudogu.scm.review.pullrequest.service.MergeObstacle;
import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import org.apache.shiro.SecurityUtils;
import sonia.scm.branchwp.service.BranchWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;

import javax.inject.Inject;
import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

@Extension(requires = "scm-review-plugin")
public class BranchWritePermissionMergeGuard implements MergeGuard {

  private final BranchWritePermissionService service;

  @Inject
  public BranchWritePermissionMergeGuard(BranchWritePermissionService service) {
    this.service = service;
  }

  @Override
  public Collection<MergeObstacle> getObstacles(Repository repository, PullRequest pullRequest) {
    User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    if (service.isPrivileged(user, repository, pullRequest.getTarget())) {
      return emptyList();
    } else {
      return singleton(new MergeObstacle() {
        @Override
        public String getMessage() {
          return "The user has no privileges to write to branch " + pullRequest.getTarget();
        }

        @Override
        public String getKey() {
          return "scm-branchwp-plugin.obstacle";
        }
      });
    }
  }
}
