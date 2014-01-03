package org.openengsb.labs.jpatest.junit;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

public class TestController {

    private final EntityManager entityManager;

    public TestController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void makeAllBars() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<TestModel> criteriaUpdate = cb.createCriteriaUpdate(TestModel.class);
        Root<TestModel> root = criteriaUpdate.from(TestModel.class);
        criteriaUpdate.set(root.get(TestModel_.value), "bar");
        entityManager.createQuery(criteriaUpdate).executeUpdate();
    }
}
