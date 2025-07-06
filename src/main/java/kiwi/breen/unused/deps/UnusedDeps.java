package kiwi.breen.unused.deps;

import com.google.devtools.build.lib.view.proto.Deps;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UnusedDeps
{
    public static void main(final String[] args)
    {
        final Deps.Dependencies.Builder builder = Deps.Dependencies.newBuilder();
        final UnusedDeps unusedDeps = new UnusedDeps();
    }

    private static final Predicate<Deps.Dependency> directDependencyFilter =
            dep -> Deps.Dependency.Kind.EXPLICIT.equals(dep.getKind());

    public Collection<String> detect(
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