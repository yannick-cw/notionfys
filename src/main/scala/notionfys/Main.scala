package notionfys

import cats.effect._
import cats.implicits._
import java.{ util => ju }
import cats.mtl.ApplicativeAsk
import cats.implicits._
import cats.mtl.implicits._
import cats.Monad
import com.monovore.decline._
import com.monovore.decline.effect._
import cats.data.Kleisli
import App._
import cats.data.ValidatedNel
import cats.data.NonEmptyList
import cats.data.Validated
import scala.util.Try

object Cli {
  def parseArgs: Opts[Args] =
    (
      Opts.option[String](
        "token",
        short = "n",
        metavar = "id",
        help = "Your notion token, found in the token_v2 cookie when you open notion in the browser"
      ),
      Opts
        .option[String](
          "page",
          short = "p",
          metavar = "id",
          help =
            "Id of the page to which the highlights should be added, you find it in the url if you open the page in the browser"
        )
        .mapValidated(parseToUUID),
      Opts
        .option[String](
          "kindle",
          short = "k",
          metavar = "Path",
          help = "Path to your kindle, e.g. on Mac /Volumes/Kindle"
        )
        .mapValidated(
          p => Validated.fromTry(Try(os.Path(p))).bimap(_ => NonEmptyList.one(s"$p is not an absolute path"), identity)
        ),
      Opts.flag("verbose", help = "turn on verbose logging").orFalse
    ).mapN(Args)

  private def parseToUUID(rawId: String): ValidatedNel[String, ju.UUID] =
    Validated
      .fromTry(Try(ju.UUID.fromString(rawId.zipWithIndex.flatMap {
        case (c, p) if p == 7 || p == 11 || p == 15 || p == 19 => List(c, '-')
        case (c, _)                                            => List(c)
      }.mkString)))
      .bimap(_ => NonEmptyList.one(s"$rawId was is not parsebble to UUID"), identity)

}

case class Args(
    token: String,
    page: ju.UUID,
    kindle: os.Path,
    verbose: Boolean = false
)

trait FS[F[_]] {
  def readF(p: os.Path): F[String]
}
object FS extends FS[AppM] {
  def readF(p: os.Path): AppM[String] = "".pure[AppM]
}

case class Highlight(title: String, content: String)
trait Notion[F[_]] {
  def addSubPage(h: Highlight): F[Unit]
  def getSubPages: F[List[Highlight]]
}
object Notion extends Notion[AppM] {
  def addSubPage(h: Highlight): AppM[Unit] = ().pure[AppM]
  def getSubPages: AppM[List[Highlight]]   = List.empty.pure[AppM]
}

trait Highlights[F[_]] {
  def parseKindleHighlights(f: String): F[List[Highlight]]
}
object Highlights extends Highlights[AppM] {
  def parseKindleHighlights(f: String): AppM[List[Highlight]] = List.empty.pure[AppM]

}

object App {
  type AppM[A] = Kleisli[IO, Args, A]
}

object Main
    extends CommandIOApp(name = "notionfy", header = "Sync your Kindle highlights to Notion", version = "0.0.0") {

  implicit val F: FS[AppM]         = FS
  implicit val N: Notion[AppM]     = Notion
  implicit val H: Highlights[AppM] = Highlights

  override def main: Opts[IO[ExitCode]] =
    Cli.parseArgs.map(updateNotion[AppM].run(_).as(ExitCode.Success))

  def updateNotion[F[_]: Monad](
      implicit FS: FS[F],
      Notion: Notion[F],
      Highlights: Highlights[F],
      Ask: ApplicativeAsk[F, Args]
  ): F[Unit] =
    for {
      kindlePath <- Ask.reader(_.kindle)
      kindleFile <- FS.readF(kindlePath / "documents" / "My Clippings.txt")
      kindleHighlights <- Highlights.parseKindleHighlights(kindleFile)
      currentHighlights <- Notion.getSubPages
      newHighlights = kindleHighlights.filterNot(
        h => currentHighlights.exists(cH => cH.title == h.title && cH.content == h.content)
      )
      _ <- newHighlights.traverse(Notion.addSubPage)
    } yield ()
}
