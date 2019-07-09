package sonia.scm.branchwp.update;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.branchwp.service.BranchWritePermission;
import sonia.scm.branchwp.service.BranchWritePermissions;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.update.V1Properties;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.version.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.branchwp.service.BranchWritePermission.Type;
import static sonia.scm.update.V1PropertyReader.REPOSITORY_PROPERTY_READER;
import static sonia.scm.version.Version.parse;

@Extension
public class BranchWPV2RepositoryConfigMigrationUpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(BranchWPV2RepositoryConfigMigrationUpdateStep.class);

  private final V1PropertyDAO v1PropertyDAO;
  private final ConfigurationStoreFactory storeFactory;

  private static final String BRANCHWP_ENABLED = "branchwp.enabled";
  private static final String BRANCHWP_PERMISSIONS = "branchwp.permissions";

  private static final String STORE_NAME = "branchWritePermission";

  @Inject
  public BranchWPV2RepositoryConfigMigrationUpdateStep(V1PropertyDAO v1PropertyDAO, ConfigurationStoreFactory storeFactory) {
    this.v1PropertyDAO = v1PropertyDAO;
    this.storeFactory = storeFactory;
  }

  @Override
  public void doUpdate() throws IOException {
    v1PropertyDAO
      .getProperties(REPOSITORY_PROPERTY_READER)
      .havingAnyOf(BRANCHWP_ENABLED, BRANCHWP_PERMISSIONS)
      .forEachEntry((key, properties) -> buildConfig(key, properties).ifPresent(configuration ->
        createConfigStore(key).set(configuration)));
  }

  private Optional<BranchWritePermissions> buildConfig(String repositoryId, V1Properties properties) {
    LOG.debug("migrating repository specific branchwp configuration for repository id {}", repositoryId);

    String v1Permissions = properties.get(BRANCHWP_PERMISSIONS);
    if (Strings.isNullOrEmpty(v1Permissions)) {
      return empty();
    }

    List<String> splittedV1Permissions = Arrays.asList(v1Permissions.split(";"));

    List<BranchWritePermission> mappedPermissions = new ArrayList<>();
    for (String v1Permission : splittedV1Permissions) {
      mappedPermissions.add(createV2Permission(v1Permission));
    }

    BranchWritePermissions v2Permissions = new BranchWritePermissions();
    v2Permissions.setEnabled(Boolean.parseBoolean(properties.get(BRANCHWP_ENABLED)));
    v2Permissions.setPermissions(mappedPermissions);

    return of(v2Permissions);
  }

  private BranchWritePermission createV2Permission(String v1Permission) {
    String[] splittedV1Permission = v1Permission.split(",");

    String branch = splittedV1Permission[0].replaceAll("!","");
    String name = splittedV1Permission[1].replaceAll("@","");
    boolean group = splittedV1Permission[1].contains("@");
    Type type = splittedV1Permission[0].contains("!") ? Type.DENY : Type.ALLOW;

    return new BranchWritePermission(branch, name, group, type);
  }

  private ConfigurationStore<BranchWritePermissions> createConfigStore(String repositoryId) {
    return storeFactory.withType(BranchWritePermissions.class).withName(STORE_NAME).forRepository(repositoryId).build();
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.branchwp.config.repository.xml";
  }
}
