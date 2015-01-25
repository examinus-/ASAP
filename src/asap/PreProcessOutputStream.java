package asap;

import java.io.IOException;
import java.io.InputStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author David Jorge Vieira Simões (a21210644@alunos.isec.pt) AKA examinus
 */
public class PreProcessOutputStream extends InputStream {

    private final byte[] data;
    private int nextReadByte;

    PreProcessOutputStream() {
        data = null;
        nextReadByte = 0;
    }

    PreProcessOutputStream(byte[] data) {
        this.data = data;
        nextReadByte = 0;
    }

    @Override
    public synchronized int read() throws IOException {
        if (nextReadByte >= data.length) {
            return -1;
        }

        return ((int) data[nextReadByte++]) & 0xFF;
    }
}
