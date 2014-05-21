(defproject shovel "0.0.1"
  :description "Simple Kafka consumer and producer"
  :url "http://localhost"
  :license {:name " Apache License Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.txt"}
  :dependencies [
    ;kafka
    [org.apache.kafka/kafka_2.10  "0.8.1"]
    ;zookeeper
    [zookeeper-clj                "0.9.3"]
    ;rest
    [org.clojure/clojure          "1.6.0"]
    [org.clojure/tools.cli        "0.3.1"]
    [com.google.guava/guava       "16.0" ]
    [org.clojure/data.json        "0.2.4"]]
  :exclusions [
    javax.mail/mail
    javax.jms/jms
    com.sun.jdmk/jmxtools
    com.sun.jmx/jmxri
    jline/jline
  ]
  :profiles {
    :uberjar {
      :aot :all
    }
  }
  :jvm-opts [
    "-Xms256m" "-Xmx512m" "-server" "-XX:MaxPermSize=128m"
    "-XX:NewRatio=2" "-XX:+UseConcMarkSweepGC"
    "-XX:+TieredCompilation" "-XX:+AggressiveOpts"
    "-Dcom.sun.management.jmxremote"
    "-Dcom.sun.management.jmxremote.local.only=false"
    "-Dcom.sun.management.jmxremote.authenticate=false"
    "-Dcom.sun.management.jmxremote.ssl=false"
    ;"-Xprof" "-Xrunhprof"
  ]
  :main shovel.core)
