package org.kevoree.tools.annotation.mavenplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.kevoree.bootstrap.Bootstrap;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/11/2013
 * Time: 11:13
 *
 * @author Francois Fouquet
 * @version 1.0
 * @goal run
 * @requiresDependencyResolution compile+runtime
 */
public class KevRunnerMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project.basedir}/src/main/kevs/main.kevs"
     */
    private File model;

    /**
     * @parameter default-value="node0"
     */
    private String nodename;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            this.getClass().getClassLoader().loadClass("org.kevoree.resolver.util.AsyncVersionResolver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Bootstrap bootstrap = new Bootstrap(nodename);
        try {
            bootstrap.bootstrapFromKevScript(new FileInputStream(model));
        } catch (Exception e) {
            getLog().error(e);
            System.setSecurityManager(null);
            throw new MojoExecutionException("Error while parsing bootstrap KevScript ", e);
        }

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
