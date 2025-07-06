load(":defs.bzl", "DirectDepsInfo", "UsedDepsInfo", "DecodedUsedDepsInfo")
load(":aspects.bzl", "direct_deps", "used_deps", "decode_used_deps")

def _unused_deps(ctx):
    outputs = []

    outputs += ctx.attr.subject[UsedDepsInfo].used_deps
    outputs += ctx.attr.subject[DecodedUsedDepsInfo].used_deps
    outputs += ctx.attr.subject[DirectDepsInfo].direct_deps

    return [
        DefaultInfo(files = depset(outputs))
    ]

unused_deps = rule(
    implementation = _unused_deps,
    attrs = {
        "subject": attr.label(
            mandatory = True,
            providers = [[JavaInfo]],
            aspects = [used_deps, direct_deps, decode_used_deps]
        ),
    },
)