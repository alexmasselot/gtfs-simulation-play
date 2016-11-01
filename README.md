#Streaming the CFF train position

Reading the raw position from Kafka, we want to get the current position of all train and broadcast back some current location

## dev

A kafka broker must be up (consider ../devtools/cff_mock_feeder & docker-compose up).
The configuration is in conf/application.conf but the file location can be changed via `-Dconfig.file=/wherever/you.conf`

    ./activator run
 
And you can check out what is coming on kafka

    kafka_2.11-0.8.2.2 >bin/kafka-console-consumer.sh --zookeeper $(docker-machine ip default):2181  --topic train_position_snapshot
    
### Testing

    ./activator ~test
 
 
##Build

### Docker

    ./activator docker