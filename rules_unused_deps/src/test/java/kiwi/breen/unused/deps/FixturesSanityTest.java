package kiwi.breen.unused.deps;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class FixturesSanityTest extends FixturesTestBase
{
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]{
                {"/hello1.txt", 1},
                {"/hello2.txt", 2},
        });
    }

    @Parameterized.Parameter
    public String resource;

    @Parameterized.Parameter(1)
    public int expectedDirectDependencyCount;

    private List<String> dependencies;

    @Before
    public void setUp() throws IOException
    {
        dependencies = loadTextFixture(resource);
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
                dependencies,
                hasSize(expectedDirectDependencyCount)
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldHaveUsedDepSLF4JImplementation()
    {
        assumeThat(
                "expecting a second direct dependency",
                expectedDirectDependencyCount,
                equalTo(2));
        assertThat(
                dependencies,
                hasItems(
                        containsString("org/slf4j/slf4j-api"),
                        containsString("org/slf4j/slf4j-simple")
                ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldHaveUsedDepSLF4JAPI()
    {
        assumeThat(
                "expecting exactly one direct dependency",
                expectedDirectDependencyCount,
                equalTo(1));
        assertThat(
                dependencies,
                hasItems(
                        containsString("org/slf4j/slf4j-api")
                ));
    }
}