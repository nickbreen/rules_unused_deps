def _unused_deps(ctx):
    text_protos = []
    for java_info in ctx.attr.subject[JavaInfo].java_outputs:
        # jdeps is only available for compiled java_* things
        # e.g. a java_binary with only runtime_deps does not require compilation
        # so does not generate a jdeps output, but also means there is no unused
        # compile-time dependencies to check
        if java_info.jdeps:
            text_proto = ctx.actions.declare_file("%s.textproto" % java_info.jdeps.basename, sibling = java_info.jdeps)
            text_protos.append(text_proto)
            ctx.actions.run_shell(
                mnemonic = "JdepsTextProto",
                inputs = [java_info.jdeps],
                outputs = [text_proto],
                command = ''' ${1?} --decode=${2?} ${3?} < ${4?} > ${5?} ''',
                arguments = [
                    ctx.actions.args().
                        add(ctx.file.protoc).
                        add(ctx.attr.msg).
                        add(ctx.file.proto).
                        add(java_info.jdeps).
                        add(text_proto)
                    ],
                tools = ctx.files.protoc + ctx.files.proto,
            )
    return [
        DefaultInfo(files = depset(text_protos))
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