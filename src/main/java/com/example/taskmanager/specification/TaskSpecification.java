package com.example.taskmanager.specification;

import com.example.taskmanager.entity.Task;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    public static Specification<Task> filter(
            String keyword,
            Boolean completed
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {

                String searchKeyword =
                        "%" + keyword.toLowerCase() + "%";

                Predicate titlePredicate =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                searchKeyword
                        );

                Predicate descriptionPredicate =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")),
                                searchKeyword
                        );

                predicates.add(
                        criteriaBuilder.or(
                                titlePredicate,
                                descriptionPredicate
                        )
                );
            }

            if (completed != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("completed"),
                                completed
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}