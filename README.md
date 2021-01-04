# spleeter_web 

**A basic web UI for Spleeter.** 

## For end-users

It uses the 2-stem model and only extracts the backing track, which is what I 
personally need in my singing exercises. Can easily be modified to support
extracting the vocals as well in the future.

How to build:
1. Install JDK 11 or later.
2. `./gradlew -PisProduction assembleDist`

Then copy the build/distributions/spleeter-web-*.tar to your destination machine
and untar. 

How to run:
1. Install prerequisites:
   1. JRE 11 or later.
   2. ffmpeg.
   3. `pip install spleeter`
2. Install and start MongoDB.
3. Run with: 
   `SRC_PATH=<SRC> DST_PATH=<DST> MONGODB_URI='<MONGO>' ./bin/spleeter-web`
   1. `SRC` and `DST` are source and destination paths (by default `/tmp/spleeter_web/src` and `/tmp/spleeter_web/dst`)
   1. `MONGODB_URI` is the MongoDB connection string, i.e. `mongodb://<USER>:<PASS>@127.0.0.1`
4. If you see error message `UnsatisfiedLinkError: no netty_transport_native_epoll`, just ignore it.
5. Browse to <your_host>:9090

## For developers
To me, this project is as much a practical thing as an opportunity to play with fun new technology. It explores Kotlin 
Multiplatform - a way to build fullstack apps in Kotlin alone.

**Tech stack**: Backend - Kotlin/Ktor/Netty/MongoDB. Frontend - Kotlin/React. No JS, no HTML, no CSS, just pure Kotlin 
all the way.
