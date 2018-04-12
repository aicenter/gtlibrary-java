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
