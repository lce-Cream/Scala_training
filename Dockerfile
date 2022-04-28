FROM bitnami/spark:latest
#COPY ./src /application/
#COPY ./project /application/
#COPY ./build.sbt /application/
COPY ./out/artifacts/test_4_jar/* /application/
CMD ["spark-submit", "--properties-file", "/application/spark-config-real.conf", "--class", "Main", "/application/test_4.jar"]
