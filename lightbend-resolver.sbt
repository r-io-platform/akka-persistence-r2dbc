import scala.io.Source
import scala.util.Try

lazy val lightbendCommercialCredentialsToken = settingKey[String]("Reads the Lightbend Commercial credentials")
Global / lightbendCommercialCredentialsToken := {
  val tokenFile           = baseDirectory.value / ".lightbend-commercial-credentials-token"
  val tokenEnvVarName     = "LIGHTBEND_COMMERCIAL_CREDENTIALS_TOKEN"
  val maybeEnvVarToken    = sys.env.get(tokenEnvVarName)
  lazy val maybeFileToken = Try(Source.fromFile(tokenFile).getLines.mkString).toOption
  val maybeToken          = maybeFileToken.orElse(maybeEnvVarToken)
  maybeToken.getOrElse {
    sLog.value.error(
      s"No Lightbend Commercial Credentials found -- need to set either env var '$tokenEnvVarName' or have file '$tokenFile' containing a valid Lightbend Commercial Credentials token"
    )
    System.exit(1)
    "ERROR"
  }
}

ThisBuild / resolvers += "lightbend-commercial-mvn" at s"https://repo.akka.io/${lightbendCommercialCredentialsToken.value}/secure"
