package validators

import enums.NotificationType


trait Validator[T] {
  def validate(t: T): Option[ApiError]
}

object NotificationTypeExists extends Validator[String] {
  def validate(notificationType: String): Option[ApiError] = {
    if (!NotificationType.values.exists(_.toString == notificationType)) {
      Some(ApiError.notificationTypeNotFound(notificationType))
    } else
      None
  }
}