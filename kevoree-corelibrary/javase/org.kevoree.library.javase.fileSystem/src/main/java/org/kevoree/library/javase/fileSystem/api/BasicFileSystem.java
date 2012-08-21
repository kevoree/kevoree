package org.kevoree.library.javase.fileSystem.api;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 22/11/11
 * Time: 20:02
 */

@Library(name = "JavaSE")
@Provides({
		@ProvidedPort(name = "files", type = PortType.SERVICE, className = FileService.class)
})
@MessageTypes({
		@MessageType(name = "saveFile", elems = {@MsgElem(name = "path", className = String.class), @MsgElem(name = "data", className = Byte[].class)})
})
@DictionaryType({
		@DictionaryAttribute(name = "basedir", optional = false)
})
@ComponentType
public class BasicFileSystem extends AbstractComponentType implements FileService {

//	private String baseURL = "";
	protected File baseFolder = null;
	private Logger logger = LoggerFactory.getLogger(BasicFileSystem.class);

	@Start
	public void start () throws Exception {
		baseFolder = new File(this.getDictionary().get("basedir").toString());
//		baseURL = this.getDictionary().get("basedir").toString();
		if (!baseFolder.exists() && baseFolder.mkdirs() || baseFolder.exists()) {
			logger.debug("FileSystem initialized with {} as root", baseFolder.getAbsolutePath());
		}
	}

	@Stop
	public void stop () {
//NOP
	}

	@Update
	public void update () throws Exception {
//		baseURL = this.getDictionary().get("basedir").toString();
		start();
	}

	protected String[] getFlatFiles (File base, String relativePath, boolean root, Set<String> extensions) {
		Set<String> files = new HashSet<String>();
		if (base.exists() && !base.getName().startsWith(".")) {
			if (base.isDirectory()) {
				File[] childs = base.listFiles();
				if (childs != null) {
					for (File child : childs) {
						if (root) {
							Collections.addAll(files, getFlatFiles(child, relativePath, false, extensions));
						} else {
							Collections.addAll(files, getFlatFiles(child, relativePath + "/" + base.getName(), false, extensions));
						}
					}
				}
			} else {
				boolean filtered = false;
				if (extensions != null) {
					filtered = true;
					logger.debug("Look for extension for {}", base.getName());
					for (String filter : extensions) {
						if (base.getName().endsWith(filter)) {
							filtered = false;
						}
					}
				}
				if (!root && !filtered) {
					files.add(relativePath + "/" + base.getName());
				}
			}
		}
		String[] filesPath = new String[files.size()];
		files.toArray(filesPath);
		return filesPath;
	}


	@Port(name = "files", method = "list")
	public String[] list () {
		return getFlatFiles(baseFolder, "", true, null);
	}

	@Port(name = "files", method = "listFromFilter")
	public String[] listFromFilter (Set<String> extensions) {
		return getFlatFiles(baseFolder, "", true, extensions);
	}

	@Port(name = "files", method = "getFileContent")
	public byte[] getFileContent (String relativePath) {
		File f = new File(baseFolder.getAbsolutePath() + File.separator + relativePath);
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
			logger.debug("No file exist = {}", baseFolder.getAbsolutePath() + File.separator + relativePath);
			return new byte[0];
		}
		return new byte[0];
	}

	@Port(name = "files", method = "getAbsolutePath")
	public String getAbsolutePath (String relativePath) {
		if (new File(baseFolder.getAbsolutePath() + File.separator + relativePath).exists()) {
			return new File(baseFolder.getAbsolutePath() + File.separator + relativePath).getAbsolutePath();
		} else {
			return null;
		}
	}

	@Port(name = "files", method = "mkdirs")
	public boolean mkdirs (String relativePath) {
		return new File(baseFolder.getAbsolutePath() + File.separator + relativePath).mkdirs();
	}

	@Port(name = "files", method = "delete")
	public boolean delete (String relativePath) {
		return new File(baseFolder.getAbsolutePath() + File.separator + relativePath).delete();
	}

	@Port(name = "files", method = "saveFile")
	public boolean saveFile (String relativePath, byte[] data) {
		File f = new File(baseFolder.getAbsolutePath() + File.separator + relativePath);
		if (f.exists()) {
			try {
				FileOutputStream fw = new FileOutputStream(f);
				fw.write(data);
				fw.flush();
				fw.close();
				return true;
			} catch (Exception e) {
				logger.error("Error while getting file ", e);
				return false;
			}
		} else {
			logger.debug("No file exist = {}", baseFolder.getAbsolutePath() + File.separator + relativePath);
			return false;
		}
	}

	@Port(name = "files", method = "move")
	public boolean move (String oldRelativePath, String newRelativePath) {

		File oldFile = new File(baseFolder.getAbsolutePath() + File.separator + oldRelativePath);
		File newFile = new File(baseFolder.getAbsolutePath() + File.separator + newRelativePath);

		if (oldFile.renameTo(newFile)) {
			return true;
		} else {
			logger.debug("Unable to move file {} on {}", oldRelativePath, newRelativePath);
			return false;
		}
	}

	@Port(name = "files", method = "getTree")
	public AbstractItem getTree (AbstractItem absRoot) {
		String nameDirectory = absRoot.getName();
		File file = new File(nameDirectory);
		FolderItem root = new FolderItem();
		root.setName(absRoot.getName());
		root.setPath("/");
		process(file, root);
		sortList(root.getChilds());
		return root;
	}

	public static byte[] convertStream (InputStream in) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int l;
		do {
			l = (in.read(buffer));
			if (l > 0) {
				out.write(buffer, 0, l);
			}
		} while (l > 0);
		return out.toByteArray();
	}

	public void process (File file, FolderItem item) {
		if (!file.getName().contains(".git") && !file.getName().endsWith("~")) {
			if (file.isFile()) {
				FileItem itemToAdd = new FileItem();
				itemToAdd.setName(file.getName());
				itemToAdd.setParent(item);
				itemToAdd.setPath(getRelativePath(file.getPath()));
				item.add(itemToAdd);
			} else if (file.isDirectory()) {
				FolderItem folder = new FolderItem();
				folder.setName(file.getName());
				folder.setParent(item);
				folder.setPath(getRelativePath(file.getPath() + "/"));
				item.add(folder);
				File[] listOfFiles = file.listFiles();
				if (listOfFiles != null) {
					for (File listOfFile : listOfFiles) process(listOfFile, folder);
				}
			}
		}
	}

	private void sortList (List<AbstractItem> list) {
		int indexCurrentChar = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(indexCurrentChar).getClass() == FileItem.class) {
				list.add(list.get(indexCurrentChar));
				list.remove(indexCurrentChar);
			} else {
				sortList(((FolderItem) list.get(indexCurrentChar)).getChilds());
				indexCurrentChar++;
			}
		}
	}

	public String getRelativePath (String absolutePath) {
		return absolutePath.substring((baseFolder.getAbsolutePath().length()) + 1);
	}

}
