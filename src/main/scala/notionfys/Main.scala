package notionfys

import cats.effect._
import cats.mtl.ApplicativeAsk
import cats.implicits._
import cats.Monad
import cats.mtl.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import App._

case class Highlight(title: String, content: String)

object Main
    extends CommandIOApp(
      name = "notionfy",
      header = "Sync your Kindle highlights to Notion",
      version = "0.0.0"
    ) {

  implicit val F: FS[AppM]         = FS
  implicit val N: Notion[AppM]     = Notion
  implicit val H: Highlights[AppM] = Highlights
  implicit val C: Console[AppM]    = Console

  override def main: Opts[IO[ExitCode]] =
    Cli.parseArgs.map(updateNotion[AppM].run(_).as(println("Welcome")).as(ExitCode.Success))

  def updateNotion[F[_]: Monad](
      implicit FS: FS[F],
      Notion: Notion[F],
      Highlights: Highlights[F],
      Console: Console[F],
      Ask: ApplicativeAsk[F, Args]
  ): F[Unit] =
    for {
      kindlePath        <- Ask.reader(_.kindle)
      _                 <- Console.log(s"Reading highlights from Kindle at $kindlePath....")
      kindleFile        <- FS.readF(kindlePath / "documents" / "My Clippings.txt")
      _                 <- Console.log("Done readingkindhle highlights")
      kindleHighlights  <- Highlights.parseKindleHighlights(kindleFile)
      _                 <- Console.log("Parsed kindle highlights to internal format")
      _                 <- Console.log("Fetching highlights from Notion...")
      currentHighlights <- Notion.getSubPages
      _                 <- Console.log("Fetched current highlghts from Notion")
      newHighlights = kindleHighlights.filterNot(
        h => currentHighlights.exists(cH => cH.title == h.title && cH.content == h.content)
      )
      _ <- Console.log(s"Syncing new highlights: ${newHighlights.mkString("\n")}")
      _ <- Console.log("....")
      _ <- newHighlights.traverse(Notion.addSubPage)
      _ <- Console.log("Done syncing new highlights to Notion")
      _ <- Console.log("Shutting down")
    } yield ()
}
