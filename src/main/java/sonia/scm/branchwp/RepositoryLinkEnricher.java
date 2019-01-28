package sonia.scm.branchwp;

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.LinkAppender;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.LinkEnricher;
import sonia.scm.api.v2.resources.LinkEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.branchwp.api.BranchWritePermissionResource;
import sonia.scm.branchwp.service.BranchWritePermissionService;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements LinkEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  private RepositoryServiceFactory serviceFactory;

  @Inject
  public RepositoryLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider, RepositoryServiceFactory serviceFactory) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
    this.serviceFactory = serviceFactory;
  }

  @Override
  public void enrich(LinkEnricherContext context, LinkAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    try (RepositoryService repositoryService = serviceFactory.create(repository)) {
      if (BranchWritePermissionService.isPermitted(repository) && repositoryService.isSupported(Command.BRANCHES)) {
        LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), BranchWritePermissionResource.class);
        appender.appendOne("branchWpConfig", linkBuilder.method("get").parameters(repository.getNamespace(), repository.getName()).href());
      }
    }
  }
}
