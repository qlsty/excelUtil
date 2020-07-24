package com.example.test.excel;

import java.util.AbstractSet;
import java.util.Iterator;

class SimpleSet<V> extends AbstractSet<V> {


    private Object[] objects;
    private int count;

    @Override
    public Iterator<V> iterator() {
        return new SimpleIte();
    }

    @Override
    public int size() {
        return count;
    }

    public SimpleSet(Object[] objects) {
        this.objects = objects;
        this.count = objects.length - 1;
    }

    class SimpleIte implements Iterator<V> {

        @Override
        public boolean hasNext() {
            return count <= 0;
        }

        @Override
        public V next() {
            return (V) objects[count];
        }
    }
}
