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

package com.gwtplatform.inject.errai.client;

import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.inject.errai.client.deffered.DefferedContentHandler;
import com.gwtplatform.inject.errai.client.deffered.DefferedEvent;
import com.gwtplatform.inject.errai.client.deffered.DefferedEventImpl;
import com.gwtplatform.inject.errai.client.deffered.DefferedGateKeeperProxyPlace;
import com.gwtplatform.inject.errai.client.deffered.DefferedHandler;
import com.gwtplatform.inject.errai.client.deffered.DefferedProxy;
import com.gwtplatform.inject.errai.client.deffered.DefferedProxyPlace;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;
import com.gwtplatform.mvp.client.proxy.NotifyingAsyncCallback;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.IOCBeanManager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Singleton
public class ProxyManager {
    @Inject
    private IOCBeanManager manager;

    @Inject
    EventBus eventBus;

    @Inject
    PlaceManager placeManager;

    private static ProxyManager instance;

    static List<DefferedProxy> defferedProxies = new LinkedList<DefferedProxy>();
    static List<DefferedHandler> defferedHandlers = new LinkedList<DefferedHandler>();
    static List<DefferedEvent> defferedEvents = new LinkedList<DefferedEvent>();
    static Map<Class<? extends Presenter<?, ?>>, Proxy> proxies = new HashMap<Class<? extends Presenter<?, ?>>, Proxy>();
    static Map<Class<? extends Presenter<?, ?>>, ProxyPlace> proxiesPlaces = new HashMap<Class<? extends Presenter<?, ?>>, ProxyPlace>();

    public static <P extends Presenter<?, ?>> DefferedContentHandler<P> registerHandler(GwtEvent.Type type, Class<P> presenterClass) {
        DefferedContentHandler<P> handler = new DefferedContentHandler<P>(type, presenterClass);
        defferedHandlers.add(handler);
        if (instance != null) {
            instance.registerDefferedHandler(handler);
        }
        return handler;
    }

    public static <P extends Presenter<?, ?>> DefferedEventImpl<P> registerEvent(Event.Type type, Class<P> presenterClass) {
        DefferedEventImpl<P> defferedEvent = new DefferedEventImpl<P>(type, presenterClass);
        defferedEvents.add(defferedEvent);
        if (instance != null) {
            instance.registerDefferedEvent(defferedEvent);
        }
        return defferedEvent;
    }

    public static <P extends Presenter<?, ?>> DefferedProxyPlace<P> registerPlace(String token, Class<P> presenterClass) {
        DefferedProxyPlace<P> proxyPlace = new DefferedProxyPlace<P>(token, presenterClass);
        defferedProxies.add(proxyPlace);
        if (instance != null) {
            instance.registerDefferedProxy(proxyPlace);
        }
        return proxyPlace;
    }

    public static <P extends Presenter<?, ?>> DefferedGateKeeperProxyPlace<P> registerPlace(String token, Class<P> presenterClass, Class<? extends Gatekeeper> gateKeeper) {
        DefferedGateKeeperProxyPlace<P> proxyPlace = new DefferedGateKeeperProxyPlace<P>(token, presenterClass, gateKeeper);
        defferedProxies.add(proxyPlace);
        if (instance != null) {
            instance.registerDefferedProxy(proxyPlace);
        }
        return proxyPlace;
    }

    @PostConstruct
    public void init() {
        instance = this;
        for (DefferedProxy defferedProxy : defferedProxies) {
            registerDefferedProxy(defferedProxy);
        }
        for (DefferedHandler defferedHandler : defferedHandlers) {
            registerDefferedHandler(defferedHandler);
        }
        for (DefferedEvent defferedEvent : defferedEvents) {
            registerDefferedEvent(defferedEvent);
        }
    }

    protected void registerDefferedEvent(DefferedEvent defferedEvent) {
        defferedEvent.registerEvent(eventBus, placeManager);
    }

    protected void registerDefferedHandler(DefferedHandler defferedHandler) {
        defferedHandler.registerHandler(eventBus, placeManager);
    }

    protected void registerDefferedProxy(DefferedProxy defferedProxy) {
        Proxy value = defferedProxy.makeProxy(eventBus, placeManager);
        if (value instanceof ProxyPlace) {
            proxiesPlaces.put(defferedProxy.getPresenterClass(), (ProxyPlace) value);
        } else {
            proxies.put(defferedProxy.getPresenterClass(), value);
        }
    }

    public static <P extends Presenter<?, ?>> Proxy<P> getPresenterProxy(Class<P> presenterClass) {
        return proxies.get(presenterClass);
    }

    public static <P extends Presenter<?, ?>> ProxyPlace<P> getPresenterProxyPlace(Class<P> presenterClass) {
        return proxiesPlaces.get(presenterClass);
    }

    public static <T extends Presenter<?, ?>> void getPresenter(Class<T> persenterClass, NotifyingAsyncCallback<T> notifyingAsyncCallback) {
        notifyingAsyncCallback.prepare();
        IOCBeanDef<T> tiocBeanDef = instance.manager.lookupBean(persenterClass);
        if (tiocBeanDef == null) {
            notifyingAsyncCallback.onFailure(new Throwable("Bean definition not found"));
        }
        notifyingAsyncCallback.checkLoading();
        T bean = tiocBeanDef.getInstance();
        if (bean == null) {
            notifyingAsyncCallback.onFailure(new Throwable("Error while getting bean"));
        } else {
            notifyingAsyncCallback.onSuccess(bean);
        }
    }
}