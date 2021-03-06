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

package org.apache.deltaspike.example.rest;

import org.apache.deltaspike.example.mongo.APIHit;
import org.apache.deltaspike.example.mongo.APIHitDAO;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Date;

@ApplicationScoped
@Provider
public class StoreAPIHitFilter implements ContainerRequestFilter, ContainerResponseFilter {
    @Inject
    private APIHitDAO apiHitDAO;
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        System.out.println("Filtering request.");
        APIHit hit = new APIHit();
        hit.setUri(requestContext.getUriInfo().getPath());
        hit.setStartTime(new Date());
        requestContext.setProperty("apiHit",hit);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        System.out.println("Filtering response.");
        Object hit = requestContext.getProperty("apiHit");
        if( hit != null ) {
            APIHit apiHit = (APIHit)hit;
            apiHit.setEndTime(new Date());
            apiHitDAO.insert(apiHit);
        }
    }
}
