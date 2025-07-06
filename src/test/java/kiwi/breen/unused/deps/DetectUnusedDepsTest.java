package kiwi.breen.unused.deps;

import com.google.devtools.build.lib.view.proto.Deps;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.StringContains.containsString;

@RunWith(Parameterized.class)
public class DetectUnusedDepsTest extends FixturesTestBase
{
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]{
                {
                        "/fixtures/fixture1.txt",
                        "/fixtures/libfixture1.jdeps",
                        empty()
                },
                {
                        "/fixtures/fixture2.txt",
                        "/fixtures/libfixture2.jdeps",
                        contains(
                            containsString(":org_slf4j_slf4j_simple")
                        )
                },
        });
    }

    @Parameterized.Parameter
    public String directResource;
    @Parameterized.Parameter(1)
    public String usedResource;
    @Parameterized.Parameter(2)
    public Matcher<Collection<String>> expectedUnusedDeps;

    private Map<String, String> directDeps;
    private Deps.Dependencies usedDeps;
    private final UnusedDeps unusedDeps = new UnusedDeps();

    @Before
    public void setUp() throws Exception
    {
        directDeps = loadTextFixture(directResource);
        usedDeps = loadProtoFixture(usedResource);
    }

    @Test
    public void shouldDetectNoUnusedDeps()
    {
        final Collection<String> unusedDeps = this.unusedDeps.detect(usedDeps, directDeps);
        assertThat(unusedDeps, expectedUnusedDeps);
    }
}
