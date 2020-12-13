package models


case class Notification(body: String, userId: Int) {
  override def toString = s"$body, $userId"
}

