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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

import org.junit.Rule;
import org.junit.Test;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import sonia.scm.group.GroupNames;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Person;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.spi.HookChangesetProvider;
import sonia.scm.repository.spi.HookChangesetRequest;
import sonia.scm.repository.spi.HookChangesetResponse;
import sonia.scm.repository.spi.HookContextProvider;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
@SubjectAware(configuration = "classpath:sonia/scm/branchwp/shiro-001.ini")
public class BranchWPPreReceiveRepositoryHookTest
{

  /** Field description */
  private static final String DUMMY_REALM = "dummy";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void testAllAllowed()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false, "hitchecker");
    Changeset c = createChangeset("0", "master");
    Changeset c2 = createChangeset("1", "devel");

    hook.onEvent(createHookEvent("*,@hitchecker", true, false, c, c2));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAllAllowedBranchProvider()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false, "hitchecker");
    List<String> cm = Lists.newArrayList("master");
    List<String> dc = Lists.newArrayList("devel");

    hook.onEvent(createHookEventBranchProvider("*,@hitchecker", true, false,
      cm, dc));
  }

  /**
   * Method description
   *
   */
  @Test(expected = BranchWPException.class)
  public void testAllAllowedButOneDenied()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false, "hitchecker");
    Changeset c = createChangeset("0", "master");
    Changeset c2 = createChangeset("1", "devel");
    Changeset c3 = createChangeset("2", "feature-x");

    hook.onEvent(
      createHookEventChangesetProvider(
        "*,@hitchecker;!feature-x,@hitchecker", true, false, c, c2, c3));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAsAdmin()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(true);
    Changeset c = createChangeset("0", "master");

    hook.onEvent(createHookEventChangesetProvider("", true, false, c));
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
  @Test(expected = BranchWPException.class)
  public void testDenied()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false, "hitchecker");
    Changeset c = createChangeset("0", "master");

    hook.onEvent(createHookEventChangesetProvider("!master,@hitchecker", true,
      false, c));
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
  public void testGlobDenied()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false, "hitchecker");
    Changeset c = createChangeset("0", "master");

    hook.onEvent(createHookEvent("*,@hitchecker;!*ter,@hitchecker", true,
      false, c));
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
  @Test(expected = BranchWPException.class)
  public void testMultipleConfigAccessDeniedWithBranchProvider()
  {
    BranchWPPreReceiveRepositoryHook hook = createHook(false, "ka", "noother");
    List<String> cm = Lists.newArrayList("master");
    List<String> dc = Lists.newArrayList("default");

    hook.onEvent(
      createHookEventBranchProvider(
        "master,dent;master,marvin;default,other", true, false, cm, dc));
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

    GroupNames groupNames = new GroupNames(Lists.newArrayList(groups));

    SimplePrincipalCollection principals = new SimplePrincipalCollection();

    principals.add(marvin.getName(), DUMMY_REALM);
    principals.add(marvin, DUMMY_REALM);
    principals.add(groupNames, DUMMY_REALM);

    Subject s = new Subject.Builder().authenticated(true).principals(
                  principals).buildSubject();

    final Subject subject = mock(Subject.class, new DelegatingAnswer(s));
    org.apache.shiro.authz.Permission p =
      any(org.apache.shiro.authz.Permission.class);

    when(subject.isPermitted(p)).thenReturn(admin);

    return new BranchWPPreReceiveRepositoryHook()
    {

      @Override
      protected Subject getSubject()
      {
        return subject;
      }

    };
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
  private PreReceiveRepositoryHookEvent createHookEvent(String permissions,
    boolean enabled, boolean owner, Changeset... changesets)
  {
    PreReceiveRepositoryHookEvent event =
      mock(PreReceiveRepositoryHookEvent.class);

    Repository repository = createRepository(permissions, enabled, owner);

    when(event.getRepository()).thenReturn(repository);

    List<Changeset> changsets = Lists.newArrayList(changesets);

    when(event.getChangesets()).thenReturn(changsets);

    return event;
  }

  /**
   * Method description
   *
   *
   * @param permissions
   * @param enabled
   * @param owner
   * @param createdOrModified
   * @param deletedOrClosed
   *
   * @return
   */
  private PreReceiveRepositoryHookEvent createHookEventBranchProvider(
    String permissions, boolean enabled, boolean owner,
    List<String> createdOrModified, List<String> deletedOrClosed)
  {
    PreReceiveRepositoryHookEvent event =
      mock(PreReceiveRepositoryHookEvent.class);

    Repository repository = createRepository(permissions, enabled, owner);

    when(event.getRepository()).thenReturn(repository);
    when(event.isContextAvailable()).thenReturn(Boolean.TRUE);

    HookBranchProvider branchProvider = mock(HookBranchProvider.class);

    when(branchProvider.getCreatedOrModified()).thenReturn(createdOrModified);
    when(branchProvider.getDeletedOrClosed()).thenReturn(deletedOrClosed);

    HookContextProvider ctxProvider = mock(HookContextProvider.class);

    when(ctxProvider.getSupportedFeatures()).thenReturn(
      EnumSet.of(HookFeature.BRANCH_PROVIDER));
    when(ctxProvider.getBranchProvider()).thenReturn(branchProvider);

    PreProcessorUtil util = mock(PreProcessorUtil.class, CALLS_REAL_METHODS);

    HookContext ctx = new HookContextFactory(util).createContext(ctxProvider,
                        repository);

    when(event.getContext()).thenReturn(ctx);

    return event;
  }

  /**
   * Method description
   *
   *
   * @param permissions
   * @param enabled
   * @param owner
   * @param changesets
   *
   * @return
   */
  private PreReceiveRepositoryHookEvent createHookEventChangesetProvider(
    String permissions, boolean enabled, boolean owner, Changeset... changesets)
  {
    PreReceiveRepositoryHookEvent event =
      mock(PreReceiveRepositoryHookEvent.class);

    Repository repository = createRepository(permissions, enabled, owner);

    when(event.getRepository()).thenReturn(repository);
    when(event.isContextAvailable()).thenReturn(Boolean.TRUE);

    HookChangesetProvider changesetProvider = mock(HookChangesetProvider.class);

    when(changesetProvider.handleRequest(
      any(HookChangesetRequest.class))).thenReturn(
        new HookChangesetResponse(Lists.newArrayList(changesets)));

    HookContextProvider ctxProvider = mock(HookContextProvider.class);

    when(ctxProvider.getSupportedFeatures()).thenReturn(
      EnumSet.of(HookFeature.CHANGESET_PROVIDER));
    when(ctxProvider.getChangesetProvider()).thenReturn(changesetProvider);

    PreProcessorUtil util = mock(PreProcessorUtil.class, CALLS_REAL_METHODS);

    HookContext ctx = new HookContextFactory(util).createContext(ctxProvider,
                        repository);

    when(event.getContext()).thenReturn(ctx);

    return event;
  }

  /**
   * Method description
   *
   *
   * @param permissions
   * @param enabled
   * @param owner
   *
   * @return
   */
  private Repository createRepository(String permissions, boolean enabled,
    boolean owner)
  {
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

    return repository;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 15/02/17
   * @author         Enter your name here...
   */
  private static class DelegatingAnswer implements Answer<Object>
  {

    /**
     * Constructs ...
     *
     *
     * @param delegate
     */
    public DelegatingAnswer(Object delegate)
    {
      this.delegate = delegate;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param invocation
     *
     * @return
     *
     * @throws Throwable
     */
    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable
    {
      return invocation.getMethod().invoke(delegate, invocation.getArguments());
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Object delegate;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public ShiroRule shiro = new ShiroRule();
}
