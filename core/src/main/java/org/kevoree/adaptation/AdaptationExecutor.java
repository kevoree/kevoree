package org.kevoree.adaptation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by leiko on 3/1/17.
 */
public class AdaptationExecutor {

    /**
     * Executes given command sequentially and returns a set of already executed command (successfully executed)
     * If the list returned by this method is non-empty it means that something went wrong while executing the given
     * commands thus allowing you to call AdaptationExecutor.undo(AdaptationExecutor.executor(cmds));
     *
     * @param cmds commands to execute
     * @return "rollbackCmds" the list of successfully executed commands
     */
    public static Result execute(List<AdaptationCommand> cmds) {
        Result result = new Result();

        for (AdaptationCommand cmd : cmds) {
            try {
                cmd.execute();
                result.executedCmds.add(cmd);
            } catch (KevoreeAdaptationException e) {
                result.error = e;
                return result;
            }
        }

        return result;
    }

    /**
     * Executes the given commands sequentially. If a command fails the error will be logged using Log.error(...)
     * but the remaining commands will still be executed
     *
     * @param cmds list to execute
     */
    public static List<KevoreeAdaptationException> forceExecute(List<AdaptationCommand> cmds) {
        List<KevoreeAdaptationException> errors = new ArrayList<>();

        for (AdaptationCommand cmd : cmds) {
            try {
                cmd.execute();
            } catch (KevoreeAdaptationException e) {
                errors.add(e);
            }
        }

        return errors;
    }

    public static void undo(List<AdaptationCommand> cmds) throws KevoreeAdaptationException {
        for (AdaptationCommand cmd : cmds) {
            cmd.undo();
        }
    }

    public static final class Result {
        private List<AdaptationCommand> executedCmds = new ArrayList<>();
        private KevoreeAdaptationException error = null;

        public List<AdaptationCommand> getExecutedCmds() {
            return executedCmds;
        }

        public KevoreeAdaptationException getError() {
            return error;
        }
    }
}
