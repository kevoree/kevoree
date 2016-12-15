package org.kevoree.kevscript;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.kevoree.ContainerRoot;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 *
 * Created by leiko on 12/15/16.
 */
public abstract class AbstractKevScriptTest {

    private static int PORT = 3000;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private KevoreeFactory factory = new DefaultKevoreeFactory();
    private WireMockConfiguration conf = options()
            .port(PORT);
//            .notifier(new ConsoleNotifier(true)); // DEBUG LOGS

    protected KevScriptEngine kevs;
    protected ContainerRoot model;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(conf, false);

    @Before
    public void init() {
        Log.set(Log.LEVEL_NONE);
        this.kevs = new KevScriptEngine(BASE_URL);
        this.model = factory.createContainerRoot();
        factory.root(this.model);
    }
}
