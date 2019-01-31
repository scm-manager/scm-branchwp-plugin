// @flow

import React from "react";
import type { BranchWP } from "./BranchWP";
import BranchWPTableRow from "./BranchWPTableRow";

type Props = {
  permissions: BranchWP[],
  // permissionListChanged: (permissions: BranchWP[]) => void
  onDelete: BranchWP => void
};

class BranchWPTable extends React.Component<Props> {
  render() {
    const tableRows = this.props.permissions.map(branchWP => {
      return (
        <>
          <BranchWPTableRow permission={branchWP} onDelete={permission => {
            this.props.onDelete(permission)
          }}/>
        </>
      );
    });
    return (
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
          <th>Branch</th>
          <th>Name</th>
          <th>Type</th>
          <th>Group</th>
          <th>Delete</th>
        </thead>
        {tableRows}
      </table>
    );
  }
}

export default BranchWPTable;
