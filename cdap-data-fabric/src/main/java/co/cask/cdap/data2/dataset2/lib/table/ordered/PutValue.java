/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.data2.dataset2.lib.table.ordered;


/**
 * Represents a normal (full) write to a datastore for in-memory buffering, overwriting the previously stored value.
 */
public class PutValue implements Update<byte[]> {
  private final byte[] bytes;

  public PutValue(byte[] bytes) {
    this.bytes = bytes;
  }

  @Override
  public byte[] getValue() {
    return bytes;
  }

  @Override
  public byte[] getBytes() {
    return bytes;
  }

  @Override
  public Update<byte[]> deepCopy() {
    return new PutValue(bytes == null ? null : bytes.clone());
  }
}
