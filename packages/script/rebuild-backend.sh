cd ..
./gradlew solawi-bid-backend:clean && \
./gradlew solawi-bid-backend:build && \
./gradlew solawi-bid-backend:buildFatJar && \
docker compose -p solawi-bid down --remove-orphans && \
docker compose -p solawi-bid up -d --build solawi-bid_backend