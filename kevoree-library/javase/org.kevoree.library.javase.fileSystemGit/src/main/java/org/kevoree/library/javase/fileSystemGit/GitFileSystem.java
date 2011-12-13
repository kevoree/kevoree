package org.kevoree.library.javase.fileSystemGit;

import com.sun.xml.internal.rngom.ast.builder.BuildException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.javase.fileSystem.LockFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/12/11
 * Time: 22:39
 * To change this template use File | Settings | File Templates.
 */
@Library(name = "JavaSE")
@Provides({
        @ProvidedPort(name = "files", type = PortType.SERVICE, className = LockFilesService.class)
})
@MessageTypes({
        @MessageType(name = "saveFile", elems = {@MsgElem(name = "path", className = String.class), @MsgElem(name = "data", className = Byte[].class)})
})
@DictionaryType({
        @DictionaryAttribute(name = "url", optional = false),
        @DictionaryAttribute(name = "login", optional = false),
        @DictionaryAttribute(name = "pass", optional = false)
})
@ComponentType
public class GitFileSystem extends AbstractComponentType implements LockFilesService {

    private File baseClone = null;
    private Logger logger = LoggerFactory.getLogger(GitFileSystem.class);

    @Start
    public void start() {
        try {
            baseClone = File.createTempFile("kevoreessh", "temp");
            baseClone.delete();
            baseClone.mkdir();
            CloneCommand clone = Git.cloneRepository();
            clone.setURI(this.getDictionary().get("url").toString()).setDirectory(baseClone).setBranch(org.eclipse.jgit.lib.Constants.HEAD);
            clone.call();
            
        } catch (Exception e) {
            logger.debug("Could not clone repository: ", e);
            throw new BuildException(e);
        }
    }

    @Stop
    public void stop() {
        //DELETE FILE
    }

    @Update
    public void update() {

    }

    @Override
    public Set<String> getFilesPath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<String> getFilteredFilesPath(Set<String> extensions) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte[] getFileContent(String relativePath, Boolean lock) {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getAbsolutePath(String relativePath) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean saveFile(String relativePath, byte[] data, Boolean unlock) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void unlock(String relativePath) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
