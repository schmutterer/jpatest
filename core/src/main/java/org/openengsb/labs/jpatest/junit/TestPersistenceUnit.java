package org.openengsb.labs.jpatest.junit;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import javax.persistence.*;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import java.util.*;

public class TestPersistenceUnit implements MethodRule {


    private static Map<String,EntityManagerFactory> emCache = new HashMap<String, EntityManagerFactory>();

    private Set<EntityManagerFactory> usedPersistenceUnits = new HashSet<EntityManagerFactory>();

    public TestPersistenceUnit() {
    }

    private EntityManager makeEntityManager(EntityManagerFactory emf){;
        Properties emProperties = new Properties();
        emProperties.put("openjpa.TransactionMode", "local");
        emProperties.put("openjpa.ConnectionFactoryMode", "local");
        return emf.createEntityManager(emProperties);
    }

    private EntityManagerFactory makeEntityManagerFactory(String s) {
        Properties props = new Properties();
        props.put("openjpa.ConnectionURL", String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1", s));
        props.put("openjpa.ConnectionDriverName", "org.h2.Driver");
        props.put("openjpa.Connection2URL", String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1", s));
        props.put("openjpa.Connection2DriverName", "org.h2.Driver");
        props.put("openjpa.jdbc.SynchronizeMappings",
                "buildSchema(SchemaAction='add,deleteTableContents',ForeignKeys=true)");
        props.put("openjpa.ConnectionRetainMode", "always");
        props.put("openjpa.ConnectionFactoryMode", "local");
        return Persistence.createEntityManagerFactory(s, props);
    }

    public EntityManager getEntityManager(String s) {
        EntityManagerFactory entityManagerFactory = getEntityManagerFactory(s);
        usedPersistenceUnits.add(entityManagerFactory);
        return makeEntityManager(entityManagerFactory);
    }

    private EntityManagerFactory getEntityManagerFactory(String s) {
        if(!emCache.containsKey(s)){
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
            usedPersistenceUnits.clear();
            parent.evaluate();
            for (EntityManagerFactory emf : usedPersistenceUnits) {
                clearTables(emf);
            }
        }

        private void clearTables(EntityManagerFactory emf) {
            Set<ManagedType<?>> types = emf.getMetamodel().getManagedTypes();
            for(ManagedType<?> type : types){
                Class<?> javaType = type.getJavaType();
                EntityManager entityManager = makeEntityManager(emf);
                entityManager.getTransaction().begin();
                Query query = entityManager.createQuery("DELETE FROM " + javaType.getSimpleName());
                query.executeUpdate();
                entityManager.getTransaction().commit();
            }
        }
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod frameworkMethod, Object o) {
        return new PersistenceStatement(statement);
    }
}
