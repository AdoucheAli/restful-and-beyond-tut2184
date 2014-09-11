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

package org.apache.deltaspike.example.components.websocket;

import org.apache.deltaspike.example.components.annotations.StartsRequestScope;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by johnament on 9/3/14.
 */
@ApplicationScoped
@ClientEndpoint
public class WebSocketTestClient {

    @Inject
    private BeanManager beanManager;

    private CountDownLatch countDownLatch = new CountDownLatch(5);

    @PostConstruct
    public void init(){
        System.out.println("Created client.");
    }

    public void send(String data, Session session) throws IOException {
        session.getBasicRemote().sendText(data);
    }

    @OnMessage
    @StartsRequestScope
    public void receive(String data) {
        System.out.println("Client received "+data);
        beanManager.getContext(RequestScoped.class);
        countDownLatch.countDown();
    }

    public void verify() throws Exception {
        countDownLatch.await();
    }
}