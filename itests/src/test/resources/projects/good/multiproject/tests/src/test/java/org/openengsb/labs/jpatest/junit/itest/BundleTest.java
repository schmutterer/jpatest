/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.labs.jpatest.junit.itest;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.openengsb.labs.jpatest.sample1.TestModel;
import org.openengsb.labs.jpatest.example2.Test2Model;
import org.openengsb.labs.jpatest.junit.TestPersistenceUnit;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for simple App.
 */
public class BundleTest {

    @Rule
    public TestPersistenceUnit persistenceXml = new TestPersistenceUnit();

    @AfterClass
    public static void makeSureTablesAreDeleted() throws Exception {
        EntityManager em = new TestPersistenceUnit().getEntityManager("jpa-test2");
        Query query = em.createQuery("SELECT t FROM TEST2 t");
        assertThat(query.getResultList().size(), is(0));
        em = new TestPersistenceUnit().getEntityManager("jpa-test");
        query = em.createQuery("SELECT t FROM TestModel t");
        assertThat(query.getResultList().size(), is(0));
    }

    @Test
    public void testEntityManagerForPU_shouldWork() throws Exception {
        TestModel testModel = new TestModel();
        testModel.setValue("TEST");
        EntityManager em = persistenceXml.getEntityManager("jpa-test");
        em.getTransaction().begin();
        em.persist(testModel);
        em.getTransaction().commit();
        TestModel queriedModel = em.find(TestModel.class, testModel.getId());
        assertThat(queriedModel.getValue(), is("TEST"));
        Query query = em.createQuery("SELECT t FROM TestModel t");
        assertThat(query.getResultList().size(), is(1));
    }

    @Test
    public void testPUWithNamedEntity_shouldWork() throws Exception {
        Test2Model testModel = new Test2Model();
        testModel.setValue("TEST");
        EntityManager em = persistenceXml.getEntityManager("jpa-test2");
        em.getTransaction().begin();
        em.persist(testModel);
        em.getTransaction().commit();
        Test2Model queriedModel = em.find(Test2Model.class, testModel.getId());
        assertThat(queriedModel.getValue(), is("TEST"));
        Query query = em.createQuery("SELECT t FROM TEST2 t");
        assertThat(query.getResultList().size(), is(1));
    }


}
