//@flow
import React from "react";
import { connect } from "react-redux";
import {
  confirmAlert,
  DropDown,
  LabelWithHelpIcon,
  InputField,
  Autocomplete
} from "@scm-manager/ui-components";
import type { SelectValue, Link } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import type { BranchWP } from "./BranchWP";

type Props = {
  branchWP: BranchWP,
  readOnly: boolean,
  onChange: BranchWP => void,
  onDelete: BranchWP => void,
  userAutocompleteLink: string,
  groupAutocompleteLink: string,
  // context prop
  t: string => string
};

type State = BranchWP;

class BranchWPComponent extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = props.branchWP;
  }

  componentWillReceiveProps(nextProps) {
    // update the branchwp in the state if the prop are changed
    // The prop can be modified if branchwps are deleted
    if (nextProps.branchWP !== this.props.branchWP) {
      this.state = nextProps.branchWP;
    }
  }

  handleChange = (value: any, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () => this.props.onChange(this.state)
    );
  };

  handleDropDownChange = (selection: string) => {
    this.setState({ ...this.state, type: selection });
    this.handleChange(selection, "type");
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("scm-branchwp-plugin.confirm-delete.title"),
      message: t("scm-branchwp-plugin.confirm-delete.message"),
      buttons: [
        {
          label: t("scm-branchwp-plugin.confirm-delete.submit"),
          onClick: () => this.props.onDelete(this.state)
        },
        {
          label: t("scm-branchwp-plugin.confirm-delete.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  loadSuggestions = (inputValue: string) => {
    const { group } = this.state;
    return loadAutocompletion(
      group
        ? this.props.groupAutocompleteLink
        : this.props.userAutocompleteLink,
      inputValue
    );
  };


  selectName = (value: SelectValue) => {
    let name = value.value.id;
    this.setState(
      {
        name
      },
      () => this.props.onChange(this.state)
    );
  };

  render() {
    const { t, readOnly } = this.props;
    const { branch, name, group } = this.state;
    const deleteIcon = readOnly ? (
      ""
    ) : (
      <a className="level-item" onClick={this.confirmDelete}>
        <span className="icon is-small">
          <i className="fas fa-trash" />
        </span>
      </a>
    );
    return (
      <article className="media">
        <div className="media-content">
          <LabelWithHelpIcon
            label={
              group
                ? t("scm-branchwp-plugin.form.group")
                : t("scm-branchwp-plugin.form.user")
            }
            helpText={t("scm-branchwp-plugin.form.permission-help-text")}
          />
          <DropDown
            options={["ALLOW", "DENY"]}
            optionSelected={this.handleDropDownChange}
            preselectedOption={this.state.type}
            disabled={readOnly}
          />
          <Autocomplete
            label={
              group
                ? t("scm-branchwp-plugin.form.group-name")
                : t("scm-branchwp-plugin.form.user-name")
            }
            loadSuggestions={this.loadSuggestions}
            helpText={
              group
                ? t("scm-branchwp-plugin.form.group-name-help-text")
                : t("scm-branchwp-plugin.form.user-name-help-text")
            }
            valueSelected={this.selectName}
            value={name}
            placeholder={name}
          />
          <InputField
            name={"branch"}
            placeholder={t("scm-branchwp-plugin.form.branch")}
            label={t("scm-branchwp-plugin.form.branch")}
            helpText={t("scm-branchwp-plugin.form.branch-help-text")}
            value={branch}
            onChange={this.handleChange}
            disabled={readOnly}
          />
        </div>
        <div className="media-right">{deleteIcon}</div>
      </article>
    );
  }
}

function getUserAutoCompleteLink(state: Object): string {
  const link = getLinkCollection(state, "autocomplete").find(
    i => i.name === "users"
  );
  if (link) {
    return link.href;
  }
  return "";
}
function getGroupAutoCompleteLink(state: Object): string {
  const link = getLinkCollection(state, "autocomplete").find(
    i => i.name === "groups"
  );
  if (link) {
    return link.href;
  }
  return "";
}

function getLinkCollection(state: Object, name: string): Link[] {
  if (state.indexResources.links && state.indexResources.links[name]) {
    return state.indexResources.links[name];
  }
  return [];
}

const mapStateToProps = state => {
  const userAutocompleteLink = getUserAutoCompleteLink(state);
  const groupAutocompleteLink = getGroupAutoCompleteLink(state);
  return {
    userAutocompleteLink,
    groupAutocompleteLink
  };
};

const loadAutocompletion = (url: string, inputValue: string) => {
  const link = url + "?q=";
  return fetch(link + inputValue)
    .then(response => response.json())
    .then(json => {
      return json.map(element => {
        const label = element.displayName
          ? `${element.displayName} (${element.id})`
          : element.id;
        return {
          value: element,
          label
        };
      });
    });
}


export default connect(mapStateToProps)(
  translate("plugins")(BranchWPComponent)
);
