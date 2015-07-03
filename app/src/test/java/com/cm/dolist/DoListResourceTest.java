package com.cm.dolist;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
public class DoListResourceTest {
    private static final String TEST_USER = "testuser";
    public static InMemoryDoListService service = new InMemoryDoListService();
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(RolesAllowedDynamicFeature.class)
            .setTestContainerFactory(new JettyTestContainerFactory())
            .addProvider(new AuthenticationFilter())
            .addResource(new DoListResource(service))
            .build();
    private Todo todo1;
    private Todo todo2;

    @Before
    public void setup() {
        service.reset();
        todo1 = new Todo();
        todo1.setDetails("Todo 1");
        todo1.setCreatedOn(LocalDateTime.now());
        todo1.setDone(false);
        todo1.setUser(TEST_USER);
        todo2 = new Todo();
        todo2.setDetails("Todo 2");
        todo2.setCreatedOn(LocalDateTime.now());
        todo2.setDone(false);
        todo2.setUser(TEST_USER);
        todo1 = service.save(todo1);
        todo2 = service.save(todo2);
    }

    @After
    public void tearDown() {
        service.reset();
    }

    @Test
    public void testGet() throws Exception {
        Collection<Todo> todos = resources.getJerseyTest()
                .target("/dolist")
                .request()
                .get(new GenericType<Collection<Todo>>() {
                });
        assertEquals(2, todos.size());
        assertTrue(todos.contains(todo1));
        assertTrue(todos.contains(todo2));

    }

    @Test
    public void testGet1() throws Exception {
        Todo todo = resources.getJerseyTest()
                .target("/dolist/1")
                .request()
                .get(Todo.class);
        assertEquals(todo1, todo);
    }

    @Test
    public void testCreate() throws Exception {
        Todo newTodo = new Todo();
        String testDetails = "New Test Todo";
        newTodo.setDetails(testDetails);
        Response response = resources.getJerseyTest()
                .target("/dolist")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Basic dGVzdHVzZXI6dGVzdHVzZXI=") //Base64 testuser:testuser
                .post(Entity.json(newTodo));
        Todo savedTodo = response.readEntity(Todo.class);
        assertEquals(new Long(3), savedTodo.getId());
        assertEquals(testDetails, savedTodo.getDetails());
        assertFalse(savedTodo.isDone());
        assertEquals(TEST_USER, savedTodo.getUser());
    }

    @Test
    public void testUpdate() throws Exception {
        String testDetails = "Updated Test Todo";
        todo1.setDetails(testDetails);
        Response response = resources.getJerseyTest()
                .target("/dolist/1")
                .request()
                .put(Entity.json(todo1));
        Todo savedTodo = response.readEntity(Todo.class);
        assertEquals(new Long(1), savedTodo.getId());
        assertEquals(testDetails, savedTodo.getDetails());
        assertFalse(savedTodo.isDone());
        assertNotNull(savedTodo.getUser());

    }

    public static class AuthenticationFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return () -> TEST_USER;
                }

                @Override
                public boolean isUserInRole(String string) {
                    return "privileged".equals(string);
                }

                @Override
                public boolean isSecure() {
                    return true;
                }

                @Override
                public String getAuthenticationScheme() {
                    return "BASIC";
                }
            });
        }
    }
}