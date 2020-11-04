FROM java:8
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
COPY *.jar /app.jar
EXPOSE 8888
ENTRYPOINT ["java","-jar","/app.jar","--server.port=8888"]
