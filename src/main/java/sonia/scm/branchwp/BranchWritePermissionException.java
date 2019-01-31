package sonia.scm.branchwp;

public class BranchWritePermissionException extends RuntimeException {

  public BranchWritePermissionException(String message) {
    super(message);
  }
}
