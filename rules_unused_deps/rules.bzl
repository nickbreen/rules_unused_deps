load(":defs.bzl", "UnusedDepsInfo")
load(":aspects.bzl", aspect_unused_deps = "unused_deps")

def _unused_deps(ctx):
    outputs = []

    outputs += ctx.attr.subject[UnusedDepsInfo].used_deps
    outputs += ctx.attr.subject[UnusedDepsInfo].used_deps_text_proto

    direct_deps = ctx.attr.subject[UnusedDepsInfo].direct_deps
    if direct_deps:
        declared_deps = ctx.actions.declare_file(
            "%s.txt" % ctx.attr.subject.label.name)
        outputs.append(declared_deps)
        ctx.actions.write(
            declared_deps,
            "\n".join([f.path for d in direct_deps for f in d.files.to_list()]) + "\n")

    return [
        DefaultInfo(files = depset(outputs))
    ]

unused_deps = rule(
    implementation = _unused_deps,
    attrs = {
        "subject": attr.label(
            mandatory = True,
            providers = [[JavaInfo]],
            aspects = [aspect_unused_deps]
        ),
    },
)