package org.kevoree.library.javase.fileSystemGitRepository;


import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.kevoree.annotation.*;
import org.kevoree.library.javase.fileSystem.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: tboschat
 * Date: 7/17/12
 * Time: 2:23 PM
 */
@Library(name = "JavaSE")
@DictionaryType({
		@DictionaryAttribute(name = "url", optional = true),
		@DictionaryAttribute(name = "login", optional = true),
		@DictionaryAttribute(name = "pass", optional = true)
})
@ComponentType
public class GitFileSystem extends BasicFileSystem {
	private Logger logger = LoggerFactory.getLogger(GitFileSystem.class);

	//	protected File baseClone = null;
	protected Repository repository = null;
	protected Git git = null;
//	private Set<String> lockedFile = Collections.synchronizedSet(new HashSet<String>());

	public File getBaseFolder() {
		return baseFolder;
	}

	public void setBaseFolder(File f) {
		baseFolder = f;
	}

	@Start
	public void start () throws Exception {
		baseFolder = File.createTempFile("GitFileSystem", "temp");
		if (baseFolder.exists() && baseFolder.delete() && baseFolder.mkdir()) {
			CloneCommand clone = Git.cloneRepository();
			clone.setURI(this.getDictionary().get("url").toString()).setDirectory(baseFolder).setBranch(org.eclipse.jgit.lib.Constants.HEAD);
			clone.call();
			git = Git.open(baseFolder);
			repository = git.getRepository();
		} else {
			logger.debug("Unable to create the local folder to clone the repository");
			throw new Exception("Unable to create the local folder to clone the repository");
		}
	}

	@Stop
	public void stop () {
		//TODO DELETE FILE

	}

	@Update
	public void update () throws Exception {
		stop();
		start();
	}

	/*private String[] getFlatFiles (File base, String relativePath, boolean root, Set<String> extensions) {
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
	}*/


	@Port(name = "files", method = "list")
	public String[] list () {
		pull();
		return super.list();
	}

	@Port(name = "files", method = "listFromFilter")
	public String[] listFromFilter (Set<String> extensions) {
		pull();
		return super.listFromFilter(extensions);
	}


//	private long lastRevisionCheck = -1;

	@Override
	@Port(name = "files", method = "getFileContent")
	public byte[] getFileContent (String relativePath/*, Boolean lock*/) {
		pull();
		return super.getFileContent(relativePath);
	}


	/*private byte[] getContent (String relativePath) {
		File f = new File(baseFolder.getAbsolutePath() + relativePath);
		if (f.exists()) {
			try {
				FileInputStream fs = new FileInputStream(f);
				byte[] result = convertStream(fs);
				fs.close();

				return result;
			} catch (IOException e) {
				logger.error("Error while getting file ", e);
			}
		} else {
			logger.debug("No file exist = {}{}", baseFolder.getAbsolutePath(), relativePath);
			return new byte[0];
		}
		return new byte[0];
	}*/

	/*@Port(name = "files", method = "getAbsolutePath")
	public String getAbsolutePath (String relativePath) {
		if (new File(baseFolder.getAbsolutePath() + relativePath).exists()) {
			return new File(baseFolder.getAbsolutePath() + relativePath).getAbsolutePath();
		} else {
			return null;
		}
	}*/

	@Override
	@Port(name = "files", method = "saveFile")
	public boolean saveFile (String relativePath, byte[] data/*, Boolean unlock*/) {
		boolean result = super.saveFile(relativePath, data);
		File f = new File(baseFolder.getAbsolutePath() + relativePath);
		if (f.exists()) {
			/*String relativePathClean = relativePath;
			if (relativePath.startsWith("/")) {
				relativePathClean = relativePath.substring(relativePath.indexOf("/") + 1);
			}*/
//			try {
				addFileToRepository(f);
				commitRepository(" File " + relativePath + " saved ", " name ", " email ");// TODO fix name and email
			/*} catch (Exception e) {
				logger.error("error while unlock and commit git ", e);
			}*/
		}
		return result;
	}

