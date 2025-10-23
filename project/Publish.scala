/*
 * Copyright (C) 2016-2020 Lightbend Inc. <https://www.lightbend.com>
 */

import java.util.concurrent.atomic.AtomicBoolean

import scala.language.postfixOps

import sbt.{ Def, _ }
import Keys._
import com.geirsson.CiReleasePlugin
import com.jsuereth.sbtpgp.PgpKeys.publishSigned
import xerial.sbt.Sonatype.autoImport.sonatypeProfileName

/**
 * For projects that are not published.
 */
object NoPublish extends AutoPlugin {
  override def requires = plugins.JvmPlugin

  override def projectSettings =
    Seq(publish / skip := true, publishArtifact := false, publish := {}, publishLocal := {})
}

object Publish extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = AllRequirements

  lazy val beforePublishTask = taskKey[Unit]("setup before publish")

  lazy val beforePublishDone = new AtomicBoolean(false)

  def beforePublish(snapshot: Boolean) = {
    if (beforePublishDone.compareAndSet(false, true)) {
      CiReleasePlugin.setupGpg()
      if (!snapshot)
        cloudsmithCredentials(validate = true)
    }
  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    sonatypeProfileName := "io.reflek",
    beforePublishTask := beforePublish(isSnapshot.value),
    publishSigned := publishSigned.dependsOn(beforePublishTask).value,
    publishTo :=
      Some("Reflek Artifact Repository".at("artifactregistry://europe-west1-maven.pkg.dev/prj-rio-operation-259855/reflek-io-maven-internal")),
    credentials ++= cloudsmithCredentials(validate = false))

  def cloudsmithCredentials(validate: Boolean): Seq[Credentials] = {
    (sys.env.get("PUBLISH_USER"), sys.env.get("PUBLISH_PASSWORD")) match {
      case (Some(user), Some(password)) =>
        Seq(Credentials("Cloudsmith API", "maven.cloudsmith.io", user, password))
      case _ =>
        if (validate)
          throw new Exception("Publishing credentials expected in `PUBLISH_USER` and `PUBLISH_PASSWORD`.")
        else
          Nil
    }
  }
}
