import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UseInMavenProjectsTest {

    @Test
    public void testMultiProject() throws Exception {
        assertThat(executeMavenBuild("good/multiproject/pom.xml"), is(0L));
    }

    @Test
    public void testSimple() throws Exception {
        assertThat(executeMavenBuild("good/sample-project/pom.xml"), is(0L));
    }

    public long executeMavenBuild(String name) throws IOException, InterruptedException, ExecutionException {
        String m2Home = System.getenv("M2_HOME");
        String mvnExecutable = "mvn";
        if (m2Home != null) {
            mvnExecutable = new File(m2Home, "bin/" + mvnExecutable).getAbsolutePath();
        }
        URL good = ClassLoader.getSystemResource(name);
        ProcessBuilder processBuilder = new ProcessBuilder(mvnExecutable, "-f", good.getFile(), "install");
        processBuilder.redirectErrorStream(true);

        final Process start = processBuilder.start();
        FutureTask<Void> target = new FutureTask<Void>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                IOUtils.copy(start.getInputStream(), System.out);
                return null;
            }
        });
        new Thread(target).start();
        long result = start.waitFor();
        target.get();
        return result;
    }

}
