:cloud: Notifications Services :cloud:
## Description:
Group Project developed during Scala learning
The project is that we have a service that sends raw notifications, another service must process notifications and group them into notification types, then, there is GET request in which need to pull out the notification of a specific user from the notification group.

## Tasks:
- [x] HTTP Server Notification Producer
  - [x] GET messages
  - [x] POST new message
  - [x] POST send message to user
- [x] There is one producer who periodically sends a lot of raw user notification messages (user.notifications)
  - [x] Include topic partition OffSet
- [x] There is service, that consumes messages
  - [x] Determine Notification Type, parse Notifications via RegEx
  - [x] Send to Collector
  - [x] Create stream for each topic partition
- [x] HTTP Server Notifications Service
  - [x] GET /notifications/<user_id>/<notification_type>, get notifications of user filtered notification type
- [ ] If the client sends a request again within 30 seconds, then the data should not just be read again, but return the old data + (new notifications that could appear in the last 30 seconds)
- [x] Collector Actor must reload state from Cassandra

## Contributors:
* Alik
* Bekbolat
