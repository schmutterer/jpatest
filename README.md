JPA Test Helper
=====================

The purpose of this library is to provide an out-of-the box configuration for testing code that relies on JPA.
While there are many other libraries for that purpuse the specific goals of this one are:
* test the **real persistence.xml** instead of a separate one just for testing (so that they don't get out of sync)
* Fast teardown of test-database (Only records are deleted, dbs are reused)
* **Out-of-the-box memory-db** config that is **easy to use**

[![Build Status](https://travis-ci.org/schmutterer/labs-jpatest.png?branch=master)](https://travis-ci.org/openengsb-labs/labs-jpatest)

How to build
====================
* Install JDK 7 or higher

You can install [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or
[OpenJDK](http://openjdk.java.net/install/index.html) depending on the OS you use.
Other JVM implementations should also work, but are untested.

* Install [Maven 3 or higher](http://maven.apache.org/download.html)

Be sure to follow the provided [installation instructions](http://maven.apache.org/download.html#Installation)

* configure **JAVA_HOME** and **PATH** environment variables

make sure the JAVA_HOME environment variable points to the path of your JDK installation and that both **javac** and
**mvn** are available in your PATH-variable

* Run **mvn install** from the project's root directory

That's it.

How to use
====================
With maven add this dependency to your pom.xml:
<pre>
&lt;dependency&gt;
  &lt;groupId&gt;org.openengsb.labs.jpatest&lt;/groupId&gt;
  &lt;artifactId&gt;jpa-test-core&lt;/artifactId&gt;
  &lt;scope&gt;test&lt;/scope&gt;
  &lt;version&gt;LATEST&lt;/version&gt;
&lt;/dependency&gt;
</pre>

To use EntityManagers in your junit-tests include the Rule in your tests like this:

<pre>
@Rule
public final TestPersistenceUnit testPersistenceUnit = new TestPersistenceUnit();

@Test
public void myTest throws Exception {
  EntityManager em = testPersistenceUnit.getPersistenceUnit("my-persistence-unit")
  // do stuff with our em
}
</pre>

By default your persistence-units will connect to an in-memory h2-database, that is cleared automatically after each test.
There's no need for creating a separate persistence.xml just for tests, because the TestPersistenceUnit will override the necessary properties, so that the h2-database with resource-local transaction is used.
Look at the java-doc for possibilities to override additional properties.
