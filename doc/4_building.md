## Building

In order to build dRTEC from source, you need to have Java SE Development Kit (e.g., OpenJDK) version 7 or higher (it is recommended to use Java SE 8 or higher) and SBT (v0.13.x) installed in your system. Furthermore, dRTEC build depends on the HEALPix and auxlib.

### HEALPix

Download and build HEALPix library v3.20. 
Ant is nessecary for building the library.

```bash
$ wget http://sourceforge.net/projects/healpix/files/Healpix_3.20/Healpix_3.20_2014Dec05.tar.gz
$ tar -xvzf Healpix_3.20_2014Dec05.tar.gz
$ cd Healpix_3.20/src/java
$ ant distonly
$ cd dist
$ mv jhealpix_compat.jar healpix.jar
```
then move Healpix_3.20/src/java/dist/healpix.jar file to the drtec/lib folder.

### AuxLib

Clone and publish locally the auxlib project:

```bash
$ git clone -b v0.1 --depth 1 https://github.com/anskarl/auxlib.git
$ cd auxlib
$ sbt ++2.11.7 publishLocal
```

#### Building dRTEC

To start building the library, type the following command:

```
$ sbt compile
```

To publish the library to your local Apache Ivy directory (e.g., inside ~/.ivy2/local/), type the following command:

```
$ sbt publishLocal
```

After publishing process, you can link to dRTEC in your sbt project (e.g., inside the build.sbt file) by adding the following dependency:

```
libraryDependencies += "gr.demokritos.iit" %% "drtec" % "0.1"
```
<br />
Apache Spark Streaming is not provided with the library. You have one of the following options:

* install Apache Spark on a cluster
* install Apache Spark locally
* add the follow dependencies for Apache Spark in your sbt project:

 ```
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.5.2",
  "org.apache.spark" %% "spark-streaming" % "1.5.2",
  "org.apache.spark" %% "spark-sql" % "1.5.2"
)
```

For more information about building Apache Spark visit http://spark.apache.org/docs/latest/building-spark.html
