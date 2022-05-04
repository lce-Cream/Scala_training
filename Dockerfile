FROM bitnami/spark:latest
COPY ./lib/* /application/lib/
COPY ./out/artifacts/test_4_jar/* /application/
CMD ["spark-submit",\
      #well yeah config is here, I'll use env or make volume a bit later
     "--properties-file", "/application/spark-config-real.conf",\
     "--jars", "/application/lib/*",\
     "--class", "Main",\
     "/application/test_4.jar"]
