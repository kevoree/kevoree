package org.kevoree.library.javase.fileSystemGitRepository;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.javase.fileSystem.api.AbstractItem;
import org.kevoree.library.javase.fileSystem.api.FileService;
import org.kevoree.library.javase.fileSystem.api.FolderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pdespagn
 * Date: 5/23/12
 * Time: 3:20 PM
 */
@Provides({
		@ProvidedPort(name = "gitManagement", type = PortType.SERVICE, className = GitRepositoryActions.class)
})
@ComponentType
public class FileSystemGitRepositoryImpl extends AbstractComponentType/*extends GitFileSystem*/ implements GitRepositoryActions, FileService {

	private Logger logger = LoggerFactory.getLogger(FileSystemGitRepositoryImpl.class);

	private GitFileSystem fileSystem;

	// FROM GitFileSystem
	// protected File baseClone = null;
	// protected org.eclipse.jgit.lib.Repository repository = null;
	// protected Git git = null;
	@Start
	public void start () throws Exception {
		/*if (!this.getDictionary().get("url").toString().isEmpty() && !this.getDictionary().get("login").toString().isEmpty() && !this.getDictionary().get("pass").toString().isEmpty()) {
			super.start();
		}*/
		fileSystem = new GitFileSystem();
	}

	@Stop
	public void stop () {
	}

	@Override
	@Port(name = "gitManagement", method = "importRepository")
	public AbstractItem importRepository (String login, String password, String url, String nameRepository, String pathRepository) {

		if (isRepoExist(login, password, nameRepository)) {
			//fileSystem. // TODO set login, url and password on dictionary
			fileSystem.setBaseFolder(new File(pathRepository + nameRepository));
			deleteDir(fileSystem.getBaseFolder());
			cloneRepository(url, nameRepository, pathRepository);
			FolderItem item = new FolderItem();
			item.setName(fileSystem.getBaseFolder().getPath());
			return item;
		}
		logger.debug("Unable to import the repository, because {} doesn't exist in {}'s account or he's not a collaborator of that repository ", nameRepository, login);
		return null;
	}

	// Deletes all files and subdirectories under dir
	public static boolean deleteDir (File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (String aChildren : children) {
				boolean success = deleteDir(new File(dir, aChildren));
				if (!success) {
					return false;
				}
			}
		}
		// The directory is now empty so delete it
		return dir.delete();
	}

	@Override
	@Port(name = "gitManagement", method = "initRepository")
	public AbstractItem initRepository (String login, String password, String nameRepository, String pathRepository) {
		boolean isCreated = createRepository(login, password, nameRepository);
		if (isCreated) {
			logger.debug(" the Repository {} is created ", nameRepository);
			cloneRepository("https://" + login + "@github.com/" + login + "/" + nameRepository + ".git", nameRepository, pathRepository);
			createFileToInitRepository("https:  //" + login + "@github.com/" + login + "/" + nameRepository + ".git", nameRepository, pathRepository);
			commitRepository("commit init", login, "Email@login.org");
			pushRepository(login, password);
			FolderItem item = new FolderItem();
			item.setName(fileSystem.getBaseFolder().getPath());
			return item;
		}
		return null;
	}

	public boolean isRepoExist (String login, String password, String nameRepository) {
		RepositoryService service = new RepositoryService();
		service.getClient().setCredentials(login, password);

		try {
			service.getRepository(login, nameRepository);

			logger.debug("The repository {} exists", nameRepository);
			return true;
		} catch (IOException e) {
			logger.debug("The repository {} doesn't exist ", nameRepository);
			return false;
		}
	}

	@Override
	@Port(name = "gitManagement", method = "createRepository")
	public boolean createRepository (String login, String password, String nameRepository) {
		RepositoryService service = new RepositoryService();
		service.getClient().setCredentials(login, password);
		if (!isRepoExist(login, password, nameRepository)) {
			Repository repo = new Repository();
			repo.setName(nameRepository);
			try {
				service.createRepository(repo);
				return true;
			} catch (IOException e) {
				logger.debug("Could not create repository", e);
				return false;
			}
		}
		logger.debug(" Can't create the repository {} because it already exists in {}'s account", nameRepository, login);
		return false;
	}

	@Override
	@Port(name = "gitManagement", method = "createFileToInitRepository")
	public void createFileToInitRepository (String url, String nomRepo, String directoryPath) {
		File file = new File(directoryPath + nomRepo + "/README.md");
		try {
			if (file.createNewFile()) {
				fileSystem.addFileToRepository(file);
				commitRepository("Init Repository with a README.md ", "", "");
			} else {
				logger.debug("Unable to create the file {}", directoryPath + nomRepo + "/README.md");
			}
		} catch (IOException e) {
			logger.debug("Unable to create the file {}", directoryPath + nomRepo + "/README.md", e);
		}
	}

	@Override
	@Port(name = "gitManagement", method = "cloneRepository")
	public void cloneRepository (String url, String nameRepository, String pathRepository) {
		fileSystem.setBaseFolder(new File(pathRepository + nameRepository));
		CloneCommand clone = new CloneCommand();
		clone.setURI(url);
		clone.setDirectory(new File(pathRepository + nameRepository));
		clone.setBare(false);
		try {
			fileSystem.git = clone.call();
		} catch (GitAPIException e) {
			e.printStackTrace();
			logger.debug("Unable to clone the repository", e);
		}
		fileSystem.repository = fileSystem.git.getRepository();
	}

	@Override
	@Port(name = "gitManagement", method = "commitRepository")
	public void commitRepository (String message, String nom, String email) {
		CommitCommand commit = fileSystem.git.commit();
		commit.setMessage(message);
		commit.setAuthor(new PersonIdent(nom, email));
		try {
			commit.call();
		} catch (GitAPIException e) {
			logger.debug("Unable to commit on repository", e);
		}
	}

	@Override
	@Port(name = "gitManagement", method = "pushRepository")
	public boolean pushRepository (String login, String password) {
		UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider(login, password);
		try {
			fileSystem.git.push().setCredentialsProvider(user).call();
			return true;
		} catch (InvalidRemoteException e) {
			logger.debug("Unable to push on repository", e);
			return false;
		} catch (TransportException e) {
			logger.debug("Unable to push on repository", e);
			return false;
		} catch (GitAPIException e) {
			logger.debug("Unable to push on repository", e);
			return false;
		}
	}

	@Override
	public String[] list () {
		return fileSystem.list();
	}

	@Override
	public String[] listFromFilter (Set<String> extensions) {
		return fileSystem.listFromFilter(extensions);
	}

	@Override
	public byte[] getFileContent (String relativePath) {
		return fileSystem.getFileContent(relativePath);
	}

	@Override
	public String getAbsolutePath (String relativePath) {
		return fileSystem.getAbsolutePath(relativePath);
	}

	@Override
	public boolean saveFile (String relativePath, byte[] data) {
		return fileSystem.saveFile(relativePath, data);
	}

	@Override
	public boolean mkdirs (String relativePath) {
		return fileSystem.mkdirs(relativePath);
	}

	@Override
	public boolean delete (String relativePath) {
		return fileSystem.delete(relativePath);
	}

	@Override
	public boolean move (String oldRelativePath, String newRelativePath) {
		return fileSystem.move(oldRelativePath, newRelativePath);
	}

	@Override
	public AbstractItem getTree (AbstractItem root) {
		return fileSystem.getTree(root);
	}
}
