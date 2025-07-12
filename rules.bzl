load(":providers.bzl", "UnusedDepsInfo", "UsedDepsInfo", "DirectDepsInfo")
load(":aspects.bzl", aspect_unused_deps = "unused_deps")

def _unused_deps(ctx):
    return [
        DefaultInfo(
            files = depset(
                ctx.attr.subject[DirectDepsInfo].direct_deps +
                ctx.attr.subject[UsedDepsInfo].used_deps +
                ctx.attr.subject[UnusedDepsInfo].unused_deps
            )
        )
    ]

unused_deps = rule(
    doc = '''
    Exposes the generated files for any other rule to consume.
    ''',
    implementation = _unused_deps,
    attrs = {
        "subject": attr.label(
            mandatory = True,
            providers = [[JavaInfo]],
            aspects = [aspect_unused_deps]
        ),
    },
)

def _unused_deps_test(ctx):
    executable = ctx.actions.declare_file(ctx.label.name)
    ctx.actions.expand_template(
        template = ctx.file._script,
        output = executable,
        is_executable = True)
    ignores = ctx.actions.declare_file("%s.ignores.txt" % ctx.label.name)
    ctx.actions.write(
        ignores,
        ctx.actions.args().add_all([
            str(i.label)  # expand the label to it's canonical bzlmod label
            for i in ctx.attr.ignore
        ])
    )
    return [
        DefaultInfo(
            executable = executable,
            runfiles = ctx.runfiles(
                files = ctx.attr.subject[UnusedDepsInfo].unused_deps + [ignores])
        ),
        RunEnvironmentInfo(
            environment = dict(
                FORMAT = ctx.attr.format,
                SUBJECT = "%s" % ctx.attr.subject.label,
                UNUSED_DEPS = " ".join([
                    f.short_path
                    for f in ctx.attr.subject[UnusedDepsInfo].unused_deps
                ]),
                IGNORE = ignores.short_path,
            )
        ),
        OutputGroupInfo(
            _validation = depset(ctx.attr.subject[UnusedDepsInfo].unused_deps)
        )
    ]

unused_deps_test = rule(
    doc = '''
    Tests that there are zero unused deps.
    ''',
    implementation = _unused_deps_test,
    attrs = {
        "subject": attr.label(
            mandatory = True,
            providers = [[JavaInfo]],
            aspects = [aspect_unused_deps]
        ),
        "ignore": attr.label_list(
            doc = '''
            unused dependencies to ignore, the test will not fail for these.
            ''',
            default = [],
            providers = [[JavaInfo]]
        ),
        "format": attr.string(
            doc = '''
            printf format string for failure reporting.
            ''',
            default = "buildozer 'remove deps %s' %s\n"
        ),
        "_script": attr.label(
            default = ":test.sh",
            allow_single_file = True,
        )
    },
    test = True
)
