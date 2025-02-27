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
package io.entgra.device.mgt.plugins.input.adapter.thrift.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import io.entgra.device.mgt.plugins.input.adapter.thrift.ThriftAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import io.entgra.device.mgt.plugins.input.adapter.thrift.ThriftEventAdapterFactory;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.*;

@Component(
        name = "io.entgra.device.mgt.plugins.input.adapter.thrift.internal.ThriftEventAdapterServiceComponent",
        immediate = true)
public class ThriftEventAdapterServiceComponent {

    private static final Log log = LogFactory.getLog(ThriftEventAdapterServiceComponent.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */


    @Activate
    protected void activate(ComponentContext context) {

        try {
            InputEventAdapterFactory wso2EventEventAdapterFactory = new ThriftEventAdapterFactory();
            context.getBundleContext().registerService(InputEventAdapterFactory.class.getName(), wso2EventEventAdapterFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the input WSO2Event adapter service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the input WSO2Event adapter service ", e);
        }
    }

    @Reference(
            name = "data.bridge.subscriber..service",
            service = org.wso2.carbon.databridge.core.DataBridgeSubscriberService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetDataBridgeSubscriberService")
    protected void setDataBridgeSubscriberService(
            DataBridgeSubscriberService dataBridgeSubscriberService) {
        if (ThriftEventAdapterServiceHolder.getDataBridgeSubscriberService() == null) {
            ThriftEventAdapterServiceHolder.registerDataBridgeSubscriberService(dataBridgeSubscriberService);

            dataBridgeSubscriberService.subscribe(new AgentCallback() {
                @Override
                public void definedStream(StreamDefinition streamDefinition, int i) {

                }

                @Override
                public void removeStream(StreamDefinition streamDefinition, int i) {

                }

                @Override
                public void receive(List<Event> events, Credentials credentials) {
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        String tenantDomain = getTenantDomain(events, credentials);
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                        if (!tenantDomain.equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                            TenantAxisUtils.getTenantConfigurationContext(tenantDomain, ThriftEventAdapterServiceHolder
                                    .getConfigurationContext());
                        }
                        for (Event event : events) {
                            ConcurrentHashMap<String, ThriftAdapter> adapters = ThriftEventAdapterServiceHolder
                                    .getAdapterService(tenantDomain, event.getStreamId());
                            if (adapters != null) {
                                event = getStrippedEvent(event, credentials);
                                for (ThriftAdapter adapter : adapters.values()) {
                                    adapter.getEventAdaptorListener().onEvent(event);
                                }
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Event received in wso2Event Adapter - " + event);
                            }
                        }
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            });
        }
    }

    protected void unSetDataBridgeSubscriberService(
            DataBridgeSubscriberService dataBridgeSubscriberService) {

    }
    @Reference(
            name = "configuration.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        ConfigurationContext serverConfigContext = contextService.getServerConfigContext();
        ThriftEventAdapterServiceHolder.setConfigurationContext(serverConfigContext);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        ThriftEventAdapterServiceHolder.setConfigurationContext(null);
    }

    private String getTenantDomain(List<Event> events, Credentials credentials) {
        Object[] objects = events.get(0).getMetaData();
        String tenantDomain = credentials.getDomainName();
        if (objects != null && objects.length > 0) {
            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                String[] splitValues = ((String) objects[0]).split("@");
                if (splitValues.length > 1) {
                    tenantDomain = splitValues[0];
                }
            }
        }
        return tenantDomain;

    }

    private Event getStrippedEvent (Event event, Credentials credentials) {
        Object[] objects = event.getMetaData();
        String tenantDomain = credentials.getDomainName();
        if (objects != null && objects.length > 0) {
            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                String[] splitValues = ((String) objects[0]).split("@");
                if (splitValues.length > 1) {
                    event.getMetaData()[0] = splitValues[1];
                }
            }
        }
        return event;

    }



}
