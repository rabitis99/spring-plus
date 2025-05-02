package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.request.TodoCondition;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface TodoRepositoryCustom{

    Page<Todo> findTodosWithWeather(TodoCondition condition);

    Optional<Todo> findTodoWithUserByQquery(Long todoId);
}
