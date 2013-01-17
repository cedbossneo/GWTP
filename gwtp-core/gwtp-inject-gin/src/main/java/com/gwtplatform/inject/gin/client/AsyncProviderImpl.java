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

package com.gwtplatform.inject.gin.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Provider;
import com.gwtplatform.common.client.injector.AsyncProvider;

import javax.inject.Inject;

public class AsyncProviderImpl<T> implements AsyncProvider<T> {
    @Inject
    Provider<T> provider;

    @Override
    public void get(final AsyncCallback<? super T> asyncCallback) {
        GWT.runAsync(new RunAsyncCallback() {
            public void onSuccess() {
                asyncCallback.onSuccess(provider.get());
            }

            public void onFailure(Throwable ex) {
                asyncCallback.onFailure(ex);
            }
        });
    }
}
