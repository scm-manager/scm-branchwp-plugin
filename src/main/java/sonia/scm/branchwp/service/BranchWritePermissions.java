package sonia.scm.branchwp.service;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@XmlRootElement(name = "branch-write-permissions")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class BranchWritePermissions {

  private boolean isEnabled = true;
  private List<BranchWritePermission> permissions = new ArrayList<>();

}
