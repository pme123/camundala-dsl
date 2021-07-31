import ammonite.ops._

/**
 * <pre>
 * Gets and updates the Plugins of https://github.com/datakurre/camunda-cockpit-plugins/.
 *
 * This uses Ammonite - see http://ammonite.io
 *
 * amm ./update-plugin.sc
 *
 */

private implicit val workDir: Path = {
  val wd = pwd
  println(s"Working Directory: $wd")
  wd
}

@arg(doc = "> Downloads and updates the plugins of f https://github.com/datakurre/camunda-cockpit-plugins/")
@main
def update(@arg(doc =  "Name of the example - e.g. twitter")
           example: String): Unit = {
  // recreate pluginsFrom
  val pluginsFrom = workDir / ".datakurre"
  rm! pluginsFrom
  mkdir! pluginsFrom
  %.git(
    "clone",
    "https://github.com/datakurre/camunda-cockpit-plugins.git",
    workDir / ".datakurre"
  )
  // recreate pluginsTo
  val pluginsTo = workDir / example / "src" / "main" / "resources" / "META-INF" / "resources" / "webjars" / "camunda" / "app" / "cockpit" / "scripts"
  rm! pluginsTo
  mkdir! pluginsTo

  %.rsync( "-av",
    "--exclude", "@types",
    "--exclude", "src",
    "--exclude", "*.md",
    "--exclude", "*.json",
    "--exclude", ".*",
    pluginsFrom.toString + "/",
    pluginsTo
  )
}
