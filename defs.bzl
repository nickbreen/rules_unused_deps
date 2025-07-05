def _unused_deps(ctx):
    outs = []
    for f in ctx.attr.subject[JavaInfo].java_outputs:
        out = ctx.actions.declare_file("%s.textproto" % f.jdeps.basename, sibling = f.jdeps)
        outs.append(out)
        ctx.actions.run_shell(
            mnemonic = "JdepsTextProto",
            inputs = [f.jdeps],
            outputs = [out],
            command = ''' ${1?} --decode=${2?} ${3?} < ${4?} > ${5?} ''',
            arguments = [
                ctx.actions.args().
                    add(ctx.file.protoc).
                    add(ctx.attr.msg).
                    add(ctx.file.proto).
                    add(f.jdeps).
                    add(out)
                ],
            tools = ctx.files.protoc + ctx.files.proto,
        )
    return [
        DefaultInfo(files = depset(outs))
    ]

unused_deps = rule(
    implementation = _unused_deps,
    attrs = {
        "subject": attr.label(
            mandatory = True,
            providers = [JavaInfo],
        ),
        "protoc": attr.label(
            default = "@protobuf//:bin/protoc",
            allow_single_file = True,
            executable = True,
            cfg = "exec",
        ),
        "proto": attr.label(
            default = "@deps.proto//file:deps.proto",
            allow_single_file = [".proto"],
        ),
        "msg": attr.string(
            default = "blaze_deps.Dependencies",
        )
    },
)