package org.example.expert.domain.todo.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.request.TodoCondition;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class TodoRepositoryCustomImpl implements TodoRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<Todo> findTodoWithWeather(TodoCondition condition) {
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

        dataQueryBuilder.append(" ORDER BY t.modifiedAt DESC");

        TypedQuery<Todo> dataQuery = em.createQuery(dataQueryBuilder.toString(), Todo.class);

        if (condition.getWeather() != null && !condition.getWeather().isBlank()) {
            dataQuery.setParameter("weather", condition.getWeather());
        }
        if (condition.getModifiedFrom() != null) {
            dataQuery.setParameter("modifiedFrom", condition.getModifiedFrom());
        }
        if (condition.getModifiedTo() != null) {
            dataQuery.setParameter("modifiedTo", condition.getModifiedTo());
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
            countQuery.setParameter("modifiedFrom", condition.getModifiedFrom());
        }
        if (condition.getModifiedTo() != null) {
            countQuery.setParameter("modifiedTo", condition.getModifiedTo());
        }

        long total = countQuery.getSingleResult();

        return new PageImpl<>(todos, pageable, total);
    }
}

//implements PagingAndSortingRepository<Todo,Long>를 상속받지 말라고합니다.
//혼동이 일어날 수 있다면서...(gpt 검수 결과)
//서비스단이 아닌 customRepository 를 사용한 이유는 추후에 경우의 수가 늘어나면 서비스단은 조건문이 2의 배수로 늘어납니다.
//일단 날씨정보,페이징 정보 받기,수정일 기준으로 기간 검색
//page 전체갯수때문에 FETCH join을 사용을 안한다는것을 알아도 놓치기 쉬움....(gpt 검수 결과)