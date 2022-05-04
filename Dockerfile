FROM bitnami/spark:latest
COPY ./lib/* /application/lib/
COPY ./out/artifacts/test_4_jar/test_4.jar /application/
CMD ["spark-submit",\
     "--jars", "/application/lib/*",\
     "--class", "Main",\
     "/application/test_4.jar"]
