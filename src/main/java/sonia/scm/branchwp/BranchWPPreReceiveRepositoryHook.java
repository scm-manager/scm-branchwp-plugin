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

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.PermissionUtil;
import sonia.scm.repository.PreReceiveRepositoryHook;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.user.User;
import sonia.scm.util.SecurityUtil;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class BranchWPPreReceiveRepositoryHook extends PreReceiveRepositoryHook
{

  /** Field description */
  private static final String BRANCH_HG_DEFAULT = "default";

  /** Field description */
  private static final String TYPE_GIT = "git";

  /** Field description */
  private static final String TYPE_HG = "hg";

  /**
   * the logger for BranchwpPreReceiveRepositoryHook
   */
  private static final Logger logger =
    LoggerFactory.getLogger(BranchWPPreReceiveRepositoryHook.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param securityContextProvider
   */
  @Inject
  public BranchWPPreReceiveRepositoryHook(
    Provider<WebSecurityContext> securityContextProvider)
  {
    this.securityContextProvider = securityContextProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Override
  public void onEvent(RepositoryHookEvent event)
  {
    Repository repository = event.getRepository();

    if (repository != null)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("received hook for repository {}", repository.getName());
      }

      WebSecurityContext context = securityContextProvider.get();
      User user = context.getUser();

      if (user == null)
      {
        throw new IllegalStateException("no user found");
      }

      if (logger.isTraceEnabled())
      {
        logger.trace("check branchwp for user {} and repository {}",
          user.getName(), repository.getName());
      }

      if (!isOwnerOrAdmin(context, user, repository))
      {

        BranchWPConfiguration config = new BranchWPConfiguration(repository);

        if (config.isEnabled())
        {
          if (logger.isDebugEnabled())
          {
            logger.debug("branchwp is enabled for repository {}",
              repository.getName());
          }

          handleBranchWP(context, config, event);

        }
        else if (logger.isDebugEnabled())
        {
          logger.debug("branchwp is disabled for repository {}",
            repository.getName());
        }

      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("received hook without repository");
    }

  }

  /**
   * Method description
   *
   *
   *
   *
   * @param context
   * @param config
   * @param event
   */
  private void handleBranchWP(WebSecurityContext context,
    BranchWPConfiguration config, RepositoryHookEvent event)
  {
    boolean privileged = false;
    Set<BranchWPPermission> permissions = config.getPermissions();

    if (!permissions.isEmpty())
    {
      Repository repository = event.getRepository();

      privileged = true;

      for (Changeset changeset : event.getChangesets())
      {
        if (!isPrivileged(context, repository, permissions, changeset))
        {
          if (logger.isWarnEnabled())
          {
            logger.warn("access denied for branch {}", changeset.getBranches());
          }

          throw new IllegalStateException("no permissions to write to branch");
        }
      }
    }
    else
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("branchwp permissions are empty, access denied");
      }

      throw new IllegalStateException("no branchwp permissions defined");
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   * @param branches
   *
   * @return
   */
  private String getBranchName(String type, List<String> branches)
  {
    String branch = null;

    if (branches.isEmpty() && TYPE_HG.equals(type))
    {
      branch = BRANCH_HG_DEFAULT;
    }
    else
    {
      branch = branches.get(0);
    }

    return branch;
  }

  /**
   * Method description
   *
   *
   * @param context
   * @param user
   * @param repository
   *
   * @return
   */
  private boolean isOwnerOrAdmin(WebSecurityContext context, User user,
    Repository repository)
  {
    boolean adminOrOwner = false;

    if (SecurityUtil.isAdmin(context))
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("skip branchwp because the user {} is admin",
          user.getName());
      }

      adminOrOwner = true;
    }
    else if (PermissionUtil.hasPermission(repository, context,
      PermissionType.OWNER))
    {
      if (logger.isDebugEnabled())
      {
        logger.debug(
          "skip branchwp because the user {} is the owner of the repository {}",
          user.getName(), repository.getName());
      }

      adminOrOwner = true;
    }

    return adminOrOwner;
  }

  /**
   * Method description
   *
   *
   *
   * @param context
   * @param repository
   * @param permissions
   * @param c
   * @param changeset
   *
   * @return
   */
  private boolean isPrivileged(WebSecurityContext context,
    Repository repository, Set<BranchWPPermission> permissions,
    Changeset changeset)
  {
    boolean privileged = false;

    String type = repository.getType();

    List<String> braches = changeset.getBranches();

    if (braches.isEmpty() && TYPE_GIT.equals(type))
    {
      if (logger.isTraceEnabled())
      {
        logger.trace(
          "git changeset {} is no repository head and has no branch informations",
          changeset.getId());
      }

      privileged = true;
    }
    else
    {
      String username = context.getUser().getName();

      String branch = getBranchName(type, braches);

      for (BranchWPPermission bwp : permissions)
      {
        //J-
        if ((branch.equals(bwp.getBranch())
           && (bwp.isGroup() && context.getGroups().contains(bwp.getName()))) 
           || (!bwp.isGroup() && username.equals(bwp.getName())))
        {
          if ( logger.isTraceEnabled() )
          {
            logger.trace("changeset {} granted by {}", changeset.getId(), bwp);
          }
          
          privileged = true;
          break;
        }
        //J+
      }

    }

    return privileged;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
