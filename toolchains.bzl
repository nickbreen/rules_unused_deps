load(":providers.bzl", "UnusedDepsToolchainInfo")

def _unused_deps_toolchain(ctx):
    return [
        platform_common.ToolchainInfo(
            unused_deps = UnusedDepsToolchainInfo(
                exec = ctx.attr.exec,
            ),
        ),
    ]

unused_deps_toolchain = rule(
    implementation = _unused_deps_toolchain,
    attrs = {
        "exec": attr.label(
            mandatory = True,
            executable = True,
            cfg = "exec",
        )
    }
)