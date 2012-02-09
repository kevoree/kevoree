package org.kevoree.library.javase.webserver.latexEditor.server;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/12/11
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.kevoree.framework.MessagePort;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.kevoree.library.javase.fileSystem.LockFilesService;
import org.kevoree.library.javase.webserver.latexEditor.LatexEditor;
import org.kevoree.library.javase.webserver.latexEditor.client.latexEditorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class latexEditorServiceImpl extends RemoteServiceServlet implements latexEditorService {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private LatexEditor editor = null;
    LockFilesService portService = null;
    Set<String> extensions = new HashSet<String>();

    public latexEditorServiceImpl() {
        //EMPTY CONSTRUCTOR FOR DEBUG ONLY IN IDE
        //INSERT FAKE COMPONENT
        portService = new LockFilesService(){
            @Override
            public Set<String> getFilesPath() {
                Set<String> files = new HashSet<String>();
                for(int i =0 ;i< 20; i ++){
                    files.add("/fakeFile"+i+".tex");

                }
                return files;
            }

            @Override
            public Set<String> getFilteredFilesPath(Set<String> extensions) {
                Set<String> files = new HashSet<String>();
                for(int i =0 ;i< 20; i ++){
                    files.add("/fakeFile"+i+".tex");

                }
                return files;
            }

            @Override
            public byte[] getFileContent(String relativePath, Boolean lock) {
                return "fake content \n %comment \n".getBytes();
            }

            @Override
            public String getAbsolutePath(String relativePath) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean saveFile(String relativePath, byte[] data, Boolean unlock) {
                System.out.println("FAKE SAVE "+ relativePath);
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void unlock(String relativePath) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        
    }

    public latexEditorServiceImpl(LatexEditor _editor) {
        editor = _editor;
        portService= editor.getPortByName("files", LockFilesService.class);
        extensions.add("tex");
        extensions.add("cls");
        extensions.add("txt");
        extensions.add("sty");
        extensions.add("bst");
        extensions.add("bib");
        logger.debug("Latex Editor Service Init "+portService);
    }

    @Override
    public boolean saveFile(String fileName, String content) {
        boolean saveResult = portService.saveFile(fileName, content.getBytes(), true);
        if (!saveResult) {
            logger.debug("Error while saving file = {}", fileName);
        }
        return saveResult;
    }

    @Override
    public Set<String> getFlatFiles() {
        try {
            return portService.getFilteredFilesPath(extensions);  
        } catch(Exception e) {
            logger.debug("",e);
            return null;
        }
    }

    @Override
    public String getFileContent(String fileName, Boolean lock) {


        byte[] content = portService.getFileContent(fileName, lock);
        /*
        if (content.length > 0) {
            response.setRawContent(content);
            if (request.getResolvedParams().get("file").endsWith(".pdf")) {
                response.setContentType("application/pdf");
            }
            if (request.getResolvedParams().get("file").endsWith(".log")) {
                response.setContentType("text/plain");
            }

            return true;
        } else {
            logger.debug("No file exist = {}", request.getResolvedParams().get("file"));
        }*/
        return new String(content);
    }

    @Override
    public String compile(String fileName) throws Exception {
        if (editor.isPortBinded("compile")) {
            String absolutePath = portService.getAbsolutePath(fileName);
            StdKevoreeMessage message = new StdKevoreeMessage();
            message.putValue("file", absolutePath);
            //CREATE TEMP UUID
            UUID compileID = UUID.randomUUID();
            message.putValue("id", compileID);
            editor.waitingID.add(compileID.toString());
            editor.getPortByName("compile", MessagePort.class).process(message);
            return compileID.toString();
        }
        throw new Exception("No Compiler Ready");
    }

    @Override
    public String[] compileresult(String uuid) {
        if (editor.waitingID.contains(uuid)) {
            return new String[] {"waiting", ""};
        } else {
			/*if (editor.compileResult.get(uuid) != null) {*/
				String[] result = {editor.compileResult.get(uuid).toString(), editor.compileLog.get(uuid).toString()};
				editor.compileResult.remove(uuid);
				editor.compileLog.remove(uuid);
				return result;
			/*}
			return new String[]{"", ""};*/
        }
    }

    @Override
    public void log(String msg) {
        logger.debug(msg);
    }

    @Override
    public void log(String message, Throwable t) {
        logger.debug(message, t);
    }
}
