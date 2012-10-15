<project>

       <modelVersion>4.0.0</modelVersion>
         <groupId>$groupId$</groupId>
        <artifactId>$artifactId$</artifactId>
        <name>$NAME$-profile</name>
        <packaging>pom</packaging>

        <parent>
            <groupId>$groupId$</groupId>
            <artifactId>$artifactId_parent$</artifactId>
            <version>$version_parent$</version>
       </parent>

    <profiles>
        <profile>
            <id>nix32</id>
            <activation>
                <os>
                    <family>unix</family>
                    <name>Linux</name>
                    <arch>i386</arch>
                </os>
            </activation>
            <modules>
                <module>nix32</module>
            </modules>
        </profile>

        <profile>
            <id>nix64</id>
            <activation>
                <os>
                    <family>unix</family>
                    <name>Linux</name>
                    <arch>x64</arch>
                </os>
            </activation>
            <modules>
                <module>nix64</module>
            </modules>
        </profile>
        <profile>
            <id>osx</id>
            <activation>
                <os>
                    <family>mac</family>
                </os>
            </activation>

            <modules>
                <module>osx</module>
            </modules>
        </profile>

    </profiles>

</project>
