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

import com.google.common.collect.Sets;

import org.junit.Test;

import sonia.scm.branchwp.BranchWPPermission.Type;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class BranchWPPermissionParserTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testParseGroupPermission()
  {
    Set<BranchWPPermission> permissions = Sets.newHashSet();

    String p = "master,@heartofgold";

    BranchWPPermissionParser.parse(permissions, p, trillian);

    assertEquals(1, permissions.size());

    BranchWPPermission permission = permissions.iterator().next();

    assertNotNull(permission);
    assertEquals("master", permission.getBranch());
    assertEquals("heartofgold", permission.getName());
    assertTrue(permission.isGroup());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testParseMultiplePermissions()
  {
    Set<BranchWPPermission> permissions = Sets.newHashSet();

    String p = "default,@hitchecker;master,trillian";

    BranchWPPermissionParser.parse(permissions, p, trillian);
    assertEquals(2, permissions.size());

    for (BranchWPPermission perm : permissions)
    {
      if (perm.getBranch().equals("default"))
      {
        assertEquals("hitchecker", perm.getName());
        assertTrue(perm.isGroup());
      }
      else if (perm.getBranch().equals("master"))
      {
        assertEquals("trillian", perm.getName());
        assertFalse(perm.isGroup());
      }
      else
      {
        fail("found unknown branch ".concat(perm.getBranch()));
      }
    }
  }

  /**
   * Method description
   *
   */
  @Test
  public void testParsePermission()
  {
    Set<BranchWPPermission> permissions = Sets.newHashSet();

    String p = "default,perfect";

    BranchWPPermissionParser.parse(permissions, p, trillian);

    assertEquals(1, permissions.size());

    BranchWPPermission permission = permissions.iterator().next();

    assertNotNull(permission);
    assertEquals("default", permission.getBranch());
    assertEquals("perfect", permission.getName());
    assertFalse(permission.isGroup());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testParsePermissionType()
  {
    BranchWPPermission perm =
      BranchWPPermissionParser.parsePermission(trillian, "default,perfect");

    assertEquals(Type.ALLOW, perm.getType());
    perm = BranchWPPermissionParser.parsePermission(trillian,
      "!default,perfect");
    assertEquals(Type.DENY, perm.getType());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testParseVariablePermission()
  {
    String p = "{username}/development,trillian";
    BranchWPPermission bwp = BranchWPPermissionParser.parsePermission(trillian,
                               p);

    assertEquals("{username}/development", bwp.getBranchPattern());
    assertEquals(trillian.getName() + "/development", bwp.getBranch());
    p = "{mail}/development,trillian";
    bwp = BranchWPPermissionParser.parsePermission(trillian, p);
    assertEquals("{mail}/development", bwp.getBranchPattern());
    assertEquals(trillian.getMail() + "/development", bwp.getBranch());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final User trillian = UserTestData.createTrillian();
}
