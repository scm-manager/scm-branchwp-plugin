package sonia.scm.branchwp;

import com.github.legman.Subscribe;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import sonia.scm.EagerSingleton;
import sonia.scm.branchwp.service.BranchWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.user.User;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Receive repository events and Verify the write permission on every branch found in the event.
 *
 * @author Mohamed Karray
 */
@Slf4j
@Extension
@EagerSingleton
public class RepositoryHook {

  private static final String BRANCH_HG_DEFAULT = "default";
  private static final String TYPE_GIT = "git";
  private static final String TYPE_HG = "hg";

  private BranchWritePermissionService service;

  @Inject
  public RepositoryHook(BranchWritePermissionService service) {
    this.service = service;
  }

  @Subscribe(async = false)
  public void onEvent(PreReceiveRepositoryHookEvent event) {
    HookContext context = event.getContext();
    if (context == null) {
      log.warn("there is no context in the received repository hook");
      return;
    }
    if (!context.isFeatureSupported(HookFeature.BRANCH_PROVIDER)) {
      log.info("The repository does not support branches. Skip BranchWP plugin.");
      return;
    }

    Repository repository = event.getRepository();
    if (repository == null) {
      log.warn("there is no repository in the received repository hook");
      return;
    }

    if (!service.isPluginEnabled(repository)){
      log.trace("branchwp plugin is disabled.");
      return;
    }

    log.trace("received hook for repository {}", repository.getName());
    List<String> branches = getBranches(context, repository);
    for (String branch : branches) {
      if (!isCurrentUserPrivileged(repository, branch)) {
        throw new BranchWritePermissionException(repository, branch);
      }
    }
  }

  public boolean isCurrentUserPrivileged(Repository repository, String branch) {
    User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    return service.isPrivileged(user, repository, branch);
  }

  private List<String> getBranches(HookContext eventContext, Repository repository) {
    List<String> branches = Lists.newArrayList();
    if (eventContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)) {
      log.trace("use hook branch provider to check permissions");
      branches.addAll(getBranches(eventContext.getBranchProvider()));
    } else if (eventContext.isFeatureSupported(HookFeature.CHANGESET_PROVIDER)) {
      log.trace("use hook changeset provider to check permissions");
      Iterable<Changeset> changesets = eventContext.getChangesetProvider().getChangesets();
      for (Changeset changeset : changesets) {
        getBranch(changeset, repository)
          .ifPresent(branches::add);
      }
    }
    log.debug("branches cannot be extracted from the repo {}", repository.getNamespaceAndName());
    return branches;
  }

  private List<String> getBranches(HookBranchProvider branchProvider) {
    LinkedList<String> branches = new LinkedList<>();
    branches.addAll(branchProvider.getCreatedOrModified());
    branches.addAll(branchProvider.getDeletedOrClosed());
    return branches;
  }

  public Optional<String> getBranch(Changeset changeset, Repository repository) {
    String type = repository.getType();
    List<String> branches = changeset.getBranches();
    if (!branches.isEmpty()) {
      return Optional.of(branches.get(0));
    } else if (TYPE_HG.equals(type)) {
      return Optional.of(BRANCH_HG_DEFAULT);
    } else if (TYPE_GIT.equals(type)) {
      log.trace("git changeset {} is not the repository head and has no branch information", changeset.getId());
    }
    return Optional.empty();
  }
}
