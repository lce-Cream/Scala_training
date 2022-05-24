FROM apache/spark:v3.1.3
WORKDIR /app
ENV PATH=/opt/spark/bin:${PATH}
COPY ./jars/* /app/
