package sonia.scm.branchwp.service;

import com.google.common.base.Strings;
import sonia.scm.group.GroupNames;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.GlobUtil;

import javax.inject.Inject;
import java.util.function.BooleanSupplier;

/**
 * Store the branch write permissions in the repository store.
 *
 * @author Mohamed Karray
 */
public class BranchWritePermissionService {

  public static final String VAR_MAIL = "\\{mail\\}";
  public static final String VAR_USERNAME = "\\{username\\}";

  private ConfigurationStoreFactory storeFactory;
  private RepositoryServiceFactory repositoryServiceFactory;
  private static final String STORE_NAME = "branchWritePermission";

  @Inject
  public BranchWritePermissionService(ConfigurationStoreFactory storeFactory, RepositoryServiceFactory repositoryServiceFactory) {
    this.storeFactory = storeFactory;
    this.repositoryServiceFactory = repositoryServiceFactory;
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
   * @param userGroups
   * @param repository
   * @param branch
   * @return true if the user is permitted to write the branch
   */
  public boolean isPrivileged(User user, GroupNames userGroups, Repository repository, String branch) {
    AssertUtil.assertIsNotNull(user);
    if (isPermitted(repository)) {
      return true;
    }

    ConfigurationStore<BranchWritePermissions> store = getStore(repository);
    BranchWritePermissions permissions = store.get();
    if (!permissions.isEnabled()) {
      return true;
    }

    BooleanSupplier userAllowed = () -> hasUserPermission(user, branch, permissions, BranchWritePermission.Type.ALLOW);
    BooleanSupplier anyUserGroupsAllowed = () -> hasAnyGroupPermission(userGroups, branch, permissions, BranchWritePermission.Type.ALLOW, user);
    BooleanSupplier userDenied = () -> hasUserPermission(user, branch, permissions, BranchWritePermission.Type.DENY);
    BooleanSupplier anyUserGroupsDenied = () -> hasAnyGroupPermission(userGroups, branch, permissions, BranchWritePermission.Type.DENY, user);

    return !userDenied.getAsBoolean() && !anyUserGroupsDenied.getAsBoolean() && (userAllowed.getAsBoolean() || anyUserGroupsAllowed.getAsBoolean());
  }

  public static boolean isPermitted(Repository repository) {
    return RepositoryPermissions.modify(repository).isPermitted();
  }

  public void checkPermission(Repository repository) {
    RepositoryPermissions.modify(repository).check();
  }

  private boolean hasAnyGroupPermission(GroupNames userGroups, String branch, BranchWritePermissions permissions, BranchWritePermission.Type type, User user) {
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
    Repository repository;
    try (RepositoryService repositoryService = repositoryServiceFactory.create(new NamespaceAndName(namespace, name))) {
      repository = repositoryService.getRepository();
    }
    return repository;
  }

  public BranchWritePermissions getPermissions(String namespace, String name) {
    Repository repository = getRepository(namespace, name);
    checkPermission(repository);
    ConfigurationStore<BranchWritePermissions> store = getStore(repository);
    return store.get();
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
