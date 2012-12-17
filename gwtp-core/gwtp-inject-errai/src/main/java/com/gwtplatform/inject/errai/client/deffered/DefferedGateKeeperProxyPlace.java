/**
 * Copyright 2011 ArcBees Inc.
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

package com.gwtplatform.inject.errai.client.deffered;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.inject.errai.client.ProxyManager;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceWithGatekeeper;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.ProxyPlaceImpl;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * Created with IntelliJ IDEA.
 * User: Cedric
 * Date: 10/24/12
 * Time: 10:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefferedGateKeeperProxyPlace<P extends Presenter<?, ?>> implements DefferedProxy<P> {
    private final String token;
    private final Class<P> presenterClass;
    private final Class<? extends Gatekeeper> gateKeeper;

    public DefferedGateKeeperProxyPlace(String token, Class<P> presenterClass, Class<? extends Gatekeeper> gateKeeper) {
        this.token = token;
        this.presenterClass = presenterClass;
        this.gateKeeper = gateKeeper;
    }

    public String getToken() {
        return token;
    }

    public Class<P> getPresenterClass() {
        return presenterClass;
    }

    @Override
    public Proxy makeProxy(EventBus eventBus, PlaceManager placeManager) {
        return new DynamicGateKeeperProxyPlace<P>(ProxyManager.getPresenterProxy(presenterClass), IOC.getBeanManager().lookupBean(gateKeeper).getInstance(), eventBus, placeManager);
    }

    private class DynamicGateKeeperProxyPlace<P extends Presenter<?, ?>> extends ProxyPlaceImpl<P> {
        public DynamicGateKeeperProxyPlace(Proxy<P> presenterProxy, Gatekeeper gatekeeper, EventBus eventBus, PlaceManager placeManager) {
            super.setPlace(new PlaceWithGatekeeper(token, gatekeeper));
            super.setProxy(presenterProxy);
            bind(placeManager, eventBus);
        }
    }
}
