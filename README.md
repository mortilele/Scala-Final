# Scala-Final
## Description:
- [x] There is one producer who periodically sends a lot of raw user notification messages (user.notifications)
- [x] There is a cluster - a consumer who subscribes to this topic and distributes these messages, for example, to 4 nodes.
- [x] Each node processes the message and, depending on the content of the message, must filter it and place it in the appropriate category (achievement notification, gift notification, payment notification)
- [x] Then there is a GET request (get user notifications of category = achievement notification), for each such type of request there are actors who pull these user notifications from this category and return them to the client
- [ ] If the client sends a request again within 30 seconds, then the data should not just be read again, but return the old data + (new notifications that could appear in the last 30 seconds)
- [x] Each user-actor must reload state from cassandra
## Contributors:
* Alik
* Bekbolat
