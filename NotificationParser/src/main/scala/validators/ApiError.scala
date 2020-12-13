package validators

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

final case class ApiError private(statusCode: StatusCode, message: String)

object ApiError {
  private def apply(statusCode: StatusCode, message: String): ApiError = new ApiError(statusCode, message)

  val generic: ApiError = new ApiError(StatusCodes.InternalServerError,
    "Unknown error.")


  def notificationTypeNotFound(notificationType: String): ApiError =
    new ApiError(StatusCodes.NotFound, s"$notificationType: NotificationType not Exists")
}