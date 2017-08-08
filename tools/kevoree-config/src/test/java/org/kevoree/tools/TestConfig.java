package org.kevoree.tools;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 *
 * Created by leiko on 8/7/17.
 */
public class TestConfig {

    @Test
    public void testDefaults() {
        KevoreeConfig config = new KevoreeConfig.Builder()
                .useDefault()
                .build();

        assertEquals("registry.kevoree.org", config.getString("registry.host"));
        assertEquals(443, config.getInt("registry.port"));
        assertEquals(true, config.getBoolean("registry.ssl"));
    }

    @Test
    public void testSave() throws IOException {
        KevoreeConfig config = new KevoreeConfig.Builder()
                .useFile(Paths.get("/", "tmp", "config.json"))
                .build();

        config.set("user.access_token", "my_access_token");
        config.set("user.refresh_token", "my_refresh_token");
        final long time = System.currentTimeMillis();
        config.set("user.expires_at", time + 1000);
        config.save();

        config.save();

        assertEquals("my_access_token", config.getString("user.access_token"));
        assertEquals("my_refresh_token", config.getString("user.refresh_token"));
        assertEquals(time + 1000, config.getLong("user.expires_at"));
    }
}
