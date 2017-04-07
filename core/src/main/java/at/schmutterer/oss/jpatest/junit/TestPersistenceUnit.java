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
package at.schmutterer.oss.jpatest.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.ManagedType;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPersistenceUnit implements MethodRule {

    public static final String JPATEST_SERVER_PORT = "h2.tcp.port";

    private static final Logger LOGGER = LoggerFactory.getLogger(TestPersistenceUnit.class);
    private static final Properties GLOBAL_DEFAULTS = new Properties(){{
        put("javax.persistence.transactionType", "RESOURCE_LOCAL");
        // EclipseLink
        put("javax.persistence.jdbc.driver", "org.h2.Driver");
        put("eclipselink.ddl-generation", "create-tables");
        put("eclipselink.ddl-generation.output-mode", "database");
        // OpenJPA
        put("openjpa.Connection2DriverName", "org.h2.Driver");
        put("openjpa.jdbc.SynchronizeMappings", "buildSchema(SchemaAction='add')");
        put("openjpa.ConnectionRetainMode", "always");
        put("openjpa.ConnectionFactoryMode", "local");
        // Hibernate
        put("hibernate.connection.driver_class", "org.h2.Driver");
        put("hibernate.hbm2ddl.auto", "create-drop");
        put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    }};

    private static Map<String, EntityManagerFactory> emCache = new HashMap<String, EntityManagerFactory>();
    private Set<EntityManagerFactory> usedPersistenceUnits = new HashSet<EntityManagerFactory>();
    private Set<EntityManager> createdEntityManagers = new HashSet<EntityManager>();
    private Server tcpServer;
    private Properties propertyOverrides;

    public TestPersistenceUnit() {
        init(new Properties());
    }

    /**
     * let the h2-database start a tcp-server on the given port, to connect during tests
     */
    public TestPersistenceUnit(int port) {
        Properties properties = new Properties();
        properties.put(JPATEST_SERVER_PORT, port);
        init(properties);
    }

    /**
     * system-resources that contain propertyfiles with additional
     * property-overrides for all persistence-units
     *
     * Files are loaded via ClassLoader#getSystemResourceAsStream
     */
    public TestPersistenceUnit(String... propertyFiles) {
        Properties properties = new Properties();
        for (String propertyFile : propertyFiles) {
            try {
                properties.load(ClassLoader.getSystemResourceAsStream(propertyFile));
            } catch (IOException e) {
                LOGGER.warn("unable to read propertyfile" + propertyFile, e);
            }
        }
        init(properties);
    }

    /**
     * system-resources that contain propertyfiles with additional
     * property-overrides for all persistence-units
     */
    public TestPersistenceUnit(InputStream... propertyStreams) {
        Properties properties = new Properties();
        for (InputStream propertyStream : propertyStreams) {
            try {
                properties.load(propertyStream);
            } catch (IOException e) {
                LOGGER.warn("unable to read propertyfile" + propertyStream, e);
            }
        }
        init(properties);
    }

    /**
     * override properties for all persistence-units
     * @param propertyMap
     */
    public TestPersistenceUnit(Map<?,?> propertyMap) {
        Properties properties = new Properties();
        properties.putAll(propertyMap);
        init(properties);
    }

    private void init(Properties properties) {
        String globalPropertyFile = System.getProperty("jpatest.global.properties");
        if (globalPropertyFile != null) {
            this.propertyOverrides = new Properties();
            File file = new File(globalPropertyFile);
            try (FileReader fileReader = new FileReader(file)){
                this.propertyOverrides.load(fileReader);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            this.propertyOverrides.putAll(properties);
        } else {
            this.propertyOverrides = properties;
        }
        if (this.propertyOverrides.containsKey(JPATEST_SERVER_PORT)) {
            String port = (String) properties.get(JPATEST_SERVER_PORT);
            try {
                tcpServer = Server.createTcpServer("-tcpPort", port);
            } catch (SQLException e) {
                throw new AssertionError(e);
            }
        }
    }

    private EntityManager makeEntityManager(EntityManagerFactory emf) {
        Properties emProperties = new Properties();
        emProperties.put("openjpa.TransactionMode", "local");
        return emf.createEntityManager(emProperties);
    }

    private EntityManagerFactory makeEntityManagerFactory(String s) throws SQLException {
        Properties persistenceUnitProperties = new Properties();
        persistenceUnitProperties.putAll(GLOBAL_DEFAULTS);
        String url = String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1", s);
        // EclipseLink
        persistenceUnitProperties.put("javax.persistence.jdbc.url", url);
        // OpenJPA
        persistenceUnitProperties.put("openjpa.Connection2URL", url);
        // Hibernate
        persistenceUnitProperties.put("hibernate.connection.url", url);
        persistenceUnitProperties.putAll(propertyOverrides);
        return Persistence.createEntityManagerFactory(s, persistenceUnitProperties);
    }

    public EntityManager getEntityManager(String s) throws SQLException {
        EntityManagerFactory entityManagerFactory = getEntityManagerFactory(s);
        usedPersistenceUnits.add(entityManagerFactory);
        EntityManager entityManager = makeEntityManager(entityManagerFactory);
        createdEntityManagers.add(entityManager);
        return entityManager;
    }

    private EntityManagerFactory getEntityManagerFactory(String s) throws SQLException {
        if (!emCache.containsKey(s)) {
            emCache.put(s, makeEntityManagerFactory(s));
        }
        return emCache.get(s);
    }

    private class PersistenceStatement extends Statement {

        private Statement parent;

        private PersistenceStatement(Statement parent) {
            this.parent = parent;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                parent.evaluate();
                for (EntityManager e : createdEntityManagers) {
                    if (e.getTransaction().isActive()) {
                        e.getTransaction().rollback();
                        throw new AssertionError("EntityManager " + e + " left an open transaction");
                    }
                }
            } finally {
                try {
                    for (EntityManager e : createdEntityManagers) {
                        e.close();
                    }
                } finally {
                    createdEntityManagers.clear();
                    for (EntityManagerFactory emf : usedPersistenceUnits) {
                        clearTables(emf);
                    }
                }
            }
        }

        private void clearTables(EntityManagerFactory emf) {
            long start = System.currentTimeMillis();
            Set<ManagedType<?>> types = emf.getMetamodel().getManagedTypes();
            EntityManager entityManager = makeEntityManager(emf);
            Set<Class<?>> javaTypes = new HashSet<Class<?>>();
            for (ManagedType<?> type : types) {
                javaTypes.add(type.getJavaType());
            }
            int lastsize = javaTypes.size();
            while (!javaTypes.isEmpty()) {
                Iterator<Class<?>> iterator = javaTypes.iterator();
                Collection<Exception> exceptionsDuringClean = new ArrayList<Exception>();
                while (iterator.hasNext()) {
                    Class<?> javaType = iterator.next();
                    String name = retrieveEntityName(javaType);
                    if (name == null) {
                        LOGGER.warn("could not determine name for entity {}", javaType);
                        iterator.remove();
                        continue;
                    }
                    try {
                        entityManager.getTransaction().begin();
                        entityManager.createQuery("DELETE FROM " + name).executeUpdate();
                        entityManager.getTransaction().commit();
                        iterator.remove();
                    } catch (Exception e) {
                        if (e instanceof PersistenceException
                                || e.getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException") // for eclipse-link < 2.5.0
                                ) {
                            exceptionsDuringClean.add(e);
                            LOGGER.debug("error during delete, could be normal", e);
                            entityManager.getTransaction().rollback();
                        }
                    }
                }
                if (javaTypes.size() == lastsize) {
                    entityManager.getTransaction().begin();
                    entityManager.createNativeQuery("SHUTDOWN").executeUpdate();

                    try {
                        entityManager.getTransaction().commit();
                    } catch (Exception e) {
                        // will always fail because database is shutting down,
                        // but we need to clear the transaction-state in the entitymanager
                        break;
                    }
                    LOGGER.error("could not clean database", exceptionsDuringClean.iterator().next());
                }
                lastsize = javaTypes.size();
            }
            entityManager.close();
            LOGGER.info("cleared database in {}ms", System.currentTimeMillis() - start);
        }

        private String retrieveEntityName(Class<?> javaType) {
            Entity entity = javaType.getAnnotation(Entity.class);
            if (entity == null) {
                return null;
            }
            if (entity.name().isEmpty()) {
                return javaType.getSimpleName();
            }
            return entity.name();
        }
    }

    private class ServerSpawningStatement extends PersistenceStatement {
        private ServerSpawningStatement(Statement parent) {
            super(parent);
        }

        @Override
        public void evaluate() throws Throwable {
            tcpServer.start();
            LOGGER.info("TCP server started");
            try {
                super.evaluate();
            } finally {
                LOGGER.info("TCP server stopped");
                tcpServer.stop();
            }
        }
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod frameworkMethod, Object o) {
        if (tcpServer != null) {
            return new ServerSpawningStatement(statement);
        }
        return new PersistenceStatement(statement);
    }
}
