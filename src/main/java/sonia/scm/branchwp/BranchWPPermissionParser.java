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
import com.google.common.base.Splitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.branchwp.BranchWPPermission.Type;

//~--- JDK imports ------------------------------------------------------------

import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class BranchWPPermissionParser
{

  /**
   * the logger for BranchWPPermissionParser
   */
  private static final Logger logger =
    LoggerFactory.getLogger(BranchWPPermissionParser.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param permissions
   * @param property
   */
  public static void parse(Set<BranchWPPermission> permissions, String property)
  {
    parse(permissions, permissions, property);
  }

  /**
   * Method description
   *
   *
   * @param allowPermissions
   * @param denyPermissions
   * @param property
   */
  public static void parse(Set<BranchWPPermission> allowPermissions,
    Set<BranchWPPermission> denyPermissions, String property)
  {
    logger.trace("try to parse permissions string {}", property);

    Iterable<String> permissionStrings =
      Splitter.on(";").omitEmptyStrings().trimResults().split(property);

    for (String permissionString : permissionStrings)
    {
      logger.trace("try to parse permission string {}", permissionString);

      BranchWPPermission permission = parsePermission(permissionString);

      if (permission != null)
      {
        logger.debug("append branchwp permission {}", permission);

        if (permission.getType() == Type.DENY)
        {
          denyPermissions.add(permission);
        }
        else
        {
          allowPermissions.add(permission);
        }
      }
      else
      {
        logger.warn("failed to parse permission string {}", permissionString);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param permissionString
   *
   * @return
   */
  @VisibleForTesting
  static BranchWPPermission parsePermission(String permissionString)
  {
    BranchWPPermission permission = null;
    Iterator<String> parts = Splitter.on(
                               ",").omitEmptyStrings().trimResults().split(
                               permissionString).iterator();

    if (parts.hasNext())
    {
      Type type = Type.ALLOW;
      String branch = parts.next();

      if (branch.startsWith("!"))
      {
        type = Type.DENY;
        branch = branch.substring(1);
      }

      if (parts.hasNext())
      {

        String name = parts.next();
        boolean group = false;

        if (name.startsWith("@"))
        {
          group = true;
          name = name.substring(1);
        }

        permission = new BranchWPPermission(branch, name, group, type);
      }
    }

    return permission;
  }
}
