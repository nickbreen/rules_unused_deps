load(":defs.bzl", "UsedDepsInfo", "DirectDepsInfo", "DecodedUsedDepsInfo", "UnusedDepsInfo")

def _direct_deps(target, ctx):
    direct_deps = [
        # If there isn't a compile_jar (an ijar) just use the actual jar
        jo.compile_jar if jo.compile_jar else jo.class_jar
        for d in ctx.rule.attr.deps
        for jo in d[JavaInfo].java_outputs
    ]
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
        DirectDepsInfo(
            direct_deps = direct_deps_text,
        )
    ]

direct_deps = aspect(
    implementation = _direct_deps,
    attr_aspects = ['deps'],
    required_providers = [[JavaInfo]],
    provides = [DirectDepsInfo],
)

def _used_deps(target, ctx):
    used_deps = [
        java_output.jdeps
        for java_output in target[JavaInfo].java_outputs
        if java_output.jdeps
    ]
    return [
        UsedDepsInfo(
            used_deps = used_deps,
        )
    ]

used_deps = aspect(
    implementation = _used_deps,
    attr_aspects = ['deps'],
    required_providers = [[JavaInfo]],
    provides = [UsedDepsInfo],
)

def _decode_used_deps(target, ctx):
    used_deps_text_proto = []
    for jdeps in target[UsedDepsInfo].used_deps:
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
    return [
        DecodedUsedDepsInfo(
            used_deps = used_deps_text_proto,
        )
    ]

decode_used_deps = aspect(
    implementation = _decode_used_deps,
    attr_aspects = ['deps'],
    requires = [used_deps],
    required_aspect_providers = [UsedDepsInfo],
    required_providers = [[JavaInfo]],
    provides = [DecodedUsedDepsInfo],
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

def _unused_deps(target, ctx):
    outputs = []
    print(
        target.label,
        target[UsedDepsInfo],
        target[DirectDepsInfo],
        target[UsedDepsInfo].used_deps and target[DirectDepsInfo].direct_deps)
    if target[UsedDepsInfo].used_deps and target[DirectDepsInfo].direct_deps:
        outputs.append(ctx.actions.declare_file("%s.unused.deps.txt" % ctx.label.name))
        ctx.actions.run(
            mnemonic = "UnusedDeps",
            executable = ctx.executable._tool,
            inputs = target[UsedDepsInfo].used_deps + target[DirectDepsInfo].direct_deps,
            outputs = outputs,
            arguments = [
                ctx.actions.args()
                        .add_all("--used-deps", target[UsedDepsInfo].used_deps)
                        .add_all("--direct-deps", target[DirectDepsInfo].direct_deps)
                        .add_all("--output", outputs)
            ]
        )
    else:
        print("nothing to do")
    return [
        UnusedDepsInfo(unused_deps = outputs)
    ]

unused_deps = aspect(
    implementation = _unused_deps,
    attr_aspects = ['deps'],
    requires = [used_deps, direct_deps],
    required_aspect_providers = [UsedDepsInfo, DirectDepsInfo],
    required_providers = [[JavaInfo]],
    provides = [UnusedDepsInfo],
    attrs = {
        "_tool": attr.label(
            default = "//:unused-deps",
            executable = True,
            cfg = "exec",
        ),
    },
)