package kiwi.breen.unused.deps;

import com.google.devtools.build.lib.view.proto.Deps;
import com.google.protobuf.TextFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Predicate;
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

    static Map<String, String> loadDeclaredDeps(final Path path) throws IOException
    {
        try (final InputStream in = Files.newInputStream(path))
        {
            return loadDeclaredDeps(in);
        }
    }

    static Map<String, String> loadDeclaredDeps(final String resource) throws IOException
    {
        try (final InputStream in = Loaders.class.getResourceAsStream(resource))
        {
            return loadDeclaredDeps(in);
        }
    }
    static Map<String, String> loadDeclaredDeps(final InputStream in) throws IOException
    {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
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

    Predicate<String> isProtoText = s -> s.endsWith(".jdeps.textproto") || s.endsWith(".jdeps.prototext");
    Predicate<String> isProtoBinary = s -> s.endsWith(".jdeps");

    static Deps.Dependencies loadUsedDeps(final Path path) throws IOException
    {
        if (isProtoText.test(path.toString()))
        {
            return loadProto(path, Loaders::loadText);
        }
        else if (isProtoBinary.test(path.toString()))
        {
            return loadProto(path, Loaders::loadBinary);
        }
        throw new IllegalArgumentException("not proto nor prototext");

    }

    static Deps.Dependencies loadUsedDeps(final String resource) throws IOException
    {
        if (isProtoText.test(resource))
        {
            return loadProto(resource, Loaders::loadText);
        }
        else if (isProtoBinary.test(resource))
        {
            return loadProto(resource, Loaders::loadBinary);
        }
        throw new IllegalArgumentException("not proto nor prototext");
    }

    private static Deps.Dependencies loadProto(
            final String resource,
            final Loader loader) throws IOException
    {
        try (final InputStream in = Loaders.class.getResourceAsStream(resource))
        {
            return loadProto(in, loader);
        }
    }

    private static Deps.Dependencies loadProto(
            final Path path,
            final Loader loader) throws IOException
    {
        try (final InputStream in = Files.newInputStream(path))
        {
            return loadProto(in, loader);
        }
    }

    private static Deps.Dependencies loadProto(final InputStream in, final Loader loader) throws IOException
    {
        final Deps.Dependencies.Builder builder = Deps.Dependencies.newBuilder();
        loader.load(in, builder);
        return builder.build();
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
}
