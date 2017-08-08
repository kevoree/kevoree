package org.kevoree.kevscript.resolver;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.kevoree.ContainerRoot;
import org.kevoree.KevScriptException;
import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kevscript.util.TypeFQN;
import org.kevoree.log.Log;
import org.kevoree.tools.KevoreeConfig;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

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

    private Resolver regResolver;
    private Resolver fsResolver;
    private Resolver modelResolver;
    private Resolver tagResolver;
    private ContainerRoot model;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(conf, false);

    @AfterClass
    public static void afterAll() throws IOException {
        FileUtils.deleteDirectory(new File(CACHE_ROOT));
    }

    @Before
    public void init() throws IOException {
        FileUtils.deleteDirectory(new File(CACHE_ROOT));

        Log.set(Log.LEVEL_TRACE);
        // use "tagResolver" as root resolver
        // the chain is as follow: tag -> model -> fs -> registry
        KevoreeConfig config = new KevoreeConfig.Builder().useDefault().build();
        config.set("registry.host", "localhost");
        config.set("registry.port", PORT);
        config.set("registry.ssl", false);
        this.regResolver = Mockito.spy(new RegistryResolver(config));
        this.fsResolver = Mockito.spy(new FileSystemResolver(regResolver, CACHE_ROOT));
        this.modelResolver = Mockito.spy(new ModelResolver(fsResolver));
        this.tagResolver = Mockito.spy(new TagResolver(modelResolver));
        this.model = emptyModel();
    }

    @Test
    public void registryHit() throws KevScriptException {
        TypeFQN fqn = new TypeFQN.Builder()
                .namespace("kevoree")
                .name("JavascriptNode")
                .build();
        TypeDefinition tdef = this.tagResolver.resolve(fqn, model);

        Assert.assertEquals("42", tdef.getVersion());
    }

    @Test
    @Ignore
    public void fsResolvingOnSecondHitWithErasedModel() throws KevScriptException {
        // this test should end with a hit in the fs on the second resolving
        // because on first hit, registry will answer, then fs/model/tag should
        // update accordingly with the data retrieved from registry
        TypeFQN ticker0 = new TypeFQN.Builder()
                .namespace("kevoree")
                .name("Ticker")
                .duTag(TypeFQN.Version.LATEST)
                .build();
        this.tagResolver.resolve(ticker0, model);

        // on second resolving, the tag LATEST will be resolved to the proper version
        // but because the model has been erased, the model resolver will be useless
        // but the fs has been updated previously, so the chain will stop on it
        TypeFQN ticker1 = new TypeFQN.Builder()
                .namespace("kevoree")
                .name("Ticker")
                .duTag(TypeFQN.Version.LATEST)
                .build();
        // resolve again using the same fqn but an empty model
        this.tagResolver.resolve(ticker1, emptyModel());

        // tag, model and fs resolvers should be used twice
        Mockito.verify(this.tagResolver, Mockito.times(2))
                .resolve(Mockito.any(TypeFQN.class), Mockito.any(ContainerRoot.class));
        Mockito.verify(this.modelResolver, Mockito.times(2))
                .resolve(Mockito.any(TypeFQN.class), Mockito.any(ContainerRoot.class));
        Mockito.verify(this.fsResolver, Mockito.times(2))
                .resolve(Mockito.any(TypeFQN.class), Mockito.any(ContainerRoot.class));

        // registry resolver should not be used for the second resolving
        Mockito.verify(this.regResolver, Mockito.times(1))
                .resolve(Mockito.any(TypeFQN.class), Mockito.any(ContainerRoot.class));
    }

    @Test
    @Ignore
    public void fsResolvingOnSecondHitWithSameModel() throws KevScriptException {
        // this test should end with a hit in the model on the second resolving
        // because on first hit, registry will answer, then fs/model/tag should
        // update accordingly with the data retrieved from registry
        TypeFQN ticker0 = new TypeFQN.Builder()
                .namespace("kevoree")
                .name("Ticker")
                .duTag(TypeFQN.Version.LATEST)
                .build();
        this.tagResolver.resolve(ticker0, model);

        // on second resolving, the tag LATEST will be resolved to the proper version
        // and because the model has been updated on first resolving
        // the model resolver hit will end the chain
        TypeFQN ticker1 = new TypeFQN.Builder()
                .namespace("kevoree")
                .name("Ticker")
                .duTag(TypeFQN.Version.LATEST)
                .build();
        // resolve again using the same fqn and model
        this.tagResolver.resolve(ticker1, model);

        // tag and model resolvers should be used twice
        Mockito.verify(this.tagResolver, Mockito.times(2))
                .resolve(Mockito.any(TypeFQN.class), Mockito.any(ContainerRoot.class));
        Mockito.verify(this.modelResolver, Mockito.times(2))
                .resolve(Mockito.any(TypeFQN.class), Mockito.any(ContainerRoot.class));

        // registry and fs resolvers should not be used for the second resolving
        Mockito.verify(this.fsResolver, Mockito.times(1))
                .resolve(Mockito.any(TypeFQN.class), Mockito.any(ContainerRoot.class));
        Mockito.verify(this.regResolver, Mockito.times(1))
                .resolve(Mockito.any(TypeFQN.class), Mockito.any(ContainerRoot.class));
    }

    @Test(expected = KevScriptException.class)
    public void unknownType() throws KevScriptException {
        this.tagResolver.resolve(new TypeFQN.Builder()
                .namespace("unknown")
                .name("Type")
                .build(), model);
    }

    private ContainerRoot emptyModel() {
        ContainerRoot model = factory.createContainerRoot();
        factory.root(model);
        return model;
    }

    // TODO add more tests plox (@maxleiko)
}
