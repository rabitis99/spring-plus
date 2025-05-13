package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long>,TodoRepositoryCustom {
    //멀티라인 문자열?-> """ 멀티라인 문자열 리터럴을 사용하면 가독성이 매우 좋아지고, 공백 문제도 방지됩니다
    @EntityGraph(attributePaths = "user")
    @Query("""
    SELECT t FROM Todo t
    WHERE (:weather IS NULL OR t.weather = :weather)
      AND (:start IS NULL OR t.modifiedAt >= :start)
      AND (:end IS NULL OR t.modifiedAt <= :end)
    ORDER BY t.modifiedAt DESC
""")
    Page<Todo> findAllByOrderByModifiedAtDesc(
            @Param("weather") String weather,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
