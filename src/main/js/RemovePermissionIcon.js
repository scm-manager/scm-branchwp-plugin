// @flow
import React from "react";
import type { BranchWP } from "./BranchWP";

type Props = {
  permission: BranchWP,
  onDeletePermission: BranchWP => void
};
type State = {};
class RemovePermissionIcon extends React.Component<Props, State> {
  render() {
    return (
      <a
        className="level-item"
        onClick={() => {this.props.onDeletePermission(this.props.permission)}}
      >
        <span className="icon is-small">
          <i className="fas fa-trash" />
        </span>
      </a>
    );
  }
}

export default RemovePermissionIcon;
