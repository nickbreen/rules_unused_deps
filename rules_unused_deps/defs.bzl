def _unused_deps(ctx):
    java_info = ctx.attr.subject[JavaInfo]
    text_protos = []
    for java_output in java_info.java_outputs:
        # jdeps is only available for compiled java_* things
        # e.g. a java_binary with only runtime_deps does not require compilation
        # so does not generate a jdeps output, but also means there is no unused
        # compile-time dependencies to check
        if java_output.jdeps:
            # generate a textproto of the jdeps
            text_proto = ctx.actions.declare_file(
                "%s.textproto" % java_output.jdeps.basename,
                sibling = java_output.jdeps)
            text_protos.append(text_proto)
            ctx.actions.run_shell(
                mnemonic = "JdepsTextProto",
                inputs = [java_output.jdeps],
                outputs = [text_proto],
                command = ''' ${1?} --decode=${2?} ${3?} < ${4?} > ${5?} ''',
                arguments = [
                    ctx.actions.args().
                        add(ctx.file._protoc).
                        add(ctx.attr.msg).
                        add(ctx.file._proto).
                        add(java_output.jdeps).
                        add(text_proto)
                    ],
                tools = ctx.files._protoc + ctx.files._proto,
            )
    # stash the declared deps too
    # technically this is the transitive compile-time deps
    compliation_classpath = java_info.compilation_info.compilation_classpath
    declared_deps = ctx.actions.declare_file(
        "%s.txt" % ctx.attr.subject.label.name)
    ctx.actions.write(
        declared_deps,
        "\n".join([f.path for f in compliation_classpath.to_list()]))

    return [
        DefaultInfo(files = depset(text_protos + [declared_deps]))
    ]

unused_deps = rule(
    implementation = _unused_deps,
    attrs = {
        "subject": attr.label(
            mandatory = True,
            providers = [JavaInfo],
        ),
        "_protoc": attr.label(
            default = "@bazel_tools//tools/proto:protoc",
            allow_single_file = True,
            executable = True,
            cfg = "exec",
        ),
        "_proto": attr.label(
            default = "@bazel_tools//src/main/protobuf:deps.proto",
            allow_single_file = True,
        ),
        "msg": attr.string(
            default = "blaze_deps.Dependencies",
        )
    },
)