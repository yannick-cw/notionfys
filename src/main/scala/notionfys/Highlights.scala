package notionfys

import cats.Applicative
import atto._
import Atto._
import cats.implicits._

trait Highlights[F[_]] {
  def parseKindleHighlights(f: String): F[List[Highlight]]
}

object Highlights {
  def apply[F[_]: Applicative] = new Highlights[F] {
    def parseKindleHighlights(f: String): F[List[Highlight]] =
      Applicative[F].pure(pp.parseOnly(f.filterNot(_ == '\r')).either.getOrElse(List.empty))

    val sepP  = string("==========")
    val title = many1(notChar('\n')).map(_.toList.mkString)
    val lines = many(notChar('\n')).map(_.mkString)
    val eol   = char('\n')

    val segmentP: Parser[Highlight] =
      (title <~ eol <~ lines <~ eol <~ lines <~ eol, lines <~ eol <~ sepP <~ eol)
        .mapN(Highlight)
    val pp = many(segmentP)
  }

}
