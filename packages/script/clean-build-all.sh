cd ..
./gradlew clean && \
./gradlew solawi-bid-frontend:clean && \
./gradlew solawi-bid-backend:clean && \

./gradlew build && \
./gradlew solawi-bid-frontend:build && \
./gradlew solawi-bid-backend:build && \
./gradlew solawi-bid-backend:buildFatJar