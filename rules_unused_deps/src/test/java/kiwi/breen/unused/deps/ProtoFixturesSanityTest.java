package kiwi.breen.unused.deps;

import com.google.devtools.build.lib.view.proto.Deps;
import com.google.protobuf.TextFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;

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
                {"/libhello1.jdeps"},
                {"/libhello2.jdeps"},
                {"/libhello1.jdeps.textproto"},
                {"/libhello2.jdeps.textproto"},
        });
    }

    @Parameterized.Parameter
    public String resource;

    private Deps.Dependencies dependencies;

    @Before
    public void setUp() throws IOException
    {
        dependencies = loadFixture(resource);
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

    private static Deps.Dependencies loadFixture(final String resource) throws IOException
    {
        if (resource.endsWith(".jdeps.textproto"))
        {
            return loadTextProto(resource);
        }
        else if (resource.endsWith(".jdeps"))
        {
            return loadBinaryProto(resource);
        }
        throw new IllegalArgumentException("not proto nor prototext");
    }

    private static Deps.Dependencies loadBinaryProto(final String resource) throws IOException
    {
        return loadProtoFixture(
                resource,
                (in, builder) -> builder.mergeFrom(in));
    }

    private static Deps.Dependencies loadTextProto(final String resource) throws IOException
    {
        return loadProtoFixture(
                resource,
                (in, builder) -> {
                    try (final InputStreamReader reader = new InputStreamReader(in))
                    {
                        TextFormat.merge(reader, builder);
                    }
                }
        );
    }

    private static Deps.Dependencies loadProtoFixture(
            final String resource,
            final Loader loader) throws IOException
    {
        try (final InputStream in = ProtoFixturesSanityTest.class.getResourceAsStream(resource))
        {
            assertNotNull("fixture is null", in);
            final Deps.Dependencies.Builder builder = Deps.Dependencies.newBuilder();
            loader.load(in, builder);
            return builder.build();
        }
    }

    @FunctionalInterface
    interface Loader
    {

        void load(InputStream in, Deps.Dependencies.Builder builder) throws IOException;
    }
}