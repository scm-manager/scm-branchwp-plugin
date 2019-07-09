package sonia.scm.branchwp.update;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.branchwp.service.BranchWritePermission;
import sonia.scm.branchwp.service.BranchWritePermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.update.V1PropertyDaoTestUtil;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class BranchWPV2RepositoryConfigMigrationUpdateStepTest {

  private final static String REPO_NAME = "repo";
  private static final String STORE_NAME = "branchWritePermission";

  V1PropertyDaoTestUtil testUtil = new V1PropertyDaoTestUtil();

  private final ConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();

  private BranchWPV2RepositoryConfigMigrationUpdateStep updateStep;

  @Before
  public void init() {
    updateStep = new BranchWPV2RepositoryConfigMigrationUpdateStep(testUtil.getPropertyDAO(), storeFactory);
  }

  @Test
  public void shouldMigratingMultiplePermissionsForRepository() throws IOException {
    ImmutableMap<String, String> mockedValues =
      ImmutableMap.of(
        "branchwp.permissions","!master,Tony;master,@Edi;",
        "branchwp.enabled", "true"
      );

    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    BranchWritePermission permission1 = new BranchWritePermission("master", "Tony", false, BranchWritePermission.Type.DENY);
    BranchWritePermission permission2 = new BranchWritePermission("master", "Edi", true, BranchWritePermission.Type.ALLOW);

    assertThat(getConfigStore().get().getPermissions().get(0).getBranch()).isEqualToIgnoringCase(permission1.getBranch());
    assertThat(getConfigStore().get().getPermissions().get(0).getName()).isEqualToIgnoringCase(permission1.getName());
    assertThat(getConfigStore().get().getPermissions().get(0).isGroup()).isEqualTo(permission1.isGroup());
    assertThat(getConfigStore().get().getPermissions().get(0).getType()).isEqualTo(permission1.getType());
    assertThat(getConfigStore().get().getPermissions().get(1).getBranch()).isEqualToIgnoringCase(permission2.getBranch());
    assertThat(getConfigStore().get().getPermissions().get(1).getName()).isEqualToIgnoringCase(permission2.getName());
    assertThat(getConfigStore().get().getPermissions().get(1).isGroup()).isEqualTo(permission2.isGroup());
    assertThat(getConfigStore().get().getPermissions().get(1).getType()).isEqualTo(permission2.getType());
    assertThat(getConfigStore().get().isEnabled()).isTrue();
  }

  @Test
  public void shouldSkipRepositoriesIfPermissionsAreEmpty() throws IOException {
    ImmutableMap<String, String> mockedValues =
      ImmutableMap.of(
        "branchwp.permissions", ""
      );
    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    assertThat(getConfigStore().get()).isNull();
  }

  private ConfigurationStore<BranchWritePermissions> getConfigStore() {
    return storeFactory.withType(BranchWritePermissions.class).withName(STORE_NAME).forRepository(REPO_NAME).build();
  }
}
