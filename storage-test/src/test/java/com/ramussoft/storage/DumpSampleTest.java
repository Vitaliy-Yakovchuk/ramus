package com.ramussoft.storage;

import java.io.File;

import org.junit.Assume;
import org.junit.Test;

import com.ramussoft.common.Engine;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

public class DumpSampleTest {

    @Test
    public void dump() throws Exception {
        String target = System.getProperty("ramus.dump.target");
        Assume.assumeTrue("ramus.dump.target is not set", target != null);

        File home = new File(target, "home");
        RsfFixture.isolateHome(home);

        String which = System.getProperty("ramus.dump.sample");
        File sample = RsfFixture.sampleFiles().get(0);
        if (which != null)
            for (File candidate : RsfFixture.sampleFiles())
                if (candidate.getName().equals(which))
                    sample = candidate;
        RsfFixture.exportProject(sample, new File(target, "out"));
    }
}
