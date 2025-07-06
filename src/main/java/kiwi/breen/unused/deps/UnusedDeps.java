package kiwi.breen.unused.deps;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.devtools.build.lib.view.proto.Deps;

import java.io.IOException;
import java.io.InputStream;
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
    @Parameter(names = {"-u"})
    private Path usedDeps;

    @Parameter(names = {"-d"})
    private Path directDeps;

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
        final Map<String, String> directDeps =
                Loaders.loadDeclaredDeps(this.directDeps.toString());
        final Deps.Dependencies usedDeps =
                Loaders.loadUsedDeps(this.usedDeps.toString());
        final Collection<String> unused = detect(usedDeps, directDeps);
        unused.forEach(System.out::println);
        return unused.size();
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
}