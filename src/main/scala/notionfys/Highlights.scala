package notionfys

import App._

trait Highlights[F[_]] {
  def parseKindleHighlights(f: String): F[List[Highlight]]
}

object Highlights extends Highlights[AppM] {
  def parseKindleHighlights(f: String): AppM[List[Highlight]] = AppM.pure(List.empty)
}
