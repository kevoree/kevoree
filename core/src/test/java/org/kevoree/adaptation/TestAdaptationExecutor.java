package org.kevoree.adaptation;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.adaptation.cmds.ExecFailCmd;
import org.kevoree.adaptation.cmds.ExecSuccessCmd;
import org.kevoree.adaptation.cmds.ExecSuccessUndoFailCmd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * Created by leiko on 3/1/17.
 */
public class TestAdaptationExecutor {

    @Test
    public void emptyList() {
        List<AdaptationCommand> cmds = new ArrayList<>();
        AdaptationExecutor.Result result = AdaptationExecutor.execute(cmds);
        Assert.assertTrue(result.getExecutedCmds().isEmpty());
    }

    @Test
    public void validCommandList() {
        List<AdaptationCommand> cmds = new ArrayList<>();
        cmds.add(new ExecSuccessCmd());
        cmds.add(new ExecSuccessCmd());
        cmds.add(new ExecSuccessCmd());
        AdaptationExecutor.Result result = AdaptationExecutor.execute(cmds);

        Assert.assertEquals(cmds.size(), result.getExecutedCmds().size());
        Assert.assertNull(result.getError());
    }

    @Test
    public void invalidCommandList() {
        List<AdaptationCommand> cmds = new ArrayList<>();
        AdaptationCommand successCommand = new ExecSuccessCmd();
        cmds.add(successCommand);
        cmds.add(new ExecFailCmd());
        cmds.add(new ExecSuccessCmd());
        AdaptationExecutor.Result result = AdaptationExecutor.execute(cmds);

        Assert.assertEquals(1, result.getExecutedCmds().size());
        Assert.assertEquals(successCommand, result.getExecutedCmds().iterator().next());

        Assert.assertNotNull(result.getError());
    }

    @Test
    public void complexInvalidCommandList() {
        List<AdaptationCommand> cmds = new ArrayList<>();
        AdaptationCommand ok0 = new ExecSuccessCmd();
        AdaptationCommand ok1 = new ExecSuccessCmd();
        AdaptationCommand ok2 = new ExecSuccessCmd();
        cmds.add(ok0);
        cmds.add(ok1);
        cmds.add(ok2);
        cmds.add(new ExecFailCmd());
        cmds.add(new ExecSuccessCmd());
        AdaptationExecutor.Result result = AdaptationExecutor.execute(cmds);

        Assert.assertEquals(3, result.getExecutedCmds().size());
        Iterator<AdaptationCommand> it = result.getExecutedCmds().iterator();
        Assert.assertEquals(ok0, it.next());
        Assert.assertEquals(ok1, it.next());
        Assert.assertEquals(ok2, it.next());

        Assert.assertNotNull(result.getError());
    }

    @Test
    public void validUndoCommandList() throws KevoreeAdaptationException {
        List<AdaptationCommand> cmds = new ArrayList<>();
        AdaptationCommand ok0 = new ExecSuccessCmd();
        AdaptationCommand ok1 = new ExecSuccessCmd();
        AdaptationCommand ok2 = new ExecSuccessCmd();
        cmds.add(ok0);
        cmds.add(ok1);
        cmds.add(ok2);
        cmds.add(new ExecFailCmd());
        cmds.add(new ExecSuccessCmd());
        AdaptationExecutor.Result result = AdaptationExecutor.execute(cmds);

        Assert.assertEquals(3, result.getExecutedCmds().size());
        Iterator<AdaptationCommand> it = result.getExecutedCmds().iterator();
        Assert.assertEquals(ok0, it.next());
        Assert.assertEquals(ok1, it.next());
        Assert.assertEquals(ok2, it.next());

        Assert.assertNotNull(result.getError());

        AdaptationExecutor.undo(result.getExecutedCmds());
    }

    @Test(expected = KevoreeAdaptationException.class)
    public void invalidUndoCommandList() throws KevoreeAdaptationException {
        List<AdaptationCommand> cmds = new ArrayList<>();
        AdaptationCommand execSuccessUndoFailCmd = new ExecSuccessUndoFailCmd();
        cmds.add(execSuccessUndoFailCmd);
        cmds.add(new ExecFailCmd());
        cmds.add(new ExecSuccessCmd());
        AdaptationExecutor.Result result = AdaptationExecutor.execute(cmds);

        Assert.assertEquals(1, result.getExecutedCmds().size());
        Assert.assertEquals(execSuccessUndoFailCmd, result.getExecutedCmds().iterator().next());

        Assert.assertNotNull(result.getError());

        AdaptationExecutor.undo(result.getExecutedCmds());
    }
}
