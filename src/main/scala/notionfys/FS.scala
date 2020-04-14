package notionfys

import App._
import cats.effect.IO
import cats.effect.LiftIO

trait FS[F[_]] {
  def readF(p: os.Path): F[String]
}
object FS extends FS[AppM] {
  private val L = implicitly[LiftIO[AppM]]

  def readF(p: os.Path): AppM[String] =
    for {
      fileContent <- L.liftIO(IO(os.read(p)))
    } yield fileContent
}
