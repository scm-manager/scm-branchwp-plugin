package sonia.scm.branchwp.service;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.group.GroupNames;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
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

  @Mock
  ConfigurationStore<BranchWritePermissions> store;

  ConfigurationStoreFactory storeFactory;


  BranchWritePermissionService service;
  public static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Before
  public void init() {
    storeFactory = new InMemoryConfigurationStoreFactory(store);
    service = new BranchWritePermissionService(storeFactory, null);
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

    verify(store).set(argThat(argPermissions -> {
      assertThat(argPermissions.getPermissions()).hasSize(1);
      assertThat(argPermissions.getPermissions().get(0))
        .isEqualToComparingFieldByField(createBranchWritePermission());
      return true;
    }));
  }

  @Test
  public void shouldFailOnStoringPermissionForNotAdminOrOwnerUsers() {
    BranchWritePermissions permissions = createBranchWPs(true);

    BranchWritePermission permission = createBranchWritePermission();
    permissions.getPermissions().add(permission);

    assertThatThrownBy(() -> service.setPermissions(REPOSITORY, permissions)).hasMessage("Subject does not have permission [repository:branchwp:id-1]");

    verify(store, never()).set(any());
  }

  @Test
  public void shouldReplaceUserNameInBranch() {
    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission("{username}/feature/*", USER.getName(), false, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(branchWritePermission);
    when(store.get()).thenReturn(permissions);

    boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME), REPOSITORY, USERNAME + "/feature/branch_1");

    assertThat(privileged).isTrue();
  }

  @Test
  public void shouldReplaceMailInBranch() {
    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission2 = new BranchWritePermission("{mail}/feature/*", USER.getName(), false, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(branchWritePermission2);
    when(store.get()).thenReturn(permissions);

    Boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME), REPOSITORY, MAIL + "/feature/branch_1");

    assertThat(privileged).isTrue();
  }

  @Test
  @SubjectAware(username = "owner", password = "secret")
  public void shouldAllowRepositoryOwnerWithoutReadingPermissions() {
    User admin = new User("owner");
    admin.setAdmin(false);
    boolean privileged = service.isPrivileged(admin, new GroupNames(GROUP_NAME), REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
    verify(store, never()).get();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldAllowAdminWithoutReadingPermissions() {
    User admin = new User("admin");
    admin.setAdmin(true);
    boolean privileged = service.isPrivileged(admin, new GroupNames(GROUP_NAME), REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
    verify(store, never()).get();
  }

  @Test
  public void shouldAllowAnyUserIfTheConfigIsDisabled() {
    BranchWritePermissions permissions = createBranchWPs(true);
    permissions.setEnabled(false);
    when(store.get()).thenReturn(permissions);
    boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME), REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
    verify(store).get();
  }

  @Test
  public void shouldPrivilegeUserBecauseTheBranchIsAllowedToTheUser() {
    BranchWritePermissions permissions = createBranchWPs(true);
    permissions.getPermissions().add(createBranchWritePermission());
    when(store.get()).thenReturn(permissions);

    boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME), REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
  }

  @Test
  public void shouldPrivilegeUserBecauseAllBranchesAreAllowedToTheUser() {
    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission("*", USER.getName(), false, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(branchWritePermission);
    when(store.get()).thenReturn(permissions);

    boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME), REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
  }

  @Test
  public void shouldPrivilegeUserBecauseAllBranchesAreAllowedToOneOfHisGroups() {
    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission("*", GROUP_NAME, true, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(branchWritePermission);
    when(store.get()).thenReturn(permissions);

    boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME, "group2", "group3"), REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
  }

  @Test
  public void shouldPrivilegeUserBecauseTheSearchedBranchIsAllowedToOneOfHisGroups() {
    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission(BRANCH, GROUP_NAME, true, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(branchWritePermission);
    when(store.get()).thenReturn(permissions);

    boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME, "group2", "group3"), REPOSITORY, BRANCH);

    assertThat(privileged).isTrue();
  }

  private BranchWritePermissions createBranchWPs(boolean enabled) {
    BranchWritePermissions permissions = new BranchWritePermissions();
    permissions.setEnabled(enabled);
    return permissions;
  }

  @Test
  public void shouldDenyPermissionBecauseAllBranchesAreAllowedToTheUserButTheSearchedBranchIsDenied() {
    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission("*", USER.getName(), false, BranchWritePermission.Type.ALLOW);
    BranchWritePermission deniedPermission = new BranchWritePermission(BRANCH, USER.getName(), false, BranchWritePermission.Type.DENY);
    permissions.getPermissions().add(branchWritePermission);
    permissions.getPermissions().add(deniedPermission);
    when(store.get()).thenReturn(permissions);

    boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME), REPOSITORY, BRANCH);

    assertThat(privileged).isFalse();
  }

  @Test
  public void shouldDenyPermissionBecauseAllBranchesAreAllowedToOneOfTheUserGroupsButTheSearchedBranchIsDenied() {
    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission branchWritePermission = new BranchWritePermission("*", GROUP_NAME, true, BranchWritePermission.Type.ALLOW);
    BranchWritePermission deniedPermission = new BranchWritePermission(BRANCH, USER.getName(), false, BranchWritePermission.Type.DENY);
    permissions.getPermissions().add(branchWritePermission);
    permissions.getPermissions().add(deniedPermission);
    when(store.get()).thenReturn(permissions);

    boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME, "group2", "group3"), REPOSITORY, BRANCH);

    assertThat(privileged).isFalse();
  }

  @Test
  public void shouldDenyPermissionBecauseTheSearchedBranchIsDenied() {
    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission deniedPermission = new BranchWritePermission(BRANCH, USER.getName(), false, BranchWritePermission.Type.DENY);
    permissions.getPermissions().add(deniedPermission);
    when(store.get()).thenReturn(permissions);

    boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME), REPOSITORY, BRANCH);

    assertThat(privileged).isFalse();
  }

  @Test
  public void shouldDenyPermissionBecauseTheSearchedBranchIsDeniedToOneOfTheUserGroups() {
    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission deniedPermission = new BranchWritePermission(BRANCH, GROUP_NAME, true, BranchWritePermission.Type.DENY);
    permissions.getPermissions().add(deniedPermission);
    when(store.get()).thenReturn(permissions);

    boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME, "group2", "group3"), REPOSITORY, BRANCH);

    assertThat(privileged).isFalse();
  }

  @Test
  public void shouldDenyPermissionBecauseThereIsNoStoredPermissionForTheSearchedBranch() {
    BranchWritePermissions permissions = createBranchWPs(true);
    BranchWritePermission deniedPermission = new BranchWritePermission("other_branch", USER.getName(), false, BranchWritePermission.Type.ALLOW);
    permissions.getPermissions().add(deniedPermission);
    when(store.get()).thenReturn(permissions);

    boolean privileged = service.isPrivileged(USER, new GroupNames(GROUP_NAME), REPOSITORY, BRANCH);

    assertThat(privileged).isFalse();
  }

  private BranchWritePermission createBranchWritePermission() {
    return new BranchWritePermission(BRANCH, USER.getName(), GROUP, TYPE);
  }
}
