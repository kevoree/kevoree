/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
