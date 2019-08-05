package sonia.scm.branchwp.service;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.group.GroupCollector;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/branchwp/shiro-001.ini", username = "user_1", password = "secret")
public class BranchWritePermissionServiceTest {

  public static final String MAIL = "email@d.de";
  public static final String USERNAME = "user_1";
  public static final User USER = new User(USERNAME, "User 1", MAIL);
  public static final String BRANCH = "feature/branch_1";
  public static final BranchWritePermission.Type TYPE = BranchWritePermission.Type.ALLOW;
  public static final boolean GROUP = false;
  public static final String GROUP_NAME = "group1";

  @Rule
  public ShiroRule shiro = new ShiroRule();

  ConfigurationStore<BranchWritePermissions> store;

  ConfigurationStoreFactory storeFactory;

  @Mock
  GroupCollector groupCollector;

  BranchWritePermissionService service;
  public static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Before
  public void init() {
    storeFactory = new InMemoryConfigurationStoreFactory();
    service = new BranchWritePermissionService(storeFactory, null, groupCollector);
    store = storeFactory.withType(BranchWritePermissions.class).withName("branchWritePermission").forRepository(REPOSITORY).build();
  }

  public BranchWritePermissionServiceTest() {
    // cleanup state that might have been left by other tests
    ThreadContext.unbindSecurityManager();
    ThreadContext.unbindSubject();
    ThreadContext.remove();
  }


  @Test
  @SubjectAware(username = "owner", password = "secret")
  public void shouldStorePermissionForOwner() {
    BranchWritePermissions permissions = createBranchWPs(true);

    BranchWritePermission permission = createBranchWritePermission();
    permissions.getPermissions().add(permission);
    service.setPermissions(REPOSITORY, permissions);

    assertThat(store.get()).isSameAs(permissions);
  }

  @Test
  public void shouldFailOnStoringPermissionForNotAdminOrOwnerUsers() {
    BranchWritePermissions permissions = createBranchWPs(true);

    BranchWritePermission permission = createBranchWritePermission();
    permissions.getPermissions().add(permission);
    store.set(permissions);

    assertThatThrownBy(() -> service.setPermissions(REPOSITORY, permissions)).hasMessage("Subject does not have permission [repository:branchwp:id-1]");
  }

