cd ..
./gradlew solawi-bid-frontend:clean && \
./gradlew solawi-bid-frontend:build && \
# -x test -x jsBrowserTest -x jsTest&& \
docker compose -p solawi-bid down --remove-orphans && \
docker compose -p solawi-bid up -d --build solawi-bid_frontend
