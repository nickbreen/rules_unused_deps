UsedDepsInfo = provider(fields = {
    "used_deps": "a list of jdep binary proto, should be exactly one",
})
DecodedUsedDepsInfo = provider(fields = {
    "used_deps": "a list of jdep text proto, should be exactly one",
})
DirectDepsInfo = provider(fields = {
    "direct_deps": "a text file with a direct dependency jar and it's generating label per line",
})