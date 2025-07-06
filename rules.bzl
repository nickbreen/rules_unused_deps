load(":defs.bzl", "DirectDepsInfo", "UsedDepsInfo", "DecodedUsedDepsInfo")
load(":aspects.bzl", "direct_deps", "used_deps", "decode_used_deps")

def _unused_deps(ctx):
    inputs = []

    inputs += ctx.attr.subject[UsedDepsInfo].used_deps
    inputs += ctx.attr.subject[DecodedUsedDepsInfo].used_deps
    inputs += ctx.attr.subject[DirectDepsInfo].direct_deps

    outputs = []
    if ctx.attr.subject[UsedDepsInfo].used_deps and ctx.attr.subject[DirectDepsInfo].direct_deps:

        outputs.append(ctx.actions.declare_file("%s.unused.deps.txt" % ctx.label.name))

        ctx.actions.run(
            mnemonic = "UnusedDeps",
            executable = ctx.executable.tool,
            inputs = inputs,
            outputs = outputs,
            arguments = [
                ctx.actions.args()
                        .add_all("--used-deps", ctx.attr.subject[UsedDepsInfo].used_deps)
                        .add_all("--direct-deps", ctx.attr.subject[DirectDepsInfo].direct_deps)
                        .add_all("--output", outputs)
            ]
        )

    return [
        DefaultInfo(files = depset(outputs, transitive = [depset(inputs)]))
    ]

unused_deps = rule(
    implementation = _unused_deps,
    attrs = {
        "subject": attr.label(
            mandatory = True,
            providers = [[JavaInfo]],
            aspects = [used_deps, direct_deps, decode_used_deps]
        ),
        "tool": attr.label(executable = True, cfg = "exec", default = "//:unused-deps"),
    },
)