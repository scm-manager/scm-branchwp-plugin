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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.PropertiesAware;
import sonia.scm.user.User;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class BranchWPConfiguration implements Serializable
{

  /** Field description */
  static final String PROPERTY_ENABLED = "branchwp.enabled";

  /** Field description */
  static final String PROPERTY_PERMISSIONS = "branchwp.permissions";

  /** Field description */
  private static final long serialVersionUID = 1089077731493333795L;

  /**
   * the logger for BranchWPConfiguration
   */
  private static final Logger logger =
    LoggerFactory.getLogger(BranchWPConfiguration.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param properties
   */
  public BranchWPConfiguration(PropertiesAware properties)
  {
    this(properties, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param properties
   * @param user
   */
  public BranchWPConfiguration(PropertiesAware properties, User user)
  {
    this.properties = properties;
    this.user = user;
    enabled = Boolean.valueOf(properties.getProperty(PROPERTY_ENABLED));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<BranchWPPermission> getAllowPermissions()
  {
    if (allowPermissions == null)
    {
      parsePermissions();
    }

    return allowPermissions;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<BranchWPPermission> getDenyPermissions()
  {
    if (denyPermissions == null)
    {
      parsePermissions();
    }

    return denyPermissions;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isEnabled()
  {
    return enabled;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isPermissionConfigEmpty()
  {
    if ((allowPermissions == null) || (denyPermissions == null))
    {
      parsePermissions();
    }

    return allowPermissions.isEmpty() && denyPermissions.isEmpty();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  private void parsePermissions()
  {
    allowPermissions = Sets.newHashSet();
    denyPermissions = Sets.newHashSet();

    String property = properties.getProperty(PROPERTY_PERMISSIONS);

    if (!Strings.isNullOrEmpty(property))
    {
      BranchWPPermissionParser.parse(allowPermissions, denyPermissions,
        property, user);
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("no permissions found");
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final PropertiesAware properties;

  /** Field description */
  private final User user;

  /** Field description */
  private Set<BranchWPPermission> allowPermissions;

  /** Field description */
  private Set<BranchWPPermission> denyPermissions;

  /** Field description */
  private boolean enabled = false;
}
