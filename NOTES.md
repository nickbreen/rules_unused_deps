
```
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
register_toolchains("@rules_unused_deps//:all")
```
