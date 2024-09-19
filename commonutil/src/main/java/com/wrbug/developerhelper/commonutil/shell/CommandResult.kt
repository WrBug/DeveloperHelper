package com.wrbug.developerhelper.commonutil.shell

data class CommandResult(
    val stdout: List<String>,
    val stderr: List<String>,
    val exitCode: Int,
    val details: Shell.Command.Result.Details?
) {
    val isSuccessful: Boolean
        get() = exitCode == 0

    fun getStdout(): String {
        return toString(stdout)
    }

    /**
     * Get the standard error.
     *
     * @return The standard error as a string.
     */
    fun getStderr(): String {
        return toString(stderr)
    }

    private fun toString(lines: List<String>?): String {
        return lines?.joinToString("\n").orEmpty()
    }
}
