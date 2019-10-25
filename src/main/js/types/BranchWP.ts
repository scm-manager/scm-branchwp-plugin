export type BranchWP = {
  branch: string;
  name: string;
  group: boolean;
  type: string;
};

export type BranchWPs = {
  permissions: BranchWP[];
  enabled: boolean;
};
