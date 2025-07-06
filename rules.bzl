load(":defs.bzl", "UnusedDepsInfo", "UsedDepsInfo", "DirectDepsInfo", "DecodedUsedDepsInfo")
load(":aspects.bzl", aspect_unused_deps = "unused_deps", "decode_used_deps")

def _unused_deps(ctx):
    outputs = [ctx.actions.declare_file("%s.txt" % ctx.label.name)]
    ctx.actions.run_shell(
        mnemonic = "UnusedDeps",
        inputs = ctx.attr.subject[UnusedDepsInfo].unused_deps,
        outputs = outputs,
        command = '''wc -l "${@:2}" > "${1}"''',
        arguments = [
            ctx.actions.args()
                .add_all(outputs)
                .add_all(ctx.attr.subject[UnusedDepsInfo].unused_deps)
        ]
    )

    return [
        DefaultInfo(
            files = depset(
                outputs,
                transitive = [
                    depset(
                        ctx.attr.subject[DirectDepsInfo].direct_deps +
                        ctx.attr.subject[UsedDepsInfo].used_deps +
                        ctx.attr.subject[DecodedUsedDepsInfo].used_deps +
                        ctx.attr.subject[UnusedDepsInfo].unused_deps
                    )
                ]
            )
        )
    ]

unused_deps = rule(
    implementation = _unused_deps,
    attrs = {
        "subject": attr.label(
            mandatory = True,
            providers = [[JavaInfo]],
            aspects = [aspect_unused_deps, decode_used_deps]
        ),
    },
)