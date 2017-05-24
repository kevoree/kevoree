package org.kevoree.kevscript;

import org.kevoree.ContainerRoot;
import org.kevoree.KevScriptException;
import org.kevoree.api.KevScriptService;
import org.kevoree.kevscript.resolver.*;
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
    private final Resolver resolver;

    public KevScriptEngine(String registryUrl, String cacheRoot)  {
        // 1 - try to convert tags to real version
        // 2 - try to find type in model
        // 3 - try to find type in file system
        // 4 - try to find type in registry
        // 5 - exception unable to resolve
        RegistryResolver registryResolver = new RegistryResolver(registryUrl);
//        FileSystemResolver fsResolver = new FileSystemResolver(registryResolver, cacheRoot);
        ModelResolver modelResolver = new ModelResolver(registryResolver);
        this.resolver = new TagResolver(modelResolver);
    }

    @Override
    public void execute(final String script, final ContainerRoot model) throws KevScriptException {
        this.execute(script, model, null);
    }

    @Override
    public void execute(final String script, final ContainerRoot model, final HashMap<String, String> ctxVars)
            throws KevScriptException {
        this.executeFromStream(new ByteArrayInputStream(script.getBytes()), model, ctxVars);
    }

    @Override
    public void executeFromStream(final InputStream script, final ContainerRoot model, HashMap<String, String> ctxVars)
            throws KevScriptException {
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
            throw new KevScriptException(parserResult.getError().toString());
        }
    }

    @Override
    public void executeFromStream(final InputStream script, final ContainerRoot model) throws KevScriptException {
        this.executeFromStream(script, model, null);
    }
}
