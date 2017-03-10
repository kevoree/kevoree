org.kevoree.library : starter-parent
------

Use this Maven artifact as your parent if you want to develop a Kevoree Java library using Maven.

### Usage
```xml
<!-- Add this to your pom.xml -->
<parent>
    <groupId>org.kevoree.library</groupId>
    <artifactId>starter-parent</artifactId>
    <!-- Use the latest release as version -->
    <version>RELEASE</version>
</parent>
```


### Complete `pom.xml` example
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>comp-helloworld</artifactId>
    <name>Kevoree HelloWorld component</name>

    <parent>
        <groupId>org.kevoree.library</groupId>
        <artifactId>starter-parent</artifactId>
        <!-- Use the latest release as version -->
        <version>RELEASE</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.kevoree</groupId>
            <artifactId>org.kevoree.api</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.kevoree.tools</groupId>
                <artifactId>org.kevoree.tools.mavenplugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```