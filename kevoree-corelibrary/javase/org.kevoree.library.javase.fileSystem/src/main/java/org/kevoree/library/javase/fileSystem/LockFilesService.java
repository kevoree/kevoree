package org.kevoree.library.javase.fileSystem;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 22/11/11
 * Time: 20:03
 */
public interface LockFilesService {

    public Set<String> getFilesPath();

    public Set<String> getFilteredFilesPath(Set<String> extensions);
    
    public byte[] getFileContent(String relativePath, Boolean lock);
	
	public String getAbsolutePath(String relativePath);
    
    public boolean saveFile(String relativePath, byte[] data, Boolean unlock);

    public void unlock(String relativePath);

	/**
	 * Build recursively the folders according to the relativePath parameter
	 * @param relativePath the relativePath that defines the folders to build
	 * @return true if all folders are built, false else
	 */
	public boolean mkdirs(String relativePath);

	/**
	 * Delete a file or a folder denoted by the relativePath parameter.
	 * If the relativePath denotes a folder, this one must be empty.
	 * @param relativePath the relativePath that defines the folder or the file to delete
	 * @return true if the file or the folder is deleted, false else
	 */
	public boolean delete(String relativePath);

}
