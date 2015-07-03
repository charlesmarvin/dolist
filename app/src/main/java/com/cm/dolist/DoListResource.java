package com.cm.dolist;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Copyright 2015 Marvin Charles
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Path("dolist")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DoListResource {
    private final DoListService service;

    public DoListResource(DoListService service) {
        this.service = service;
    }

    @GET
    @Timed
    public Collection<Todo> get() {
        return service.findAll();
    }

    @GET
    @Path("{id}")
    @Timed
    public Todo get(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    @Timed
    public Todo create(@Context SecurityContext principal, Todo todo) {
        todo.setCreatedOn(LocalDateTime.now());
        todo.setUser(principal.getUserPrincipal().getName());
        todo.setDone(false);
        return service.save(todo);
    }

    @PUT
    @Path("{id}")
    @Timed
    public Todo update(@PathParam("id") Long id, Todo todo) {
        Todo update = service.findById(id);
        if (update == null) {
            throw new WebApplicationException("Unknown todo");
        }
        if (todo.getDetails() != null) {
            update.setDetails(todo.getDetails());
        }
        if (todo.isDone() != null) {
            update.setDone(todo.isDone());
        }
        return service.save(todo);
    }
}
