package kiwi.breen.unused.deps;

import com.google.devtools.build.lib.view.proto.Deps;
import com.google.protobuf.TextFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface Loaders
{
    @FunctionalInterface
    interface Loader
    {
        void load(InputStream in, Deps.Dependencies.Builder builder) throws IOException;
    }

    Pattern LINE_PARSER = Pattern.compile("(.*)\\t(.*)");

    static Map<String, String> loadDeclaredDeps(final String resource) throws IOException
    {
        try (
                final InputStream in = Loaders.class.getResourceAsStream(resource);
                final BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
        {
            return reader.lines()
                    .map(LINE_PARSER::matcher)
                    .filter(Matcher::matches)
                    .collect(Collectors.toMap(
                            m -> m.group(1),
                            m -> m.group(2)
                    ));
        }
    }

    static Deps.Dependencies loadUsedDeps(final String resource) throws IOException
    {
        if (resource.endsWith(".jdeps.textproto") || resource.endsWith(".jdeps.prototext"))
        {
            return loadProto(resource, Loaders::loadText);
        }
        else if (resource.endsWith(".jdeps"))
        {
            return loadProto(resource, Loaders::loadBinary);
        }
        throw new IllegalArgumentException("not proto nor prototext");
    }

    private static void loadBinary(final InputStream in, final Deps.Dependencies.Builder builder) throws IOException
    {
        builder.mergeFrom(in);
    }

    private static void loadText(final InputStream in, final Deps.Dependencies.Builder builder) throws IOException
    {
        try (final InputStreamReader reader = new InputStreamReader(in))
        {
            TextFormat.merge(reader, builder);
        }
    }

    private static Deps.Dependencies loadProto(
            final String resource,
            final Loader loader) throws IOException
    {
        try (final InputStream in = Loaders.class.getResourceAsStream(resource))
        {
            assert null != in : "fixture is null";
            final Deps.Dependencies.Builder builder = Deps.Dependencies.newBuilder();
            loader.load(in, builder);
            return builder.build();
        }
    }
}
