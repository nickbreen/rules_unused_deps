
## Using MODULE.bazel (Bzlmod - Bazel 7.0+)

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

## Using WORKSPACE (Bazel 6.x)

```
# WORKSPACE
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "rules_unused_deps",
    urls = [
        "${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/archive/refs/tags/${GITHUB_REF_NAME}.tar.gz",
    ],
    strip_prefix = "rules_unused_deps-${ver}",
    sha256 = "${sha256}",
)

load("@rules_unused_deps//:repositories.bzl", "rules_unused_deps_dependencies")

rules_unused_deps_dependencies()

load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")
rules_java_dependencies()
rules_java_toolchains()

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")
rules_proto_dependencies()
rules_proto_toolchains()

# Initialize protobuf
load("@protobuf//bazel:system_python.bzl", "system_python")
system_python(
    name = "system_python",
    minimum_python_version = "3.8",
)

load("@protobuf//bazel:protobuf_deps.bzl", "protobuf_deps")
protobuf_deps()

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")
rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")
rules_jvm_external_setup()

# Register the toolchain
register_toolchains("@rules_unused_deps//:all")
```

Note: The sha256 value for the release archive can be computed with:
```
curl -sSfL ${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/archive/refs/tags/${GITHUB_REF_NAME}.tar.gz | sha256sum
```

Alternatively you can define your own toolchain and use the released JAR of the
tool. **If you do not have a working C++ toolchain available on your system or 
your C++ toolchain is earlier than c++17 you must use this approach** unless 
using [a pre-built protoc binary](https://github.com/protocolbuffers/protobuf/issues/19558) or configuring a custom 
protoc toolchain.

This avoids having to build the toolchain which depends on `@bazel_tools//src/main/protobuf/...`
which suffers from `protoc`'s [recompilation sensitivity](https://github.com/bazelbuild/bazel/issues/7095)
to being cache-invalidated and rebuilt. 

### With MODULE.bazel

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

### With WORKSPACE

```
# WORKSPACE
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_jar")

http_jar(
    name = "unused-deps",
    url = "${bin_url}",
    sha256 = "${bin_sha256}",
)
register_toolchains("//tools:unused-deps-toolchain")
```

### Toolchain Definition (for both MODULE.bazel and WORKSPACE)

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
)

java_binary(
    name = "unused-deps-java",
    main_class = "kiwi.breen.unused.deps.UnusedDeps",
    runtime_deps = ["@unused-deps//jar"],
)
```

Note: The sha256 value for the binary JAR can be computed with:
```
curl -sSfL ${bin_url} | sha256sum
```