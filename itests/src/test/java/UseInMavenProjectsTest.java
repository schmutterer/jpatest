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
