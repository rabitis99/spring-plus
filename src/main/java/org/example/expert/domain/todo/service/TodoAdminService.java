package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoAdminResponse;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class TodoAdminService {
    private final TodoRepository todoRepository;

    public Page<TodoAdminResponse> findTodo(LocalDate createdFrom, LocalDate createdTo, String nickName, String title, Pageable pageable){

        LocalDateTime TimeCreatedFrom=null;
        LocalDateTime TimeCreatedTo=null;
        if (createdFrom!=null){
            TimeCreatedFrom=createdFrom.atTime(LocalTime.MIN);
        }
        if (createdTo!=null){
            TimeCreatedTo=createdTo.atTime(LocalTime.MAX);
        }


        return todoRepository.findTodosBySearchCondition(TimeCreatedFrom,TimeCreatedTo,nickName,title,pageable) ;
    }
}
