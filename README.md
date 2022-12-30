# istint-backend

This is the backend for [istint](https://istint.juelps.de) web application (link on [Github](https://github.com/mpapenbr/istint))

# Developer notes

## Testcontainers
Moved from `de.flapdoodle.embed` to `org.testcontainers` for tests using a mongodb. 

Some issues arise when running tests in VSC:
- When starting the tests from the root there seem to be race conditions. Tests may fail which are otherwise ok when running alone (and with `mvn test`)


## Gradle
see [#66](https://github.com/mpapenbr/istint-backend/issues/66) for details why not yet done with Gradle

## Mongo data


- create volume: `docker create volume mongo-backup`
- create a container: `docker run -it --rm -v mongo-backup:/backup  mongo'
- inside the container:  `mongodump --host host.docker.internal -u root -p example --authenticationDatabase admin  -d local -o /backup/`
- get the data out of the volume to local dir: `docker run -u 1000:1000 -it --rm -v mongo-backup:/backup -v $PWD:/local  busybox cp /backup/local.tgz /local/data.tgz`

### restore
- create container: `docker run --rm -it -v $PWD/data:/local -v mongo-backup:/mongo busybox`
- inside the container: copy and extract the dump file. The files should be available at /mongo/local
- restore the data: `docker run --rm -it  -v mongo-data:/backup mongo  mongorestore  -h host.docker.internal -u root -p example --authenticationDatabase admin -d local /backup/local`
