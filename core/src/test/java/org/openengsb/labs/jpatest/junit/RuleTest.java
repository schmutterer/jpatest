package org.openengsb.labs.jpatest.junit;

import org.junit.Rule;
import org.junit.Test;

import javax.persistence.EntityManager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class RuleTest {

    @Rule
    public TestPersistenceUnit persistenceXml = new TestPersistenceUnit();

    @Test
    public void testApp() {
        TestModel testModel = new TestModel();
        testModel.setValue("TEST");
        EntityManager em = persistenceXml.getEntityManagerFactory("jpa-unit-test");
        em.getTransaction().begin();
        em.persist(testModel);
        em.getTransaction().commit();
        TestModel queriedModel = em.find(TestModel.class, testModel.getId());
        assertThat(queriedModel.getValue(), is("TEST"));
    }
}
