import org.junit.Before;
import org.junit.Test;
import org.kevoree.ComponentType;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;
import org.kevoree.KControlModel.KControlRule;
import org.kevoree.TypeDefinition;
import org.kevoree.adaptation.control.api.ControlException;
import org.kevoree.adaptation.control.api.SignedModel;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.impl.ChannelImpl;
import org.kevoree.impl.ComponentInstanceImpl;
import org.kevoree.kompare.JavaSePrimitive;
import org.kevoree.tools.control.framework.ControlFactory;
import org.kevoree.tools.control.framework.api.IAccessControl;
import org.kevoree.tools.control.framework.command.CreateRulesCommand;
import org.kevoree.tools.control.framework.command.CreateSignatureCommand;
import org.kevoree.tools.control.framework.impl.SignedModelImpl;
import org.kevoree.tools.control.framework.utils.HelperMatcher;
import org.kevoree.tools.control.framework.utils.HelperSignature;
import org.kevoreeAdaptation.AdaptationPrimitive;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class TestControlFramework
{

    KeyPair key1;
    ContainerRoot current_model;
    ContainerRoot target_model;
    @Before
    public void init() throws NoSuchAlgorithmException, ControlException {

        key1 = HelperSignature.generateKeys(512);

        current_model = KevoreeXmiHelper.loadStream(TestControlFramework.class.getClassLoader().getResourceAsStream("empty_node.kev"));
        target_model = KevoreeXmiHelper.loadStream(TestControlFramework.class.getClassLoader().getResourceAsStream("random_nio_grapher_group.kev"));
    }

    @Test
    public void test_empty_rules() throws ControlException
    {
        IAccessControl accessControl = ControlFactory.createAccessControl();
        CreateRulesCommand rules = new CreateRulesCommand(key1.getPublic());
        rules.setAccessControl(accessControl);
        SignedModel signedmodel = new SignedModelImpl(target_model);

        // create a signature
        CreateSignatureCommand c = new CreateSignatureCommand();
        c.setSignedModel(signedmodel);
        c.setKey(key1);
        c.execute();

        List<AdaptationPrimitive> result =     accessControl.approval("node0", current_model, signedmodel);

        org.junit.Assert.assertEquals(result.size(), 11);
    }

    @Test
    public void test_rules() throws ControlException
    {
        IAccessControl accessControl = ControlFactory.createAccessControl();


        CreateRulesCommand rules = new CreateRulesCommand(key1.getPublic());
        rules.setAccessControl(accessControl);

        KControlRule r1 = rules.addAuthorizedMatcher("typeDefinitions[FakeConsole]");
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        KControlRule r2 = rules.addAuthorizedMatcher("typeDefinitions[BasicGroup]");
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));


        KControlRule r3 = rules.addAuthorizedMatcher("typeDefinitions[Grapher]");
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        KControlRule r4 = rules.addAuthorizedMatcher( "typeDefinitions[NioChannel]");
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        rules.execute();

        SignedModel signedmodel = new SignedModelImpl(target_model);

        // create a signature
        CreateSignatureCommand c = new CreateSignatureCommand();
        c.setSignedModel(signedmodel);
        c.setKey(key1);
        c.execute();

        List<AdaptationPrimitive> result =     accessControl.approval("node0", current_model, signedmodel);

        org.junit.Assert.assertEquals(result.size(), 0);

    }


    @Test
    public void test_Grapher615() throws ControlException
    {
        IAccessControl accessControl = ControlFactory.createAccessControl();

        CreateRulesCommand rules = new CreateRulesCommand(key1.getPublic());
        rules.setAccessControl(accessControl);

        KControlRule r1 = rules.addAuthorizedMatcher("typeDefinitions[FakeConsole]");
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        KControlRule r2 = rules.addAuthorizedMatcher("typeDefinitions[BasicGroup]");
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));




        KControlRule r4 = rules.addAuthorizedMatcher( "typeDefinitions[NioChannel]");
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        rules.execute();

        SignedModel signedmodel = new SignedModelImpl(target_model);

        // create a signature
        CreateSignatureCommand c = new CreateSignatureCommand();
        c.setSignedModel(signedmodel);
        c.setKey(key1);
        c.execute();

        List<AdaptationPrimitive> result =     accessControl.approval("node0", current_model, signedmodel);

        org.junit.Assert.assertEquals("Grapher615",((ComponentInstanceImpl)result.get(0).getRef()).getName());
        org.junit.Assert.assertEquals(JavaSePrimitive.AddInstance(),result.get(0).getPrimitiveType().getName());
        org.junit.Assert.assertEquals(JavaSePrimitive.StartInstance(),result.get(1).getPrimitiveType().getName());
        org.junit.Assert.assertEquals(JavaSePrimitive.UpdateDictionaryInstance(),result.get(2).getPrimitiveType().getName());

    }

    @Test
    public void test_NioChanne995() throws ControlException
    {
        IAccessControl accessControl = ControlFactory.createAccessControl();

        CreateRulesCommand rules = new CreateRulesCommand(key1.getPublic());
        rules.setAccessControl(accessControl);

        KControlRule r1 = rules.addAuthorizedMatcher("typeDefinitions[FakeConsole]");
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        KControlRule r2 = rules.addAuthorizedMatcher("typeDefinitions[BasicGroup]");
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));


        KControlRule r3 = rules.addAuthorizedMatcher("typeDefinitions[Grapher]");
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        KControlRule r4 = rules.addAuthorizedMatcher( "typeDefinitions[NioChannel]");
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
      //  r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        rules.execute();

        SignedModel signedmodel = new SignedModelImpl(target_model);

        // create a signature
        CreateSignatureCommand c = new CreateSignatureCommand();
        c.setSignedModel(signedmodel);
        c.setKey(key1);
        c.execute();

        List<AdaptationPrimitive> result =     accessControl.approval("node0", current_model, signedmodel);

        org.junit.Assert.assertEquals("NioChanne995", ((ChannelImpl) result.get(0).getRef()).getName());
        org.junit.Assert.assertEquals(JavaSePrimitive.UpdateDictionaryInstance(),result.get(0).getPrimitiveType().getName());

    }



}
