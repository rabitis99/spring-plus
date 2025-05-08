package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.request.TodoCondition;
import org.example.expert.domain.todo.dto.response.TodoAdminResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryCustomImpl implements TodoRepositoryCustom {

    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Todo> findTodosWithWeather(TodoCondition condition) {

        Pageable pageable = condition.getPageable();

        // ===== 1. 데이터 쿼리 (Fetch Join 포함) =====
        StringBuilder dataQueryBuilder = new StringBuilder();
        dataQueryBuilder.append("SELECT t FROM Todo t LEFT JOIN FETCH t.user u WHERE 1=1");

        if (condition.getWeather() != null && !condition.getWeather().isBlank()) {
            dataQueryBuilder.append(" AND t.weather = :weather");
        }
        if (condition.getModifiedFrom() != null) {
            dataQueryBuilder.append(" AND t.modifiedAt >= :modifiedFrom");
        }
        if (condition.getModifiedTo() != null) {
            dataQueryBuilder.append(" AND t.modifiedAt <= :modifiedTo");
        }

        TypedQuery<Todo> dataQuery = em.createQuery(dataQueryBuilder.toString(), Todo.class);

        if (condition.getWeather() != null && !condition.getWeather().isBlank()) {
            dataQuery.setParameter("weather", condition.getWeather());
        }
        if (condition.getModifiedFrom() != null) {
            LocalDateTime localDateTimeFrom=condition.getModifiedFrom().atTime(LocalTime.MIN);
            dataQuery.setParameter("modifiedFrom", localDateTimeFrom);
        }
        if (condition.getModifiedTo() != null) {
            LocalDateTime localDateTimeTo=condition.getModifiedTo().atTime(LocalTime.MAX);
            dataQuery.setParameter("modifiedTo", localDateTimeTo);
        }

        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        List<Todo> todos = dataQuery.getResultList();

        // ===== 2. Count 쿼리 (Fetch Join 제거) =====
        StringBuilder countQueryBuilder = new StringBuilder();
        countQueryBuilder.append("SELECT COUNT(t) FROM Todo t WHERE 1=1");

        if (condition.getWeather() != null && !condition.getWeather().isBlank()) {
            countQueryBuilder.append(" AND t.weather = :weather");
        }
        if (condition.getModifiedFrom() != null) {
            countQueryBuilder.append(" AND t.modifiedAt >= :modifiedFrom");
        }
        if (condition.getModifiedTo() != null) {
            countQueryBuilder.append(" AND t.modifiedAt <= :modifiedTo");
        }

        TypedQuery<Long> countQuery = em.createQuery(countQueryBuilder.toString(), Long.class);

        if (condition.getWeather() != null && !condition.getWeather().isBlank()) {
            countQuery.setParameter("weather", condition.getWeather());
        }
        if (condition.getModifiedFrom() != null) {
            LocalDateTime localDateTimeFrom=condition.getModifiedFrom().atTime(LocalTime.MIN);
            countQuery.setParameter("modifiedFrom", localDateTimeFrom);
        }
        if (condition.getModifiedTo() != null) {
            LocalDateTime localDateTimeTo=condition.getModifiedTo().atTime(LocalTime.MAX);
            countQuery.setParameter("modifiedTo", localDateTimeTo);
        }

        //page 직접구현으로 진행
        long total = countQuery.getSingleResult();

        return new PageImpl<>(todos, pageable, total);
    }

    @Override
    public Optional<Todo> findTodoWithUserByQquery(Long todoId) {
        //처음이라서 static 사용 X
        QTodo todo=QTodo.todo;
        QUser user=QUser.user;

        return Optional.ofNullable(jpaQueryFactory.select(todo).
                from(todo)
                .where(todo.id.eq(todoId))
                //명시적 join(on역할)
                .leftJoin(todo.user, user).fetchJoin()
                .fetchOne());
    }
    //page 조금 더 깔끔하게

    @Override
    public Page<TodoAdminResponse> findTodosBySearchCondition(LocalDateTime createdFrom, LocalDateTime createdTo, String nickName, String title, Pageable pageable) {
        QTodo todo= QTodo.todo;
        QComment comment=QComment.comment;
        QManager manager=QManager.manager;
        QUser user=QUser.user;

        BooleanBuilder where =new BooleanBuilder();
        if (createdFrom!= null){
            where.and(todo.createdAt.after(createdFrom));
        }
        if (createdTo!= null){
            where.and(todo.createdAt.before(createdTo));
        }
        if (title != null && !title.isBlank()) {
            where.and(todo.title.containsIgnoreCase(title));
        }
        if (nickName != null && !nickName.isBlank()) {
            where.and(todo.managers.any().user.nickname.containsIgnoreCase(nickName));
        }

        List<Long> todoIds = jpaQueryFactory
                .select(todo.id)
                .from(todo)
                .where(where)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (todoIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<TodoAdminResponse> content = jpaQueryFactory
                .select(Projections.fields(
                        TodoAdminResponse.class,
                        todo.id.as("todoId"),
                        todo.title,
                        todo.managers.size().as("managerCount"),
                        todo.comments.size().as("commentCount")
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.comments, comment)
                .where(todo.id.in(todoIds))
                .groupBy(todo.id)
                .fetch();
        // 분석 필요
        Map<Long, List<String>> nicknameMap = jpaQueryFactory
                .select(todo.id, user.nickname)
                .from(todo)
                .join(todo.managers, manager)
                .join(manager.user, user)
                .where(todo.id.in(todoIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(todo.id),//없으면 위에서 체크가 되서
                        Collectors.mapping(tuple -> tuple.get(user.nickname), Collectors.toList())
                ));

        for (TodoAdminResponse dto : content) {
            dto.setManagerNickNames(nicknameMap.getOrDefault(dto.getTodoId(), List.of()));
        }

        Long total = Optional.ofNullable(
                jpaQueryFactory
                        .select(todo.countDistinct())
                        .from(todo)
                        .leftJoin(todo.managers, manager)
                        .leftJoin(todo.comments, comment)
                        .where(where)
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content,pageable,total);
    }
}


