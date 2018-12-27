FROM tomcat:9-jre8

ADD bin /usr/local/tomcat/webapps/fuseki
ADD target/fuseki-oauth-security-1.0-SNAPSHOT.jar /usr/local/tomcat/webapps/fuseki/WEB-INF/lib/jwt-auth.jar

ADD conf /usr/local/fuseki

ENV FUSEKI_HOME=/usr/local/fuseki
ENV FUSEKI_BASE=/usr/local/fuseki

CMD ["catalina.sh", "run"]



