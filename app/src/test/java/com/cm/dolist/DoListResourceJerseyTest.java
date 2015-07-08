package com.cm.dolist;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
public class DoListResourceJerseyTest extends JerseyTest {
    private static final String TEST_USER = "testUser";
    public static InMemoryDoListService service = new InMemoryDoListService();

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private Todo todo1;
    private Todo todo2;

    @Before
    public void beforeTest() {

        when(request.getRemoteUser()).thenReturn(TEST_USER);

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

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new JettyTestContainerFactory();
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        enable(TestProperties.LOG_TRAFFIC);
        ResourceConfig cfg = new ResourceConfig(DoListResource.class);
        cfg.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(request).to(HttpServletRequest.class);
                bind(service).to(DoListService.class);
            }
        });
        return cfg;
    }

    @After
    public void afterTest() {
        service.reset();
    }

    @Test
    public void testGet() throws Exception {
        Collection<Todo> todos = target("/dolist")
                .request()
                .get(new GenericType<Collection<Todo>>() {
                });
        assertEquals(2, todos.size());
        assertTrue(todos.contains(todo1));
        assertTrue(todos.contains(todo2));

    }

    @Test
    public void testGet1() throws Exception {
        Todo todo = target("/dolist/1")
                .request()
                .get(Todo.class);
        assertEquals(todo1, todo);
    }

    @Test
    public void testCreate() throws Exception {
        Todo newTodo = new Todo();
        String testDetails = "New Test Todo";
        newTodo.setDetails(testDetails);
        Response response = target("/dolist")
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
        Response response = target("/dolist/1")
                .request()
                .put(Entity.json(todo1));
        Todo savedTodo = response.readEntity(Todo.class);
        assertEquals(new Long(1), savedTodo.getId());
        assertEquals(testDetails, savedTodo.getDetails());
        assertFalse(savedTodo.isDone());
        assertNotNull(savedTodo.getUser());

    }

}
