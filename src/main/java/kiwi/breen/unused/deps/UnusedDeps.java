package kiwi.breen.unused.deps;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.devtools.build.lib.view.proto.Deps;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UnusedDeps implements Callable<Integer>
{
    @Parameter(names = {"-u", "--used", "--used-deps"})
    private Path usedDeps;

    @Parameter(names = {"-d", "--direct", "--direct-deps"})
    private Path directDeps;

    @Parameter(names = {"-o", "--out", "--output"}, converter = PrintStreamConverter.class)
    private PrintStream output = System.out;

    @Parameter(names = {"-x", "-e", "--exit"})
    private boolean exitOnFailures;

    public static void main(final String[] args) throws Exception
    {
        final UnusedDeps unusedDeps = new UnusedDeps();
        JCommander.newBuilder()
                .addObject(unusedDeps)
                .build()
                .parse(args);
        final int err = unusedDeps.call();
        System.exit(err);
    }

    private static final Predicate<Deps.Dependency> directDependencyFilter =
            dep -> Deps.Dependency.Kind.EXPLICIT.equals(dep.getKind());

    @Override
    public Integer call() throws Exception
    {
        System.err.printf("Direct Deps %s (%s) %n", directDeps, directDeps.toAbsolutePath());
        System.err.printf("Used Deps %s (%s) %n", usedDeps, usedDeps.toAbsolutePath());
        final Map<String, String> directDeps =
                Loaders.loadDeclaredDeps(this.directDeps);
        final Deps.Dependencies usedDeps =
                Loaders.loadUsedDeps(this.usedDeps);
        final Collection<String> unused = detect(usedDeps, directDeps);
        unused.forEach(output::println);
        return exitOnFailures ? unused.size() : 0;
    }

    Collection<String> detect(
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