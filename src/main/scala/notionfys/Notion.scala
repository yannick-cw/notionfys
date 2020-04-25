package notionfys

import App._
import sttp.client._
import sttp.client.circe._
import io.circe._
import io.circe.generic.semiauto._
import cats.effect.IO
import java.{util => ju}

trait Notion[F[_]] {
  def addSubPage(h: Highlight): F[Unit]
  def getSubPages: F[List[Highlight]]
}
object Notion extends Notion[AppM] {
  implicit val backend                     = HttpURLConnectionBackend()
  val notionUrl                            = "https://www.notion.so/api/v3"
  def addSubPage(h: Highlight): AppM[Unit] = AppM.pure(())
  def getSubPages: AppM[List[Highlight]] =
    for {
      Args(token, page, _, _) <- AppM.ask[IO, Args]
      reqBody = PageChunkRequest(page)
      request = basicRequest
        .post(uri"$notionUrl/loadPageChunk")
        .cookie("token_v2", token)
        .body(reqBody)
        .response(asJson[PageChunkResponse])
      res = request.send()
      pageChunkResponse <- AppM.liftF(
        IO.fromEither(res.body.left.map(err => new RuntimeException(err.toString)))
      )
      highligts = extractHighlights(pageChunkResponse, page)
    } yield highligts

  private def extractHighlights(response: PageChunkResponse, pageId: ju.UUID): List[Highlight] =
    for {
      contents <- response.recordMap.block.get(pageId).toList.flatMap(_.value.content)
      (title, content) <- contents
        .flatMap(cId => response.recordMap.block.get(cId).toList.map(_.value))
        .grouped(3)
        .toList
        .collect {
          case (List(
              Value("sub_header", Some(propsT), _),
              Value("text", Some(propsH), _),
              Value("divider", None, _)
              )) =>
            (propsT, propsH)
        }
      titleA <- title.title.toList.flatMap(_.headOption.toList.flatMap(_.headOption.toList))
      contentA <- content.title.toList
        .flatMap(_.headOption.toList.flatMap(_.headOption.toList))
    } yield Highlight(titleA, contentA, List.empty)
}

// Response
case class Properties(title: Option[List[List[String]]])
object Properties { implicit val decoder: Decoder[Properties] = deriveDecoder }
case class Value(`type`: String, properties: Option[Properties], content: Option[List[ju.UUID]])
object Value { implicit val decoder: Decoder[Value] = deriveDecoder }
case class Block(value: Value)
object Block { implicit val decoder: Decoder[Block] = deriveDecoder }
case class RecordMap(block: Map[ju.UUID, Block])
object RecordMap { implicit val decoder: Decoder[RecordMap] = deriveDecoder }
case class PageChunkResponse(recordMap: RecordMap)
object PageChunkResponse { implicit val decoder: Decoder[PageChunkResponse] = deriveDecoder }

// Request
case class Stack(table: String, id: ju.UUID, index: Int)
object Stack { implicit val encoder: Encoder[Stack] = deriveEncoder }

case class Cursor(stack: List[List[Stack]])
object Cursor { implicit val encoder: Encoder[Cursor] = deriveEncoder }

case class PageChunkRequest(
    pageId: ju.UUID,
    limit: Int,
    cursor: Cursor,
    chunkNumber: Int,
    verticalColumns: Boolean
)
object PageChunkRequest {
  implicit val encoder: Encoder[PageChunkRequest] = deriveEncoder
  def apply(pageId: ju.UUID): PageChunkRequest =
    PageChunkRequest(
      pageId = pageId,
      limit = 100000,
      cursor = Cursor(stack = List(List(Stack(table = "block", id = pageId, index = 0)))),
      chunkNumber = 0,
      verticalColumns = false
    )
}
