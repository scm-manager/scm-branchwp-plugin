/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.branchwp;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import lombok.ToString;

import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@ToString
public class BranchWPPermission implements Serializable {

  /** Field description */
  private static final long serialVersionUID = -2725077333533181778L;

  //~--- constant enums -------------------------------------------------------

  /**
   * Enum description
   *
   */
  public static enum Type {
    ALLOW, DENY;
  }

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param branch
   * @param name
   * @param group
   */
  public BranchWPPermission(String branch, String name, boolean group) {
    this(branch, name, group, Type.ALLOW);
  }

  /**
   * Constructs ...
   *
   *
   * @param branch
   * @param name
   * @param group
   * @param type
   */
  public BranchWPPermission(String branch, String name, boolean group,
                            Type type) {
    this(branch, branch, name, group, type);
  }

  /**
   * Constructs ...
   *
   *
   * @param branchPattern
   * @param branch
   * @param name
   * @param group
   * @param type
   */
  public BranchWPPermission(String branchPattern, String branch, String name,
                            boolean group, Type type) {
    this.branchPattern = branchPattern;
    this.branch = branch;
    this.name = name;
    this.group = group;
    this.type = type;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param obj
   *
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final BranchWPPermission other = (BranchWPPermission) obj;

    //J-
    return Objects.equal(branchPattern, other.branchPattern)
      && Objects.equal(branch, other.branch)
      && Objects.equal(name, other.name)
      && Objects.equal(group, other.group)
      && Objects.equal(type, other.type);
    //J+
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(branchPattern, branch, name, group, type);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getBranch() {
    return branch;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getBranchPattern() {
    return branchPattern;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Type getType() {
    return type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isGroup() {
    return group;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final String branch;

  /** Field description */
  private final String branchPattern;

  /** Field description */
  private final boolean group;

  /** Field description */
  private final String name;

  /** Field description */
  private final Type type;
}
