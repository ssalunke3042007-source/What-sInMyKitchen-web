FROM eclipse-temurin:17-jdk

WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY src ./src
COPY public ./public
COPY scripts ./scripts
COPY database ./database
COPY README.md ./README.md

RUN mkdir -p /app/lib && \
    curl -fsSL https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar -o /app/lib/mysql-connector-j-8.4.0.jar

RUN javac --add-modules jdk.httpserver -cp "/app/lib/mysql-connector-j-8.4.0.jar" -d /app/build/classes $(find /app/src -name "*.java")

ENV PORT=8080
EXPOSE 8080

CMD ["sh", "-c", "java --add-modules jdk.httpserver -cp "/app/build/classes:/app/lib/mysql-connector-j-8.4.0.jar" com.whatsinmykitchen.Main"]
