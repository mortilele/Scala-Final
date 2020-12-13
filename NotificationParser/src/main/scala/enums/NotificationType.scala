package enums

import scala.util.matching.Regex

case object NotificationType extends Enumeration {

  protected case class Val(name: String, pattern: Regex) extends super.Val {
    def isPatternMatched(text: String): Boolean = pattern.findFirstIn(text) match {
      case Some(_) => true
      case None => false
    }

    override def toString(): String = this.name
  }

  import scala.language.implicitConversions

  implicit def valueToNotificationTypeVal(x: Value): Val = x.asInstanceOf[Val]

  val Discount: Val = Val("discount", "\\b(\\w*[Dd]iscount\\w*)\\b".r)
  val News: Val = Val("news", "\\b(\\w*[Nn]ews\\w*)\\b".r)
  val Personal: Val = Val("personal", "\\b(\\w*[Pp]ersonal\\w*)\\b".r)
  val Other: Val = Val("other", "\\b(\\w*SOMEIMPOSSIBLEWORD\\w*)\\b".r)

  def convertStringToClassInstance(notificationType: String): Value = NotificationType.values.filter(_.toString == notificationType).head
}
