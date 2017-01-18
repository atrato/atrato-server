# Atrato Server

## How to test (for now)

* Open atrato-server project with your IDE

* Add the following to your run-time classpath
 (in IntelliJ IDEA, it's File->Project Structure->Modules->atrato-server->(+)->Choose directory, then move those directories before the hadoop dependencies)
  * $HADOOP_PREFIX/etc/hadoop
  * $HADOOP_PREFIX/share/hadoop/common/lib
  * $HADOOP_PREFIX/share/hadoop/common
  * $HADOOP_PREFIX/share/hadoop/hdfs
  * $HADOOP_PREFIX/share/hadoop/hdfs/lib
  * $HADOOP_PREFIX/share/hadoop/hdfs
  * $HADOOP_PREFIX/share/hadoop/yarn/lib
  * $HADOOP_PREFIX/share/hadoop/yarn

* Start Hadoop (yarn and hdfs)

* Optional: Start Yarn Timeline Server (experimental, necessary if you want attempt container info)

* Run atrato-server in your IDE. (Go to AtratoServer class and Run)

* Check the `/about` REST call:
```
$ curl -s http://localhost:8800/ws/v1/about | python -mjson.tool
{
    "apexVersionInfo": {
        "buildVersion": "3.5.0 from rev: 6de8828 branch: 6de8828e4f3d5734d0a6f9c1be0aa7057cb60ac8 by Thomas Weise on 06.12.2016 @ 22:11:51 PST",
        "date": "06.12.2016 @ 22:11:51 PST",
        "revision": "rev: 6de8828 branch: 6de8828e4f3d5734d0a6f9c1be0aa7057cb60ac8",
        "user": "Thomas Weise",
        "version": "3.5.0"
    },
    "atratoServerVersionInfo": {
        "buildVersion": "Unknown from rev: 53df263 branch: master by David Yan on 30.12.2016 @ 23:22:29 PST",
        "date": "30.12.2016 @ 23:22:29 PST",
        "revision": "rev: 53df263 branch: master",
        "user": "David Yan",
        "version": "Unknown"
    },
    "javaVersion": "1.8.0_112"
}
```
* Check application list. None if you haven't started any. For example:

```
$ curl -s http://localhost:8800/ws/v1/applications | python -mjson.tool
{
    "applications": [
        {
            "id": "application_1483391433545_0001",
            "name": "PiDemo",
            "queue": "default",
            "state": "KILLED",
            "tags": [],
            "trackingUrl": "http://0.0.0.0:8188/applicationhistory/app/application_1483391433545_0001",
            "user": "david"
        }
    ]
}
```

* Start any Apex application (e.g. pi demo). Note that application ID.

* Verify REST calls. For example:

```
$ curl -s http://localhost:8800/ws/v1/applications | python -mjson.tool
{
    "applications": [
        {
            "id": "application_1483391433545_0001",
            "name": "PiDemo",
            "queue": "default",
            "state": "KILLED",
            "tags": [],
            "trackingUrl": "http://0.0.0.0:8188/applicationhistory/app/application_1483391433545_0001",
            "user": "david"
        },
        {
            "id": "application_1483391433545_0002",
            "name": "PiDemo",
            "queue": "default",
            "state": "RUNNING",
            "tags": [],
            "trackingUrl": "http://david-ubuntu:8088/proxy/application_1483391433545_0002/",
            "user": "david"
        }
    ]
}

$ curl -s http://localhost:8800/ws/v1/applications/application_1483391433545_0002/physicalPlan | python -mjson.tool
{
    "operators": [
        {
            "checkpointStartTime": "1483392597365",
            "checkpointTime": "56",
            "checkpointTimeMA": "180",
            "className": "com.datatorrent.lib.testbench.RandomEventGenerator",
            "container": "container_1483391433545_0002_01_000002",
            "counters": null,
            "cpuPercentageMA": "9.533145966210137",
            "currentWindowId": "6371122437840437401",
            "failureCount": "0",
            "host": "david-ubuntu:37617",
            "id": "1",
            "lastHeartbeat": "1483392614835",
            "latencyMA": "0",
            "logicalName": "rand",
            "metrics": {},
            "name": "rand",
            "ports": [
                {
                    "bufferServerBytesPSMA": "783337",
                    "name": "integer_data",
                    "queueSizeMA": "0",
                    "recordingId": null,
                    "totalTuples": "5494000",
                    "tuplesPSMA": "89173",
                    "type": "output"
                }
            ],
            "recordingId": null,
            "recoveryWindowId": "6371122437840437367",
            "status": "ACTIVE",
            "totalTuplesEmitted": "5494000",
            "totalTuplesProcessed": "0",
            "tuplesEmittedPSMA": "89173",
            "tuplesProcessedPSMA": "0",
            "unifierClass": null
        },
        {
            "checkpointStartTime": "1483392597368",
            "checkpointTime": "56",
            "checkpointTimeMA": "154",
            "className": "com.datatorrent.demos.pi.PiCalculateOperator",
            "container": "container_1483391433545_0002_01_000003",
            "counters": null,
            "cpuPercentageMA": "4.676976136250125",
            "currentWindowId": "6371122437840437401",
            "failureCount": "0",
            "host": "david-ubuntu:37617",
            "id": "2",
            "lastHeartbeat": "1483392614423",
            "latencyMA": "2",
            "logicalName": "picalc",
            "metrics": {},
            "name": "picalc",
            "ports": [
                {
                    "bufferServerBytesPSMA": "870467",
                    "name": "input",
                    "queueSizeMA": "544",
                    "recordingId": null,
                    "totalTuples": "5494000",
                    "tuplesPSMA": "89102",
                    "type": "input"
                },
                {
                    "bufferServerBytesPSMA": "40",
                    "name": "output",
                    "queueSizeMA": "0",
                    "recordingId": null,
                    "totalTuples": "153",
                    "tuplesPSMA": "2",
                    "type": "output"
                }
            ],
            "recordingId": null,
            "recoveryWindowId": "6371122437840437367",
            "status": "ACTIVE",
            "totalTuplesEmitted": "153",
            "totalTuplesProcessed": "5494000",
            "tuplesEmittedPSMA": "2",
            "tuplesProcessedPSMA": "89102",
            "unifierClass": null
        },
        {
            "checkpointStartTime": "0",
            "checkpointTime": "0",
            "checkpointTimeMA": "0",
            "className": "com.datatorrent.lib.io.ConsoleOutputOperator",
            "container": "container_1483391433545_0002_01_000004",
            "counters": null,
            "cpuPercentageMA": "0.6139284831409615",
            "currentWindowId": "6371122437840437400",
            "failureCount": "0",
            "host": "david-ubuntu:37617",
            "id": "3",
            "lastHeartbeat": "1483392614329",
            "latencyMA": "7",
            "logicalName": "console",
            "metrics": {},
            "name": "console",
            "ports": [
                {
                    "bufferServerBytesPSMA": "44",
                    "name": "input",
                    "queueSizeMA": "0",
                    "recordingId": null,
                    "totalTuples": "152",
                    "tuplesPSMA": "2",
                    "type": "input"
                }
            ],
            "recordingId": null,
            "recoveryWindowId": "6371122437840437367",
            "status": "ACTIVE",
            "totalTuplesEmitted": "0",
            "totalTuplesProcessed": "152",
            "tuplesEmittedPSMA": "0",
            "tuplesProcessedPSMA": "2",
            "unifierClass": null
        }
    ],
    "streams": [
        {
            "locality": null,
            "logicalName": "rand_calc",
            "sinks": [
                {
                    "operatorId": "2",
                    "portName": "input"
                }
            ],
            "source": {
                "operatorId": "1",
                "portName": "integer_data"
            }
        },
        {
            "locality": null,
            "logicalName": "rand_console",
            "sinks": [
                {
                    "operatorId": "3",
                    "portName": "input"
                }
            ],
            "source": {
                "operatorId": "2",
                "portName": "output"
            }
        }
    ]
}

```


## Notes

### This project

* We probably should not implement the websocket pubsub because it's a push-based mechanism, which has these problems:
  * The client (web browser) or the network may not be able to handle the incoming volume
  * The client cannot configure the refresh rate of the incoming data
  * Instead, we should have the client poll for new data with a persistent (keep-alive) HTTP connection

* Instead of having a thread that queries YARN for application list every second, I decided to use a time-based cache that
only queries the application list if it's needed. This is because some users have complained about DT's gateway is generating
too many requests to YARN. We may need to revisit this if we want to support alerts.

* I decided to do away from the Web Services methods returning JSONObject. I am now using Java beans instead. This is because
we would be able to use something like `enunciate` to auto generate REST API doc. We need to think about what to do with the STRAM proxy calls though.

* YARN Timeline Server stores history of containers and attempt info. It can be enabled in yarn-site.xml. It needs to be started separately and the start-yarn.sh script does not start it
automatically.

* YARN Timeline Server is relatively new and it's buggy. When the application is running and when the containers of a previous attempt is queried,
it actually returns the containers of the current attempt. https://issues.apache.org/jira/browse/YARN-6008

#### Installer

##### Config flow

* DFS, hadoop location
* jdbc in config file for bootstrap

##### bash script (Thomas will work on it)

* Port number from command line

##### Security

* keytab, secured cluster
* User and RBAC not needed yet

##### Container Log Retrieval

* grep regex

##### App Packages

### Miscellaneous

* Ubuntu 16.10 Desktop is the only good version that works with my new Dell laptop (Inspiron 15 7000 series 7579). Older versions have the screen flickering and flashing horizontal lines.
* IntelliJ sometimes outputs repeated 's' (as if I was pressing and holding the s key) after I do Ctrl-S to save. Did not happen on my old Sony laptop.

