package sonia.scm.branchwp;

import sonia.scm.ExceptionWithContext;
import sonia.scm.repository.Repository;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class BranchWritePermissionException extends ExceptionWithContext {

  public BranchWritePermissionException(Repository repository, String branch) {
    super(entity("Branch", branch).in(repository).build(), "Permission denied to modify branch " + branch);
  }

  @Override
  public String getCode() {
    return "EiRnRS0Tw1";
  }
}
