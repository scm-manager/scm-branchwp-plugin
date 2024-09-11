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

package sonia.scm.branchwp;

import com.github.legman.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import sonia.scm.EagerSingleton;
import sonia.scm.branchwp.service.BranchWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.user.User;

import jakarta.inject.Inject;
import java.util.stream.Stream;

/**
 * Receive repository events and Verify the write permission on every branch found in the event.
 *
 * @author Mohamed Karray
 */
@Slf4j
@Extension
@EagerSingleton
public class RepositoryHook {

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

    if (!service.isPluginEnabled(repository)) {
      log.trace("branchwp plugin is disabled.");
      return;
    }

    log.trace("received hook for repository {}", repository.getName());
    Stream<String> branches = getBranches(context);
    User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    branches.forEach(
      branch -> {
        if (!service.isPrivileged(user, repository, branch)) {
          throw new BranchWritePermissionException(repository, branch);
        }
      }
    );
  }

  private Stream<String> getBranches(HookContext eventContext) {
    HookBranchProvider branchProvider = eventContext.getBranchProvider();
    return Stream.concat(
      branchProvider.getCreatedOrModified().stream(),
      branchProvider.getDeletedOrClosed().stream()
    );
  }
}
