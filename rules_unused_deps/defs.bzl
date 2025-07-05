UnusedDepsInfo = provider(fields = {
    "direct_deps": "a depset of direct dependencies of the target",
    "direct_deps_text": "a text file with a direct dependency jar per line",
    "used_deps": "a list of jdep binary proto, should be exactly one",
    "used_deps_text_proto": "a list of jdep text proto, should be exactly one",
})