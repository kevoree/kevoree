package org.kevoree.library.javase.fileSystem;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 22/11/11
 * Time: 20:02
 * To change this template use File | Settings | File Templates.
 */

@Library(name = "JavaSE")
@Provides({
        @ProvidedPort(name = "save", type = PortType.MESSAGE, messageType = "saveFile"),
        @ProvidedPort(name = "files", type = PortType.SERVICE, className = FilesService.class)
})
@MessageTypes({
        @MessageType(name = "saveFile", elems = {@MsgElem(name = "path", className = String.class), @MsgElem(name = "data", className = Byte[].class)})
})
@DictionaryType({
        @DictionaryAttribute(name = "basedir", optional = false)
})
@ComponentType
public class BasicFileSystem extends AbstractComponentType implements FilesService {

    private static String baseURL = "";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void start() {
        baseURL = this.getDictionary().get("basedir").toString();
    }

    @Stop
    public void stop() {
//NOP
    }

    @Update
    public void update() {
        baseURL = this.getDictionary().get("basedir").toString();
    }

    private static Set<String> getFlatFiles(File base, String relativePath, boolean root, Set<String> extensions) {
        Set<String> files = new HashSet<String>();
        if (base.exists() && !base.getName().startsWith(".")) {
            if (base.isDirectory()) {
                File[] childs = base.listFiles();
                for (int i = 0; i < childs.length; i++) {
                    if (root) {
                        files.addAll(getFlatFiles(childs[i], relativePath, false, extensions));
                    } else {
                        files.addAll(getFlatFiles(childs[i], relativePath + "/" + base.getName(), false, extensions));
                    }
                }
            } else {

                boolean filtered = false;
                if (extensions != null) {
                    filtered = true;
                    for (String filter : extensions) {
                        if(base.getName().endsWith("filter")){
                            filtered = false;
                        }
                    }
                }
                if (!root && !filtered) {
                    files.add(relativePath + "/" + base.getName());
                }
            }
        }
        return files;
    }


    @Port(name = "files", method = "getFilesPath")
    public Set<String> getFilesPath() {
        return getFlatFiles(new File(baseURL), "", true, null);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Port(name = "files", method = "getFilteredFilesPath")
    public Set<String> getFilteredFilesPath(Set<String> extensions) {
        return getFlatFiles(new File(baseURL), "", true, extensions);
    }

    @Port(name = "files", method = "getFileContent")
    public byte[] getFileContent(String relativePath) {

        System.out.println("intpu=" + relativePath);

        File f = new File(baseURL + relativePath);
        if (f.exists()) {
            try {

                FileInputStream fs = new FileInputStream(f);
                byte[] result = convertStream(fs);
                fs.close();

                return result;
            } catch (Exception e) {
                logger.error("Error while getting file ", e);
            }
        } else {
            logger.debug("No file exist = {}", baseURL + relativePath);
            return new byte[0];
        }
        return new byte[0];
    }

    @Port(name = "files", method = "getAbsolutePath")
    public String getAbsolutePath(String relativePath) {
        if (new File(baseURL + relativePath).exists()) {
            return new File(baseURL + relativePath).getAbsolutePath();
        } else {
            return null;
        }
    }

    public static byte[] convertStream(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int l;
        do {
            l = (in.read(buffer));
            if (l > 0)
                out.write(buffer, 0, l);
        } while (l > 0);
        return out.toByteArray();
    }

    @Port(name = "save")
    public void saveMessage(Object o) {

    }

}
