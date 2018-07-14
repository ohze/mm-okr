import sbt.{Def, _}
import sbt.Keys._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.docker._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.Docker


object DockerHelper {
  val distroless = settingKey[Boolean]("if dockerBaseImage is gcr.io/distroless/java")

  private def patch[T](s: Seq[T], p: T => Boolean, patch: Seq[T]): Seq[T] = {
    val (s1, s2) = s.span(p)
    s1 ++ patch ++ s2.dropWhile(!p(_))
  }
  private def notCmd(CMD: String)(c: CmdLike) = c match {
    case Cmd(CMD, _ @ _*) => false
    case _ => true
  }

  val settings: Seq[Def.Setting[_]] = inConfig(Docker)(Seq(
    distroless := dockerBaseImage.value startsWith "gcr.io/distroless/java",
    defaultLinuxInstallLocation := "/app",
    mappings := {
      val excludeFiles = List(
        "README.md",
        s".bat"
      )
      def warn(msg: String): Unit = streams.value.log.warn(msg)
      def exclude(p: String): Boolean = {
        if (excludeFiles.exists(p.endsWith)){
          warn(s"docker - excluding $p")
          true
        } else false
      }

      val d = "/" + defaultLinuxInstallLocation.value.split(DockerPlugin.UnixSeparatorChar)(1)
      def layerPath(p: String): String = {
        val inLayer2 = List(
          "/bin/", "/conf/", s"/lib/${organization.value}.${name.value}-"
        ).exists(p.contains)
        if (inLayer2) d + "-2" + p.substring(d.length)
        else p
      }

      mappings.value
        .filterNot { case (_, p) => exclude(p) }
        .map { case (f, p) => f -> layerPath(p) }
    },

    dockerCommands := {
      val baseDir = defaultLinuxInstallLocation.value
      val d = baseDir.split(DockerPlugin.UnixSeparatorChar)(1)
      val copyCmds = if (distroless.value) Seq(
        Cmd("COPY", s"$d /$d"),
        Cmd("COPY", s"$d-2 /$d")
      ) else {
        val user = daemonUser.value
        val group = daemonGroup.value
        Seq(
          Cmd("COPY", s"--chown=$user:$group $d /$d"),
          Cmd("COPY", s"--chown=$user:$group $d-2 /$d")
        )
      }

      val cmds = patch(dockerCommands.value, notCmd("ADD"), copyCmds)
      if (distroless.value) cmds.filter(notCmd("USER"))
      else cmds
    }
  ))
}
