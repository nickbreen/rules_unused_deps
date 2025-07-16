load("@rules_java//java:defs.bzl", "JavaInfo")
load(":providers.bzl", "UsedDepsInfo", "DirectDepsInfo", "UnusedDepsInfo", "UnusedDepsToolchainInfo")

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
    required_providers = [[JavaInfo]],
    provides = [UsedDepsInfo],
)

def _unused_deps(target, ctx):
    toolchain = ctx.toolchains["//:toolchain_type"].unused_deps
    outputs = []
    if target[UsedDepsInfo].used_deps and target[DirectDepsInfo].direct_deps:
        outputs.append(ctx.actions.declare_file("%s.unused.deps.txt" % ctx.label.name))
        ctx.actions.run(
            mnemonic = "UnusedDeps",
            executable = toolchain.exec.files_to_run.executable,
            inputs = target[UsedDepsInfo].used_deps + target[DirectDepsInfo].direct_deps,
            outputs = outputs,
            toolchain = "//:toolchain_type",
            tools = [toolchain.exec.files_to_run],
            arguments = [
                ctx.actions.args()
                        .add_all("--used-deps", target[UsedDepsInfo].used_deps)
                        .add_all("--direct-deps", target[DirectDepsInfo].direct_deps)
                        .add_all("--output", outputs)
            ]
        )
    return [
        UnusedDepsInfo(unused_deps = outputs)
    ]

unused_deps = aspect(
    implementation = _unused_deps,
    requires = [used_deps, direct_deps],
    required_aspect_providers = [UsedDepsInfo, DirectDepsInfo],
    required_providers = [[JavaInfo]],
    provides = [UnusedDepsInfo],
    toolchains = ["//:toolchain_type"],
)