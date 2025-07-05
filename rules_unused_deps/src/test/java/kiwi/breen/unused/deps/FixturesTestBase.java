package kiwi.breen.unused.deps;

import com.google.devtools.build.lib.view.proto.Deps;
import com.google.protobuf.TextFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class FixturesTestBase
{
    protected static List<String> loadTextFixture(final String resource) throws IOException
    {
        try (
                final InputStream in = FixturesTestBase.class.getResourceAsStream(resource);
                final BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
        {
            return reader.lines().toList();
        }
    }

    protected static Deps.Dependencies loadProtoFixture(final String resource) throws IOException
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
        try (final InputStream in = FixturesTestBase.class.getResourceAsStream(resource))
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
