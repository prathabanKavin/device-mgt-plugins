/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.plugins.input.adapter.extension.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import io.entgra.device.mgt.plugins.input.adapter.extension.ContentTransformer;
import io.entgra.device.mgt.plugins.input.adapter.extension.ContentValidator;
import io.entgra.device.mgt.plugins.input.adapter.extension.InputAdapterExtensionService;
import io.entgra.device.mgt.plugins.input.adapter.extension.InputAdapterExtensionServiceImpl;
import io.entgra.device.mgt.plugins.input.adapter.extension.transformer.DefaultContentTransformer;
import io.entgra.device.mgt.plugins.input.adapter.extension.transformer.MQTTContentTransformer;
import io.entgra.device.mgt.plugins.input.adapter.extension.validator.DefaultContentValidator;
import io.entgra.device.mgt.plugins.input.adapter.extension.validator.HTTPContentValidator;
import io.entgra.device.mgt.plugins.input.adapter.extension.validator.MQTTContentValidator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.*;

@Component(
        name = "io.entgra.device.mgt.plugins.input.adapter.extension.internal.InputAdapterServiceComponent",
        immediate = true)
public class InputAdapterServiceComponent {

	private static final Log log = LogFactory.getLog(
			InputAdapterServiceComponent.class);

    @Activate
	protected void activate(ComponentContext context) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Successfully deployed the input adapter extension service");
			}

            InputAdapterServiceDataHolder.getInstance().addContentTransformer(new DefaultContentTransformer());
            InputAdapterServiceDataHolder.getInstance().addContentTransformer(new MQTTContentTransformer());
            InputAdapterServiceDataHolder.getInstance().addContentValidator(new DefaultContentValidator());
            InputAdapterServiceDataHolder.getInstance().addContentValidator(new HTTPContentValidator());
            InputAdapterServiceDataHolder.getInstance().addContentValidator(new MQTTContentValidator());

            context.getBundleContext().registerService(InputAdapterExtensionService.class,
                                                       new InputAdapterExtensionServiceImpl(), null);
        } catch (RuntimeException e) {
            log.error("Can not create the input adapter service ", e);
        }
    }

    @Reference(
            name = "content.validator.extension",
            service = io.entgra.device.mgt.plugins.input.adapter.extension.ContentValidator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetContentValidator")
    protected void setContentValidator(ContentValidator contentValidator) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ContentValidator Service");
        }
        InputAdapterServiceDataHolder.getInstance().addContentValidator(contentValidator);
    }

    protected void unsetContentValidator(ContentValidator contentValidator) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting ContentValidator Service");
        }
    }

    @Reference(
            name = "content.transformer.extension",
            service = io.entgra.device.mgt.plugins.input.adapter.extension.ContentTransformer.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetContentTransformer")
    protected void setContentTransformer(ContentTransformer contentTransformer) {
        if (log.isDebugEnabled()) {
            log.debug("Setting contentTransformer Service");
        }
        InputAdapterServiceDataHolder.getInstance().addContentTransformer(contentTransformer);
    }

    protected void unsetContentTransformer(ContentTransformer contentTransformer) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting ContentTransformer Service");
        }
    }

}
