Check for unused java dependencies for `java_*` target.

```BUILD.bazel
load("//:rules.bzl", "unused_deps")

java_library(
    name = "example",
    # elided
)

unused_deps(
    name = "example_unused_deps",
    subject = ":example", 
)
```