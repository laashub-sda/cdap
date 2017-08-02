/*
 * Copyright © 2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
*/

import React, {PropTypes, Component} from 'react';
import {Row, Col} from 'reactstrap';
import {DragTypes} from 'components/RulesEngineHome/Rule';
import { DropTarget } from 'react-dnd';
import classnames from 'classnames';
import MyRulesEngineApi from 'api/rulesengine';
import NamespaceStore from 'services/NamespaceStore';
import {getRulesForActiveRuleBook} from 'components/RulesEngineHome/RulesEngineStore/RulesEngineActions';

require('./RulesList.scss');

const dropTarget = {
  drop: (props, monitor, component) => {
    let item = monitor.getItem();
    component.addRuleToRulebook(item.rule);
  }
};

function collect(connect, monitor) {
  return {
    connectDropTarget: connect.dropTarget(),
    isOver: monitor.isOver(),
    canDrop: monitor.canDrop()
  };
}

class RulesList extends Component {
  static propTypes = {
    rulebookid: PropTypes.string,
    rules: PropTypes.arrayOf(PropTypes.object),
    onRemove: PropTypes.func,
    connectDropTarget: PropTypes.func.isRequired,
    isOver: PropTypes.bool.isRequired,
    onRuleAdd: PropTypes.func
  };
  addRuleToRulebook(rule) {
    if (this.props.onRuleAdd) {
      this.props.onRuleAdd(rule);
      return;
    }
    let {selectedNamespace: namespace} = NamespaceStore.getState();
    MyRulesEngineApi
      .addRuleToRuleBook({
        namespace,
        rulebookid: this.props.rulebookid,
        ruleid: rule.id
      })
      .subscribe(
        (res) => {
          console.log(res);
          getRulesForActiveRuleBook();
        }
      );
  }
  render() {
    let rules = this.props.rules;
    return this.props.connectDropTarget(
      <div className={classnames("rules-container", {
        'drag-hover': this.props.isOver
      })}>
        <div className="title"> Rules ({rules.length}) </div>
        {
          (!Array.isArray(rules) || (Array.isArray(rules) && !rules.length)) ?
            null
          :
            rules.map((rule, i) => {
              return (
                <Row>
                  <Col xs={1}>{i + 1}</Col>
                  <Col xs={3}>{rule.id}</Col>
                  <Col xs={5}>{rule.description}</Col>
                  <Col xs={3}>
                    <button
                      className="btn btn-link"
                      href
                      onClick={() => this.props.onRemove(rule.id)}
                    >
                      Remove
                    </button>
                  </Col>
                </Row>
              );
            })
        }
      </div>
    );
  }
}

export default DropTarget(DragTypes.RULE, dropTarget, collect)(RulesList);
