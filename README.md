# Properties
1. Add to `/path/to/tomcat/conf/catalina.properties`:
```xml
shared.loader=${catalina.base}/shared/classes
```
2. Add bot properties to `/path/to/tomcat/shared/classes/shop-list-bot-global.properties`:
```xml
shop.list.bot.token=<telegram-bot-token>
shop.list.bot.webhook.url=<server-domain-name>/shop-list-bot/webhook
    
db.driverClassName=org.postgresql.Driver
db.url=<database-url>
db.username=<database-username>
db.password=<database-admin>
```