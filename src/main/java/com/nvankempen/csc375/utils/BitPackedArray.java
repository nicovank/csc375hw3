package com.nvankempen.csc375.utils;

import com.nvankempen.csc375.Sudoku;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Allow to condense data so no bits are wasted.
 * This class can be used to store positive integers requiring at most 63 bits to be stored.
 */
public final class BitPackedArray {

    private final int n;
    private final int k;
    private final long[] data;

    /**
     * Creates a new BitPackedArray with n elements, each element requiring at most k bits to be stored.
     * Each element is initialized to zero.
     *
     * @param n the number of elements in the array.
     * @param k the amount of bits that should be allocated for each element. k cannot be greater than 63.
     */
    public BitPackedArray(int n, int k) {
        if (n < 0) throw new NegativeArraySizeException();
        if (k < 0 || k >= 64) throw new IllegalArgumentException();

        this.n = n;
        this.k = k;

        if ((n * k) % Long.SIZE == 0) data = new long[(n * k) / 64];
        else data = new long[(n * k) / 64 + 1];
    }

    private BitPackedArray(int n, int k, long[] data) {
        if (n < 0) throw new NegativeArraySizeException();
        if (k < 0 || k >= 64) throw new IllegalArgumentException();
        if (data.length != (n * k) / 64 + (((n * k) % Long.SIZE == 0) ? 0 : 1))
            throw new IllegalArgumentException();

        this.n = n;
        this.k = k;
        this.data = data;
    }

    /**
     * Sets the value at the given position to the given value. The index must be positive and less than n,
     * while the value must be positive and less than 2^k.
     *
     * @param index The index to update.
     * @param value The value to store.
     */
    public void set(int index, long value) {
        if (index < 0 || index >= n)
            throw new ArrayIndexOutOfBoundsException(index);
        if (value < 0 || value >= (1L << k))
            throw new IllegalArgumentException(value + " cannot be stored with only " + k + " bits");

        for (int i = 0; i < k; ++i) {
            final int j = i + index * k;
            final int p = j >>> 6;
            final int q = j & 63;

            if ((data[p] & (1L << q)) != 0) {
                if ((value & (1L << i)) == 0)
                    data[p] &= ~(1L << q);
            } else {
                if ((value & (1L << i)) != 0)
                    data[p] |= 1L << q;
            }
        }
    }

    /**
     * Retrieves the value stored at the given index. The index must be positive and less than n.
     *
     * @param index The index to retrieve.
     * @return The value that was stored at that index.
     */
    public long get(int index) {
        if (index < 0 || index >= n)
            throw new ArrayIndexOutOfBoundsException(index);

        long value = 0L;

        for (int i = 0; i < k; ++i) {
            final int j = i + index * k;
            final int p = j >>> 6;
            final int q = j & 63;

            if ((data[p] & (1L << q)) != 0)
                value |= 1L << i;
        }

        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(n, k, Arrays.hashCode(data));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BitPackedArray
                && ((BitPackedArray) other).n == n
                && ((BitPackedArray) other).k == k
                && Arrays.equals(((BitPackedArray) other).data, data);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("[");
        builder.append(get(0));

        for (int i = 1; i < n; ++i)
            builder.append(", ").append(get(i));

        builder.append("]");

        return new String(builder);
    }

    public void serialize(OutputStream out) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2 + Long.BYTES * data.length);

        buffer.putInt(n);
        buffer.putInt(k);
        Arrays.stream(data).forEach(buffer::putLong);

        out.write(buffer.array());
    }

    public static BitPackedArray deserialize(ByteBuffer buffer) {
        final int n = buffer.getInt();
        final int k = buffer.getInt();

        if (n < 0) throw new NegativeArraySizeException();
        if (k < 0 || k >= 64) throw new IllegalArgumentException();

        final int size = (n * k) / 64 + (((n * k) % Long.SIZE == 0) ? 0 : 1);
        final long[] data = new long[size];

        for (int i = 0; i < size; ++i) data[i] = buffer.getLong();

        return new BitPackedArray(n, k, data);
    }
}
