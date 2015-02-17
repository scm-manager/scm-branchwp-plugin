/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.branchwp;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.eventbus.Subscribe;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.EagerSingleton;
import sonia.scm.event.Subscriber;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.security.RepositoryPermission;
import sonia.scm.user.User;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
@EagerSingleton
@Subscriber(async = false)
public class BranchWPPreReceiveRepositoryHook
{

  /**
   * the logger for BranchwpPreReceiveRepositoryHook
   */
  private static final Logger logger =
    LoggerFactory.getLogger(BranchWPPreReceiveRepositoryHook.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Subscribe
  public void onEvent(PreReceiveRepositoryHookEvent event)
  {
    Repository repository = event.getRepository();

    if (repository != null)
    {
      logger.trace("received hook for repository {}", repository.getName());

      Subject subject = getSubject();

      logger.trace("check branchwp for user {} and repository {}",
        subject.getPrincipal(), repository.getName());

      if (!subject.isPermitted(owner(repository)))
      {
        User user = subject.getPrincipals().oneByType(User.class);
        BranchWPConfiguration config = new BranchWPConfiguration(repository,
                                         user);

        if (config.isEnabled())
        {
          logger.debug("branchwp is enabled for repository {}",
            repository.getName());

          handleBranchWP(subject, user, config, event);

        }
        else
        {
          logger.debug("branchwp is disabled for repository {}",
            repository.getName());
        }

      }
      else
      {
        logger.debug(
          "skip user {}, because the user has owner permissions for repository {}",
          subject.getPrincipal(), repository.getName());
      }
    }
    else
    {
      logger.warn("received hook without repository");
    }

  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @VisibleForTesting
  protected Subject getSubject()
  {
    return SecurityUtils.getSubject();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param ctx
   * @param branch
   */
  private void checkBranch(BranchWPContext ctx, String branch)
  {
    if (!ctx.isPrivileged(branch))
    {
      logger.warn("access denied for branch {}", branch);

      throw new BranchWPException("no write permissions for the branch ".concat(branch));
    }
  }

  /**
   * Method description
   *
   *
   * @param ctx
   * @param branchProvider
   */
  private void checkBranchProvider(BranchWPContext ctx,
    HookBranchProvider branchProvider)
  {
    for (String branch : branchProvider.getCreatedOrModified())
    {
      checkBranch(ctx, branch);
    }

    for (String branch : branchProvider.getDeletedOrClosed())
    {
      checkBranch(ctx, branch);
    }
  }

  /**
   * Method description
   *
   *
   * @param ctx
   * @param changesets
   */
  private void checkChangesets(BranchWPContext ctx,
    Iterable<Changeset> changesets)
  {
    for (Changeset changeset : changesets)
    {
      if (!ctx.isPrivileged(changeset))
      {
        logger.warn("access denied for branch {}", changeset.getBranches());

        throw new BranchWPException(
          "no write permissions for one of the following branches: ".concat(
            Joiner.on(", ").join(changeset.getBranches())
          )
        );
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param ctx
   * @param event
   * @param hookCtx
   */
  private void checkWithHookContext(BranchWPContext ctx,
    PreReceiveRepositoryHookEvent event, HookContext hookCtx)
  {
    if (hookCtx.isFeatureSupported(HookFeature.BRANCH_PROVIDER))
    {
      logger.trace("use hook branch provider to check permissions");
      checkBranchProvider(ctx, hookCtx.getBranchProvider());
    }
    else if (hookCtx.isFeatureSupported(HookFeature.CHANGESET_PROVIDER))
    {
      logger.trace("use hook changeset provider to check permissions");
      checkChangesets(ctx, hookCtx.getChangesetProvider().getChangesets());
    }
    else
    {
      logger.trace("use changesets provided by event to check permissions");
      checkChangesets(ctx, event.getChangesets());
    }
  }

  /**
   * Method description
   *
   *
   * @param context
   *
   * @param subject
   * @param user
   * @param config
   * @param event
   */
  private void handleBranchWP(Subject subject, User user,
    BranchWPConfiguration config, PreReceiveRepositoryHookEvent event)
  {
    if (!config.isPermissionConfigEmpty())
    {
      Repository repository = event.getRepository();

      BranchWPContext ctx = new BranchWPContext(subject, user, repository,
                              config);

      if (event.isContextAvailable())
      {
        checkWithHookContext(ctx, event, event.getContext());
      }
      else
      {
        logger.trace("use changesets provided by event to check permissions");
        checkChangesets(ctx, event.getChangesets());
      }

    }
    else
    {
      logger.warn("branchwp permissions are empty, access denied");

      throw new BranchWPException("no branchwp permissions defined");
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private RepositoryPermission owner(Repository repository)
  {
    return new RepositoryPermission(repository, PermissionType.OWNER);
  }
}
