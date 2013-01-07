package org.openengsb.labs.jpatest.junit;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestPersistenceUnit implements MethodRule {


    private static Map<String,EntityManagerFactory> emCache = new HashMap<String, EntityManagerFactory>();

    public TestPersistenceUnit() {
    }

    private EntityManager makeEntityManager(String s){
        EntityManagerFactory emf = makeEntityManagerFactory(s);
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

    public EntityManager getEntityManagerFactory(String s) {
        if(!emCache.containsKey(s)){
            emCache.put(s, makeEntityManagerFactory(s));
        }
        return makeEntityManager(s);
    }

    private class PersistenceStatement extends Statement {

        private Statement parent;

        private PersistenceStatement(Statement parent) {
            this.parent = parent;
        }

        @Override
        public void evaluate() throws Throwable {
            parent.evaluate();
        }
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod frameworkMethod, Object o) {
        return statement;
    }
}
