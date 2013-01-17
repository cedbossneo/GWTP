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

package com.gwtplatform.inject.errai.rebind.ioc;

import com.gwtplatform.common.client.CodeSplitBundleProvider;
import com.gwtplatform.common.client.CodeSplitProvider;
import com.gwtplatform.inject.errai.client.AsyncProviderImpl;
import com.gwtplatform.inject.errai.client.ProxyManager;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplitBundle;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;
import com.gwtplatform.mvp.client.proxy.NotifyingAsyncCallback;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.ProxyImpl;
import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.builder.ClassDefinitionBuilderAbstractOption;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessorFactory;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;

import java.util.Collection;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

@SuppressWarnings("UnusedDeclaration")
@IOCExtension
public class ProxyManagerIOCExtension implements IOCExtensionConfigurator {

    public ProxyManagerIOCExtension() {
    }

    @Override
    public void configure(IOCProcessingContext context, InjectionContext injectionContext, IOCProcessorFactory procFactory) {
    }

    @Override
    public void afterInitialization(IOCProcessingContext context, InjectionContext injectionContext, IOCProcessorFactory procFactory) {
        final BlockStatement instanceInitializer = context.getBootstrapClass().getInstanceInitializer();
        for (MetaClass proxyClass : ClassScanner.getSubTypesOf(MetaClassFactory.get(Proxy.class))) {
            if (!proxyClass.isInterface() || (!proxyClass.isAnnotationPresent(ProxyStandard.class) && !proxyClass.isAnnotationPresent(ProxyCodeSplit.class) && !proxyClass.isAnnotationPresent(ProxyCodeSplitBundle.class))) {
                continue;
            }
            MetaParameterizedType presenterType = getPresenterFromProxy(proxyClass);
            MetaClass presenterClass = (MetaClass) presenterType.getTypeParameters()[0];
            ClassDefinitionBuilderAbstractOption<? extends ClassStructureBuilder<?>> proxy = createProxy(proxyClass, presenterType, presenterClass);
            InnerClass finalProxyClass = new InnerClass(proxy.body().getClassDefinition());

            for (MetaMethod method : presenterClass.getMethodsAnnotatedWith(ProxyEvent.class)) {
                MetaParameter event = method.getParameters()[0];
                createMethod(injectionContext, getHandler(presenterClass, method.getName(), event.getType()), presenterClass, proxy, method.getReturnType(), method.getName(), event);
                MetaMethod staticMethod = event.getType().getBestMatchingStaticMethod("getType", new Class[]{});
                if (staticMethod == null) {
                    instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerEvent", InjectUtil.invokePublicOrPrivateMethod(injectionContext, Stmt.newObject(event.getType()), event.getType().getBestMatchingMethod("getAssociatedType", new Class[]{})), finalProxyClass.getType()));
                } else {
                    instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerEvent", Stmt.invokeStatic(event.getType(), staticMethod.getName()), finalProxyClass.getType()));
                }
            }
            context.getBootstrapClass().addInnerClass(finalProxyClass);
            injectionContext.addType(finalProxyClass.getType());
            instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerProxy", finalProxyClass.getType()));
            for (MetaMethod method : presenterClass.getMethodsAnnotatedWith(ContentSlot.class)) {
                if (!method.isStatic()) {
                    continue;
                }
                instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerHandler", Stmt.invokeStatic(presenterClass, method.getName()), finalProxyClass.getType()));
            }

            Class<? extends Gatekeeper> defaultGateKeeper = null;
            Collection<MetaClass> defaultGatekeeperClasses = ClassScanner.getTypesAnnotatedWith(DefaultGatekeeper.class);
            if (defaultGatekeeperClasses.size() > 0) {
                Class<? extends Gatekeeper> aClass = (Class<? extends Gatekeeper>) defaultGatekeeperClasses.iterator().next().asClass();
                defaultGateKeeper = aClass;
            }

            if (proxyClass.isAnnotationPresent(NameToken.class)) {
                boolean useGateKeeper = proxyClass.isAnnotationPresent(UseGatekeeper.class);
                if (useGateKeeper || (defaultGateKeeper != null && !proxyClass.isAnnotationPresent(NoGatekeeper.class))) {
                    Class<? extends Gatekeeper> gateKeeper = defaultGateKeeper;
                    if (useGateKeeper) {
                        Class<? extends Gatekeeper> value = proxyClass.getAnnotation(UseGatekeeper.class).value();
                        gateKeeper = (value != null) ? value : gateKeeper;
                    }
                    instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerPlace", proxyClass.getAnnotation(NameToken.class).value(), finalProxyClass.getType(), gateKeeper));
                } else {
                    instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerPlace", proxyClass.getAnnotation(NameToken.class).value(), finalProxyClass.getType()));
                }
            }
        }
    }

