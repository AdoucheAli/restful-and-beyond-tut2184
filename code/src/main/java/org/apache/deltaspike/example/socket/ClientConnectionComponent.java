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

package org.apache.deltaspike.example.socket;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ClientConnectionComponent {
    private Map<String,Session> sessionMap = new HashMap<>();

    public void addSession(Session session) {
        if(session != null) {
            sessionMap.put(session.getId(),session);
        }
    }

    public void removeSession(Session session) {
        if (session != null) {
            sessionMap.remove(session.getId());
        }
    }

    public void notifyAllSessions(String message) {
        System.out.println("Notifying all sessions "+message+" with contents "+sessionMap);
        sessionMap.forEach((s, session) -> {
//            try {
                System.out.println("Sending to "+s);
                session.getAsyncRemote().sendText(message);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        });
    }
}
