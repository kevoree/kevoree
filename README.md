# Kevoree

Please find documentation at http://doc.kevoree.org


![Kevoree icon](https://raw.github.com/dukeboard/kevoree/master/kevoree-docs/kevoree-gris.png)

Please visit [kevoree.org](http://kevoree.org/)


## Structure
This repository contains all the **Kevoree Java** core, api and tooling.  

## Kevoree Java Runtime
Current dev version that only works with `https://new-registry.kevoree.org` is `5.5.0-SNAPSHOT`
### Download
```sh
mkdir -p /tmp/kevoree
cd /tmp/kevoree
wget https://oss.sonatype.org/service/local/repositories/snapshots/content/org/kevoree/org.kevoree.tools.runtime/5.5.0-SNAPSHOT/org.kevoree.tools.runtime-5.5.0-20170526.084607-1.jar -O kevoree.jar
```
### Run
```sh
java -jar kevoree.jar
```
:warning: Note that you need to make your `.kevoree/config.json` point to the new Kevoree registry available at `https://new-registry.kevoree.org` for dev version **5.5.0-SNAPSHOT**

## Usage with Maven

### POM file inheriting
If you want to create your own Kevoree component, node, group or channel you can use the **starter-parent** artefact:

```xml
<parent>
  <groupId>org.kevoree.library</groupId>
  <artifactId>starter-parent</artifactId>
  <version>KEVOREE_VERSION</version>
</parent>
```

### Manual dependencies management
You can also create your own **pom.xml** from scratch without using any `<parent>` like so:
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>your.group.id</groupId>
	<artifactId>my-kevoree-comp</artifactId>
	<version>1.0.0-SNAPSHOT</version>
	<name>YourGroupId :: MyKevoreeComp</name>

	<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<java.version>1.8</java.version>
		<maven.compiler.source>${java.version}</maven.compiler.source>
		<maven.compiler.target>${java.version}</maven.compiler.target>
		<kevoree.version>KEVOREE_VERSION</kevoree.version>
		<kevoree.registry.namespace>yournamespace</kevoree.registry.namespace>
		<kevoree.registry.url>https://registry.kevoree.org</kevoree.registry.url>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.kevoree</groupId>
			<artifactId>org.kevoree.api</artifactId>
			<version>${kevoree.version}</version>
			<scope>provided</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
			</plugin>
			<!-- Kevoree plugins -->
			<plugin>
				<groupId>org.kevoree</groupId>
				<artifactId>org.kevoree.tools.mavenplugin</artifactId>
				<version>${kevoree.version}</version>
				<executions>
					<execution>
						<goals>
							<goal>generate</goal>
							<goal>deploy</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<registry>${kevoree.registry.url}</registry>
					<namespace>${kevoree.registry.namespace}</namespace>
					<kevscript>${env.KEVS}</kevscript>
				</configuration>
			</plugin>
		</plugins>
	</build>

</project>
```

## 2. Standard Library documentation

[> Standard libraries repository](https://github.com/kevoree/kevoree-library)
