# Solawi Auction Application
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.22-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

## Running the project
### Setup Docker
First, install Docker on your system. 

### Start Project
Open a terminal, move to the root folder of the project and run the commands 
```shell
./gradlew solawi-bid-backend:clean && \
./gradlew solawi-bid-backend:build && \
./gradlew solawi-bid-backend:buildFatJar && \
./gradlew solawi-bid-frontend:clean && \
./gradlew solawi-bid-frontend:build && \
docker compose -p solawi-bid down --remove-orphans && \
docker compose -p solawi-bid up -d 
```



Access the frontend on
```
localhost:8080
```
Access the backend on
```
localhost:8081
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

- [Kotlin Compatibility](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compatibility-and-versioning.html)

- [Ktor Docs](https://ktor.io/docs/welcome.html)



# solawi-bid
- [Frontend Notes](./solawi-bid-frontend/Notes.md)

# CICD

## Provisioning a Server

to provision a server, you can use the install-server github action.
the following inputs are required:

- vars.SERVER_ADDRESS: the address of the server, e.g. `solyton.org`
- vars.SERVER_USER: the user to connect to the server, e.g. `root` **This User needs passwordless sudo acess for ansible to work**
- secrets.SERVER_KEY: the ssh key to connect to the server

## Continuous Deployment

on every push the project is built and deployed to the server.
with the following rules:

normal push: published to d.solyton.org
push to main: published to q.solyton.org
push to release/tag: published to solyton.org

the following inputs are required:

- vars.SERVER_ADDRESS: the address of the server, e.g. `solyton.org`
- vars.SERVER_USER: the user to connect to the server, e.g. `root` **This User needs passwordless sudo acess for ansible to work**
- secrets.APPLICATION_OWNER_PASSWORD_D
- secrets.APPLICATION_OWNER_PASSWORD_P
- secrets.APPLICATION_OWNER_PASSWORD_Q
- secrets.JWT_SECRET_D
- secrets.JWT_SECRET_P
- secrets.JWT_SECRET_Q
- secrets.MYSQL_PASSWORD_D
- secrets.MYSQL_PASSWORD_P
- secrets.MYSQL_PASSWORD_Q
- secrets.SERVER_KEY

# Troubleshooting

## Connecting to the traefik dashboard

Because the traefik dashboard is not secured, it is only accessible over an ssh tunnel.

```shell
ssh <user>@solyton.org -L 8080:localhost:8080
```

Then you can access the dashboard at `http://localhost:8080/dashborad` in your browser.

## Accessing the MySQL databases

For the same reason as the traefik dashboard, the MySQL databases are only accessible over an ssh tunnel.
Because we have three databases, docker maps the databases to different random ports.
To find out which port is mapped to which database, you can use the following command after logging into the server:

```shell
docker ps --filter "name=solawi-bid_database" --format "table {{.Names}}\t{{.Ports}}"
```

Then you can create an ssh tunnel for the database you want to access:

```shell
ssh <user>@solyton.org -L 3306:localhost:<port>
```

Then you can access the database with your favorite MySQL client e.g. DBeaver on `localhost:3306`.