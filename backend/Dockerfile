FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

# DB-IP Lite country database (CC BY 4.0), so the IP of a visit never leaves the server (HU025). A new
# file comes out every month: the current one, or the previous one early in the month. Without network
# the image is still built, and the visits are saved without country.
RUN mkdir -p /geoip \
    && for month in "$(date +%Y-%m)" "$(date -d "$(date +%Y-%m-15) -1 month" +%Y-%m)"; do \
         curl -fsSL --max-time 120 "https://download.db-ip.com/free/dbip-country-lite-${month}.mmdb.gz" -o /tmp/dbip.gz \
           && gunzip -c /tmp/dbip.gz > /geoip/dbip-country-lite.mmdb \
           && break; \
         rm -f /geoip/dbip-country-lite.mmdb; \
       done; \
    rm -f /tmp/dbip.gz

COPY src src

RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /geoip geoip

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]