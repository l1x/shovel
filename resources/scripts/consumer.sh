java -Xms256m -Xmx512m -server \
    -XX:NewRatio=2 -XX:+UseConcMarkSweepGC \
    -XX:+TieredCompilation -XX:+AggressiveOpts \
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.local.only=false \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    -XX:+UnlockCommercialFeatures -XX:+FlightRecorder \
    -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=consumer.jfr \
    -XX:+HeapDumpOnOutOfMemoryError \
    -jar target/shovel-0.1.3-standalone.jar consumer-test

