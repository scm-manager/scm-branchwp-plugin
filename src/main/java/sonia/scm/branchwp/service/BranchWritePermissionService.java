package sonia.scm.branchwp.service;

import com.google.common.base.Strings;
import sonia.scm.group.GroupCollector;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.GlobUtil;

import javax.inject.Inject;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Store the branch write permissions in the repository store.
 *
 * @author Mohamed Karray
 */
public class BranchWritePermissionService {

  public static final String VAR_MAIL = "\\{mail\\}";
  public static final String VAR_USERNAME = "\\{username\\}";
  public static final String CUSTOM_ACTION = "branchwp";

  private final ConfigurationStoreFactory storeFactory;
  private final RepositoryManager repositoryManager;
  private final GroupCollector groupCollector;
  private static final String STORE_NAME = "branchWritePermission";

  @Inject
  public BranchWritePermissionService(ConfigurationStoreFactory storeFactory, RepositoryManager repositoryManager, GroupCollector groupCollector) {
    this.storeFactory = storeFactory;
    this.repositoryManager = repositoryManager;
    this.groupCollector = groupCollector;
  }

  /**
   * The user is privileged for the given branch
   * if he or one of his groups has not the DENY permission
   * and
   * if he or one of his groups has the ALLOW permission
   * <p>
   * The user is not privileged if there is no permission found for him or one of his groups.
   *
   * @param user
   * @param repository
   * @param branch
   * @return true if the user is permitted to write the branch
   */
  public boolean isPrivileged(User user, Repository repository, String branch) {
    AssertUtil.assertIsNotNull(user);

    BranchWritePermissions permissions = getPermissions(repository);
    if (!isPluginEnabled(permissions)) {
      return true;
    }

    Set<String> groups = groupCollector.collect(user.getName());

    BooleanSupplier userAllowed = () -> hasUserPermission(user, branch, permissions, BranchWritePermission.Type.ALLOW);
    BooleanSupplier anyUserGroupsAllowed = () -> hasAnyGroupPermission(groups, branch, permissions, BranchWritePermission.Type.ALLOW, user);
    BooleanSupplier userDenied = () -> hasUserPermission(user, branch, permissions, BranchWritePermission.Type.DENY);
    BooleanSupplier anyUserGroupsDenied = () -> hasAnyGroupPermission(groups, branch, permissions, BranchWritePermission.Type.DENY, user);

    return !userDenied.getAsBoolean() && !anyUserGroupsDenied.getAsBoolean() && (userAllowed.getAsBoolean() || anyUserGroupsAllowed.getAsBoolean());
  }

  public boolean isPrivileged(User user, NamespaceAndName namespaceAndName, String branch) {
    return isPrivileged(user, repositoryManager.get(namespaceAndName), branch);
  }

  public static boolean isPermitted(Repository repository) {
    return RepositoryPermissions.custom(CUSTOM_ACTION, repository).isPermitted();
  }

  private boolean isPluginEnabled(BranchWritePermissions permissions){
    return permissions.isEnabled();
  }

  public boolean isPluginEnabled(Repository repository){
    BranchWritePermissions permissions = getPermissions(repository);
    return isPluginEnabled(permissions);
  }

  public void checkPermission(Repository repository) {
    RepositoryPermissions.custom(CUSTOM_ACTION, repository).check();
  }

  private boolean hasAnyGroupPermission(Set<String> userGroups, String branch, BranchWritePermissions permissions, BranchWritePermission.Type type, User user) {
    return permissions.getPermissions().stream()
      .filter(branchWritePermission -> matchBranch(branch, branchWritePermission, user))
      .filter(BranchWritePermission::isGroup)
      .filter(branchWritePermission -> userGroups.contains(branchWritePermission.getName()))
      .anyMatch(branchWritePermission -> branchWritePermission.getType().equals(type));
  }

  private boolean hasUserPermission(User user, String branch, BranchWritePermissions permissions, BranchWritePermission.Type type) {
    return permissions.getPermissions().stream()
      .filter(branchWritePermission -> matchBranch(branch, branchWritePermission, user))
      .filter(branchWritePermission -> !branchWritePermission.isGroup())
      .filter(branchWritePermission -> user.getName().equals(branchWritePermission.getName()))
      .anyMatch(branchWritePermission -> branchWritePermission.getType().equals(type));
  }

  private boolean matchBranch(String branch, BranchWritePermission branchWritePermission, User user) {
    String branchPattern = branchWritePermission.getBranch();
    if (user != null) {
      branchPattern = branchPattern.replaceAll(VAR_USERNAME, Strings.nullToEmpty(user.getName()))
        .replaceAll(VAR_MAIL, Strings.nullToEmpty(user.getMail()));
    }
    return GlobUtil.matches(branchPattern, branch);
  }

  private ConfigurationStore<BranchWritePermissions> getStore(Repository repository) {
    return storeFactory.withType(BranchWritePermissions.class).withName(STORE_NAME).forRepository(repository).build();
  }
  private Repository getRepository(String namespace, String name) {
    return repositoryManager.get(new NamespaceAndName(namespace, name));
  }

  public BranchWritePermissions getPermissions(String namespace, String name) {
    Repository repository = getRepository(namespace, name);
    checkPermission(repository);
    return getPermissions(repository);
  }

  private BranchWritePermissions getPermissions(Repository repository) {
    ConfigurationStore<BranchWritePermissions> store = getStore(repository);
    BranchWritePermissions permissions = store.get();
    if (permissions == null) {
      permissions = new BranchWritePermissions();
      store.set(permissions);
    }
    return permissions;
  }

  public void setPermissions(String namespace, String name, BranchWritePermissions permissions) {
    setPermissions(getRepository(namespace, name), permissions);

  }

  public void setPermissions(Repository repository, BranchWritePermissions permissions) {
    checkPermission(repository);
    ConfigurationStore<BranchWritePermissions> store = getStore(repository);
    store.set(permissions);
  }
}
