docker pull istvan/zookeeper:latest
docker pull istvan/kafka:0.8.2.0
docker pull istvan/shovel:0.2.1

docker run --name zookeeper -d -p 127.0.0.1:2181:2181 istvan/zookeeper:latest 
docker run --name kafka --link zookeeper:zookeeper  -d -p 127.0.0.1:9092:9092 istvan/kafka:0.8.2.0
docker run istvan/shovel:0.2.1
