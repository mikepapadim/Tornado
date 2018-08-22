/*
 * This file is part of Tornado: A heterogeneous programming framework: 
 * https://github.com/beehive-lab/tornado
 *
 * Copyright (c) 2013-2018, APT Group, School of Computer Science,
 * The University of Manchester. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Authors: James Clarkson
 *
 */
package uk.ac.manchester.tornado.api.collections.types;

import static java.lang.Float.MAX_VALUE;
import static java.lang.Float.MIN_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.nio.FloatBuffer.wrap;
import static uk.ac.manchester.tornado.api.collections.types.Float4.loadFromArray;
import static uk.ac.manchester.tornado.api.collections.types.FloatOps.findMaxULP;
import static uk.ac.manchester.tornado.api.collections.types.FloatOps.fmt4m;

import java.nio.FloatBuffer;

import uk.ac.manchester.tornado.api.exceptions.TornadoInternalError;

public class Matrix4x4Float implements PrimitiveStorage<FloatBuffer> {

    /**
     * backing array
     */
    final protected float[] storage;

    /**
     * number of elements in the storage
     */
    final private static int numElements = 16;

    /**
     * Number of rows
     */
    final protected int M = 4;

    /**
     * Number of columns
     */
    final protected int N = 4;

    public Matrix4x4Float() {
        this(new float[numElements]);
    }

    public Matrix4x4Float(float[] array) {
        storage = array;
    }

    private int toIndex(int i, int j) {
        return j + (i * N);
    }

    private float get(int index) {
        return storage[index];
    }

    private void set(int index, float value) {
        storage[index] = value;
    }

    /**
     * Returns the value
     * 
     * @param i
     *            row index
     * @param j
     *            col index
     * @return
     */
    public float get(int i, int j) {
        return storage[toIndex(i, j)];
    }

    /**
     * Sets the value
     * 
     * @param i
     *            row index
     * @param j
     *            col index
     * @return
     */
    public void set(int i, int j, float value) {
        storage[toIndex(i, j)] = value;
    }

    /**
     * Returns the number of rows in this matrix
     * 
     * @return
     */
    public int M() {
        return M;
    }

    /**
     * Returns the number of columns in the matrix
     * 
     * @return
     */
    public int N() {
        return N;
    }

    public Float4 row(int row) {
        int offset = M * row;
        return loadFromArray(storage, offset);
    }

    public Float4 column(int col) {
        return new Float4(get(col), get(col + M), get(col + (2 * M)), get(col + (3 * M)));
    }

    public Float4 diag() {
        return new Float4(get(0), get(1 + M), get(2 + (2 * M)), get(3 + (3 * M)));
    }

    public void fill(float value) {
        for (int i = 0; i < storage.length; i++)
            storage[i] = value;
    }

    public Matrix4x4Float duplicate() {
        Matrix4x4Float matrix = new Matrix4x4Float();
        matrix.set(this);
        return matrix;
    }

    public void set(Matrix4x4Float m) {
        for (int i = 0; i < M; i++) {
            int offset = M * i;
            m.row(i).storeToArray(storage, offset);
        }
    }

    public String toString(String fmt) {
        String str = "";

        for (int i = 0; i < M; i++) {
            str += row(i).toString(fmt) + "\n";
        }
        str.trim();

        return str;
    }

    public String toString() {
        String result = format("MatrixFloat <%d x %d>", M, N);
        result += "\n" + toString(fmt4m);
        return result;
    }

    /**
     * Turns this matrix into an identity matrix
     */
    public void identity() {
        fill(0f);
        set(0, 1f);
        set(1 + M, 1f);
        set(2 + (2 * M), 1f);
        set(3 + (3 * M), 1f);
    }

    @Override
    public void loadFromBuffer(FloatBuffer buffer) {
        asBuffer().put(buffer);
    }

    @Override
    public FloatBuffer asBuffer() {
        return wrap(storage);
    }

    @Override
    public int size() {
        return numElements;
    }

    public FloatingPointError calculateULP(Matrix4x4Float ref) {
        float maxULP = MIN_VALUE;
        float minULP = MAX_VALUE;
        float averageULP = 0f;

        /*
         * check to make sure dimensions match
         */
        if (ref.M != M && ref.N != N) {
            return new FloatingPointError(-1f, 0f, 0f, 0f);
        }

        for (int j = 0; j < M; j++) {
            for (int i = 0; i < N; i++) {
                final float v = get(i, j);
                final float r = ref.get(i, j);

                final float ulpFactor = findMaxULP(v, r);
                averageULP += ulpFactor;
                minULP = min(ulpFactor, minULP);
                maxULP = max(ulpFactor, maxULP);

            }
        }

        averageULP /= (float) M * N;

        return new FloatingPointError(averageULP, minULP, maxULP, -1f);
    }

}
