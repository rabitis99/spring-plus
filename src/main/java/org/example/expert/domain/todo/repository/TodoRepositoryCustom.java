package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.request.TodoCondition;
import org.example.expert.domain.todo.dto.response.TodoAdminResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepositoryCustom{

    Page<Todo> findTodosWithWeather(TodoCondition condition);

    Optional<Todo> findTodoWithUserByQquery(Long todoId);
    //이름이 별론데...
    Page<TodoAdminResponse> findTodosBySearchCondition(LocalDateTime createdFrom, LocalDateTime createdTo, String nickName, String title, Pageable pageable);
}
