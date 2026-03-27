load("@rules_java//java:defs.bzl", "JavaInfo")
load(":providers.bzl", "UnusedDepsInfo", "UnusedDepsToolchainInfo")

def _format_direct_dep(f):
    return "%s\t%s" % (f.owner, f.path)

def _unused_deps(target, ctx):
    toolchain = ctx.toolchains["//:toolchain_type"].unused_deps
    outputs = []
    used_deps = [jo.jdeps for jo in target[JavaInfo].java_outputs if jo.jdeps]
    direct_deps = [
        # If there isn't a compile_jar (an ijar) just use the actual jar
        jo.compile_jar if jo.compile_jar else jo.class_jar
        for d in ctx.rule.attr.deps
        for jo in d[JavaInfo].java_outputs
    ]
    if used_deps and direct_deps:
        outputs.append(ctx.actions.declare_file("%s.unused.deps.txt" % ctx.label.name))
        ctx.actions.run(
            mnemonic = "UnusedDeps",
            executable = toolchain.exec.files_to_run.executable,
            inputs = used_deps,
            outputs = outputs,
            toolchain = "//:toolchain_type",
            tools = [toolchain.exec.files_to_run],
            arguments = [
                ctx.actions.args()
                        .add_all("--used-deps", used_deps)
                        .add_all("--output", outputs),
                "--direct-deps", # abuse args param files to write this to a file below
                ctx.actions.args()
                        .use_param_file("%s", use_always = True)
                        .set_param_file_format("multiline")
                        .add_all(direct_deps, map_each = _format_direct_dep)
            ]
        )
    return [
        UnusedDepsInfo(
            used_deps = used_deps,
            direct_deps = direct_deps,
            unused_deps = outputs,
        )
    ]

unused_deps = aspect(
    implementation = _unused_deps,
    required_providers = [[JavaInfo]],
    provides = [UnusedDepsInfo],
    toolchains = ["//:toolchain_type"],
)