package org.kevoree.library.frascati.mavenplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 30/01/12
 * Time: 17:33
 */

/**
 * @author ffouquet
 * @author <a href="mailto:ffouquet@irisa.fr">Fouquet François</a>
 * @version $Id$
 * @goal compile
 * @phase process-classes
 * @requiresDependencyResolution compile
 */
public class KevoreeFrascatiMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     *
     * @parameter default-value="${project.basedir}"
     */
    private File basedir;

    /**
     *
     * @parameter default-value="${project.basedir}/src/main/resources"
     */
    private File resources;


    public void execute() throws MojoExecutionException, MojoFailureException {



    }
}
