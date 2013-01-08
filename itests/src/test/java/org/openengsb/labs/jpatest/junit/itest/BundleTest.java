package org.openengsb.labs.jpatest.junit.itest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.openengsb.labs.jpatest.example2.Test2Model;
import org.openengsb.labs.jpatest.junit.TestPersistenceUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class BundleTest {

    @Rule
    public TestPersistenceUnit persistenceXml = new TestPersistenceUnit();

    @AfterClass
    public static void tearDownClass() throws Exception {
        EntityManager em = new TestPersistenceUnit().getEntityManager("jpa-test2");
        Query query = em.createQuery("SELECT t FROM Test2Model t");
        assertThat(query.getResultList().size(), is(0));
    }
    @Test
    public void testApp() {
        Test2Model testModel = new Test2Model();
        testModel.setValue("TEST");
        EntityManager em = persistenceXml.getEntityManager("jpa-test2");
        em.getTransaction().begin();
        em.persist(testModel);
        em.getTransaction().commit();
        Test2Model queriedModel = em.find(Test2Model.class, testModel.getId());
        assertThat(queriedModel.getValue(), is("TEST"));
        Query query = em.createQuery("SELECT t FROM Test2Model t");
        assertThat(query.getResultList().size(), is(1));
    }


}