  @Test
  public void shouldReplaceUserNameInBranch() {
    assignGroups(GROUP_NAME);

    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission("{username}/feature/*", USER.getName(), false, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(branchWritePermission);
    store.set(permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, USERNAME + "/feature/branch_1");

    assertThat(privileged).isTrue();
  }

  private void assignGroups(String... groups) {
    when(groupCollector.collect(USER.getName())).thenReturn(ImmutableSet.copyOf(groups));
  }

  @Test
  public void shouldReplaceMailInBranch() {
    assignGroups(GROUP_NAME);

    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission2 = new BranchWritePermission("{mail}/feature/*", USER.getName(), false, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(branchWritePermission2);
    store.set(permissions);

    Boolean privileged = service.isPrivileged(USER, REPOSITORY, MAIL + "/feature/branch_1");

    assertThat(privileged).isTrue();
  }

  @Test
  @SubjectAware(username = "owner", password = "secret")
  public void shouldAllowRepositoryOwnerWithoutReadingPermissions() {
    assignGroups(GROUP_NAME);

    User admin = new User("owner");
    boolean privileged = service.isPrivileged(admin, REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldAllowAdminWithoutReadingPermissions() {
    assignGroups(GROUP_NAME);

    User admin = new User("admin");
    boolean privileged = service.isPrivileged(admin, REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
  }

  @Test
  public void shouldAllowAnyUserIfTheConfigIsDisabled() {
    assignGroups(GROUP_NAME);

    BranchWritePermissions permissions = createBranchWPs(false);
    store.set(permissions);
    boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
  }

  @Test
  public void shouldPrivilegeUserBecauseTheBranchIsAllowedToTheUser() {
    assignGroups(GROUP_NAME);

    BranchWritePermissions permissions = createBranchWPs(true);
    permissions.getPermissions().add(createBranchWritePermission());
    store.set(permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
  }

  @Test
  public void shouldPrivilegeUserBecauseAllBranchesAreAllowedToTheUser() {
    assignGroups(GROUP_NAME);

    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission("*", USER.getName(), false, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(branchWritePermission);
    store.set(permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
  }

  @Test
  public void shouldPrivilegeUserBecauseAllBranchesAreAllowedToOneOfHisGroups() {
    assignGroups(GROUP_NAME, "group2", "group3");

    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission("*", GROUP_NAME, true, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(branchWritePermission);
    store.set(permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
  }

  @Test
  public void shouldPrivilegeUserBecauseTheSearchedBranchIsAllowedToOneOfHisGroups() {
    assignGroups(GROUP_NAME, "group2", "group3");

    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission(BRANCH, GROUP_NAME, true, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(branchWritePermission);
    store.set(permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
  }

  private BranchWritePermissions createBranchWPs(boolean enabled) {
    BranchWritePermissions permissions = new BranchWritePermissions();
    permissions.setEnabled(enabled);
    return permissions;
  }

  @Test
  public void shouldDenyPermissionBecauseAllBranchesAreAllowedToTheUserButTheSearchedBranchIsDenied() {
    assignGroups(GROUP_NAME);

    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission("*", USER.getName(), false, BranchWritePermission.Type.ALLOW);
    BranchWritePermission deniedPermission = new BranchWritePermission(BRANCH, USER.getName(), false, BranchWritePermission.Type.DENY);
    permissions.getPermissions().add(branchWritePermission);
    permissions.getPermissions().add(deniedPermission);
    store.set(permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH);

    assertThat(privileged).isFalse();
  }

  @Test
  public void shouldDenyPermissionBecauseAllBranchesAreAllowedToOneOfTheUserGroupsButTheSearchedBranchIsDenied() {
    assignGroups(GROUP_NAME, "group2", "group3");

    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission("*", GROUP_NAME, true, BranchWritePermission.Type.ALLOW);
    BranchWritePermission deniedPermission = new BranchWritePermission(BRANCH, USER.getName(), false, BranchWritePermission.Type.DENY);
    permissions.getPermissions().add(branchWritePermission);
    permissions.getPermissions().add(deniedPermission);
    store.set(permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH);

    assertThat(privileged).isFalse();
  }

  @Test
  public void shouldDenyPermissionBecauseTheSearchedBranchIsDenied() {
    assignGroups(GROUP_NAME);

    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission deniedPermission = new BranchWritePermission(BRANCH, USER.getName(), false, BranchWritePermission.Type.DENY);
    permissions.getPermissions().add(deniedPermission);
    store.set(permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH);

    assertThat(privileged).isFalse();
  }

  @Test
  public void shouldDenyPermissionBecauseTheSearchedBranchIsDeniedToOneOfTheUserGroups() {
    assignGroups(GROUP_NAME, "group2", "group3");

    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission deniedPermission = new BranchWritePermission(BRANCH, GROUP_NAME, true, BranchWritePermission.Type.DENY);
    permissions.getPermissions().add(deniedPermission);
    store.set(permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH);

    assertThat(privileged).isFalse();
  }

  @Test
  public void shouldDenyPermissionBecauseThereIsNoStoredPermissionForTheSearchedBranch() {
    assignGroups(GROUP_NAME);

    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission deniedPermission = new BranchWritePermission("other_branch", USER.getName(), false, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(deniedPermission);
    store.set(permissions);

    boolean privileged = service.isPrivileged(USER, REPOSITORY, BRANCH);

    assertThat(privileged).isFalse();
  }

  private BranchWritePermission createBranchWritePermission() {
    return new BranchWritePermission(BRANCH, USER.getName(), GROUP, TYPE);
  }
}
