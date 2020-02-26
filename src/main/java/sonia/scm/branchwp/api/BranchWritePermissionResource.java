package sonia.scm.branchwp.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.branchwp.service.BranchWritePermissionService;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@OpenAPIDefinition(tags = {
  @Tag(name = "BranchWP Plugin", description = "BranchWP plugin provided endpoints")
})
@Path(BranchWritePermissionResource.PATH)
public class BranchWritePermissionResource {
  public static final String PATH = "v2/plugins/branchwp";

  private BranchWritePermissionService service;
  private BranchWritePermissionMapper mapper;

  @Inject
  public BranchWritePermissionResource(BranchWritePermissionService service, BranchWritePermissionMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GET
  @Path("/{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get branchwp configuration", description = "Returns the branchwp configuration.", tags = "BranchWP Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = BranchWritePermissionsDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the \"branchwp\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public BranchWritePermissionsDto get(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name) {
    return mapper.using(uriInfo).map(service.getPermissions(namespace, name), new NamespaceAndName(namespace, name));
  }


  @PUT
  @Path("/{namespace}/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Update branchwp configuration", description = "Modifies the branchwp configuration.", tags = "AuthorMapping Plugin")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the \"branchwp\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void put(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name, BranchWritePermissionsDto permissions) {
    service.setPermissions(namespace, name, mapper.using(uriInfo).map(permissions));
  }

}
