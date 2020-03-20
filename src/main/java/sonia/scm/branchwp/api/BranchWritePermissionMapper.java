/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

import static de.otto.edison.hal.Link.link;

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
    Links.Builder links = Links.linkingTo();
    links.self(linkBuilder.method("get").parameters(namespaceAndName.getNamespace(), namespaceAndName.getName()).href());
    links.single(link("update", linkBuilder.method("put").parameters(namespaceAndName.getNamespace(), namespaceAndName.getName()).href()));
    dto.add(links.build());
  }

}
