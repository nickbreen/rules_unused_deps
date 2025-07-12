UsedDepsInfo = provider(fields = {
    "used_deps": "a list of jdep binary proto, should be exactly one",
})
DirectDepsInfo = provider(fields = {
    "direct_deps": "a text file with a direct dependency jar and it's generating label per line",
})
UnusedDepsInfo = provider(fields = {
    "unused_deps": "a text file with (by default) buildozer commands to remove unused deps"
})