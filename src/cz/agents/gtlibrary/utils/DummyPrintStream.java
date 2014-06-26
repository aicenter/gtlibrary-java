package cz.agents.gtlibrary.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by kail on 6/25/14.
 */
public class DummyPrintStream extends PrintStream {

    private static DummyPrintStream ps = null;

    public DummyPrintStream(OutputStream out) {
        super(out);
    }

    public static PrintStream getDummyPS() {
        if (ps == null)
            ps = new DummyPrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    //no op
                }
            });
        return ps;
    }
}