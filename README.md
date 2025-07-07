Check for unused java dependencies for `java_*` target.

# Usage

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

Generating the Subresource Integrity value (the bit after `sha256-`):
```shell
openssl dgst -sha256 -binary rules_unused_deps-0.0.0.tar.gz | openssl base64 -A
```

# Air-Gapped Environments

Use [--downloader_config](https://bazel.build/reference/command-line-reference#common_options-flag--downloader_config)
to rewrite maven central repository URL's to your air-gapped repository.

```
# downloader.cfg
rewrite repo1.maven.org/maven2/(.*) https://${YOUR_PROXY_HOST}/${YOUR_PROXY_PATH}/$1
```

