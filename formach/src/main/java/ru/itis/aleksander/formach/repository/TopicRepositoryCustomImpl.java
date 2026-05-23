package ru.itis.aleksander.formach.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.itis.aleksander.formach.entity.Topic;
import ru.itis.aleksander.formach.entity.TopicType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TopicRepositoryCustomImpl implements TopicRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Topic> advancedSearch(String titleContains,
                                      TopicType type,
                                      Boolean isClosed,
                                      LocalDateTime createdAfter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Topic> cq = cb.createQuery(Topic.class);
        Root<Topic> root = cq.from(Topic.class);

        List<Predicate> preds = new ArrayList<>();
        if (titleContains != null && !titleContains.isBlank()) {
            preds.add(cb.like(cb.lower(root.get("title")),
                    "%" + titleContains.toLowerCase() + "%"));
        }
        if (type != null) {
            preds.add(cb.equal(root.get("type"), type));
        }
        if (isClosed != null) {
            preds.add(cb.equal(root.get("isClosed"), isClosed));
        }
        if (createdAfter != null) {
            preds.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
        }

        cq.where(preds.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("pinnedByAdmin")), cb.desc(root.get("createdAt")));

        return em.createQuery(cq).getResultList();
    }
}
