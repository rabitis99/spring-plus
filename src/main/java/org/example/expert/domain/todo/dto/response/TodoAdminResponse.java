package org.example.expert.domain.todo.dto.response;

import lombok.Getter;

import java.util.List;
@Getter
public class TodoAdminResponse {

    //조건
    Long todoId;
    String title;
    Integer managerCount;
    Integer commentCount;
    //추가 상황
    List<String> MangerNickNames;

    public void setManagerNickNames(List<String> managerNickNames) {
        this.MangerNickNames=managerNickNames;
    }
}
