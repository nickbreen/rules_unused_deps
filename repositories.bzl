"""Repository rules for rules_unused_deps."""

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:utils.bzl", "maybe")

def rules_unused_deps_dependencies():
    """Declares external repositories that rules_unused_deps depends on.

    This function should be loaded and called from the user's WORKSPACE file.
    """

    # rules_java - required for Java compilation
    maybe(
        http_archive,
        name = "rules_java",
        urls = [
            "https://github.com/bazelbuild/rules_java/releases/download/7.12.3/rules_java-7.12.3.tar.gz",
        ],
        sha256 = "5e5ea3f8fa5c4eb06953f80c2c93c88f0afc4c29f5a0b0660fd57e6f91b42f79",
    )

    # rules_proto - required for proto_library
    maybe(
        http_archive,
        name = "rules_proto",
        urls = [
            "https://github.com/bazelbuild/rules_proto/releases/download/6.0.2/rules_proto-6.0.2.tar.gz",
        ],
        sha256 = "303e86e722a520f6f326a50b41cfc16b98fe6d1955ce46642a5b7a67c11c0f5d",
    )

    # protobuf - required for Protocol Buffers support  
    # Using version 27.x for compatibility across Bazel versions
    maybe(
        http_archive,
        name = "protobuf",
        urls = [
            "https://github.com/protocolbuffers/protobuf/releases/download/v27.5/protobuf-27.5.tar.gz",
        ],
        sha256 = "572b1f2c3e94bfdd20c9e26e47b5a4bd6b7d00bb68bde4ef36d0c09e7f7c5e49",
        strip_prefix = "protobuf-27.5",
    )

    # Also set up com_google_protobuf as an alias for legacy compatibility
    maybe(
        http_archive,
        name = "com_google_protobuf",
        urls = [
            "https://github.com/protocolbuffers/protobuf/releases/download/v27.5/protobuf-27.5.tar.gz",
        ],
        sha256 = "572b1f2c3e94bfdd20c9e26e47b5a4bd6b7d00bb68bde4ef36d0c09e7f7c5e49",
        strip_prefix = "protobuf-27.5",
    )

    # rules_jvm_external - required for Maven dependency management
    maybe(
        http_archive,
        name = "rules_jvm_external",
        urls = [
            "https://github.com/bazelbuild/rules_jvm_external/releases/download/6.6/rules_jvm_external-6.6.tar.gz",
        ],
        sha256 = "c44568854d8bb92fe1240c37269455a8d6e5757e30fe5d7dcbe93c0f8ae92e41",
        strip_prefix = "rules_jvm_external-6.6",
    )
