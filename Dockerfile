FROM tomcat:9-jre8

ADD bin /usr/local/tomcat/webapps/fuseki
ADD target/fuseki-oauth-security-1.0-SNAPSHOT.jar /usr/local/tomcat/webapps/fuseki/WEB-INF/lib/jwt-auth.jar
ADD bin/test_security_data.ttl /sec_data.ttl

ADD conf /usr/local/fuseki

ENV FUSEKI_HOME=/usr/local/fuseki
ENV FUSEKI_BASE=/usr/local/fuseki
ENV JPDA_ADDRESS="15005"
ENV JPDA_TRANSPORT=dt_socket
ENV SERVER=y
ENV JJPDA_SUSPEND=n

RUN echo "172.17.0.1 docker.server.com" >> /etc/hosts

CMD ["catalina.sh", "jpda", "run"]


