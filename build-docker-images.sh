#!/bin/sh
cd campsite-catalog-service;
docker build --build-arg JAR_FILE=build/libs/*.jar -t com/newisland/campsitecatalog-service .
cd ../reservation-service
docker build --build-arg JAR_FILE=build/libs/*.jar -t com/newisland/reservation-service .
cd ../user-service
docker build --build-arg JAR_FILE=build/libs/*.jar -t com/newisland/user-service .
cd ../gateway
docker build --build-arg JAR_FILE=build/libs/*.jar -t com/newisland/gateway .
