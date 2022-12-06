fun readResourceLines(path: String): List<String> =
    object {}.javaClass.getResource(path).readText().lines() ?: error("Couldn't find input file at $path")