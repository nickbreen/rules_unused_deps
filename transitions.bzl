def _allow_implicit_exports(settings, attr):
    flag = "//command_line_option:incompatible_no_implicit_file_export"
    return {
        flag: False,
    }

allow_implicit_exports = transition(
    implementation = _allow_implicit_exports,
    inputs = [],
    outputs = ["//command_line_option:incompatible_no_implicit_file_export"],
)
