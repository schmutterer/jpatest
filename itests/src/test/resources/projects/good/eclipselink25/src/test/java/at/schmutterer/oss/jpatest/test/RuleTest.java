/**
 * Licensed to Schmutterer & Partner IT GmbH under one or more
 * contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Schmutterer & Partner IT GmbH
 * licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
package at.schmutterer.oss.jpatest.test;

import at.schmutterer.oss.jpatest.junit.TestPersistenceUnit;
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
        TestModel testModel = new TestModel("TEST");
        EntityManager em = persistenceXml.getEntityManager("jpa-unit-test");
        em.getTransaction().begin();
        em.persist(testModel);
        em.getTransaction().commit();
        TestModel queriedModel = em.find(TestModel.class, testModel.getId());
        assertThat(queriedModel.getValue(), is("TEST"));
    }

    @Test
    public void testSupportsJPA21() throws Exception {
        EntityManager entityManager = persistenceXml.getEntityManager("jpa-unit-test");
        TestController testController = new TestController(entityManager);
        entityManager.getTransaction().begin();
        TestModel foo = new TestModel("foo");
        entityManager.persist(foo);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        testController.makeAllBars();
        entityManager.getTransaction().commit();

        entityManager.refresh(foo);
        assertThat(foo.getValue(), is("bar"));
    }
}
