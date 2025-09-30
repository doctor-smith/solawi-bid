# Solawi Auction Application
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0.-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)  
*Open-source use under Apache 2.0; commercial use requires a separate license.*

## Licensing

This project is dual-licensed:

1. **Open-Source License (Apache 2.0)**  
   You may use, modify, and redistribute this project under the terms of the Apache License 2.0.  
   See the [LICENSE](./LICENSE) file for full details.

2. **Commercial License**  
   Required for any commercial use, including deployment as a SaaS product.  
   Commercial licenses are available on request: schmidt@alpha-structure.com

## Running the project
### Setup Docker
First, install Docker on your system. 

### Start Project locally
Make sure that the environment variables are set:
```SMTP_PASSWORD in .env``` under ```solawi-bid-backend``` 

Make shure 

Open a terminal, move to the root folder of the project and run the command
```shell
docker compose -f docker-compose.yml up -d
```
Stop it using command
```shell
docker compose down --remove-orphans
```


Access the frontend on
```
localhost:8080
```
Access the backend on
```
localhost:8081
```

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

## (Local) Build Reports
See [Build Reports](build-reports/HOWTO.md)