package notionfys

import App._
import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import cats.effect.LiftIO

trait Console[F[_]] {
  def log(msg: String): F[Unit]
}

object Console extends Console[AppM] {
  private val L = implicitly[LiftIO[AppM]]
  override def log(msg: String): App.AppM[Unit] =
    Kleisli.ask[IO, Args].flatMap(args => if (args.verbose) L.liftIO(IO(println(msg))) else ().pure[AppM])
}
