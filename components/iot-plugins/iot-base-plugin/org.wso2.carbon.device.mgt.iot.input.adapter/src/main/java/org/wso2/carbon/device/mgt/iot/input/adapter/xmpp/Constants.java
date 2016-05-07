/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.iot.input.adapter.xmpp;

/**
 * This holds the constants related to MQTT input adapter.
 */
public class Constants {

	public static final String DEFAULT = "default";
	public static final String XMPP_CONTENT_VALIDATION_DEFAULT_PARAMETERS =
			"device_id_json_path:event.metaData.deviceId,device_id_topic_hierarchy_index:2";
	public static final String FROM_KEY = "from";
	public static final String SUBJECT_KEY = "subject";
	public static final String DEVICE_ID_JSON_PATH = "device_id_json_path";
	public static final String DEVICE_ID_TOPIC_HIERARCHY_INDEX = "device_id_topic_hierarchy_index";
}
