package org.kevoree.resolver;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

import java.io.File;
import java.util.List;

/**
 *
 * Created by leiko on 2/27/17.
 */
public class MavenResolver {

    private RepositorySystem repoSystem;
    private RepositorySystemSession session;

    private MavenResolver(RepositorySystem repo, RepositorySystemSession session) {
        this.repoSystem = repo;
        this.session = session;
    }

    public PreorderNodeListGenerator resolve(String coordinate, List<RemoteRepository> repositories)
            throws MavenResolverException {
        try {
            Dependency project = new Dependency(new DefaultArtifact(coordinate), "compile");

            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot(project);
            for (RemoteRepository repo : repositories) {
                collectRequest.addRepository(repo);
            }
            DependencyNode node = this.repoSystem.collectDependencies(this.session, collectRequest).getRoot();

            DependencyRequest dependencyRequest = new DependencyRequest();
            dependencyRequest.setRoot(node);

            this.repoSystem.resolveDependencies(session, dependencyRequest);

            PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
            node.accept(nlg);

            return nlg;
        } catch (DependencyCollectionException | DependencyResolutionException e) {
            throw new MavenResolverException("Unable to resolve " + coordinate + " from " + repositories.toString(), e);
        }
    }

    public static class Builder {
        public MavenResolver build() throws MavenResolverException {
            RepositorySystem repo = newRepositorySystem();
            RepositorySystemSession session = newSession(repo);
            return new MavenResolver(repo, session);
        }
    }

    private static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        return locator.getService( RepositorySystem.class );
    }

    private static RepositorySystemSession newSession(RepositorySystem system) throws MavenResolverException {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, findLocalRepository()));

        return session;
    }

    private static LocalRepository findLocalRepository() throws MavenResolverException {
        LocalRepository localRepo = null;

        // first try M2_REPO
        String repoHome = System.getenv("M2_REPO");
        if (repoHome != null) {
            File f = new File(repoHome);
            if (f.exists()) {
                localRepo = new LocalRepository(f);
            }
        }

        // then user home
        if (localRepo == null) {
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                File fh = new File(userHome, ".m2" + File.separator
                        + "repository");
                if (fh.exists()) {
                    localRepo = new LocalRepository(fh);
                }
            }
        }

        // if all fail => throw
        if (localRepo == null) {
            throw new MavenResolverException("Unable to locate local .m2 repository");
        }

        return localRepo;
    }
}
