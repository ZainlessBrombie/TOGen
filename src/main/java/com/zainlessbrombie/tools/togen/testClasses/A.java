package com.zainlessbrombie.tools.togen.testClasses;

import com.zainlessbrombie.tools.togen.TOValue;

/**
 * Created by mathis on 01.05.18 20:20.
 */
public class A {
    @TOValue.StringArr({"random: {random 1 10} {timestamp} {className} {generated test}  {fieldName} \\\\ {counter} {}","{counter}"})
    String[] s;
    @TOValue.BooleanArr(true)
    boolean[] x;
    @TOValue.BooleanArr(true)
    Boolean[] y;
    @TOValue.ByteArr(100)
    byte[] a;
    @TOValue.ByteArr(101)
    Byte[] b;
    @TOValue.CharArr('c')
    char[] c;
    @TOValue.CharArr('x')
    Character[] d;
    @TOValue.ShortArr(123)
    Short[] e;
    @TOValue.ShortArr(321)
    short[] f;
    @TOValue.IntegerArr(99)
    Integer[] g;
    @TOValue.IntegerArr(90)
    int[] h;
    @TOValue.LongArr(76)
    Long[] i;
    @TOValue.LongArr(23)
    long[] j;
    @TOValue.FloatArr(3.2F)
    Float[] k;
    @TOValue.FloatArr(43.2F)
    float[] l;
    @TOValue.DoubleArr(9.1)
    Double[] m;
    @TOValue.DoubleArr(32.1)
    double[] n;


    @TOValue.EnumArr(enumClass = TestEnum.class,enumOrdinals = {1,0})
    TestEnum[] en;

        /*@TOValue.String("random: {random 1 10} {timestamp} {className} {generated test}  {fieldName} \\\\ {counter} {}")
        String s;
        @TOValue.RandomDouble(upper = 100)
        boolean x;
        @TOValue.Boolean(true)
        Boolean y;
        @TOValue.Byte(100)
        byte a;
        @TOValue.Byte(101)
        Byte b;
        @TOValue.Char('c')
        char c;
        @TOValue.Char('x')
        Character d;
        @TOValue.Short(123)
        Short e;
        @TOValue.Short(321)
        short f;
        @TOValue.Integer(99)
        Integer g;
        @TOValue.Integer(90)
        int h;
        @TOValue.Long(76)
        Long i;
        @TOValue.Long(23)
        long j;
        @TOValue.Float(3.2F)
        Float k;
        @TOValue.Float(43.2F)
        float l;
        @TOValue.Double(9.1)
        Double m;
        @TOValue.Double(32.1)
        double n;*/

        enum TestEnum {
            A,B,C;
        }

    /*
    @TOValue.String("random: {random 1 10} {timestamp} {className} {generated test}  {fieldName} \\\\ {counter} {}")
    String s;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    Boolean y;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    byte a;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    Byte b;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    char c;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    Character d;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    Short e;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    short f;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    Integer g;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    int h;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    Long i;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    long j;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    Float k;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    float l;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    Double m;
    @TOValue.RandomDouble(lower = 2,upper = 2.5)
    double n;
     */
    B rec;


    public A() {

    }

static class B {
    public B() {

    }
    @TOValue.String("path is {path}")
    String pathTest;
}
}
