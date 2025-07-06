package kiwi.breen.unused.deps;

import com.google.devtools.build.lib.view.proto.Deps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static kiwi.breen.unused.deps.Loaders.loadUsedDeps;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class ProtoFixturesSanityTest
{
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]{
                {"/fixtures/libfixture1.jdeps"},
                {"/fixtures/libfixture2.jdeps"},
                {"/fixtures/libfixture1.jdeps.textproto"},
                {"/fixtures/libfixture2.jdeps.textproto"},
        });
    }

    @Parameterized.Parameter
    public String resource;

    private Deps.Dependencies dependencies;

    @Before
    public void setUp() throws IOException
    {
        dependencies = loadUsedDeps(resource);
    }

    @Test
    public void shouldReadBinaryJdeps()
    {
        assertNotNull(dependencies);
    }

    @Test
    public void shouldHaveExactlyOneDep()
    {
        assertEquals(1, dependencies.getDependencyCount());
    }

    @Test
    public void shouldHaveExactlyOneUsedDep()
    {
        assertEquals(1, dependencies.getDependencyList().size());
    }

    @Test
    public void shouldHaveUsedDepSLF4JAPI()
    {
        assertThat(
                dependencies.getDependencyList(),
                hasItem(
                        allOf(
                                hasProperty(
                                        "kind",
                                        equalTo(Deps.Dependency.Kind.EXPLICIT)),
                                hasProperty(
                                        "path",
                                        containsString("org/slf4j/slf4j-api")
                                )
                        )
                ));
    }

}