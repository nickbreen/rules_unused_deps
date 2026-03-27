[![CI](https://github.com/nickbreen/rules_unused_deps/actions/workflows/ci.yml/badge.svg)](https://github.com/nickbreen/rules_unused_deps/actions/workflows/ci.yml)

Check for unused java dependencies for `java_*` target.

See [release notes](https://github.com/nickbreen/rules_unused_deps/releases) for installation instructions.

# Installation

## Using MODULE.bazel (Bzlmod)

For Bazel 7.0 and later, add the following to your `MODULE.bazel`:

```python
# MODULE.bazel
bazel_dep(name = "rules_unused_deps", version = "${ver}")
# This is not published to the BCR, so use an archive_override.
archive_override(
    module_name = "rules_unused_deps",
    urls = [
        "https://github.com/nickbreen/rules_unused_deps/archive/refs/tags/v${ver}.tar.gz",
    ],
    strip_prefix = "rules_unused_deps-${ver}",
    integrity = "${sri}",
)
# Register the toolchain
register_toolchains("@rules_unused_deps//:all")
```

## Using WORKSPACE

For Bazel 6.x and projects still using WORKSPACE, add the following to your `WORKSPACE`:

```python
# WORKSPACE
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "rules_unused_deps",
    urls = [
        "https://github.com/nickbreen/rules_unused_deps/archive/refs/tags/v${ver}.tar.gz",
    ],
    strip_prefix = "rules_unused_deps-${ver}",
    sha256 = "${sha256}",
)

load("@rules_unused_deps//:repositories.bzl", "rules_unused_deps_dependencies")

rules_unused_deps_dependencies()

# Load and setup rules_java
load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")
rules_java_dependencies()
rules_java_toolchains()

# Load and setup rules_proto
load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")
rules_proto_dependencies()
rules_proto_toolchains()

# Load and setup rules_jvm_external
load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")
rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")
rules_jvm_external_setup()

# Register the toolchain
register_toolchains("@rules_unused_deps//:all")
```

# Usage

Use the `unused_deps_test` rule on any `java_*` target.
```
# BUILD.bazel
load("@rules_unused_deps//:rules.bzl", "unused_deps_test")

java_library(
    name = "example",
    # elided
)

unused_deps_test(
    name = "example_unused_deps",
    subject = ":example", 
)
```

One can also ignore failures for some unused dependencies, which
is useful for applying to existing code bases or strange edge cases.

```
unused_deps_test(
    name = "example_unused_deps",
    subject = ":example",
    ignore = [
        "//some:dep"
    ],
)
```

By default, the output format of the test rule are `buildozer` commands.
Change this with the `format` attribute. It is a `printf` format string.
See [man 1 printf](https://www.man7.org/linux/man-pages/man1/printf.1.html).

```
unused_deps_test(
    name = "example_unused_deps",
    subject = ":example",
    format = "%s\t%s\n",  # output tab-separated-values
)
```

# `rules_java` and `rules_jvm_external` version conflicts

Use [single_version_override](https://bazel.build/rules/lib/globals/module#single_version_override)
to resolve conflicts between the versions of `rules_java` and `rules_jvm_external`
that this module uses in the module graph.

```
# MODULE.bazel
single_version_override(module_name = "rules_java", version="8.12.0")
single_version_override(module_name = "rules_jvm_external", version="6.6")
```

# Air-Gapped Environments

Use [--downloader_config](https://bazel.build/reference/command-line-reference#common_options-flag--downloader_config)
to rewrite maven central repository URL's to your air-gapped repository.

```
# downloader.cfg
rewrite repo1.maven.org/maven2/(.*) https://${YOUR_PROXY_HOST}/${YOUR_PROXY_PATH}/$1
```

