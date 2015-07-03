package com.cm.dolist;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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
public class InMemoryDoListService implements DoListService {
    private final Map<Long, Todo> cache = new LinkedHashMap<>();
    private AtomicLong seq = new AtomicLong(0);

    public void reset() {
        cache.clear();
        seq.set(0);

    }

    public Collection<Todo> findAll() {
        return cache.values();
    }

    public Todo findById(long id) {
        return cache.get(id);
    }

    public Todo save(Todo todo) {
        if (todo.getId() == null) {
            todo.setId(seq.incrementAndGet());
        }
        cache.put(todo.getId(), todo);
        return todo;
    }
}
