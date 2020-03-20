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
package sonia.scm.branchwp;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@SubjectAware(configuration = "classpath:sonia/scm/branchwp/shiro-001.ini", username = "user_1", password = "secret")
public class RepositoryHalEnricherTest {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private RepositoryServiceFactory serviceFactory;

  @Mock
  private RepositoryService service;

  @Mock
  private HalAppender appender;
  private RepositoryHalEnricher enricher;

  public RepositoryHalEnricherTest() {
    // cleanup state that might have been left by other tests
    ThreadContext.unbindSecurityManager();
    ThreadContext.unbindSubject();
    ThreadContext.remove();
  }

  @Before
  public void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldEnrichRepository() {
    enricher = new RepositoryHalEnricher(scmPathInfoStoreProvider, serviceFactory);
    Repository repo = new Repository("id", "type", "space", "name");
    when(serviceFactory.create(repo)).thenReturn(service);
    when(service.isSupported(Command.BRANCHES)).thenReturn(true);
    HalEnricherContext context = HalEnricherContext.of(repo);
    enricher.enrich(context, appender);
    verify(appender).appendLink("branchWpConfig", "https://scm-manager.org/scm/api/v2/plugins/branchwp/space/name");
  }

  @Test
  public void shouldNotEnrichRepositoryBecauseOfMissingPermission() {
    enricher = new RepositoryHalEnricher(scmPathInfoStoreProvider, serviceFactory);
    Repository repo = new Repository("id", "type", "space", "name");
    HalEnricherContext context = HalEnricherContext.of(repo);
    enricher.enrich(context, appender);
    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  public void shouldNotEnrichRepositoryBecauseBranchIsNotSupported() {
    enricher = new RepositoryHalEnricher(scmPathInfoStoreProvider, serviceFactory);
    Repository repo = new Repository("id", "type", "space", "name");
    when(serviceFactory.create(repo)).thenReturn(service);
    when(service.isSupported(Command.BRANCHES)).thenReturn(false);

    HalEnricherContext context = HalEnricherContext.of(repo);
    enricher.enrich(context, appender);
    verify(appender, never()).appendLink(any(), any());
  }
}
