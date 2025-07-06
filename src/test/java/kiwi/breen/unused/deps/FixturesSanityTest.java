package kiwi.breen.unused.deps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class FixturesSanityTest
{
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]{
                {"/fixtures/fixture1.txt", 1},
                {"/fixtures/fixture2.txt", 2},
        });
    }

    @Parameterized.Parameter
    public String resource;

    @Parameterized.Parameter(1)
    public int expectedDirectDependencyCount;

    private Map<String, String> dependencies;

    @Before
    public void setUp() throws IOException
    {
        dependencies = Loaders.loadDeclaredDeps(resource);
    }

    @Test
    public void shouldReadDirectDepsText()
    {
        assertNotNull(dependencies);
    }

    @Test
    public void shouldHaveDirectDependencyCount()
    {
        assertThat(
                dependencies.keySet(),
                hasSize(expectedDirectDependencyCount)
        );
    }

    @Test
    public void shouldHaveUsedDepSLF4JImplementation()
    {
        assumeThat(
                "expecting a second direct dependency",
                expectedDirectDependencyCount,
                equalTo(2));
        assertThat(
                dependencies,
                allOf(
                        hasEntry(
                                endsWith(":org_slf4j_slf4j_api"),
                                containsString("org/slf4j/slf4j-api")
                        ),
                        hasEntry(
                                endsWith(":org_slf4j_slf4j_simple"),
                                containsString("org/slf4j/slf4j-simple")
                        )

                ));
    }

    @Test
    public void shouldHaveUsedDepSLF4JAPI()
    {
        assumeThat(
                "expecting exactly one direct dependency",
                expectedDirectDependencyCount,
                equalTo(1));
        assertThat(
                dependencies,
                hasEntry(
                        endsWith(":org_slf4j_slf4j_api"),
                        containsString("org/slf4j/slf4j-api")
                ));
    }

    @Test
    public void sanityCheckMatcher()
    {
        final Map<String, String> map = Map.of(
                "@@rules_jvm_external++maven+maven//:org_slf4j_slf4j_simple",
                "bazel-out/k8-fastbuild/bin/external/rules_jvm_external++maven+maven/org/slf4j/slf4j-simple/2.0.17/processed_slf4j-simple-2.0.17.jar",
                "@@rules_jvm_external++maven+maven//:org_slf4j_slf4j_api",
                "bazel-out/k8-fastbuild/bin/external/rules_jvm_external++maven+maven/org/slf4j/slf4j-api/2.0.17/processed_slf4j-api-2.0.17.jar"
        );

        assertThat(map, hasEntry(
                "@@rules_jvm_external++maven+maven//:org_slf4j_slf4j_simple",
                "bazel-out/k8-fastbuild/bin/external/rules_jvm_external++maven+maven/org/slf4j/slf4j-simple/2.0.17/processed_slf4j-simple-2.0.17.jar"));

        assertThat(map, hasEntry(
                equalTo("@@rules_jvm_external++maven+maven//:org_slf4j_slf4j_simple"),
                equalTo("bazel-out/k8-fastbuild/bin/external/rules_jvm_external++maven+maven/org/slf4j/slf4j-simple/2.0.17/processed_slf4j-simple-2.0.17.jar")));

        assertThat(map, hasEntry(
                endsWith(":org_slf4j_slf4j_simple"),
                containsString("org/slf4j/slf4j-simple")));

    }
}