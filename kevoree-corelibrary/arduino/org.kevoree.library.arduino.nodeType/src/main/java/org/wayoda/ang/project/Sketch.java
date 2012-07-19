/**
 * This work is dreived from the Processing project - http://processing.org
 * Copyright (c) 2004-05 Ben Fry and Casey Reas
 * Copyright (c) 2001-04 Massachusetts Institute of Technology
 *
 * Copyright (c) 2009 Eberhard Fahle
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.wayoda.ang.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.kevoree.api.Bootstraper;
import org.wayoda.ang.libraries.CodeManager;
import org.wayoda.ang.libraries.Library;
import org.wayoda.ang.utils.FileSelector;
import org.wayoda.ang.utils.FileUtils;

/**
 * An instance of class Sketch has all the information
 * about the users source code files that belong to a project.
 * Sketches are rather simple animals.
 * All files belonging to a sketch live in the same directory.
 * The name of the directory matches the name of the sketch.
 * Besides the main sketch file itself,
 * four types of files are allowed they have the extensions
 * *.pde, *.h, *.c  *·cpp
 * Before starting the compilation an existing build-directory is clean and a new one
 * is created in which and all processing for a specific target happens.
 */
public class Sketch {

    /**
     * The environment in which our sketch lives
     */
    private ArduinoBuildEnvironment env;
    /**
     * The name of sketch
     */
    private String sketchName;
    /**
     * The full path to the directory where the sketch itself lives
     */
    private File sketchRoot;
    /**
     * The list of libraries that are used by this Sketch
     */
    private ArrayList<Library> libs = new ArrayList<Library>();

    /**
     * Creates a new Sketch from the contents of existing
     * directory. We exactly know how a (valid) Sketch has to be organized.
     * The easy test made in this constructor is to check wether the directory
     * argument really contains the sources for a Sketch of the same name.
     *
     * @param path The path that is supposed to be either the root of
     *             a Sketch directory or the path to the main pde-file of the sketch.
     * @throws FileNotFoundException if the path argument does not exist
     *                               or does not look like the home of Sketch.
     */
    public Sketch(File path) throws FileNotFoundException {

        //We accept a relative path argument too, so we  have to 
        //check wether the path can be turned into an canonical file arg
        File sketchPath = null;
        try {
            sketchPath = path.getCanonicalFile();
        } catch (Exception edit) {
            throw new FileNotFoundException(path.toString() + " is not a valid sketch");
        }
        FileSelector.SketchFilter sf = new FileSelector.SketchFilter();
        if (!sf.accept(sketchPath)) {
            //not accepted as a Sketch
            throw new FileNotFoundException(sketchPath.toString() + " is not a valid sketch");
        }
        //we have a valid sketch
        if (sketchPath.isFile()) {
            /*
              If we were called with a .pde in the current working dir,
              the file-object returns null for the getParentFile().
              We have to construct the absolute pathz first.
            */
            sketchRoot = sketchPath.getParentFile();
        } else {
            /* if this is a directroy we don't have to do anything with the path */
            sketchRoot = sketchPath;
        }
        sketchName = sketchRoot.getName();
        env = ArduinoBuildEnvironment.getInstance();
    }

    /**
     * Gets the name of the Sketch.
     *
     * @return String the name of the Sketch
     */
    public String getName() {

        return sketchName;
    }

