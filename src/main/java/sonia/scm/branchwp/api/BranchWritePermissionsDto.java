package sonia.scm.branchwp.api;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class BranchWritePermissionsDto extends HalRepresentation {
  private boolean isEnabled = true;
  private List<BranchWritePermissionDto> permissions = new ArrayList<>();

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

}
