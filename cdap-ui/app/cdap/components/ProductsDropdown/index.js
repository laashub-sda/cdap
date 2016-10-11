/*
 * Copyright © 2016 Cask Data, Inc.
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

import React, {Component, PropTypes} from 'react';
import { Dropdown, DropdownMenu } from 'reactstrap';
import T from 'i18n-react';
import classnames from 'classnames';
require('./ProductsDropdown.less');
import head from 'lodash/head';

export default class ProductsDropdown extends Component {
  constructor(props) {
    super(props);
    let products = [
      {
        link: '/cask-cdap',
        label: T.translate('commons.cdap'),
        name: 'cdap',
        icon: 'icon-fist'
      },
      {
        link: '/cask-hydrator',
        label: T.translate('commons.hydrator'),
        name: 'hydrator',
        icon: 'icon-hydrator'
      },
      {
        link: '/cask-tracker',
        label: T.translate('commons.tracker'),
        name: 'tracker',
        icon: 'icon-tracker'
      }
    ];
    let currentChoice = head(products.filter(product => product.name === props.currentChoice));
    this.state = {
      productsDropdown: false,
      currentChoice,
      products
    };
  }
  toggle() {
    this.setState({
      productsDropdown: !this.state.productsDropdown
    });
  }
  render() {
    return (
      <div>
        <Dropdown
          isOpen={this.state.productsDropdown}
          toggle={this.toggle.bind(this)}
        >
          <div className="current-product"
            onClick={this.toggle.bind(this)}
          >
            <span className={classnames("fa", this.state.currentChoice.icon)}></span>
            <span className="product-name">{this.state.currentChoice.label}</span>
          </div>
          <span className="fa fa-angle-down"></span>
          <DropdownMenu>
            {
              this.state
                .products
                .filter(product => product.name !== this.state.currentChoice.name)
                .map(product => {
                  return (
                    <div className="dropdown-item">
                      <a
                        className={classnames("product-link", product.name)}
                        href={product.link}
                      >
                        <span className={classnames("fa", product.icon)}></span>
                        <span>{product.label}</span>
                      </a>
                    </div>
                  );
                })
            }
          </DropdownMenu>
        </Dropdown>
      </div>
    );
  }
}

ProductsDropdown.propTypes = {
  currentChoice: PropTypes.string
};
