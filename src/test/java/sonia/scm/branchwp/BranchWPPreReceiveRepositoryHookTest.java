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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Provider;

import org.junit.Test;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.web.security.WebSecurityContext;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class BranchWPPreReceiveRepositoryHookTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testAsAdmin()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(true);
    Changeset c = createChangeset("0", "master");

    hook.onEvent(createHookEvent("", true, false, c));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAsOwner()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(true);
    Changeset c = createChangeset("0", "master");

    hook.onEvent(createHookEvent("", true, true, c));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testDisabled()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false);
    Changeset c = createChangeset("0", "master");

    hook.onEvent(createHookEvent("master,@hitchecker", false, false, c));
  }

  /**
   * Method description
   *
   */
  @Test(expected = BranchWPException.class)
  public void testEmptyAndDenied()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false);
    Changeset c = createChangeset("0", "master");

    hook.onEvent(createHookEvent("", true, false, c));
  }

  /**
   * Method description
   *
   */
  @Test(expected = BranchWPException.class)
  public void testGroupAccessDenied()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false, "other");
    Changeset c = createChangeset("0", "master");

    hook.onEvent(createHookEvent("master,@hitchecker", true, false, c));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGroupAccessGranted()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false, "hitchecker");
    Changeset c = createChangeset("0", "master");

    hook.onEvent(createHookEvent("master,@hitchecker", true, false, c));
  }

  /**
   * TODO fix test
   *
   */
  @Test(expected = BranchWPException.class)
  public void testMultipleConfigAccessDenied()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false, "ka", "noother");
    Changeset c1 = createChangeset("0", "master");
    Changeset c2 = createChangeset("1", "default");

    hook.onEvent(createHookEvent("master,dent;master,marvin;default,other",
      true, false, c1, c2));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testMultipleConfigAccessGranted()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false, "ka", "other");
    Changeset c1 = createChangeset("0", "master");
    Changeset c2 = createChangeset("1", "default");

    hook.onEvent(createHookEvent("master,dent;master,marvin;default,@other",
      true, false, c1, c2));
  }

  /**
   * Method description
   *
   */
  @Test(expected = BranchWPException.class)
  public void testUserAccessDenied()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false);
    Changeset c = createChangeset("0", "master");

    hook.onEvent(createHookEvent("master,dent", true, false, c));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testUserAccessGranted()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false);
    Changeset c = createChangeset("0", "master");

    hook.onEvent(createHookEvent("master,marvin", true, false, c));
  }

  /**
   * Method description
   *
   *
   * @param id
   * @param branch
   *
   * @return
   */
  private Changeset createChangeset(String id, String branch)
  {
    Changeset changeset = new Changeset(id, System.currentTimeMillis(),
                            new Person("marvin"));

    changeset.setBranches(Lists.newArrayList(branch));

    return changeset;
  }

  /**
   * Method description
   *
   *
   * @param admin
   * @param groups
   *
   * @return
   */
  private BranchWPPreReceiveRepositoryHook createHook(boolean admin,
    String... groups)
  {
    Set<String> groupSet = Sets.newHashSet(groups);
    User marvin = UserTestData.createMarvin();

    marvin.setAdmin(admin);

    WebSecurityContext context = mock(WebSecurityContext.class);

    when(context.getUser()).thenReturn(marvin);
    when(context.getGroups()).thenReturn(groupSet);
    when(context.isAuthenticated()).thenReturn(true);

    Provider<WebSecurityContext> provider = mock(Provider.class);

    when(provider.get()).thenReturn(context);

    return new BranchWPPreReceiveRepositoryHook(provider);
  }

  /**
   * Method description
   *
   *
   *
   *
   * @param permissions
   * @param enabled
   * @param owner
   * @param changesets
   *
   * @return
   */
  private RepositoryHookEvent createHookEvent(String permissions,
    boolean enabled, boolean owner, Changeset... changesets)
  {
    RepositoryHookEvent event = mock(RepositoryHookEvent.class);

    when(event.getType()).thenReturn(RepositoryHookType.PRE_RECEIVE);

    Repository repository = RepositoryTestData.create42Puzzle();

    repository.setProperty(BranchWPConfiguration.PROPERTY_ENABLED,
      Boolean.toString(enabled));
    repository.setProperty(BranchWPConfiguration.PROPERTY_PERMISSIONS,
      permissions);

    if (owner)
    {
      repository.setPermissions(Lists.newArrayList(new Permission("marvin",
        PermissionType.OWNER)));
    }

    when(event.getRepository()).thenReturn(repository);

    List<Changeset> changsets = Lists.newArrayList(changesets);

    when(event.getChangesets()).thenReturn(changsets);

    return event;
  }
}
