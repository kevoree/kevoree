package org.kevoree.tools.test;

import org.kevoree.tools.test.context.MockContext;
import org.kevoree.tools.test.kevs.MockKevsService;
import org.kevoree.tools.test.model.MockModelService;

/**
 *
 * Created by leiko on 1/16/17.
 */
public class KevoreeMocks {

    // TODO refactor something with Mockito: checkout CentralizedWSGroup tests

    public static MockContext.Builder context() {
        return new MockContext.Builder();
    }

    public static MockModelService.Builder modelService() {
        return new MockModelService.Builder();
    }

    public static MockKevsService.Builder kevsService() {
        return new MockKevsService.Builder();
    }
}
