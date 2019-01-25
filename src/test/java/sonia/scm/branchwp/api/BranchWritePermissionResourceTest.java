package sonia.scm.branchwp.api;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.branchwp.service.BranchWritePermission;
import sonia.scm.branchwp.service.BranchWritePermissionService;
import sonia.scm.branchwp.service.BranchWritePermissions;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BranchWritePermissionResourceTest {

  public static final String PERMISSIONS_JSON = "{\"permissions\":" +
    "[{\"branch\":\"branch\"," +
    "\"name\":\"user_1\"," +
    "\"group\":false," +
    "\"type\":\"ALLOW\"" +
    "}" +
    "]," +
    "\"enabled\":true," +
    "\"_links\":{" +
    "\"self\":{" +
    "\"href\":\"/v2/plugins/branchwp/space/repo\"}" +
    "}" +
    "}";
  private BranchWritePermissionResource resource;

  @Mock
  BranchWritePermissionService service;

  private BranchWritePermissionMapper mapper = new BranchWritePermissionMapperImpl();

  private Dispatcher dispatcher;
  private final MockHttpResponse response = new MockHttpResponse();


  @BeforeEach
  public void init() {
    resource = new BranchWritePermissionResource(service, mapper);
    dispatcher = MockDispatcherFactory.createDispatcher();
    dispatcher.getRegistry().addSingletonResource(resource);
  }

  @Test
  public void shouldGetBranchWritePermissions() throws URISyntaxException {
    BranchWritePermissions permissions = new BranchWritePermissions();
    permissions.getPermissions().add(new BranchWritePermission("branch", "user_1", false, BranchWritePermission.Type.ALLOW));
    when(service.getPermissions("space", "repo")).thenReturn(permissions);

    MockHttpRequest request = MockHttpRequest
      .get("/" + BranchWritePermissionResource.PATH + "/space/repo")
      .accept(MediaType.APPLICATION_JSON);

    dispatcher.invoke(request, response);
    assertThat(response.getStatus())
      .isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .isEqualTo(PERMISSIONS_JSON);
  }

  @Test
  public void shouldPUTBranchWritePermissions() throws URISyntaxException {

    MockHttpRequest request = MockHttpRequest
      .put("/" + BranchWritePermissionResource.PATH + "/space/repo")
      .contentType(MediaType.APPLICATION_JSON)
      .content(PERMISSIONS_JSON.getBytes());

    dispatcher.invoke(request, response);
    assertThat(response.getStatus())
      .isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(service).setPermissions(eq("space"), eq("repo"), argThat(branchWritePermissions -> {
      BranchWritePermissions permissions = new BranchWritePermissions();
      permissions.getPermissions().add(new BranchWritePermission("branch", "user_1", false, BranchWritePermission.Type.ALLOW));
      assertThat(branchWritePermissions).isEqualToComparingFieldByFieldRecursively(permissions);
      return true;
    }));
  }

}
