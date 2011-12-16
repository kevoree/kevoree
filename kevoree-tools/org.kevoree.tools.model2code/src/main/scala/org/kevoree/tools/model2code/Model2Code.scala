/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.model2code

import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.net.URI

import org.kevoree._

/**
 * @author Gregory NAIN
 */
class Model2Code extends CompilationUnitHelpers {

  /**
   * Synchronizes or generates for all TypeDefinition in the given model. Overrides original files.
   */
  def modelToCode(model: ContainerRoot, srcRoot: URI) {
    model.getTypeDefinitions.foreach {
      typeDefninition =>
        modelToCode(model, typeDefninition, srcRoot)
    }
  }

  /**
   * Synchronizes or generates for all TypeDefinition of the give DeployUnit. Overrides original files.
   */
  def modelToCode(model: ContainerRoot, srcRoot: URI, du:DeployUnit) {
    //selects only the TypeDefnitions in the giver deployUnit
    model.getTypeDefinitions.filter(td=>td.getDeployUnits.exists(tdDu=>tdDu==du)).foreach {
      typeDefninition =>
        modelToCode(model, typeDefninition, srcRoot)
    }
  }

  /**
   * Synchronizes or generates for all TypeDefinition in the given model. Does NOT override original files.
   */
  def modelToCode(model: ContainerRoot, srcRoot: URI, targetRoot: URI) {
    model.getTypeDefinitions.foreach {
      typeDefninition =>
        modelToCode(model, typeDefninition, srcRoot, targetRoot)
    }
  }


  /**
   * Synchronizes or generates the given TypeDefinition in the given model. Overrides original files.
   */
  def modelToCode(model: ContainerRoot, typeDef: TypeDefinition, srcRoot: URI) {
    modelToCode(model, typeDef, srcRoot, srcRoot)
  }

  /**
   * Synchronizes or generates the given TypeDefinition in the given model. Does NOT override original files.
   */
  def modelToCode(model: ContainerRoot, typeDef: TypeDefinition, srcRoot: URI, targetRoot: URI) {

    if (typeDef.getBean == null || typeDef.getBean.equals("")) {
      System.err.println("Can not generate code of TypeDefinition with no bean attribute. " + typeDef.getName + " ignored.")
    } else if(!typeDef.getBean.contains(".")) {
      System.err.println("Can not generate code of TypeDefinition if the bean is out of any package (bean:"+typeDef.getBean+"). " + typeDef.getName + " ignored.")
    } else {

      val fileSrcLocation = srcRoot.toString + typeDef.getBean.replace(".", "/").concat(".java")
      val fileSrcLocationUri = new URI(fileSrcLocation)

      val fileTargetLocation = targetRoot.toString + typeDef.getBean.replace(".", "/").concat(".java")
      val fileTargetLocationUri = new URI(fileTargetLocation)

      //Load Java File
      val compilationUnit = compilationUnitLoader(fileSrcLocationUri)

      if (compilationUnit != null) {
        typeDef match {
          case componentType: ComponentType => {
            val ctw = new ComponentTypeWorker(model, componentType, compilationUnit)
            ctw.synchronize()
          }
          case channelType: ChannelType => {
            val ctw = new ChannelTypeWorker(model, channelType, compilationUnit)
            ctw.synchronize()
          }
          case nodeType: NodeType => {
            val ntw = new NodeTypeWorker(model, nodeType, compilationUnit)
            ntw.synchronize()
          }
          case groupType: GroupType => {
            val gtw = new GroupTypeWorker(model, groupType, compilationUnit)
            gtw.synchronize()
          }
          case _ => throw new UnsupportedOperationException("Synchronization of " + typeDef.getClass.getName + " is not available.")
        }

        //Save CU
        compilationUnitWriter(compilationUnit, fileTargetLocationUri)

      }
    }
  }

  def modelToDeployUnit(model: ContainerRoot, deployUnitRoot: URI , deployUnit : DeployUnit) {

    val srcLocation = deployUnitRoot.toString + "/src/main/java/"
    val srcLocationUri = new URI(srcLocation)
    val srcFolder = new File(srcLocationUri)
    if (!srcFolder.exists) {
      srcFolder.mkdirs
    }

    System.out.print("Generating DeployUnit...")
    generatePom(model, deployUnitRoot,deployUnit)
    System.out.println("Done.")
    System.out.println("Generating Code...")
    modelToCode(model, srcLocationUri, deployUnit)
  }

