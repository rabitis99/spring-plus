package org.example.expert.domain.todo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.common.entity.Timestamped;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "todos")
public class Todo extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String contents;
    private String weather;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "todo", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    //할 일을 새로 저장할 시, 할 일을 생성한 유저는 담당자로 자동 등록되어야 합니다.
    // 담당자가 삭제되더라도, Todo는 업무 기록 차원에서 유지되어야 합니다.
    @OneToMany(mappedBy = "todo",cascade = CascadeType.PERSIST)
    private List<Manager> managers = new ArrayList<>();


    // 매니저를 중복 없이 등록하고, 양방향 연관관계를 설정합니다.
    //연관 관계 메세드
    public void addManager(Manager manager) {
        if (!this.managers.contains(manager)) {
            this.managers.add(manager);
            manager.setTodo(this);
        }
    }
    //생성자
    public Todo(String title, String contents, String weather, User user) {
        this.title = title;
        this.contents = contents;
        this.weather = weather;
        this.user = user;
        // Todo 생성 시, 작성자가 자동으로 첫 번째 매니저로 등록됩니다.
        Manager manager = new Manager(user);
        this.addManager(manager);

    }
}
