package notionfys

import App._
import cats.effect.IO

trait Console[F[_]] {
  def log(msg: String): F[Unit]
}

object Console extends Console[AppM] {
  override def log(msg: String): AppM[Unit] =
    AppM
      .ask[IO, Args]
      .flatMap(args => if (args.verbose) AppM.liftF(IO(println(msg))) else AppM.pure(()))
}
