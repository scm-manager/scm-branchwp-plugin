/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.branchwp;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;
import sonia.scm.util.GlobUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import org.apache.shiro.subject.Subject;
import sonia.scm.group.GroupNames;

/**
 *
 * @author Sebastian Sdorra
 */
public class BranchWPContext
{

  /** Field description */
  private static final String BRANCH_HG_DEFAULT = "default";

  /** Field description */
  private static final String TYPE_GIT = "git";

  /** Field description */
  private static final String TYPE_HG = "hg";

  /**
   * the logger for BranchWPContext
   */
  private static final Logger logger =
    LoggerFactory.getLogger(BranchWPContext.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param subject
   * @param user
   * @param repository
   * @param config
   */
  public BranchWPContext(Subject subject, User user, Repository repository,
    BranchWPConfiguration config)
  {
    this.subject = subject;
    this.repository = repository;
    this.config = config;
    this.user = user;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @param changeset
   *
   * @return
   */
  public boolean isPrivileged(Changeset changeset)
  {
    boolean privileged = false;

    String type = repository.getType();

    List<String> branches = changeset.getBranches();

    if (branches.isEmpty() && TYPE_GIT.equals(type))
    {
      logger.trace(
        "git changeset {} is not the repository head and has no branch informations",
        changeset.getId());

      privileged = true;
    }
    else
    {
      String username = user.getName();

      String branch = getBranchName(type, branches);

      if (!isChangesetDenied(changeset, branch))
      {
        privileged = isChangesetAllowed(changeset, branch);
      }

      if (!privileged)
      {
        logger.warn("access denied for user {} at branch {}", username, branch);
      }

    }

    return privileged;
  }

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
    String branch;

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
   * @param config
   * @param changeset
   * @param branch
   * @param context
   * @param username
   *
   * @return
   */
  private boolean isChangesetAllowed(Changeset changeset, String branch)
  {
    boolean allowed = false;

    logger.trace("check allow permissions of user {} for branch {}",
      user.getName(), branch);

    for (BranchWPPermission bwp : config.getAllowPermissions())
    {
      if (isPermissionMatching(bwp, branch))
      {
        logger.trace("changeset {} granted by {}", changeset.getId(), bwp);

        allowed = true;

        break;
      }
    }

    return allowed;
  }

  /**
   * Method description
   *
   *
   * @param config
   * @param changeset
   * @param branch
   * @param context
   * @param username
   *
   * @return
   */
  private boolean isChangesetDenied(Changeset changeset, String branch)
  {
    boolean denied = false;

    logger.trace("check deny permissions of user {} for branch {}",
      user.getName(), branch);

    for (BranchWPPermission bwp : config.getDenyPermissions())
    {
      if (isPermissionMatching(bwp, branch))
      {
        logger.trace("changeset {} denied by {}", changeset.getId(), bwp);
        denied = true;

        break;
      }
    }

    return denied;
  }

  /**
   * Method description
   *
   *
   * @param bwp
   * @param branch
   * @param context
   * @param username
   *
   * @return
   */
  private boolean isPermissionMatching(BranchWPPermission bwp, String branch)
  {
    //J-
    return GlobUtil.matches(bwp.getBranch(), branch)
           && ((bwp.isGroup() && getGroups().contains(bwp.getName()))
           || (!bwp.isGroup() && user.getName().equals(bwp.getName())));
    //J+
  }
  
  private GroupNames getGroups(){
    return subject.getPrincipals().oneByType(GroupNames.class);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final BranchWPConfiguration config;

  private final Subject subject;

  /** Field description */
  private final Repository repository;

  /** Field description */
  private final User user;
}
