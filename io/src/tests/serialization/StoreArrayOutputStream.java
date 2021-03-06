package tests.serialization;

/* $Id: StoreArrayOutputStream.java 5349 2007-04-07 13:59:32Z ceriel $ */


import ibis.io.DataOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

final class StoreArrayOutputStream extends DataOutputStream {

    long len = 0;

    StoreBuffer buf;

    public int bufferSize() {
        return -1;
    }

    public StoreArrayOutputStream(StoreBuffer buf) {
        this.buf = buf;
    }

    public void writeByte(byte b) {
        buf.writeByte(b);
    }

    public void writeBoolean(boolean b) {
        buf.writeBoolean(b);
    }

    public void writeChar(char b) {
        buf.writeChar(b);
    }

    public void writeShort(short b) {
        buf.writeShort(b);
    }

    public void writeInt(int b) {
        buf.writeInt(b);
    }

    public void writeFloat(float b) {
        buf.writeFloat(b);
    }

    public void writeLong(long b) {
        buf.writeLong(b);
    }

    public void writeDouble(double b) {
        buf.writeDouble(b);
    }

    public void write(int b) {
        buf.write(b);
    }

    public void write(byte[] b) {
        buf.write(b);
    }

    public void write(byte[] b, int off, int len) {
        buf.write(b, off, len);
    }

    public void writeArray(boolean[] a, int off, int len) throws IOException {
        this.len += len;
        buf.writeArray((boolean[]) a, off, len);
    }

    public void writeArray(byte[] a, int off, int len) throws IOException {
        this.len += len;
        buf.writeArray((byte[]) a, off, len);
    }

    public void writeArray(short[] a, int off, int len) throws IOException {
        this.len += 2 * len;
        buf.writeArray((short[]) a, off, len);
    }

    public void writeArray(char[] a, int off, int len) throws IOException {
        this.len += 2 * len;
        buf.writeArray((char[]) a, off, len);
    }

    public void writeArray(int[] a, int off, int len) throws IOException {
        this.len += 4 * len;
        buf.writeArray((int[]) a, off, len);
    }

    public void writeArray(long[] a, int off, int len) throws IOException {
        this.len += 8 * len;
        buf.writeArray((long[]) a, off, len);
    }

    public void writeArray(float[] a, int off, int len) throws IOException {
        this.len += 4 * len;
        buf.writeArray((float[]) a, off, len);
    }

    public void writeArray(double[] a, int off, int len) throws IOException {
        this.len += 8 * len;
        buf.writeArray((double[]) a, off, len);
    }

    public void writeByteBuffer(ByteBuffer arg0) throws IOException {
        buf.writeByteBuffer(arg0);
    }

    public void flush() throws IOException {
    }

    public boolean finished() {
        return true;
    }

    public void finish() throws IOException {
        flush();
    }

    public void close() throws IOException {
        flush();
    }

    public long bytesWritten() {
        return len;
    }

    public void resetBytesWritten() {
        len = 0;
    }
}
