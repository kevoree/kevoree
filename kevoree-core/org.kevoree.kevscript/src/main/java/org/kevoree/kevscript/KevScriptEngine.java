package org.kevoree.kevscript;

import org.kevoree.ContainerRoot;
import org.kevoree.api.KevScriptService;
import org.kevoree.kevscript.util.KevoreeRegistryResolver;
import org.kevoree.log.Log;
import org.waxeye.ast.IAST;
import org.waxeye.input.InputBuffer;
import org.waxeye.parser.ParseResult;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA. User: duke Date: 25/11/2013 Time: 15:53
 */
public class KevScriptEngine implements KevScriptService {

    private final Parser parser = new Parser();
    private final KevoreeRegistryResolver resolver;

    public KevScriptEngine(String registryUrl)  {
        this.resolver = new KevoreeRegistryResolver(registryUrl);
    }

    @Override
    public void execute(final String script, final ContainerRoot model) throws Exception {
        this.execute(script, model, null);
    }

    @Override
    public void execute(final String script, final ContainerRoot model, final HashMap<String, String> ctxVars)
            throws Exception {
        this.executeFromStream(new ByteArrayInputStream(script.getBytes()), model, ctxVars);
    }

    @Override
    public void executeFromStream(final InputStream script, final ContainerRoot model, HashMap<String, String> ctxVars)
            throws Exception {
        if (ctxVars == null) {
            ctxVars = new HashMap<>();
        }

        // override ctxVar with System.props (ie. -DctxVar.foo=bar
        // -DctxVar.port=4242)
        Properties props = System.getProperties();
        for (String propName : props.stringPropertyNames()) {
            String[] splitted = propName.split("\\.");
            if (splitted[0].equals("ctxVar")) {
                Log.debug("Adding ctxVar {}={}", splitted[1], System.getProperty(propName));
                ctxVars.put(splitted[1], System.getProperty(propName));
            }
        }

        String kevs = new Scanner(script).useDelimiter("\\A").next();
        final ParseResult<Type> parserResult = parser.parse(new InputBuffer(kevs.toCharArray()));
        final IAST<Type> ast = parserResult.getAST();
        if (ast != null) {
            for (IAST<Type> stmt : ast.getChildren()) {
                Interpreter.interpret(stmt.getChildren().get(0), model, ctxVars, resolver);
            }
        } else {
            throw new KevScriptError(parserResult.getError().toString());
        }
    }

    @Override
    public void executeFromStream(final InputStream script, final ContainerRoot model) throws Exception {
        this.executeFromStream(script, model, null);
    }
}
