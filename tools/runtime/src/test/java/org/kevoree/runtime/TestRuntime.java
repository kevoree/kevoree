package org.kevoree.runtime;

import org.junit.*;
import org.kevoree.Runtime;
import org.kevoree.log.Log;

/**
 *
 * Created by leiko on 3/1/17.
 */
public class TestRuntime {

    private Runtime runtime;

    @Before
    public void setUp() {
        this.runtime = new Runtime();
        Log.set(Log.LEVEL_TRACE);
    }

    @After
    public void tearDown() {
        this.runtime.stop();
    }

    @Test
    @Ignore
    public void testDefault() throws Exception {
        runtime.bootstrap();
    }

    @Test
    @Ignore
    public void testWithScript0() throws Exception {
        runtime.bootstrapFromKevScript(getClass().getResourceAsStream("/script0.kevs"), Assert::assertNull);
    }

    @Test
    @Ignore
    public void testWithWrongFormatModel() throws Exception {
        runtime.bootstrapFromModel(getClass().getResourceAsStream("/wrong-format.json"), Assert::assertNotNull);
    }

    @Test
    @Ignore
    public void testWithModel1() throws Exception {
        runtime.bootstrapFromModel(getClass().getResourceAsStream("/model1.json"), Assert::assertNull);
    }

    @Test
    @Ignore
    public void testWithModel2() throws Exception {
        runtime.bootstrapFromModel(getClass().getResourceAsStream("/model2.json"), Assert::assertNull);
    }
}
