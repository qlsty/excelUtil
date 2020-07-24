package com.example.test.excel;

@ExcelPaser(required = true)
public class TestBean {

    public final static class FieldPaserImpl implements FieldPaser {

        @Override
        public Object paser(String var,Class clazz) {
            return var + 1000;
        }
    }

    @ExcelPaser(paserClass = FieldPaserImpl.class)
    private String a;

    private String b;

    private Integer c;

    private Integer d;

    public Integer getD() {
        return d;
    }

    public void setD(Integer d) {
        this.d = d;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public Integer getC() {
        return c;
    }

    public void setC(Integer c) {
        this.c = c;
    }


}
