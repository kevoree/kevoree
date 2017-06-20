/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.mavenplugin;

import de.vandermeer.asciitable.AsciiTable;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.kevoree.*;
import org.kevoree.annotation.*;
import org.kevoree.annotation.ChannelType;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.NodeType;
import org.kevoree.api.Port;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.modeling.api.json.JSONModelSerializer;
import org.kevoree.reflect.ReflectUtils;
import org.kevoree.tools.mavenplugin.util.ModelBuilderHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class KevGenerateMojo extends AbstractMojo {

	@Parameter(required = true, readonly = true, defaultValue = "${project}")
	public MavenProject project;

	@Parameter(defaultValue = "${project.build.directory}/classes", required = true)
	protected File modelOutputDirectory;
	
	@Parameter(required = true)
	private String namespace = null;

	@Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
	private File sourcesDir;

	private KevoreeFactory factory = new DefaultKevoreeFactory();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (project.getArtifact().getType().equals("jar")) {
			try {
				ContainerRoot model = factory.createContainerRoot().withGenerated_KMF_ID("0.0");
				factory.root(model);
				org.kevoree.Package pkg = createPackage();
				model.addPackages(pkg);
				getLog().info("Namespace:    " + namespace);

				DeployUnit du = createDeployUnit();
				pkg.addDeployUnits(du);
				getLog().info("DeployUnit:   " + du.getName() + ":" + du.getVersion());
				getLog().info("");

				for (Class<?> tdefClass : findTypeDefinitionClasses()) {
					TypeDefinition tdef = createTypeDefinition(tdefClass);
					getLog().info("TypeDefinition");
					getLog().info("      name         " + tdef.getName());
					getLog().info("      version      " + tdef.getVersion());
					getLog().info("      description  " + printDesc(tdef));
					getLog().info("");

					AsciiTable at = new AsciiTable();
					at.addRule();
					at.addRow("Param", "DataType", "Default value", "Optional?", "Fragmented?");
					DictionaryType dic = tdef.getDictionaryType();
					for (DictionaryAttribute attr : dic.getAttributes()) {
						at.addRule();
						at.addRow(attr.getName(), attr.getDatatype(), attr.getDefaultValue(), attr.getOptional() ? "✔":"✘", attr.getFragmentDependant() ? "✔":"✘");
					}
					at.addRule();
					for (String logLine : at.renderAsArray()) {
						getLog().info("  " + logLine);
					}

					if (tdef instanceof org.kevoree.ComponentType) {
						List<PortTypeRef> inputs = createInputPortTypes(tdefClass);
						((org.kevoree.ComponentType) tdef).addAllProvided(inputs);
						printPorts("Inputs", inputs);

						List<PortTypeRef> outputs = createOutputPortTypes(tdefClass);
						((org.kevoree.ComponentType) tdef).addAllRequired(outputs);
						printPorts("Outputs", outputs);
					}

					// add DeployUnit to tdef
					tdef.addDeployUnits(du);
					// add this tdef class to du
					Value classFilter = (Value) factory.createValue().withName("class:" + tdef.getName() + ":" + tdef.getVersion());
					classFilter.setValue(tdefClass.getName());
					du.addFilters(classFilter);

					pkg.addTypeDefinitions(tdef);
					getLog().info("");
				}

				serializeModel(model);
				getLog().debug("Kevoree Model Generator - Done");
			} catch (Exception e) {
				throw new MojoExecutionException("Unable to generate a Kevoree model from the compiled project", e);
			}
		}
	}

	private org.kevoree.Package createPackage() {
		return (org.kevoree.Package) factory.createPackage().withName(namespace);
	}
	
	private DeployUnit createDeployUnit() {
        DeployUnit du = factory.createDeployUnit();
        du.setName(project.getArtifact().getArtifactId());
        String hashcode = ModelBuilderHelper.createKey(namespace, project.getArtifactId(), project.getVersion(), null);
        du.setHashcode(hashcode);
        du.setVersion(project.getArtifact().getBaseVersion());

        Value platform = factory.createValue();
        platform.setName("platform");
        platform.setValue("java");
        du.addFilters(platform);

        Value kevoreeVersion = factory.createValue();
        kevoreeVersion.setName("kevoree_version");
        kevoreeVersion.setValue(factory.getVersion());
        du.addFilters(kevoreeVersion);

		Value timestamp = factory.createValue();
		timestamp.setName("timestamp");
		timestamp.setValue(String.valueOf(System.currentTimeMillis()));
		du.addFilters(timestamp);

        // add repositories
        for (Repository repo : project.getRepositories()) {
            Value repoVal = factory.createValue();
            repoVal.setName("repo_" + repo.getId());
            repoVal.setValue(repo.getUrl());
            du.addFilters(repoVal);
        }


        // set Maven groupId:artifactId:version
        du.setUrl(project.getArtifact().getGroupId() + ":" + du.getName() + ":" + du.getVersion());

        return du;
	}

	private TypeDefinition createTypeDefinition(Class<?> clazz) throws Exception {
		TypeDefinition tdef;
		Value desc = (Value) factory.createValue().withName("description");

		if (ReflectUtils.hasAnnotation(clazz, ComponentType.class)) {
			tdef =factory.createComponentType();
			ComponentType ca = ReflectUtils.findAnnotation(clazz, ComponentType.class);
			tdef.setVersion(String.valueOf(ca.version()));
			desc.setValue(ca.description());

		} else if (ReflectUtils.hasAnnotation(clazz, NodeType.class)) {
			tdef = factory.createNodeType();
			NodeType ca = ReflectUtils.findAnnotation(clazz, NodeType.class);
			tdef.setVersion(String.valueOf(ca.version()));
			desc.setValue(ca.description());

		} else if (ReflectUtils.hasAnnotation(clazz, GroupType.class)) {
			tdef = factory.createGroupType();
			GroupType ca = ReflectUtils.findAnnotation(clazz, GroupType.class);
			tdef.setVersion(String.valueOf(ca.version()));
			desc.setValue(ca.description());

		} else if (ReflectUtils.hasAnnotation(clazz, ChannelType.class)) {
			tdef = factory.createChannelType();
			ChannelType ca = ReflectUtils.findAnnotation(clazz, ChannelType.class);
			tdef.setVersion(String.valueOf(ca.version()));
			desc.setValue(ca.description());
		} else {
			// this should never happen (unless a new type is added and the check in findTypeDefinitionClass() visitor
			// is not updated accordingly)
			throw new Exception("Unable to find the TypeDefinition of "+clazz.getName()+" based on its annotation");
		}

		tdef.setName(clazz.getSimpleName());
		tdef.addMetaData(desc);

		tdef.setDictionaryType(createDictionaryType(clazz));

		return tdef;
	}

	private List<PortTypeRef> createOutputPortTypes(Class<?> clazz) {
		List<PortTypeRef> portTypeRefs = new ArrayList<>();
		for (Field f : clazz.getDeclaredFields()) {
			if (f.isAnnotationPresent(Output.class) && f.getType().equals(Port.class)) {
				PortTypeRef ref = createPortTypeRef(f.getName());
				ref.setName(f.getName());
				portTypeRefs.add(ref);
			}
		}
		return portTypeRefs;
	}

	private List<PortTypeRef> createInputPortTypes(Class<?> clazz) {
		List<PortTypeRef> portTypeRefs = new ArrayList<>();
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.isAnnotationPresent(Input.class)) {
				PortTypeRef ref = createPortTypeRef(m.getName());
				ref.setName(m.getName());
				portTypeRefs.add(ref);
			}
		}
		return portTypeRefs;
	}

	private PortTypeRef createPortTypeRef(String name) {
		PortTypeRef ref = factory.createPortTypeRef();
		PortType portType = factory.createPortType();
		portType.setName(name);
		ref.setRef(portType);
		return ref;
	}

	private DictionaryType createDictionaryType(Class<?> clazz) throws Exception {
		DictionaryType dic = factory.createDictionaryType().withGenerated_KMF_ID("0.0");
		List<Field> fields = ReflectUtils.getAllFieldsWithAnnotation(clazz, Param.class);
		if (!fields.isEmpty()) {
			for (Field field : fields) {
				try {
					DictionaryAttribute attr = createAttribute(clazz, field);
					if (attr != null) {
						dic.addAttributes(attr);
					}
				} catch (MojoExecutionException e) {
					throw new MojoExecutionException("Cannot create dictionary for "+clazz.getName(), e);
				}
			}
		}
		return dic;
	}

	private DictionaryAttribute createAttribute(Class<?> clazz, Field field) throws Exception {
		StringBuilder logAttr = new StringBuilder("  ");
		logAttr.append(field.getName());
		DictionaryAttribute attr = factory.createDictionaryAttribute();
		attr.setName(field.getName());

		if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
			attr.setDatatype(DataType.INT);
			String defaultVal = String.valueOf(getDefaultValue(clazz, field));
			attr.setDefaultValue(defaultVal);
			logAttr.append(": int");
			if (defaultVal != null) {
				logAttr.append(" = ");
				logAttr.append(defaultVal);
			}

		} else if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
			attr.setDatatype(DataType.DOUBLE);
			String defaultVal = String.valueOf(getDefaultValue(clazz, field));
			attr.setDefaultValue(defaultVal);
			logAttr.append(": double");
			if (defaultVal != null) {
				logAttr.append(" = ");
				logAttr.append(defaultVal);
			}

		} else if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
			attr.setDatatype(DataType.LONG);
			String defaultVal = String.valueOf(getDefaultValue(clazz, field));
			attr.setDefaultValue(defaultVal);
			logAttr.append(": long");
			if (defaultVal != null) {
				logAttr.append(" = ");
				logAttr.append(defaultVal);
			}

		} else if (field.getType().equals(Float.class) || field.getType().equals(float.class)) {
			attr.setDatatype(DataType.FLOAT);
			String defaultVal = String.valueOf(getDefaultValue(clazz, field));
			attr.setDefaultValue(defaultVal);
			logAttr.append(": float");
			if (defaultVal != null) {
				logAttr.append(" = ");
				logAttr.append(defaultVal);
			}

		} else if (field.getType().equals(Short.class) || field.getType().equals(short.class)) {
			attr.setDatatype(DataType.SHORT);
			String defaultVal = String.valueOf(getDefaultValue(clazz, field));
			attr.setDefaultValue(defaultVal);
			logAttr.append(": short");
			if (defaultVal != null) {
				logAttr.append(" = ");
				logAttr.append(defaultVal);
			}

		} else if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
			attr.setDatatype(DataType.BOOLEAN);
			String defaultVal = String.valueOf(getDefaultValue(clazz, field));
			attr.setDefaultValue(defaultVal);
			logAttr.append(": boolean");
			if (defaultVal != null) {
				logAttr.append(" = ");
				logAttr.append(defaultVal);
			}

		} else if (field.getType().equals(String.class)) {
			attr.setDatatype(DataType.STRING);
			String defaultVal = String.valueOf(getDefaultValue(clazz, field));
			attr.setDefaultValue(defaultVal);
			logAttr.append(": string");
			if (defaultVal != null) {
				logAttr.append(" = ");
				logAttr.append(defaultVal);
			}

		} else if (field.getType().equals(Character.class)) {
			attr.setDatatype(DataType.CHAR);
			String defaultVal = String.valueOf(getDefaultValue(clazz, field));
			attr.setDefaultValue(defaultVal);
			logAttr.append(": char");
			if (defaultVal != null) {
				logAttr.append(" = ");
				logAttr.append(defaultVal);
			}

		} else {
			throw new MojoExecutionException("Unknown type \""+field.getType().toString()+"\" for @Param "+field.getName());
		}

		Param p = field.getAnnotation(Param.class);
		attr.setFragmentDependant(p.fragmentDependent());
		attr.setOptional(p.optional());
		return attr;
	}

	private void serializeModel(ContainerRoot model) throws MojoExecutionException {
		JSONModelSerializer saver = factory.createJSONSerializer();
		Path path = Paths.get(modelOutputDirectory.getPath(), "KEV-INF", "kevlib.json");
		File localModel = new File(path.toString());

		try {
			localModel.getParentFile().mkdirs();
			localModel.createNewFile();
			FileOutputStream fos = new FileOutputStream(localModel);
			saver.serializeToStream(model, fos);
			fos.flush();
			fos.close();
			getLog().info("Model saved at " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(path));
		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException("Unable to write model at "
					+ Paths.get(project.getBasedir().getAbsolutePath()).relativize(path));
		}
	}

	private Set<Class<?>> findTypeDefinitionClasses() throws Exception {
		if (sourcesDir.exists()) {
			final Set<Class<?>> tdefs = new HashSet<>();
			final Set<URL> urls = new HashSet<>();
			for (Object elem: project.getCompileClasspathElements()) {
				try {
					urls.add(new File((String) elem).toURI().toURL());
				} catch (Exception ignore) {}
			}
			final ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[] {}), Thread.currentThread().getContextClassLoader());

			Files.walkFileTree(sourcesDir.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String filePath = file.toString();
					String fqn = filePath.substring(sourcesDir.toString().length()+1, filePath.length() - ".class".length())
							.replaceAll("/", ".");
					Class<?> clazz;
					try {
						clazz = classLoader.loadClass(fqn);
						if (ReflectUtils.hasAnnotation(clazz, org.kevoree.annotation.ComponentType.class, NodeType.class, GroupType.class, ChannelType.class)) {
							tdefs.add(clazz);
						}
					} catch (ClassNotFoundException ignore) {}
					return super.visitFile(file, attrs);
				}
			});

			if (tdefs.isEmpty()) {
				throw new MojoExecutionException("No TypeDefinition found in your project");
			} else {
				return tdefs;
			}
		} else {
			throw new MojoExecutionException("Empty directory: "+ sourcesDir);
		}
	}

	private Object getDefaultValue(Class<?> clazz, Field field) throws Exception {
		Object o = clazz.newInstance();
		field.setAccessible(true);
		return field.get(o);
	}

	private String printDesc(TypeDefinition tdef) {
		String desc = tdef.findMetaDataByID("description").getValue();
		if (desc.isEmpty()) {
			return "<none>";
		} else {
			if (desc.length() > 50) {
				return desc.substring(0, 50) + "...";
			} else {
				return desc;
			}
		}
	}

	private void printPorts(String type, List<PortTypeRef> ports) {
		List<String> strPorts = ports.stream().map(p -> p.getName()).collect(Collectors.toList());
		if (strPorts.isEmpty()) {
			strPorts.add("<none>");
		}

		AsciiTable at = new AsciiTable();
		at.addRule();
		at.addRow(type, strPorts);
		at.addRule();
		for (String logLine : at.renderAsArray()) {
			getLog().info("  " + logLine);
		}
	}
}