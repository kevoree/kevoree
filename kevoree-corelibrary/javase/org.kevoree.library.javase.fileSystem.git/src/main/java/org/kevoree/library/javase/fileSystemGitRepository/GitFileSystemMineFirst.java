package org.kevoree.library.javase.fileSystemGitRepository;


import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kevoree.annotation.*;
import org.kevoree.library.javase.fileSystem.api.AbstractItem;
import org.kevoree.library.javase.fileSystem.api.BasicFileSystem;
import org.kevoree.library.javase.fileSystem.api.FileItem;
import org.kevoree.library.javase.fileSystem.api.FolderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
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
		@DictionaryAttribute(name = "pass", optional = true),
        @DictionaryAttribute(name = "pulldelayinms", defaultValue = "15000")

})
@ComponentType
public class GitFileSystemMineFirst extends GitFileSystem {
	private Logger logger = LoggerFactory.getLogger(GitFileSystemMineFirst.class);




    @Start
    public void start () throws Exception {
        super.start();

        delay = Long.parseLong((String) this.getDictionary().get("pulldelayinms"));

    }

    @Stop
    public void stop () {

    }

    @Update
    public void update () throws Exception {
        stop();
        start();
    }


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


	private long lastRevisionCheck = -1;
    private long delay = -1;



    @Override
	@Port(name = "files", method = "getFileContent")
	public byte[] getFileContent (String relativePath/*, Boolean lock*/) {

        long newtime = System.currentTimeMillis();

        if (newtime - lastRevisionCheck > delay){
            pull();
            lastRevisionCheck =  newtime;
        }


        return super.getFileContent(relativePath);
	}



    public boolean save (String relativePath, byte[] data) {
        File f = new File(baseFolder.getAbsolutePath() + File.separator + relativePath);
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
    }

	@Override
	@Port(name = "files", method = "saveFile")
	public boolean saveFile (String relativePath, byte[] data) {
		boolean result = true;//super.saveFile(relativePath, data);
		File f = new File(baseFolder.getAbsolutePath() + relativePath);
        if (f.exists()) {
           result = save(relativePath, data);

			/*String relativePathClean = relativePath;
			if (relativePath.startsWith("/")) {
				relativePathClean = relativePath.substring(relativePath.indexOf("/") + 1);
			}*/
            commitRepository(" File " + relativePath + " saved ", " from site ", " kevoree@kevoree.org ");// TODO fix name and email

            try {

				//addFileToRepository(f);
				git.pull().call();

			/*} catch (Exception e) {
				logger.error("error while unlock and commit git ", e);
			}*/
		} catch (DetachedHeadException e) {
                try {

                    git.revert().call();
                    save(relativePath+".bak_" + new Date(),data);
                    commitRepository(" File " + relativePath + " saved with conflict ", " name ", " email ");// TODO fix name and email
                    UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider((String)this.getDictionary().get("login"), (String)this.getDictionary().get("pass"));
                    git.push().setCredentialsProvider(user).call();
                    return false;

                } catch (GitAPIException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }


                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoHeadException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (TransportException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvalidRemoteException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (CanceledException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (WrongRepositoryStateException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (RefNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (GitAPIException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            try {
                UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider((String)this.getDictionary().get("login"), (String)this.getDictionary().get("pass"));
                git.push().setCredentialsProvider(user).call();
                return true;
            } catch (GitAPIException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

            return true;
	}

	@Override
	@Port(name = "files", method = "mkdirs")
	public boolean mkdirs (String relativePath) {
		File f1 = new File(baseFolder.getAbsolutePath() + relativePath);

		addFileToRepository(f1);
		commitRepository(" folders " + f1.getPath() + " created ", " name ", " email ");// TODO fix name and email

		return !f1.exists() && f1.mkdirs();
	}

	@Override
	@Port(name = "files", method = "delete")
	public boolean delete (String relativePath) {
		File f1 = new File(baseFolder.getAbsolutePath() + relativePath);

		if (f1.exists() && f1.delete()) {
			removeFileToRepository(f1);
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