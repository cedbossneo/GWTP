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

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.gwtplatform.mvp.client.proxy.ParameterTokenFormatter;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class Errai {
    private EventBus eventBus;
    private TokenFormatter tokenFormater;

    @Produces
    private EventBus produceEventBus() {
        if (eventBus == null) {
            eventBus = GWT.create(SimpleEventBus.class);
        }
        return eventBus;
    }

    @Produces
    private TokenFormatter produceTokenFormatter() {
        if (tokenFormater == null) {
            tokenFormater = GWT.create(ParameterTokenFormatter.class);
        }
        return tokenFormater;
    }
}