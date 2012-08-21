package org.kevoree.library.javase.fileSystemGitRepository;

import org.kevoree.library.javase.fileSystem.api.AbstractItem;

/**
 * Created with IntelliJ IDEA.
 * User: pdespagn
 * Date: 5/23/12
 * Time: 3:16 PM
 */
public interface GitRepositoryActions {

    boolean createRepository(String login, String password, String nameRepository);

    void commitRepository(String message, String nom, String email);

    boolean pushRepository(String login, String password);

    AbstractItem importRepository(String login, String password, String url, String nameRepository, String path);

    void createFileToInitRepository(String url, String nomRepo, String directoryPath);

    void cloneRepository(String url, String nameRepository, String pathRepository);

    AbstractItem initRepository(String login, String password, String nameRepository, String pathRepository);

}