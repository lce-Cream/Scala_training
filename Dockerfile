FROM bitnami/spark:latest
COPY ./lib/* /application/lib/
COPY ./out/artifacts/test_4_jar/test_4.jar /application/
CMD ["/bin/sh"]
