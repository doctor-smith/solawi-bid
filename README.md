# Solawi Auction Application
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.22-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

## Running the project
### Setup Docker
First, install Docker on your system. 

### Start Project
Open a terminal, move to the root folder of the project and run the commands 
```shell
docker compose -p solawi-bid down --remove-orphans && \
docker compose -p solawi-bid up -d
```

Access the frontend on
```
localhost:80
```
Access the backend on
```
localhost:8080
```

If you need to rebuild frontend or backend use one of the bash scripts in the scripts folder:

- rebuild-backend.sh
- rebuild-frontend.sh
- rebuild-be-and-fe.sh

### Run the backend separately
Use the command line to run the backend:
```shell
./gradlew runFatJar
```
Access the backend on 
```
localhost:8081
```

### Run the Frontend separately
Use the command line to run the frontend:

```shell 
./gradlew jsBrowserRun
```
Access the frontend on
```
localhost:8080
```
in your browser.


Hint: 
Instead of manually compiling and executing a Kotlin/JS project every time you want to see the changes you made, you can use the continuous compilation mode:
```shell
./gradlew jsBrowserRun --continuous
```


## Useful links 

- [Compose Multiplatform](https://github.com/JetBrains/compose-jb)

- [Kotlin Compatibility](https://github.com/JetBrains/compose-jb/blob/master/VERSIONING.md#kotlin-compatibility)

- [Ktor Docs](https://ktor.io/docs/welcome.html)



# solawi-bid
- [Frontend Notes](./solawi-bid-frontend/Notes.md)
