package kiwi.breen.unused.deps;

import com.google.devtools.build.lib.view.proto.Deps;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class UnusedDeps
{
    public static void main(final String[] args)
    {
        final Deps.Dependencies.Builder builder = Deps.Dependencies.newBuilder();
        final UnusedDeps unusedDeps = new UnusedDeps();
    }

    public Collection<String> detect(
            final Deps.Dependencies usedDeps,
            final Set<String> directDeps)
    {
        final List<String> usedDirectDeps = usedDeps.getDependencyList().stream()
                .filter(dep -> Deps.Dependency.Kind.EXPLICIT.equals(dep.getKind()))
                .map(Deps.Dependency::getPath)
                .toList();

        usedDirectDeps.forEach(directDeps::remove);

        return directDeps;
    }
}