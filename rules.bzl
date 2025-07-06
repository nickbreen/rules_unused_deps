load(":defs.bzl", "UnusedDepsInfo")
load(":aspects.bzl", aspect_unused_deps = "unused_deps")

def _unused_deps(ctx):
    outputs = []

    outputs += ctx.attr.subject[UnusedDepsInfo].used_deps
    outputs += ctx.attr.subject[UnusedDepsInfo].used_deps_text_proto
    outputs += ctx.attr.subject[UnusedDepsInfo].direct_deps_text

    return [
        DefaultInfo(files = depset(outputs))
    ]

unused_deps = rule(
    implementation = _unused_deps,
    attrs = {
        "subject": attr.label(
            mandatory = True,
            providers = [[JavaInfo]],
            aspects = [aspect_unused_deps]
        ),
    },
)