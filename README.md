Check for unused java dependencies for `java_*` target.

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

```
# MODULE.bazel
bazel_dep(name = "rules_unused_deps", version = "0.0.0")
# This is not published to the BCR, so use an archive_override.
archive_override(
    module_name = "rules_unused_deps",
    urls = [
        "https://github.com/nickbreen/rules_unused_deps/archive/refs/tags/v0.0.0.tar.gz",
    ],
    strip_prefix = "rules_unused_deps-0.0.0",
    integrity = "sha256-/li7/8YRUuOZzwc87cGl4NDsFSKD6cHk0My8RvtYJ6Y=",
)
```