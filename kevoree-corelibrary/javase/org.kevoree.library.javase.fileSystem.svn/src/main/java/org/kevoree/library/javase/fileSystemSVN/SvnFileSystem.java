package org.kevoree.library.javase.fileSystemSVN;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.fileSystem.api.BasicFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 22/11/11
 * Time: 20:02
 */

@Library(name = "JavaSE")
@DictionaryType({
		@DictionaryAttribute(name = "url", optional = false),
		@DictionaryAttribute(name = "login", optional = false),
		@DictionaryAttribute(name = "pass", optional = false)
})
@ComponentType
public class SvnFileSystem extends BasicFileSystem {

	//	private File baseClone = null;
	private Logger logger = LoggerFactory.getLogger(SvnFileSystem.class);

	private SVNRepository repository = null;
	private ISVNAuthenticationManager authManager = null;
	private SVNUpdateClient client = null;
	private Set<String> lockedFile = Collections.synchronizedSet(new HashSet<String>());

	@Start
	public void start () throws Exception {
		lockedFile.clear();
		baseFolder = File.createTempFile("SVNFileSystem", "temp");

		if (baseFolder.exists() && baseFolder.delete() && baseFolder.mkdir()) {
			logger.info("Create temp SVN clone at " + baseFolder.getAbsolutePath());
			repository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(this.getDictionary().get("url").toString()));
			authManager = SVNWCUtil.createDefaultAuthenticationManager(this.getDictionary().get("login").toString(), this.getDictionary().get("pass").toString());
			repository.setAuthenticationManager(authManager);
			repository.testConnection();
			long lastRevision = repository.getLatestRevision();
			client = new SVNUpdateClient(authManager, SVNWCUtil.createDefaultOptions(true));
			client.doCheckout(SVNURL.parseURIDecoded(this.getDictionary().get("url").toString()), baseFolder, SVNRevision.create(lastRevision), SVNRevision.create(lastRevision), SVNDepth.INFINITY,
					true);
		} else {
			logger.debug("Unable to create the local folder to checkout the repository");
			throw new Exception("Unable to create the local folder to checkout the repository");
		}
	}

	@Stop
	public void stop () {
		//KILL ALL LOCK
		Map<String, Long> unlocks = new HashMap<String, Long>();
		for (String aLockedFile : lockedFile) {
			unlocks.put(aLockedFile, null);
		}
		try {
			repository.unlock(unlocks, false, null);
		} catch (SVNException e) {
			logger.error("Error while release lock ", e);
		}
		repository.closeSession();
		repository = null;
		lockedFile.clear();
	}

	@Update
	public void update () throws Exception {
		stop();
		start();
	}

	/*private Set<String> getFlatFiles (File base, String relativePath, boolean root, Set<String> extensions) {
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
					logger.debug("Look for extension for " + base.getName());
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
		return files;
	}*/


	@Port(name = "files", method = "list")
	public String[] list () {
		updateRepository();
		return super.list();
	}

	@Port(name = "files", method = "listFromFilter")
	public String[] listFromFilter (Set<String> extensions) {
		updateRepository();
		return super.listFromFilter(extensions);
	}


	private long lastRevisionCheck = -1;

	@Override
	@Port(name = "files", method = "getFileContent")
	public byte[] getFileContent (String relativePath) {

		updateRepository();
		final String[] relatvePathClean = {relativePath};
		if (relativePath.startsWith("/")) {
			relatvePathClean[0] = relativePath.substring(relativePath.indexOf("/") + 1);
		}
		if (!lockedFile.contains(relatvePathClean)) {
			lockedFile.add(relatvePathClean[0]);
			Thread t = new Thread() {
				@Override
				public void run () {
					Map<String, Long> locks = new HashMap<String, Long>();
					locks.put(relatvePathClean[0], lastRevisionCheck);
					try {
						repository.lock(locks, "AutoLock Kevoree Editor", false, null);
						lockedFile.add(relatvePathClean[0]);
					} catch (SVNException e) {
						logger.error("Error while acquire lock ", e);
					}
				}
			};
			t.start();
		}
		return super.getFileContent(relativePath);
	}

	/*@Port(name = "files", method = "getFileContent")
	public byte[] getFileContent (String relativePath) {
		File f = new File(baseClone.getAbsolutePath() + relativePath);
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
			logger.debug("No file exist = {}", baseClone.getAbsolutePath() + relativePath);
			return new byte[0];
		}
		return new byte[0];
	}*/

	@Port(name = "files", method = "getAbsolutePath")
	public String getAbsolutePath (String relativePath) {
		if (new File(baseFolder.getAbsolutePath() + relativePath).exists()) {
			return new File(baseFolder.getAbsolutePath() + relativePath).getAbsolutePath();
		} else {
			return null;
		}
	}

	@Override
	@Port(name = "files", method = "saveFile")
	public boolean saveFile (String relativePath, byte[] data) {
		boolean result = super.saveFile(relativePath, data);

		File f = new File(baseFolder.getAbsolutePath() + relativePath);
		if (f.exists()) {
			SVNCommitClient clientCommit = new SVNCommitClient(authManager, SVNWCUtil.createDefaultOptions(true));
			File[] paths = {f};
			Map<String, Long> unlocks = new HashMap<String, Long>();
			String relatvePathClean = relativePath;
			if (relativePath.startsWith("/")) {
				relatvePathClean = relativePath.substring(relativePath.indexOf("/") + 1);
			}
			unlocks.put(relatvePathClean, null);
			try {
				repository.unlock(unlocks, false, null);
				clientCommit.doCommit(paths, true, "AutoCommit Kevoree Editor", null, null, false, false, SVNDepth.INFINITY);
				lockedFile.remove(relatvePathClean);
			} catch (SVNException e) {
				logger.error("error while unkock and commit svn ", e);
			}
		}
		return result;
	}

	@Port(name = "files", method = "unlock")
	public void unlock (String relativePath) {
		final String[] relatvePathClean = {relativePath};
		if (relativePath.startsWith("/")) {
			relatvePathClean[0] = relativePath.substring(relativePath.indexOf("/") + 1);
		}
		Map<String, Long> locks = new HashMap<String, Long>();
		locks.put(relatvePathClean[0], lastRevisionCheck);
		try {
			repository.lock(locks, "AutoLock Kevoree Editor", false, null);
			lockedFile.add(relatvePathClean[0]);
		} catch (SVNException e) {
			logger.error("Error while acquire lock ", e);
		}
	}

	@Override
	@Port(name = "files", method = "mkdirs")
	public boolean mkdirs (String relativePath) {
		File f = new File(baseFolder.getAbsolutePath() + relativePath);
		if (!f.exists() && f.mkdirs()) {
			SVNCommitClient clientCommit = new SVNCommitClient(authManager, SVNWCUtil.createDefaultOptions(true));
			File[] paths = {f};
			Map<String, Long> unlocks = new HashMap<String, Long>();
			String relativePathClean = relativePath;
			if (relativePath.startsWith("/")) {
				relativePathClean = relativePath.substring(relativePath.indexOf("/") + 1);
			}
			unlocks.put(relativePathClean, null);
			try {
				repository.unlock(unlocks, false, null);
				clientCommit.doCommit(paths, true, "AutoCommit Kevoree Editor", false, false);
				lockedFile.remove(relativePathClean);
				return true;
			} catch (SVNException e) {
				logger.error("error while unkock and commit svn ", e);
			}
		}
		return false;
	}

	@Override
	@Port(name = "files", method = "delete")
	public boolean delete (String relativePath) {
		File f = new File(baseFolder.getAbsolutePath() + relativePath);
		if (f.exists() && f.delete()) {
			SVNCommitClient clientCommit = new SVNCommitClient(authManager, SVNWCUtil.createDefaultOptions(true));
			File[] paths = {f};
			Map<String, Long> unlocks = new HashMap<String, Long>();
			String relativePathClean = relativePath;
			if (relativePath.startsWith("/")) {
				relativePathClean = relativePath.substring(relativePath.indexOf("/") + 1);
			}
			unlocks.put(relativePathClean, null);
			try {
				repository.unlock(unlocks, false, null);
				clientCommit.doCommit(paths, true, "AutoCommit Kevoree Editor", null, null, false, false, SVNDepth.INFINITY);
				lockedFile.remove(relativePathClean);
				return true;
			} catch (SVNException e) {
				logger.error("error while unlock and commit svn ", e);
			}
		}
		return false;
	}

	/*@Port(name = "files", method = "saveFile")
	public boolean saveFile (String relativePath, byte[] data) {
		File f = new File(baseClone.getAbsolutePath() + relativePath);
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
			logger.debug("No file exist = {}", baseClone.getAbsolutePath() + relativePath);
			return false;
		}
	}*/

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

	private void updateRepository () {
		//UPDATE PHASE
		try {
			long newRevision = repository.getLatestRevision();
			if (newRevision != lastRevisionCheck) {
				File f = new File(baseFolder.getAbsolutePath());
				if (f.exists()) {
					lastRevisionCheck = client.doUpdate(f, SVNRevision.HEAD, SVNDepth.INFINITY, false, false);
				}
			}
		} catch (SVNException e) {
			logger.error("Error while getRevision");
		}
	}

}
