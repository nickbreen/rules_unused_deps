load(":defs.bzl", "UnusedDepsInfo", "UsedDepsInfo", "DirectDepsInfo")
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

test_script = '''#!/bin/bash -eu
function unused_deps_test()
{
  local -i count=0
  while read -r inc _
  do
      count+=$inc
  done < <(wc -l "$@")
  if [ $count -gt 0 ]
  then
      cat "$@" >&2
      exit 65
  fi
}
unused_deps_test %s
'''

def _unused_deps_test(ctx):
    executable = ctx.actions.declare_file(ctx.label.name)
    ctx.actions.write(
        executable,
        test_script % " ".join([f.short_path for f in ctx.attr.subject[UnusedDepsInfo].unused_deps]),
        is_executable = True)
    return [
            DefaultInfo(
                executable = executable,
                runfiles = ctx.runfiles(files = ctx.attr.subject[UnusedDepsInfo].unused_deps)
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
    },
    test = True
)
