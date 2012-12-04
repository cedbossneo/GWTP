package com.gwtplatform.common.client.injector;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created with IntelliJ IDEA.
 * User: cedric
 * Date: 04/12/12
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public interface AsyncProvider<B> {
    void get(AsyncCallback<B> asyncCallback);
}
