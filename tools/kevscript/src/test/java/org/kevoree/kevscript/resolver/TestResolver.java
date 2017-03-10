package org.kevoree.kevscript.resolver;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.kevoree.ContainerRoot;
import org.kevoree.KevScriptException;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kevscript.util.TypeFQN;
import org.kevoree.log.Log;

import java.io.File;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 *
 * Created by leiko on 3/8/17.
 */
public class TestResolver {

    private static int PORT = 3000;
    private static final String CACHE_ROOT = FileUtils.getTempDirectoryPath() + File.separator + "kevoree-cache";
    private static final String BASE_URL = "http://localhost:" + PORT;
    private KevoreeFactory factory = new DefaultKevoreeFactory();
    private WireMockConfiguration conf = options()
            .port(PORT);

    private Resolver resolver;
    private ContainerRoot model;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(conf, false);

    @Before
    public void init() {
        Log.set(Log.LEVEL_TRACE);
        this.resolver = new TagResolver(new ModelResolver(new FileSystemResolver(new RegistryResolver(BASE_URL), CACHE_ROOT)));
        this.model = factory.createContainerRoot();
        factory.root(this.model);
    }

    @Test
    public void simple() throws KevScriptException {
        TypeFQN fqn = new TypeFQN.Builder()
                .namespace("kevoree")
                .name("JavascriptNode")
                .build();
        this.resolver.resolve(fqn, model);
    }

    @Test
    public void complex() throws KevScriptException {
        TypeFQN jsNode = new TypeFQN.Builder()
                .namespace("kevoree")
                .name("JavascriptNode")
                .build();
        this.resolver.resolve(jsNode, model);

        TypeFQN ticker0 = new TypeFQN.Builder()
                .namespace("kevoree")
                .name("Ticker")
                .duVersion(TypeFQN.Version.LATEST)
                .build();
        this.resolver.resolve(ticker0, model);

        TypeFQN ticker1 = new TypeFQN.Builder()
                .namespace("kevoree")
                .name("Ticker")
                .duVersion(TypeFQN.Version.LATEST)
                .build();
        this.resolver.resolve(ticker1, model);
    }

    @Test(expected = KevScriptException.class)
    public void unknownType() throws KevScriptException {
        this.resolver.resolve(new TypeFQN.Builder()
                .namespace("unknown")
                .name("Type")
                .build(), model);
    }
}