    public String getBuildRootPath(Target target) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(TargetDirectoryService.getTargetDirectoryPath());
        stringBuffer.append(File.separator);
        stringBuffer.append(target.getKey());
        return stringBuffer.toString();
    }

    /**
     * Gets the path to the root of the build directory.
     * If the sources have not been successfully prepared yet
     * this will return null. So you get a valid path only when
     * the Sketch is ready to be build.
     *
     * @return File The path to the build directroy.
     */
    public File getBuildRoot(Target target) {

        File f = new File(getBuildRootPath(target));
        if (f != null && f.exists() && f.canWrite() && f.canRead())
            return f;
        return null;
    }

    /**
     * Gets the path to the root of the build directory
     * for the core of a specific target.
     * If the sources have not been successfully prepared yet
     * this will return null. So you get a valid path only when
     * the Sketch is ready to be build.
     *
     * @return File The path to the build directroy.
     */
    public File getCoreBuildRoot(Target target) {

        File br = getBuildRoot(target);
        if (br != null) {
            File f = new File(br, "core");
            if (f != null && f.exists() && f.canWrite() && f.canRead())
                return f;
        }
        return null;
    }

    /**
     * Gets the path to the root of the build directory for a specific target
     * and library.
     * If the sources have not been successfully prepared yet
     * this will return null. So you get a valid path only when
     * the Sketch is ready to be build.
     *
     * @return File The path to the build directroy.
     */
    public File getLibraryBuildRoot(Target target, Library lib) {

        File br = getBuildRoot(target);
        if (br != null) {
            File f = new File(br, "libraries" + File.separator + lib.getName());
            if (f != null && f.exists() && f.canWrite() && f.canRead())
                return f;
        }
        return null;
    }

    /**
     * Gets the compiled and linked hex-file for a target.
     *
     * @param target the target for which the files are requested
     * @return File The file with the code to be uploaded
     *         to a board. If there are no code-files for the target returns null.
     */
    public File getFlash(Target target) {

        File f = new File(getBuildRoot(target), sketchName + ".hex");
        if (f != null && f.exists() && f.canRead() && f.isFile()) {
            return f;
        }
        return null;
    }

    public String getPath(Target target){
        return  ""+getBuildRoot(target)+"/"+ sketchName + ".hex";
    }

    /**
     * Preprocesses the sources of the Sketch and creates the build-directories
     * needed for running the sketch through the compiler.
     *
     * @param target the target for which this Sketch is to be preprocessed.
     * @return boolean true if the sketch was successfully processed.
     */
    public void preprocess(Target target) {

        if (target == null) {
            System.err.println("Cnannot preprocess for Target `null`");
            throw new IllegalStateException();
        }
        //process the pde-files
        String pdeCode = processPde(target);
        if (pdeCode == null) {
            throw new IllegalStateException();
        }
        //create all the directories we need to build the sketch
        if (!createBuildDirectories(target)) {
            throw new IllegalStateException();
        }
        //copy all C/C++ files in the sketch into the build directory
        if (!copyExtraSources(target)) {
            throw new IllegalStateException();
        }
        //copy the preprocessed pde-code into the build dir
        if (!copyPde(pdeCode, target)) {
            throw new IllegalStateException();
        }
    }

    /**
     * Gets a list of the libraries that are required
     * to build this Sketch.
     *
     * @return ArrayList<Libaray> a list of all the
     *         libraries that are referenced in this Sketch (which can be empty).
     *         Returns null if the Sketch is not configured for a target or
     *         if an error was raised during the configuration.
     */
    public ArrayList<Library> getLibraries() {

        return libs;
    }

    /**
     * Gets a list of all the sourcefiles that need to be compiled
     * for this Sketch. The files returned here are the ones located
     * in the build directory of the sketch. So you will have to call
     * configure(target) for this Sketch before using this method.
     *
     * @return ArrayList<File> the list of source File objects in the build
     *         directory. The list is empty if the Skecth has not been configured
     *         for a target before calling this method.
     */
    public ArrayList<File> getSourceFiles(Target target) {

        return FileUtils.getFiles(getBuildRoot(target), new FileSelector.SourceFileFilter());
    }

    /**
     * Gets a list of all the object-files that have been compiled
     * from the users Sketch-code.
     *
     * @return ArrayList<File> the list of object files in the build
     *         directory. The list is empty if the Skecth has not been configured
     *         or compiled for a target before calling this method.
     */
    public ArrayList<File> getObjectFiles(Target target) {

        return FileUtils.getFiles(getBuildRoot(target), new FileSelector.ObjectFileFilter());
    }

    public ArrayList<File> getLibraryObjectFiles(Target target, Library lib) {

        return FileUtils.getFiles(getLibraryBuildRoot(target, lib), new FileSelector.ObjectFileFilter());
    }

    /**
     * Creates the directory-tree where the Sketch is going to be build.
     *
     * @return boolean True if the directories where created, false otherwise.
     */
    private boolean createBuildDirectories(Target target) {

        //the root of all build dirs
        File buildRoot = new File(getBuildRootPath(target));
        //If there are remains from a previous build delete the whole tree 
        if (buildRoot.exists()) {
            if (!FileUtils.deleteTree(buildRoot)) {
                //unable to clean out the buildRoot for the Target.
                System.err.println("Unable to clean build directory `" + buildRoot.getPath() + "`");
                return false;
            }
        }
        //create the root of the target build-dir
        if (!buildRoot.mkdirs()) {
            //unable to create the buildRoot for the Target.
            System.err.println("Unable to create build directory `" + buildRoot.getPath() + "`");
            buildRoot = null;
            return false;
        }
        System.out.println("Build directory created `" + buildRoot.getPath() + "`");
        //the dir where the core archive is build
        File coreRoot = new File(buildRoot, "core");
        if (!coreRoot.mkdirs()) {
            //unable to create the coreRoot for the Target.
            System.err.println("Unable to create core build directory `" + coreRoot.getPath() + "`");
            coreRoot = null;
            return false;
        }
        System.out.println("Core build directory created `" + coreRoot.getPath() + "`");
        //the dir where the libraries are build
        File libRoot = new File(buildRoot, "libraries");
        if (!libRoot.mkdirs()) {
            //unable to create the libRoot for the Target.
            System.err.println("Unable to create library build root directory `" + libRoot.getPath() + "`");
            libRoot = null;
            return false;
        }
        for (Library l : libs) {
            File ldir = new File(libRoot, l.getName());
            if (!ldir.mkdirs()) {
                //unable to create the library build dir.
                System.err.println("Unable to create library build directory `" + ldir.getPath() + "`");
                //   return false;
            }
            System.out.println("Library build directory created `" + ldir.getPath() + "`");
        }
        return true;
    }

    /**
     * Copies all "normal" C or C++  sources to the build directory.
     * These files will be compiled independently of the users pde-sources
     * and linked into the resulting program to be uploaded.
     *
     * @return boolean True if all files are in place
     */
    private boolean copyExtraSources(Target target) {

        File buildRoot = getBuildRoot(target);
        for (File src : FileUtils.getFiles(sketchRoot, new FileSelector.AllSourceFilter())) {
            File dest = new File(buildRoot, src.getName());
            try {
                FileUtils.copyFile(src, dest);
                System.out.println("Copied file " + dest.getPath());
            } catch (IOException ioe) {
                System.err.println("Error copying source files");
                ioe.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean copyPde(String pdeCode, Target target) {

        try {
            /* Save the source in the target-build dir */
            File outFile = new File(getBuildRoot(target), sketchName + ".cpp");
            BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
            out.write(pdeCode.toString());
            out.close();
            System.out.println("Sketch source written to `" + outFile.getPath() + "`");
        } catch (Exception e) {
            System.err.println("Error writing Sketch source");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Copys the code from all pde-files in the build-dir into a single
     * String, starting with the main pde-file (the one that has the same name
     * as the sketch).
     * After that all files that have a name ending with either *.pde or
     * files that carry no extension.
     *
     * @return String the source with the full sourcecode of the sketch.
     *         Returns null if an error was encountered during processing.
     */
    private String processPde(Target target) {

        //merge all pde-files into a single string
        String code = concatPde();
        if (code == null)
            return null;
        //delete all comments from the source
        code = scrubComments(code);
        //replace non-ascii chars with unicode escaped constants
        code = replaceUnicode(code);
        //build the list of included libraries 
        libs = processIncludes(code);
        //generate the prototypes for the functions in the pde
        code = processPrototypes(code);
        //add some stuff we want to show up at the top of the source
        code = addPdeHeader(code);
        //add the main-function template for the target.
        code = addMain(code, target);
        return code;
    }

    /**
     * Concatenates all pde-files into a single string starting
     * with the main pde-file (the one that gives the sketch its name.
     *
     * @return String the concatenated files or null if there was an error
     *         merging the files.
     */
    private String concatPde() {

        BufferedReader in = null;
        String sourceLine;
        File mainPde = null;

        String newLine = System.getProperty("line.separator");

        ArrayList<File> fl = FileUtils.getFiles(sketchRoot, new FileSelector.PdeFilter());
        /* look for the main pde-file */
        for (File f : fl) {
            if (f.getName().equals(sketchName + ".pde")) {
                mainPde = f;
                fl.remove(f);
                break;
            }
        }
        if (mainPde == null) {
            System.err.println("Main pde file `" + sketchName + ".pde` for Sketch " + sketchName + " does not exist");
            return null;
        }
        //copy all pde-sources into a single string
        StringBuilder source = new StringBuilder();
        try {
            in = new BufferedReader(new FileReader(mainPde));
            while ((sourceLine = in.readLine()) != null) {
                source.append(sourceLine + newLine);
            }
            in.close();
            source.append("\n");
            for (File src : fl) {
                in = new BufferedReader(new FileReader(src));
                while ((sourceLine = in.readLine()) != null) {
                    source.append(sourceLine + newLine);
                }
                in.close();
                source.append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error concatenating pde-sources");
            e.printStackTrace();
            return null;
        }
        return source.toString();
    }

    /**
     * Searches for and removes all comments from a users pde source.
     * If all comments are syntactically correct the will simply
     * be removed. If we find any underterminated commments
     * we throw a IllegalArgumentException.
     *
     * @param source the contents of the pde source
     * @return String the content with the comments removed
     */
    private String scrubComments(String source) {

        StringBuilder sb = new StringBuilder();

        /* we will replace a few chars with unicode code points 
           from the private use section since it makes 
           parsing the source so much easier */
        String escapedQuote = "\uE000";
        String escapedSCStart = "\uE001";
        String escapedMCStart = "\uE002";
        String escapedMCEnd = "\uE003";
        //replace all escaped quotes inside static strings 
        source = source.replaceAll("\"", escapedQuote);
        //replace single line comment start '//' 
        source = source.replaceAll("//", escapedSCStart);
        //replace multiline line comment start '/\\*' 
        source = source.replaceAll("/\\*", escapedMCStart);
        //replace multiline line comment end'\\*/' 
        source = source.replaceAll("\\*/", escapedMCEnd);

        //true when we are inside a static String definition
        boolean quoted = false;
        //true when inside a singleline comment
        boolean sc = false;
        //true when inside a multiline comment
        boolean mc = false;
        int len = source.length();
        int srcIndex = 0;
        char c;
        while (srcIndex < len) {
            c = source.charAt(srcIndex);
            if (quoted) {
                //we are in a quote and dont care what we find
                sb.append(c);
                srcIndex++;
            } else if (c == '"') {
                //we found the beginning or end of a quote
                quoted = !quoted;
                sb.append(c);
                srcIndex++;
            } else if (c == '\uE001') {
                //found the start of a single line comment
                //not copied into the result
                sc = true;
                srcIndex++;
            } else if (c == '\n') {
                //found end of line 
                if (sc) {
                    //its the end of a comment 
                    sc = false;
                }
                //we keep all newlines even if the comment is gone
                sb.append(c);
                srcIndex++;
            } else if (c == '\uE002') {
                //start of multiline comment
                mc = true;
                srcIndex++;
            } else if (c == '\uE003') {
                //end of multiline comment
                if (!mc) {
                    //never saw the start of the comment
                    throw new IllegalArgumentException("no start for comment found");
                }
                mc = false;
                srcIndex++;
            } else {
                if (!(sc || mc)) {
                    //if we are not inside a comment
                    sb.append(c);
                }
                srcIndex++;
            }
        }
        String retval = sb.toString();
        /* undo the replacement of the quoted String char */
        retval = retval.replaceAll(escapedQuote, "\"");
        return retval;
    }

    /**
     * Searches for non ascii-chars and replaces
     * them with unicode constants.
     *
     * @param source the contents of the pde source
     * @return String the content with non-ascii chars turned into unicode
     */
    private String replaceUnicode(String source) {

        StringBuilder sb = new StringBuilder();
        int len = source.length();
        int srcIndex = 0;
        char c;
        while (srcIndex < len) {
            c = source.charAt(srcIndex);
            if (c > 127) {
                //we have some unicode here
                StringBuilder us = new StringBuilder("\\u0000");
                String hex = Integer.toString((int) c, 16);
                for (int i = hex.length() - 1; i >= 0; i--) {
                    us.setCharAt(us.length() - 1 - i, hex.charAt(i));
                }
                sb.append(us);
            } else {
                sb.append(c);
            }
            srcIndex++;
        }
        return sb.toString();
    }

    /**
     * Find all the lines in a source that are c-style includes
     *
     * @param source the String to be scanned for include-statements
     * @return ArrayList<String> an ArrayList withe the names of include-statements
     */
    private ArrayList<String> getIncludes(String source) {

        Pattern pattern = null;
        Matcher matcher = null;
        ArrayList<String> includes = new ArrayList<String>();

        String incPattern = "^\\s*#include\\s+[<\"](\\S+)[\">]";
        pattern = Pattern.compile(incPattern, Pattern.MULTILINE);
        matcher = pattern.matcher(source);

        while (matcher.find()) {
            includes.add(matcher.group(1));
        }
        return includes;
    }

    /**
     * Scan the source for #include statements for Arduino-libraries that
     * are referenced from the Sketch.
     *
     * @param source the (already preprocessed) String to be scanned for library
     *               include-statements
     * @return ArrayList<Library> a list of the libraries used in this sketch.
     *         The list is empty if no libraries are used from this sketch.
     */
    private ArrayList<Library> processIncludes(String source) {

        CodeManager cm = CodeManager.getInstance();
        ArrayList<Library> libList = new ArrayList<Library>();

        for (String includeName : getIncludes(source)) {
            //lets see if this references a header in the sketch-dir
            File f = new File(sketchRoot, includeName);
            if (f.exists()) {
                /* 
                   We have a matching header inside the Sketch itself.
                   No need to check for a library 
                */
                System.out.println("Using internal header `" + includeName + "`");
            } else {
                /* user includes a library (or some other header) */
                String libName = includeName.substring(0, includeName.lastIndexOf("."));
                Library lib = cm.getLibrary(libName);
                if (lib != null) {
                    boolean alreadyfound = false;
                    for (Library libLoop : libList) {
                        if (libLoop.getName().equals(lib.getName())) {
                            alreadyfound = true;
                        }
                    }
                    if (!alreadyfound) {
                        System.out.println("Using Library `" + libName + "`");
                        libList.add(lib);
                    }
                } else {
                    System.out.println("Includes header `" + includeName + "`");
                }
            }
        }
        return libList;
    }

    /**
     * Generates the prototypes used in a sketch and
     * inserts them in the correct location of the code.
     *
     * @param source thze code to be processed
     * @return String the code with prototypes or null
     *         on error
     */
    private String processPrototypes(String source) {

        ArrayList<String> ptypes = getPrototypes(source);
        int pos = getPrototypeLocation(source);
        StringBuilder src = new StringBuilder();
        src.append(source.substring(0, pos));
        src.append("\n");
        /* Add the standard arduino-core header file */
        src.append("#include \"Arduino.h\"\n");
        src.append("\n");
        /* Add the prototypes themselves */
        for (String ptype : ptypes) {
            src.append(ptype + "\n");
        }
        src.append("\n");
        /* add the rest of the user-code */
        src.append(source.substring(pos));
        return src.toString();
    }

    /**
     * Builds the list of C-prototypes for the user defined functions in the Sketch.
     *
     * @param source the sourcecode for which the prototypes have to be generated.
     * @return ArrayList<String> the list of prototypes.
     */
    private ArrayList<String> getPrototypes(String source) {

        Pattern pattern = null;
        Matcher matcher = null;
        ArrayList<String> protos = new ArrayList<String>();

        //remove everything but the pure function definitions from the source 
        String s = collapseBraces(strip(source));
        String funcPattern = "[\\w\\[\\]\\*]+\\s+[\\[\\]\\*\\w\\s]+\\([,\\[\\]\\*\\w\\s]*\\)(?=\\s*\\{)";
        pattern = Pattern.compile(funcPattern);
        matcher = pattern.matcher(s);
        while (matcher.find()) {
            protos.add(matcher.group(0) + ";");
        }
        return protos;
    }

    /**
     * Returns the index of the first character that is not a whitespace char
     * or a pre-processor directive.
     *
     * @param in the String to be processed
     * @return int the postion where the prototypes need to be filled in
     */
    private int getPrototypeLocation(String in) {

        Pattern pattern = null;
        Matcher matcher = null;

        try {
            /* Define a pattern that returns the index of the the first character that
               is not 
               - Whitespace
               - A preprocessor directive (i.e. a line  starting with #)
            */
            String whiteSpacePattern = "(\\G\\s+)";
            String preProcessorDirective = "(\\G#(?:\\\\\\n|.)*)";
            String patternString = whiteSpacePattern + "|" + preProcessorDirective;
            pattern = Pattern.compile(patternString, Pattern.MULTILINE);

        } catch (PatternSyntaxException pse) {
            throw new RuntimeException("Internal error in firstStatement()", pse);
        }
        //create a matcher for our input 
        matcher = pattern.matcher(in);
        int position = 0;
        while (matcher.find()) {
            //rest of input started with a match 
            //postion  is now behind the last char matched so far
            position = matcher.end();
        }
        return position;
    }

    /**
     * Strips pre-processor directives, single- and double-quoted
     * strings from a string.
     *
     * @param in the String to be processed
     * @return the stripped String
     */
    private String strip(String in) {

        Pattern pattern = null;
        Matcher matcher = null;
        String quotedChar = "('.')";
        String quotedString = "(\"(?:[^\"\\\\]|\\\\.)*\")";
        String preProcessorDirective = "(#(?:\\\\\\n|.)*)";
        String patternString = quotedChar + "|" + quotedString + "|" + preProcessorDirective;
        pattern = Pattern.compile(patternString, Pattern.MULTILINE);
        matcher = pattern.matcher(in);
        return matcher.replaceAll("");
    }

    /**
     * Removes the complete content between top-level curly brace pairs {} in order
     * to identify the function name for which prototypes have to be generated.
     *
     * @param in the String to collapse
     * @return String the collapsed String
     */
    private String collapseBraces(String in) {

        StringBuffer buffer = new StringBuffer();
        int nesting = 0;
        int start = 0;

        // XXX: need to keep newlines inside braces so we can determine the line
        // number of a prototype
        for (int i = 0; i < in.length(); i++) {
            if (in.charAt(i) == '{') {
                if (nesting == 0) {
                    buffer.append(in.substring(start, i + 1)); // include the '{'
                }
                nesting++;
            }
            if (in.charAt(i) == '}') {
                nesting--;
                if (nesting == 0) {
                    start = i; // include the '}'
                }
            }
        }
        buffer.append(in.substring(start));
        return buffer.toString();
    }

    /**
     * Gets some source-code stuff we want to have right at the top
     * of the resulting source for the pde.
     *
     * @param source the source to which we want to add some stuff
     * @return String the modified user source
     */
    private String addPdeHeader(String source) {

        //we add a comment, to show that this actually works
        StringBuilder sb = new StringBuilder();
        sb.append("//created by Kevoree\n");
        sb.append(source);
        return sb.toString();
    }

    /**
     * Gets the code for the main-function which depends on
     * the type of target for which this sketch is to be build.
     *
     * @param source the user-code to which the targets main-function
     *               is to be concatenated .
     * @return String The source for the sketch with the main-function
     *         for this target at the end.
     *         Returns null if we had an error while trying to read the file
     *         with the main-functions definition.
     */
    private String addMain(String source, Target target) {

        StringBuilder sb = new StringBuilder(source);
        String fname = env.getCoreDirectory().getPath() + File.separator + target.getCore() + File.separator + "main.cpp";
        try {
            BufferedReader in = new BufferedReader(new FileReader(fname));
            String sourceLine;
            while ((sourceLine = in.readLine()) != null) {
                sb.append(sourceLine + "\n");
            }
            in.close();
            sb.append("\n");
        } catch (IOException ioe) {
            System.err.println("Error while trying to read main() function");
            ioe.printStackTrace();

            return null;
        }
        return sb.toString();
    }
}
