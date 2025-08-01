# E2E-Module
In order to run the E2E tests, you need to have the following prerequisites installed:
- Docker & Docker Compose

Then you have to run the following commands in the root folder of the project to set up the environment and start the docker containers:
```shell
docker compose up
```
After the containers are up and running, you can run the E2E tests with the following command:
```shell
cd ..
./gradlew :e2e:test
```