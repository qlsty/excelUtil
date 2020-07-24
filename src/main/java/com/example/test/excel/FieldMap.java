package com.example.test.excel;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

class FieldMap extends AbstractMap<Field, FieldPaser> {

    private Field[] fields;
    private FieldPaser[] fieldPasers;
    private int count;


    @Override
    public Set<Field> keySet() {
        return new SimpleSet<>(fields);
    }

    public FieldMap() {
        this.fields = new Field[16];
        this.fieldPasers = new FieldPaser[16];
    }

    @Override
    public FieldPaser put(Field key, FieldPaser value) {

        if (count + 1 >= fields.length) {
            extend();
        }
        fields[count + 1] = key;
        fieldPasers[count + 1] = value;
        return value;
    }

    @Override
    public FieldPaser get(Object key) {
        for (int i = 0; i < fields.length; i++) {
            if (key.equals(fields[i])) {
                return fieldPasers[i];
            }
        }
        return null;
    }

    private void extend() {
        double v = count + (count * 0.75);
        int size = Double.valueOf(v).intValue();
        Field[] tmpFields = new Field[size];
        System.arraycopy(fields, 0, tmpFields, 0, count);
        fields = tmpFields;
        FieldPaser[] tmpFieldPasers = new FieldPaser[size];
        System.arraycopy(fieldPasers, 0, tmpFieldPasers, 0, count);
        fieldPasers = tmpFieldPasers;
    }

    @Override
    public Set<Entry<Field, FieldPaser>> entrySet() {
        return null;
    }
}