  def generatePom(model: ContainerRoot, deployUnitRoot: URI , deployUnit : DeployUnit) {
    val pomLocation = deployUnitRoot.toString + "/pom.xml"
    val pomLocationUri = new URI(pomLocation)
    val pomFile = new File(pomLocationUri)
    if (!pomFile.exists) {
      pomFile.createNewFile
    }
    val pr = new PrintWriter(new FileOutputStream(pomFile))
    pr.println("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">")
    pr.println("");
    pr.println("  <modelVersion>4.0.0</modelVersion>")
    pr.println("  <groupId>" + deployUnit.getGroupName + "</groupId>")
    pr.println("  <artifactId>" + deployUnit.getUnitName + "</artifactId>")
    pr.println("  <version>" + deployUnit.getVersion + "</version>")
    pr.println("  <name>" + deployUnit.getUnitName + " Generated by Kevoree</name>")
    pr.println("  <packaging>bundle</packaging>")
    generateDependencies(model, pr)
    generateBuild(model, pr)
    generateRepositories(model, pr)
    pr.println("</project>")
    pr.flush
    pr.close

  }

  private def generateDependencies(model: ContainerRoot, pr: PrintWriter) {
    pr.println("")
    pr.println("  <dependencies>")
    pr.println("    <dependency>")
    pr.println("      <groupId>org.kevoree.tools</groupId>")
    pr.println("      <artifactId>org.kevoree.tools.annotation.api</artifactId>")
    pr.println("      <version>1.5.0-SNAPSHOT</version>")
    pr.println("      <scope>compile</scope>")
    pr.println("    </dependency>")
    pr.println("    <dependency>")
    pr.println("      <groupId>org.kevoree</groupId>")
    pr.println("      <artifactId>org.kevoree.framework</artifactId>")
    pr.println("      <version>1.5.0-SNAPSHOT</version>")
    pr.println("      <scope>compile</scope>")
    pr.println("    </dependency>")
    model.getDeployUnits.filter({
      du => du.getUrl != null && !du.getUrl.equals("")
    }).foreach {
      du =>
        val duVals = du.getUrl.split(":").last.split("/").toArray

        pr.println("    <dependency>")
        pr.println("      <groupId>" + duVals(0) + "</groupId>")
        pr.println("      <artifactId>" + duVals(1) + "</artifactId>")
        if (duVals(2) != null) {
          pr.println("      <version>" + duVals(2) + "</version>")
        }
        pr.println("      <scope>provided</scope>")
        pr.println("    </dependency>")
    }

    pr.println("  </dependencies>")
    pr.println("")
  }

  private def generateRepositories(model: ContainerRoot, pr: PrintWriter) {
    pr.println("")
    pr.println("  <repositories>")
    model.getRepositories.foreach {
      repo =>
        pr.println("    <repository>")
        pr.println("      <id>" + repo.getUrl + "</id>")
        pr.println("      <name>" + repo.getUrl + "</name>")
        pr.println("      <url>" + repo.getUrl + "</url>")
        pr.println("      <releases><enabled>true</enabled></releases>")
        pr.println("      <snapshots><enabled>true</enabled></snapshots>")
        pr.println("    </repository>")
    }
    pr.println("  </repositories> ")
    pr.println("")
    pr.println("  <pluginRepositories>")
    pr.println("    <pluginRepository>")
    pr.println("      <id>kevoree-plugin-release</id>")
    pr.println("      <url>http://maven.kevoree.org/release</url>")
    pr.println("    </pluginRepository>")
    pr.println("  </pluginRepositories>")
    pr.println("")
  }

  private def generateBuild(model: ContainerRoot, pr: PrintWriter) {
    pr.println("")
    pr.println("  <build>")
    pr.println("    <plugins>")
    pr.println("      <plugin>")
    pr.println("        <groupId>org.kevoree.tools</groupId>")
    pr.println("        <artifactId>org.kevoree.tools.annotation.mavenplugin</artifactId>")
    pr.println("        <version>1.5.0-SNAPSHOT</version>")
    pr.println("        <extensions>true</extensions>")
    pr.println("        <configuration>")
    pr.println("          <nodeTypeNames>JavaSENode</nodeTypeNames>")
    pr.println("        </configuration>")
    pr.println("        <executions>")
    pr.println("          <execution>")
    pr.println("            <goals>")
    pr.println("              <goal>generate</goal>")
    pr.println("              <goal>compile</goal>")
    pr.println("            </goals>")
    pr.println("          </execution>")
    pr.println("        </executions>")
    pr.println("      </plugin>")
    pr.println("      <plugin>")
    pr.println("        <groupId>org.apache.felix</groupId>")
    pr.println("        <artifactId>maven-bundle-plugin</artifactId>")
    pr.println("        <version>2.3.5</version>")
    pr.println("        <extensions>true</extensions>")
    pr.println("      </plugin>")
    pr.println("      <plugin>")
    pr.println("        <groupId>org.apache.maven.plugins</groupId>")
    pr.println("        <artifactId>maven-compiler-plugin</artifactId>")
    pr.println("        <version>2.3.2</version>")
    pr.println("        <configuration>")
    pr.println("          <source>1.6</source>")
    pr.println("          <target>1.6</target>")
    pr.println("          <encoding>${project.build.sourceEncoding}</encoding>")
    pr.println("        </configuration>")
    pr.println("      </plugin>")
    pr.println("    </plugins>")
    pr.println("  </build>")
    pr.println("")
  }

}
