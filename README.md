JPA Test Helper
=====================

The purpose of this library is to provide an out-of-the box configuration for testing code that relies on JPA.
While there are many other libraries for that purpuse the specific goals of this one are:
* test the **real persistence.xml** instead of a separate one just for testing (so that they don't get out of sync)
* Fast teardown of test-database (Only records are deleted, dbs are reused)
* **Out-of-the-box memory-db** config that is **easy to use**

[![Build Status](https://travis-ci.org/openengsb-labs/labs-jpatest.png?branch=master)](https://travis-ci.org/openengsb-labs/labs-jpatest)

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
&lt;dependency&gt;
<pre>
&lt;dependency&gt;
  &lt;groupId&gt;org.openengsb.labs.jpatest&lt;/groupId&gt;
  &lt;artifactId&gt;jpa-test-core&lt;/artifactId&gt;
  &lt;scope&gt;test&lt;/scope&gt;
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

Limitations
====================
Right now the library will only work testing with OpenJPA and H2Database. While this might sound as a no go
for your project consider that most of your JPA tests will tackle if your queries are correct JPA provider and
database independent. So, while this might not be a 100% match it will (in many cases) still provider ways better
results than going without it.