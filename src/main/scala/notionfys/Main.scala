package notionfys

import cats.effect._
import cats.implicits._
import cats.mtl.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import App._

case class Highlight(title: String, content: String, tags: List[String])

object Main
    extends CommandIOApp(
      name = "notionfy",
      header = "Sync your Kindle highlights to Notion",
      version = "0.0.0"
    ) {

  implicit val F: FS[AppM]         = FS
  implicit val N: Notion[AppM]     = Notion
  implicit val H: Highlights[AppM] = Highlights[AppM]
  implicit val C: Console[AppM]    = Console

  override def main: Opts[IO[ExitCode]] =
    Cli.parseArgs.map(Program.updateNotion[AppM].run(_).as(println("Welcome")).as(ExitCode.Success))
}
