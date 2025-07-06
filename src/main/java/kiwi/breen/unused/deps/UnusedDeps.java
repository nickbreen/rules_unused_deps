package kiwi.breen.unused.deps;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.devtools.build.lib.view.proto.Deps;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UnusedDeps
{
    @Parameter(names = {"-u", "--used", "--used-deps"})
    private Path usedDeps;

    @Parameter(names = {"-d", "--direct", "--direct-deps"})
    private Path directDeps;

    @Parameter(names = {"-o", "--out", "--output"}, converter = PrintStreamConverter.class)
    private PrintStream output = System.out;

    @Parameter(names = {"-f", "--format"})
    private String format = "buildozer 'remove deps %2$s' %1$s";

    public static void main(final String[] args) throws Exception
    {
        final UnusedDeps conf = new UnusedDeps();
        JCommander.newBuilder()
                .addObject(conf)
                .build()
                .parse(args);

        final Map<String, String> directDeps =
                Loaders.loadDeclaredDeps(conf.directDeps);
        final Deps.Dependencies usedDeps =
                Loaders.loadUsedDeps(conf.usedDeps);
        final Collection<String> unused = detect(usedDeps, directDeps);
        unused.stream()
                .map(u -> String.format(conf.format, usedDeps.getRuleLabel(), u))
                .forEach(conf.output::println);
    }

    private static final Predicate<Deps.Dependency> directDependencyFilter =
            dep -> Deps.Dependency.Kind.EXPLICIT.equals(dep.getKind());

    static Collection<String> detect(
            final Deps.Dependencies usedDeps,
            final Map<String, String> directDeps)
    {
        final Set<String> usedDirectDeps = usedDeps.getDependencyList().stream()
                .filter(directDependencyFilter)
                .map(Deps.Dependency::getPath)
                .collect(Collectors.toSet());

        final Predicate<Map.Entry<String, String>> dependencyWasUsedFilter =
                e -> !usedDirectDeps.contains(e.getValue());

        return directDeps.entrySet().stream()
                .filter(dependencyWasUsedFilter)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private static class PrintStreamConverter implements IStringConverter<PrintStream>
    {
        @Override
        public PrintStream convert(final String value)
        {
            try
            {
                return new PrintStream(Files.newOutputStream(Path.of(value)));
            }
            catch (final IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}