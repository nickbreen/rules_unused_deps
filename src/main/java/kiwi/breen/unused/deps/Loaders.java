package kiwi.breen.unused.deps;

import com.google.devtools.build.lib.view.proto.Deps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface Loaders
{
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

    private static Map<String, String> loadDeclaredDeps(final InputStream in) throws IOException
    {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
        {
            return reader
                    .lines()
                    .map(LINE_PARSER::matcher)
                    .filter(Matcher::matches)
                    .collect(Collectors.toMap(
                            m -> m.group(1),
                            m -> m.group(2)
                    ));
        }
    }

    static Deps.Dependencies loadUsedDeps(final Path path) throws IOException
    {
        try (final InputStream in = Files.newInputStream(path))
        {
            return Deps.Dependencies.parseFrom(in);
        }
    }

    static Deps.Dependencies loadUsedDeps(final String resource) throws IOException
    {
        try (final InputStream in = Loaders.class.getResourceAsStream(resource))
        {
            return Deps.Dependencies.parseFrom(in);
        }
    }
}
