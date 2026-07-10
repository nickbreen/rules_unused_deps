[![CI](https://github.com/nickbreen/rules_unused_deps/actions/workflows/ci.yml/badge.svg)](https://github.com/nickbreen/rules_unused_deps/actions/workflows/ci.yml)

Check for unused java dependencies for `java_*` target.

See [release notes](https://github.com/nickbreen/rules_unused_deps/releases) for installation instructions.

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

# Analysis of Results Outside of Bazel with Remote Execution

If you are using an external (to bazel) tool to further process output you may 
need to instruct bazel to download the outputs of the test rules. Specifically, 
when using [--remote_download_minimal](https://bazel.build/reference/command-line-reference#common_options-flag--remote_download_minimal) 
remotely built outputs are not downloaded. Use the [--remote_download_regex](https://bazel.build/reference/command-line-reference#common_options-flag--remote_download_regex) 
flag to explicitly download these outputs.

```
--remote_download_regex='.*\.unused\.deps\.txt$'
```


```shell
bazel clean
bazel test ... --remote_download_minimal
find bazel-out/ -name '*.unused.deps.txt' -print -exec sh -uec 'for link; do [ -L $link ] && [ ! -e $link ] && echo Broken! $link; done' - \{\} \+
```
Note that the all the symlinks are 'broken'.

```shell
bazel clean
bazel test ... --remote_download_minimal
find bazel-out/ -name '*.unused.deps.txt' -exec readlink -ev \{\} \+
```

```shell
bazel clean
bazel test ... --remote_download_minimal --remote_download_regex='.*\.unused\.deps\.txt$'
find bazel-out/ -name '*.unused.deps.txt' -exec readlink -ev \{\} \+
```

