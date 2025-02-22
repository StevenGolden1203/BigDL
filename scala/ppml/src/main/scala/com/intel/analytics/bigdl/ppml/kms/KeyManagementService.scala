/*
 * Copyright 2016 The BigDL Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.analytics.bigdl.ppml.kms

import com.intel.analytics.bigdl.ppml.utils.Supportive

object KMS_CONVENTION {
  val MODE_SIMPLE_KMS = "SimpleKeyManagementService"
  val MODE_EHSM_KMS = "EHSMKeyManagementService"
}

trait KeyManagementService extends Supportive {
  var _appId:String = _
  var _appKey:String = _

  def retrievePrimaryKey(primaryKeySavePath: String)
  def retrieveDataKey(primaryKeyPath: String, dataKeySavePath: String)
  def retrieveDataKeyPlainText(primaryKeyPath: String, dataKeyPath: String): String
}
