package notionfys

import App._

trait Notion[F[_]] {
  def addSubPage(h: Highlight): F[Unit]
  def getSubPages: F[List[Highlight]]
}
object Notion extends Notion[AppM] {
  def addSubPage(h: Highlight): AppM[Unit] = AppM.pure(())
  def getSubPages: AppM[List[Highlight]]   = AppM.pure(List.empty)
}
