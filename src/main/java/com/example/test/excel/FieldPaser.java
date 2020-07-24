package com.example.test.excel;

import org.apache.commons.beanutils.ConvertUtils;

public interface FieldPaser<T> {

    T paser(String string, Class<T> clazz);

    class FieldPaser1 implements FieldPaser {

        @Override
        public Object paser(String z, Class c) {
            return ConvertUtils.convert(z, c);
        }
    }

    FieldPaser FieldPaser0 = (c, z) -> ConvertUtils.convert(c, z);
}
