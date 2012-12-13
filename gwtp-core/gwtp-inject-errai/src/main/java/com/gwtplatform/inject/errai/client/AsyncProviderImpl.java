/*
 * *
 *  * Copyright 2011 ArcBees Inc.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License. You may obtain a copy of
 *  * the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *
 */

package com.gwtplatform.inject.errai.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.common.client.injector.AsyncProvider;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;

/**
 * Created with IntelliJ IDEA.
 * User: cedric
 * Date: 04/12/12
 * Time: 16:41
 */
public class AsyncProviderImpl<B> implements AsyncProvider<B> {
    public Class<B> providerClass;

    public AsyncProviderImpl(Class<B> providerClass) {
        this.providerClass = providerClass;
    }

    @Override
    public void get(AsyncCallback<B> asyncCallback) {
        IOCBeanDef<B> tiocBeanDef = IOC.getBeanManager().lookupBean(providerClass);
        if (tiocBeanDef == null)
            asyncCallback.onFailure(new Throwable("Bean definition not found"));
        B bean = tiocBeanDef.getInstance();
        if (bean == null)
            asyncCallback.onFailure(new Throwable("Error while getting bean"));
        else
            asyncCallback.onSuccess(bean);
    }
}
