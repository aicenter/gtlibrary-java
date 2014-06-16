package cz.agents.gtlibrary.utils.io;

import java.io.IOException;
import java.io.OutputStream;

public class EmptyPrintStream extends OutputStream {

    private static EmptyPrintStream instance;

    private EmptyPrintStream() {
    }

    public static  EmptyPrintStream getInstance() {
        if(instance == null)
            instance = new EmptyPrintStream();
        return instance;
    }
    @Override
    public void write(int b) throws IOException {
    }
}
