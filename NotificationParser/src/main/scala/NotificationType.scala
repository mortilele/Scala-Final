import scala.util.matching.Regex

case object NotificationType extends Enumeration {
  protected case class Val(pattern: Regex) extends super.Val {
    def isPatternMatched(text: String): Boolean = pattern.findFirstIn(text) match {
      case Some(_) => true
      case None => false
    }
  }
  import scala.language.implicitConversions
  implicit def valueToNotificationTypeVal(x: Value): Val = x.asInstanceOf[Val]

  val Discount: Val = Val("\\b(\\w*[Dd]iscount\\w*)\\b".r)
  val News: Val = Val("\\b(\\w*[Nn]ews\\w*)\\b".r)
  val Personal: Val = Val("\\b(\\w*[Pp]ersonal\\w*)\\b".r)
  val Other: Val = Val("\\b(\\w*SOMEIMPOSSIBLEWORD\\w*)\\b".r)
}

