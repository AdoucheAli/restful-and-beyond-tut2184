/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 */

package org.apache.deltaspike.example.components.servlet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;

import javax.enterprise.inject.Vetoed;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Vetoed
public class UndertowComponent {
    private int port;
    private String bindAddress;
    private String name;
    private String contextPath;
    private Map<WebServlet,Class<? extends Servlet>> servlets;
    private Map<WebFilter,Class<? extends Filter>> filters;
    private List<Class<? extends EventListener>> listeners;

    private Undertow server;

    public UndertowComponent(int port, String bindAddress,String name, String contextPath) {
        this.port = port;
        this.bindAddress = bindAddress;
        this.name = name;
        this.contextPath = contextPath;
        servlets = Maps.newHashMap();
        filters = Maps.newHashMap();
        listeners = Lists.newArrayList();
    }

    public UndertowComponent addServlet(WebServlet webServlet, Class<? extends Servlet> clazz) {
        servlets.put(webServlet,clazz);
        return this;
    }

    public UndertowComponent addFilter(WebFilter webFilter, Class<? extends Filter> clazz) {
        filters.put(webFilter,clazz);
        return this;
    }

    public UndertowComponent addListener(Class<? extends EventListener> clazz) {
        listeners.add(clazz);
        return this;
    }

    private static Function<Map.Entry<WebServlet,Class<? extends Servlet>>,ServletInfo> servletInfoFunction =
            webServletClassEntry -> {
        WebServlet ws = webServletClassEntry.getKey();
        Class<? extends Servlet> clazz = webServletClassEntry.getValue();
        ServletInfo si = Servlets.servlet(ws.name(),clazz).addMappings(ws.urlPatterns())
                .setAsyncSupported(ws.asyncSupported())
                .setLoadOnStartup(ws.loadOnStartup());
        Arrays.stream(ws.initParams()).forEach(initParam -> si.addInitParam(initParam.name(),initParam.value()));
        return si;
    };

    private static Function<Map.Entry<WebFilter,Class<? extends Filter>>,FilterInfo> filterInfoFunction = webFilter -> {
        WebFilter wf = webFilter.getKey();
        FilterInfo fi = Servlets.filter(wf.filterName(),webFilter.getValue()).setAsyncSupported(wf.asyncSupported());
        Arrays.stream(wf.initParams()).forEach(initParam -> fi.addInitParam(initParam.name(),initParam.value()));
        return fi;
    };


    public UndertowComponent start() {
        List<ListenerInfo> listenerInfoList = listeners.stream().map(Servlets::listener)
                .collect(Collectors.toList());

        List<ServletInfo> servletInfoList = servlets.entrySet().stream().map(servletInfoFunction)
                .collect(Collectors.toList());

        List<FilterInfo> filterInfoList = filters.entrySet().stream().map(filterInfoFunction)
                .collect(Collectors.toList());

        DeploymentInfo di = new DeploymentInfo()
                .addListeners(listenerInfoList)
                .addFilters(filterInfoList)
                .addServlets(servletInfoList)
                .setContextPath(contextPath)
                .setDeploymentName(name)
                .setClassLoader(ClassLoader.getSystemClassLoader());
        DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(di);
        deploymentManager.deploy();
        try {
            server = Undertow.builder()
                    .addHttpListener(port, this.bindAddress)
                    .setHandler(deploymentManager.start())
                    .build();
            server.start();
            return this;
        } catch (ServletException e) {
            throw new RuntimeException("Unable to start container",e);
        }
    }

    public void stop() {
        this.server.stop();
    }
}