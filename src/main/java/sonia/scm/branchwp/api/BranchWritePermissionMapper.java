package sonia.scm.branchwp.api;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.branchwp.service.BranchWritePermission;
import sonia.scm.branchwp.service.BranchWritePermissions;
import sonia.scm.repository.NamespaceAndName;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Mapper
public abstract class BranchWritePermissionMapper {

  private LinkBuilder linkBuilder;

  public abstract BranchWritePermission map(BranchWritePermissionDto dto);

  @Mapping(target = "attributes", ignore = true)
  public abstract BranchWritePermissionDto map(BranchWritePermission branchWritePermission);

  public abstract BranchWritePermissions map(BranchWritePermissionsDto dto);

  @Mapping(target = "attributes", ignore = true)
  public abstract BranchWritePermissionsDto map(BranchWritePermissions branchWritePermissions, @Context NamespaceAndName namespaceAndName);


  public BranchWritePermissionMapper using(UriInfo uriInfo) {
    this.linkBuilder = new LinkBuilder(uriInfo::getBaseUri, BranchWritePermissionResource.class);
    return this;
  }

  @AfterMapping
  void addLinks(@MappingTarget BranchWritePermissionsDto dto, @Context NamespaceAndName namespaceAndName) {
    dto.add(Links.linkingTo().self(linkBuilder.method("get").parameters(namespaceAndName.getNamespace(), namespaceAndName.getName()).href()).build());

  }

}
