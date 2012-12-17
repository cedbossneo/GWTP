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
import com.gwtplatform.mvp.client.proxy.PlaceImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.ProxyPlaceImpl;

/**
 * Created with IntelliJ IDEA.
 * User: Cedric
 * Date: 10/24/12
 * Time: 10:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefferedProxyPlace<P extends Presenter<?, ?>> implements DefferedProxy<P> {
    private final String token;
    private final Class<P> presenterClass;

    public DefferedProxyPlace(String token, Class<P> presenterClass) {
        this.token = token;
        this.presenterClass = presenterClass;
    }

    public Class<P> getPresenterClass() {
        return presenterClass;
    }

    @Override
    public Proxy makeProxy(EventBus eventBus, PlaceManager placeManager) {
        return new DynamicProxyPlace<P>(ProxyManager.getPresenterProxy(presenterClass), eventBus, placeManager);
    }

    private class DynamicProxyPlace<P extends Presenter<?, ?>> extends ProxyPlaceImpl<P> {
        public DynamicProxyPlace(Proxy<P> presenterProxy, EventBus eventBus, PlaceManager placeManager) {
            super.setPlace(new PlaceImpl(token));
            super.setProxy(presenterProxy);
            bind(placeManager, eventBus);
        }
    }
}