load(":defs.bzl", "UnusedDepsInfo")

def _unused_deps(target, ctx):
    used_deps = [
        java_output.jdeps
        for java_output in target[JavaInfo].java_outputs
        if java_output.jdeps
    ]
    used_deps_text_proto = []
    for jdeps in used_deps:
        # generate a textproto of the jdeps
        text_proto = ctx.actions.declare_file(
            "%s.textproto" % jdeps.basename,
            sibling = jdeps)
        used_deps_text_proto.append(text_proto)
        ctx.actions.run_shell(
            mnemonic = "JdepsTextProto",
            inputs = [jdeps],
            outputs = [text_proto],
            command = ''' ${1?} --decode=${2?} ${3?} < ${4?} > ${5?} ''',
            arguments = [
                ctx.actions.args().
                    add(ctx.file._protoc).
                    add(ctx.attr.msg).
                    add(ctx.file._proto).
                    add(jdeps).
                    add(text_proto)
                ],
            tools = ctx.files._protoc + ctx.files._proto,
        )
    direct_deps = [jo.compile_jar for d in ctx.rule.attr.deps for jo in d[JavaInfo].java_outputs]
    direct_deps_text = []
    if direct_deps:
        out = ctx.actions.declare_file(
            "%s.txt" % target.label.name)
        direct_deps_text.append(out)
        ctx.actions.write(
            out,
            "\n".join([
                "%s\t%s" % (
                    d.owner,
                    d.path
                )
                for d in direct_deps
            ]) + "\n")
    return [
        UnusedDepsInfo(
            direct_deps = direct_deps,
            direct_deps_text = direct_deps_text,
            used_deps = used_deps,
            used_deps_text_proto = used_deps_text_proto,
        )
    ]

unused_deps = aspect(
    implementation = _unused_deps,
    attr_aspects = ['deps'],
    required_providers = [[JavaInfo]],
    provides = [UnusedDepsInfo],
    attrs = {
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
            values = ["blaze_deps.Dependencies"]
        )
    },
)