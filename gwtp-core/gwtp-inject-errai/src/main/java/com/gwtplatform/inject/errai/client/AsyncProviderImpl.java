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
 * To change this template use File | Settings | File Templates.
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
