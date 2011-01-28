/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kermeta.emf.genmodel;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Generates files based on grammar files with Antlr tool.
 *
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @author <a href="mailto:ffouquet@irisa.fr">Fouquet François</a>
 * @version $Id$
 */
public class GenmodelPlugin extends AbstractMojo {

    /**
     * Ecore file
     *
     * @parameter
     */
    private File ecore;
    /**
     * Source base directory
     *
     * @parameter
     */
    private File output;
    
    /**
     * Genmodel file
     *
     * @parameter
     */
    private File genmodel;


    /**
     * Clear output dir
     *
     * @parameter
     */
    private Boolean clearOutput=true;


    @Override
    public void execute() throws MojoExecutionException {
        

        Util.createGenModel(ecore, genmodel, output, getLog(),clearOutput);
    }
}