	@Override
	@Port(name = "files", method = "mkdirs")
	public boolean mkdirs (String relativePath) {
		File f = new File(baseFolder.getAbsolutePath() + relativePath);

		addFileToRepository(f);
		commitRepository(" folders " + f.getPath() + " created ", " name ", " email ");// TODO fix name and email

		return !f.exists() && f.mkdirs();
	}

	@Override
	@Port(name = "files", method = "delete")
	public boolean delete (String relativePath) {
		File f = new File(baseFolder.getAbsolutePath() + relativePath);

		if (f.exists() && f.delete()) {
			removeFileToRepository(f);
			return true;
		}
		return false;
	}

	@Override
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

	public String getRelativePath (String absolutePath) {
		return "/" + absolutePath.substring((baseFolder.getPath().length()) + 1);
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


	/*public boolean save (String relativePath, byte[] data) {
		File f = new File(baseFolder.getAbsolutePath() + relativePath);
		try {
			if (data.length != 0 && !f.exists() && f.createNewFile()) {
				FileOutputStream fw = new FileOutputStream(f);
				fw.write(data);
				fw.flush();
				fw.close();
				return true;
			} else {
				logger.debug("Unable to save data ({}) on  {}", data, relativePath);
				return false;
			}
		} catch (IOException e) {
			logger.error("Error while getting file {}", relativePath, e);
			return false;
		}
	}*/

	public static byte[] convertStream (InputStream in) throws IOException {
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

	@Override
	@Port(name = "files", method = "move")
	public boolean move (String oldRelativePath, String newRelativePath) {

		File oldFile = new File(baseFolder.getAbsolutePath() + File.separator + oldRelativePath);
		File newFile = new File(baseFolder.getAbsolutePath() + File.separator + newRelativePath);

		if (oldFile.renameTo(newFile)) {
			addFileToRepository(newFile);
			removeFileToRepository(oldFile);
			return true;
		} else {
			logger.debug("Unable to move file {} on {}", oldRelativePath, newRelativePath);
			return false;
		}
	}

	private void pull () {
		try {
			git.pull().call();
		} catch (GitAPIException e) {
			logger.error("Error while trying to update local repository", e);
		}
	}

	public void commitRepository (String message, String nom, String email) {
		CommitCommand commit = git.commit();
		commit.setMessage(message);
		commit.setAuthor(new PersonIdent(nom, email));
		commit.setAll(true);
		try {
			commit.call();
		} catch (GitAPIException e) {
			logger.error("Unable to commit on repository ", e);
		}
	}

	public boolean addFileToRepository (File fileToAdd) {
		Boolean result = false;
		try {
			String finalFilePath = fileToAdd.getPath().substring(fileToAdd.getPath().indexOf(baseFolder.getPath()) + baseFolder.getPath().length() + 1);
			git.add().addFilepattern(finalFilePath).call();
			result = true;
		} catch (NoFilepatternException e) {
			logger.debug("Unable to add file on repository ", e);
		} catch (GitAPIException e) {
			logger.debug("Unable to add file on repository ", e);
		}
		return result;
	}

	public boolean removeFileToRepository (File fileToRemove) {
		Boolean result = false;
		try {
			String finalFilePath = fileToRemove.getPath().substring(fileToRemove.getPath().indexOf(baseFolder.getPath()) + baseFolder.getPath().length() + 1);
			logger.debug(" file f " + fileToRemove.getPath() + " string " + finalFilePath);
			git.rm().addFilepattern(finalFilePath).call();
			commitRepository(" File " + finalFilePath + " removed ", " name ", " email ");// TODO fix name and email
			result = true;
		} catch (NoFilepatternException e) {
			logger.debug("Cannot remove file to repository " + e);
		} catch (GitAPIException e) {
			logger.debug("Unable to remove file on repository ", e);
		}
		return result;
	}
}