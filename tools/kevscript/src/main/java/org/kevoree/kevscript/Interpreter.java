package org.kevoree.kevscript;

import org.kevoree.ContainerRoot;
import org.kevoree.KevScriptException;
import org.kevoree.kevscript.resolver.Resolver;
import org.kevoree.kevscript.statement.*;
import org.kevoree.log.Log;
import org.waxeye.ast.IAST;

import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class Interpreter {

    public static void interpret(IAST<Type> stmt, ContainerRoot model, Map<String, String> ctxVars,
                                 Resolver resolver) throws KevScriptException {
        switch (stmt.getType()) {
            case Add:
                AddStmt.interpret(stmt, model, ctxVars, resolver);
                break;

            case Move:
                MoveStmt.interpret(stmt, model, ctxVars);
                break;

            case Attach:
                AttachStmt.interpret(stmt, model, ctxVars);

                break;
            case Detach:
                DetachStmt.interpret(stmt, model, ctxVars);
                break;

            case AddRepo:
                AddRepoStmt.interpret(stmt, model);
                break;

            case Remove:
                RemoveStmt.interpret(stmt, model, ctxVars);
                break;

            case Start:
                StartStmt.interpret(stmt, model, ctxVars);
                break;

            case Stop:
                StopStmt.interpret(stmt, model, ctxVars);
                break;

            case Network:
                NetworkStmt.interpret(stmt, model, ctxVars);
                break;

            case Set:
                SetStmt.interpret(stmt, model, ctxVars);
                break;

            case AddBinding:
                AddBindingStmt.interpret(stmt, model, ctxVars);
                break;

            case DelBinding:
                DelBindingStmt.interpret(stmt, model, ctxVars);
                break;

            default:
                Log.info("Deprecated KevScript statement: {} ({})", stmt.getType().name(), stmt.getPosition());
                break;
        }
    }
}
