# indoqa-zookeeper-config

Uses [indoqa-zookeeper](https://github.com/Indoqa/indoqa-zookeeper) to read properties from [Apache ZooKeeper](https://zookeeper.apache.org/) and provide them as a [Spring](https://spring.io/) PropertySource.

Applications will also register themselves in the ZooKeeper ensemble to keep track of the active services and where and since when they are running.
Additional information, such as descriptions, links, and names, can also be stored to provide a complete overview of a system.
