package sonia.scm.branchwp.api;

import de.otto.edison.hal.HalRepresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class BranchWritePermissionDto extends HalRepresentation {

  private String branch;
  private String name;
  private boolean group;
  private String type;

}
