package org.kevoree.kevscript;

import org.kevoree.ContainerRoot;
import org.kevoree.kevscript.statement.*;
import org.kevoree.kevscript.util.KevoreeRegistryResolver;
import org.kevoree.log.Log;
import org.waxeye.ast.IAST;

import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class Interpreter {

    public static void interpret(IAST<Type> stmt, ContainerRoot model, Map<String, String> ctxVars,
                                 KevoreeRegistryResolver resolver) throws Exception {
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

            case Pause:
                // TODO
                throw new KevScriptError("Pause statement is not implemented yet.");

            case Network:
                NetworkStmt.interpret(stmt, model, ctxVars);
                break;

            case Set:
                SetStmt.interpret(stmt, model, ctxVars);
                break;

            case AddBinding:

                break;
            case DelBinding:
                DelBinding.interpret(stmt, model, ctxVars);
                break;
            default:
                Log.info("Deprecated KevScript statement: {}", stmt.getType().name());
                break;
        }
    }
}
