# Properties
1. Add to `/path/to/tomcat/conf/catalina.properties`:
```xml
shared.loader=${catalina.base}/shared/classes
```
2. Add bot properties to `/path/to/tomcat/shared/classes/shop-list-bot-global.properties`:
```xml
shop.list.bot.token=<telegram-bot-token>
shop.list.bot.webhook.url=<server-domain-name>/shop-list-bot/webhook
```
# Deploy
1. Add user in `/path/to/tomcat/conf/tomcat-users.xml`:

```xml
<user username="robot" password="must-be-changed" roles="manager-script"/>
```
2. Add server settings in `~/.m2/settings.xml`:

For local:  
```xml
<servers>
    <server>
        <id>local-tomcat</id>
        <username>robot</username>
        <password>must-be-changed</password>
    </server>
</servers>
```

For remote:  
```xml
<servers>
    <server>
        <id>remote-tomcat</id>
        <username>robot</username>
        <password>must-be-changed</password>
    </server>
</servers>

<profiles>
    <profile>
        <id>remote</id>
        <properties>
            <tomcat.url>http://remote-server-ip:port/manager/text</tomcat.url>
            <tomcat.server>remote-tomcat</tomcat.server>
        </properties>
    </profile>
</profiles>
```

3. To deploy run command:

For local:  
```bash
mvn tomcat7:deploy
```

For remote:  
```bash
mvn tomcat7:deploy -Premote
```