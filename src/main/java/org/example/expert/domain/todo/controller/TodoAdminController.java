package org.example.expert.domain.todo.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoAdminResponse;
import org.example.expert.domain.todo.service.TodoAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class TodoAdminController {

    private final TodoAdminService adminService;

    @GetMapping("admin/todos")
    public ResponseEntity<Page<TodoAdminResponse>> findTodo (@RequestParam(required = false) String title,
                                                            @RequestParam(required = false) String nickname,
                                                            @RequestParam(required = false) LocalDate createdFrom,
                                                            @RequestParam(required = false) LocalDate createdTo,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int size){
        Pageable pageable= PageRequest.of(page-1,size);

        return ResponseEntity.ok().body(adminService.findTodo(createdFrom,createdTo,nickname,title,pageable));
    }


}
