load("@rules_jvm_external//:defs.bzl", _artifact = "artifact")

def artifact(a, *args, **kwargs):
    return _artifact(a, repository_name = "rules_unused_deps_maven", *args, **kwargs)