FROM openjdk:17
#11.0.16
# EXPOSE 8081:8080
RUN mkdir /app
COPY solawi-bid-backend/build/resources/main/application.docker.conf /app/application.docker.conf
COPY solawi-bid-backend/build/resources/main/wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh
COPY solawi-bid-backend/build/libs/*-all.jar /app/server.jar
ENTRYPOINT ["/app/wait-for-it.sh", "solawi-bid_database:3306", "--","java","-jar","/app/server.jar", "-config=/app/application.docker.conf"]
