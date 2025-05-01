package org.example.expert.domain.todo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class TodoCondition {
    //날씨
    String weather;
    //언제부터
    LocalDate modifiedFrom;
    //언제까지
    LocalDate modifiedTo;
    //페이지객체
    Pageable pageable;

}
