/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


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