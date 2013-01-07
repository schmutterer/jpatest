package org.openengsb.labs.jpatest.junit.itest;

import org.junit.Rule;
import org.junit.Test;
import org.openengsb.labs.jpatest.example2.Test2Model;
import org.openengsb.labs.jpatest.junit.TestPersistenceUnit;

import javax.persistence.EntityManager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class BundleTest {

    @Rule
    public TestPersistenceUnit persistenceXml = new TestPersistenceUnit();

    @Test
    public void testApp() {
        Test2Model testModel = new Test2Model();
        testModel.setValue("TEST");
        EntityManager em = persistenceXml.getEntityManagerFactory("jpa-test2");
        em.persist(testModel);
        Test2Model queriedModel = em.find(Test2Model.class, testModel.getId());
        assertThat(queriedModel.getValue(), is("TEST"));
    }
}
