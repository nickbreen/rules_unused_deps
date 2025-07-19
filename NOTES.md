
```
# MODULE.bazel
bazel_dep(name = "rules_unused_deps", version = "${ver}")
# This is not published to the BCR, so use an archive_override.
archive_override(
    module_name = "rules_unused_deps",
    urls = [
        "${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/archive/refs/tags/${GITHUB_REF_NAME}.tar.gz",
    ],
    strip_prefix = "rules_unused_deps-${ver}",
    integrity = "${sri}",
)
# Register the toolchain, it will build automatically. 
register_toolchains("@rules_unused_deps//:all")
```

Alternatively you can define your own toolchain and use the released JAR of the tool.

```
# MODULE.bazel
http_jar = use_repo_rule("@bazel_tools//tools/build_defs/repo:http.bzl", "http_jar")
http_jar(
    name = "unused-deps",
    url = "${bin_url}", 
    integrity = "${bin_sri}",
)
register_toolchains("//tools:unused-deps-toolchain")
```

The location of `tools/BUILD.bazel` is entirely arbitrary in this example:

```
# tools/BUILD.bazel
load("@rules_unused_deps//:toolchains.bzl", "unused_deps_toolchain")
load("@rules_java//java:defs.bzl", "java_binary")

toolchain(
    name = "unused-deps-toolchain",
    toolchain_type = "@rules_unused_deps//:toolchain_type",
    toolchain = ":unused-deps-java-toolchain",
)

unused_deps_toolchain(
    name = "unused-deps-java-toolchain",
    exec = ":unused-deps-java",
    visibility = ["//visibility:public"],
)

java_binary(
    name = "unused-deps-java",
    main_class = "kiwi.breen.unused.deps.UnusedDeps",
    runtime_deps = ["@unused-deps//jar"],
)

```