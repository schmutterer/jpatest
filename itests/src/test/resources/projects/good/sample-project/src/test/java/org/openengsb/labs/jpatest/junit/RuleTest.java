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
package org.openengsb.labs.jpatest.junit;

import org.junit.Rule;
import org.junit.Test;

import javax.persistence.EntityManager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for simple App.
 */
public class RuleTest {

    @Rule
    public TestPersistenceUnit persistenceXml = new TestPersistenceUnit();

    @Test
    public void testApp() throws Exception {
        TestModel testModel = new TestModel();
        testModel.setValue("TEST");
        EntityManager em = persistenceXml.getEntityManager("jpa-unit-test");
        em.getTransaction().begin();
        em.persist(testModel);
        em.getTransaction().commit();
        TestModel queriedModel = em.find(TestModel.class, testModel.getId());
        assertThat(queriedModel.getValue(), is("TEST"));
    }
}
