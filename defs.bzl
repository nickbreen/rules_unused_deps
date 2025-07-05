def _unused_deps(ctx):
    jdeps_proto = [
        f.jdeps
        for f in ctx.attr.subject[JavaInfo].java_outputs
    ]
    jdeps_text_proto = [
        ctx.actions.declare_file("%s.textproto" % f.basename, sibling = f)
        for f in jdeps_proto
    ]
    # should we loop the ctx.actions.run_shell instead?
    ctx.actions.run_shell(
        mnemonic = "JdepsTextProto",
        inputs = jdeps_proto,
        outputs = jdeps_text_proto,
        command = '''
        set -xeuo pipefail
        for jdep in "${@:3}"
        do
            ${1?} --decode=blaze_deps.Dependencies ${2?} < $jdep > ${jdep}.textproto
        done
        ''',
        arguments = [ctx.actions.args().add(ctx.file.protoc).add(ctx.file.proto).add_all(jdeps_proto)],
        tools = ctx.files.protoc + ctx.files.proto,
    )
    return [
        DefaultInfo(files = depset(jdeps_text_proto))
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
    },
)