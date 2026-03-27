UnusedDepsInfo = provider(fields = {
    "used_deps": "jdeps binary proto file(s) of all used deps",
    "direct_deps": "a sequence of direct java library dependencies",
    "unused_deps": "a text file with (by default) buildozer commands to remove unused deps"
})
UnusedDepsToolchainInfo = provider("exec")