    private MetaClass getHandler(MetaClass klass, String name, MetaClass parameter) {
        for (MetaClass handler : klass.getInterfaces()) {
            if (handler.getMethod(name, parameter) != null) {
                return handler;
            }
        }
        return null;
    }

    private ClassStructureBuilder<?> createMethod(InjectionContext injectionContext, MetaClass handler, MetaClass klass, ClassDefinitionBuilderAbstractOption<? extends ClassStructureBuilder<?>> proxy, MetaClass returnType, String name, MetaParameter event) {
        Parameter parameter = Parameter.of(event.getType(), "event", true);
        MetaClass metaClass = parameterizedAs(NotifyingAsyncCallback.class, typeParametersOf(klass));
        proxy.implementsInterface(handler);
        return proxy.body().publicMethod(returnType, name, parameter).body()
                .append(
                        InjectUtil.invokePublicOrPrivateMethod(injectionContext, Stmt.loadVariable("this"),
                                proxy.body().getClassDefinition().getBestMatchingMethod("getPresenter", metaClass),
                                createCallback(metaClass, klass, proxy, name, parameter))
                ).finish();
    }

    private ObjectBuilder createCallback(MetaClass callbackClass, MetaClass presenterKlass, ClassDefinitionBuilderAbstractOption<? extends ClassStructureBuilder<?>> proxy, String name, Parameter parameter) {
        Parameter presenter = Parameter.of(presenterKlass, "presenter", true);
        return Stmt.newObject(callbackClass).extend(Stmt.loadVariable("this").invoke("getEventBus")).publicOverridesMethod("success", presenter).append(Stmt.loadVariable(presenter.getName()).invoke(name, Refs.get(parameter.getName()))).finish().finish();
    }

    private ClassDefinitionBuilderAbstractOption<? extends ClassStructureBuilder<?>> createProxy(MetaClass proxyInterface, MetaParameterizedType presenterType, MetaClass presenterClass) {
        MetaClass proxyClass = parameterizedAs(ProxyImpl.class, presenterType);

        ClassDefinitionBuilderAbstractOption<? extends ClassStructureBuilder<?>> definitionStaticOption = ClassBuilder.define("org.jboss.errai.ioc.client.BootstrapperImpl." + presenterClass.getName() + "Proxy", proxyClass).publicScope().staticClass();
        //  definitionStaticOption.implementsInterface(proxyInterface);
        definitionStaticOption.body().publicConstructor().append(Stmt.loadVariable("presenter").assignValue(createProvider(proxyInterface, presenterClass, presenterType))).finish();
        return definitionStaticOption;
    }

    private MetaParameterizedType getPresenterFromProxy(MetaClass proxyInterface) {
        MetaClass[] interfaces = proxyInterface.getInterfaces();
        for (MetaClass anInterface : interfaces) {
            if (anInterface.asClass().equals(Proxy.class)) {
                return anInterface.getParameterizedType();
            }
        }
        return null;
    }

    private Object createProvider(MetaClass proxyInterface, MetaType presenterClass, MetaParameterizedType presenterType) {
        if (proxyInterface.getAnnotation(ProxyCodeSplit.class) != null || proxyInterface.getAnnotation(ProxyStandard.class) != null) {
            return ObjectBuilder.newInstanceOf(parameterizedAs(CodeSplitProvider.class, presenterType)).withParameters(ObjectBuilder.newInstanceOf(parameterizedAs(AsyncProviderImpl.class, presenterType)).withParameters(presenterClass));
        }
        ProxyCodeSplitBundle proxyCodeSplitBundle = proxyInterface.getAnnotation(ProxyCodeSplitBundle.class);
        if (proxyCodeSplitBundle != null) {
            return ObjectBuilder.newInstanceOf(parameterizedAs(CodeSplitBundleProvider.class, typeParametersOf(presenterClass, proxyCodeSplitBundle.bundleClass()))).withParameters(ObjectBuilder.newInstanceOf(parameterizedAs(AsyncProviderImpl.class, typeParametersOf(proxyCodeSplitBundle.bundleClass()))).withParameters(proxyCodeSplitBundle.bundleClass()), proxyCodeSplitBundle.id());
        }
        return null;
    }
}