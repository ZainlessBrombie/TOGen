package com.zainlessbrombie.tools.togen;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mathis on 29.04.18 13:39.
 */
public class DefaultGenerators {

    static final Set<Class<?>> primitiveClasses = new HashSet<>();

    static {
        primitiveClasses.add(Boolean.class);
        primitiveClasses.add(boolean.class);
        primitiveClasses.add(Byte.class);
        primitiveClasses.add(byte.class);
        primitiveClasses.add(Character.class);
        primitiveClasses.add(char.class);
        primitiveClasses.add(Short.class);
        primitiveClasses.add(short.class);
        primitiveClasses.add(Integer.class);
        primitiveClasses.add(int.class);
        primitiveClasses.add(Long.class);
        primitiveClasses.add(long.class);
        primitiveClasses.add(Float.class);
        primitiveClasses.add(float.class);
        primitiveClasses.add(Double.class);
        primitiveClasses.add(double.class);
        primitiveClasses.add(String.class);
        primitiveClasses.add(Class.class);
    }


    /*
    returns the default value for the specified type
     */
    private static final TOValueGen<?> defaultGen = ((path, field, fieldOwner, gen) -> {
        Class<?> type = field.getType();
        if(type == Boolean.class || type == boolean.class) {
            return false;
        }
        if(type == Byte.class || type == byte.class) {
            return (byte)0;
        }
        if(type == Character.class || type == char.class) {
            return (char)0;
        }
        if(type == Short.class || type == short.class) {
            return (short)0;
        }
        if(type == Integer.class || type == int.class) {
            return 0;
        }
        if(type == Long.class || type == long.class) {
            return 0L;
        }
        if(type == Float.class || type == float.class) {
            return 0F;
        }
        if(type == Double.class || type == double.class) {
            return 0D;
        }
        if(type == String.class) {
            return fieldOwner.getSimpleName() + '_' + field.getName() + "_" + gen.getName();
        }
        if(type == Class.class) {
            return Object.class;
        }
        if(type.isArray()) {
            return Array.newInstance(type.getComponentType(),0);
        }
        throw new RuntimeException("Type "+type+" is not a known primitive and cannot be generated. This should not have happened.");
    });

    /*
    generates a value for a primitive or string. Defaults if no annotations were found.
     */
    static final TOValueGen<?> annotationGen = ((path, field, fieldOwner, gen) -> {
        if(field.getAnnotation(TOValue.String.class) != null) {
            return stringAnnotationGen(path,field,fieldOwner,gen,field.getAnnotation(TOValue.String.class).value());
        }
        if(field.getAnnotation(TOValue.Random.class) != null) {
            TOValue.Random r = field.getAnnotation(TOValue.Random.class);
            return doCast(field.getType(),longRandomGen(gen,r.lower(),r.upper()));
        }
        if(field.getAnnotation(TOValue.RandomDouble.class) != null) {
            TOValue.RandomDouble r = field.getAnnotation(TOValue.RandomDouble.class);
            return doCast(field.getType(),doubleRandomGen(gen,r.lower(),r.upper()));
        }
        if(field.getAnnotation(TOValue.RandomArr.class) != null) {
            TOValue.RandomArr r = field.getAnnotation(TOValue.RandomArr.class);
            Class<?> target = field.getType().getComponentType();
            if (target == Object.class || target == Number.class)
                target = Long.class;
            Object ret = Array.newInstance(target,r.arrayLength());
            for (int i = 0; i < r.arrayLength(); i++) {
                Array.set(ret,i,doCast(target,longRandomGen(gen,r.lower(),r.upper())));
            }
            return ret;
        }
        if(field.getAnnotation(TOValue.RandomDoubleArr.class) != null) {
            TOValue.RandomDoubleArr r = field.getAnnotation(TOValue.RandomDoubleArr.class);
            Class<?> target = field.getType().getComponentType();
            if (target == Object.class || target == Number.class)
                target = Double.class;
            if(target == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            Object ret = Array.newInstance(target,r.arrayLength());
            for (int i = 0; i < r.arrayLength(); i++) {
                Array.set(ret,i,doCast(target,doubleRandomGen(gen,r.lower(),r.upper())));
            }
            return ret;
        }
        if(field.getAnnotation(TOValue.Null.class) != null) {
            return null;
        }
        if(field.getAnnotation(TOValue.NullArr.class) != null) {
            if(field.getType().getComponentType() == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            return Array.newInstance(field.getType().getComponentType(),field.getAnnotation(TOValue.NullArr.class).arrayLength());
        }

        if(field.getAnnotation(TOValue.Boolean.class) != null) {
            return field.getAnnotation(TOValue.Boolean.class).value();
        }

        if(field.getAnnotation(TOValue.Byte.class) != null) {
            return field.getAnnotation(TOValue.Byte.class).value();
        }

        if(field.getAnnotation(TOValue.Char.class) != null) {
            return field.getAnnotation(TOValue.Char.class).value();
        }

        if(field.getAnnotation(TOValue.Short.class) != null) {
            return field.getAnnotation(TOValue.Short.class).value();
        }

        if(field.getAnnotation(TOValue.Integer.class) != null) {
            return field.getAnnotation(TOValue.Integer.class).value();
        }

        if(field.getAnnotation(TOValue.Long.class) != null) {
            return field.getAnnotation(TOValue.Long.class).value();
        }

        if(field.getAnnotation(TOValue.Float.class) != null) {
            return field.getAnnotation(TOValue.Float.class).value();
        }

        if(field.getAnnotation(TOValue.Double.class) != null) {
            return field.getAnnotation(TOValue.Double.class).value();
        }

        if(field.getAnnotation(TOValue.Class.class) != null) {
            return field.getAnnotation(TOValue.Class.class).value();
        } // todo @ignore



        if(field.getAnnotation(TOValue.BooleanArr.class) != null) {
            TOValue.BooleanArr arr = field.getAnnotation(TOValue.BooleanArr.class);
            if(field.getType().getComponentType() == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            Object ret = Array.newInstance(field.getType().getComponentType(),arr.value().length);
            for (int i = 0; i < arr.value().length; i++) {
                Array.set(ret,i,arr.value()[i]);
            }
            return ret;
        }

        if(field.getAnnotation(TOValue.ByteArr.class) != null) {
            TOValue.ByteArr arr = field.getAnnotation(TOValue.ByteArr.class);
            if(field.getType().getComponentType() == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            Object ret = Array.newInstance(field.getType().getComponentType(),arr.value().length);
            for (int i = 0; i < arr.value().length; i++) {
                Array.set(ret,i,arr.value()[i]);
            }
            return ret;
        }

        if(field.getAnnotation(TOValue.CharArr.class) != null) {
            TOValue.CharArr arr = field.getAnnotation(TOValue.CharArr.class);
            if(field.getType().getComponentType() == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            Object ret = Array.newInstance(field.getType().getComponentType(),arr.value().length);
            for (int i = 0; i < arr.value().length; i++) {
                Array.set(ret,i,arr.value()[i]);
            }
            return ret;
        }

        if(field.getAnnotation(TOValue.ShortArr.class) != null) {
            TOValue.ShortArr arr = field.getAnnotation(TOValue.ShortArr.class);
            if(field.getType().getComponentType() == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            Object ret = Array.newInstance(field.getType().getComponentType(),arr.value().length);
            for (int i = 0; i < arr.value().length; i++) {
                Array.set(ret,i,arr.value()[i]);
            }
            return ret;
        }

        if(field.getAnnotation(TOValue.IntegerArr.class) != null) {
            TOValue.IntegerArr arr = field.getAnnotation(TOValue.IntegerArr.class);
            if(field.getType().getComponentType() == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            Object ret = Array.newInstance(field.getType().getComponentType(),arr.value().length);
            for (int i = 0; i < arr.value().length; i++) {
                Array.set(ret,i,arr.value()[i]);
            }
            return ret;
        }

        if(field.getAnnotation(TOValue.LongArr.class) != null) {
            TOValue.LongArr arr = field.getAnnotation(TOValue.LongArr.class);
            if(field.getType().getComponentType() == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            Object ret = Array.newInstance(field.getType().getComponentType(),arr.value().length);
            for (int i = 0; i < arr.value().length; i++) {
                Array.set(ret,i,arr.value()[i]);
            }
            return ret;
        }

        if(field.getAnnotation(TOValue.FloatArr.class) != null) {
            TOValue.FloatArr arr = field.getAnnotation(TOValue.FloatArr.class);
            if(field.getType().getComponentType() == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            Object ret = Array.newInstance(field.getType().getComponentType(),arr.value().length);
            for (int i = 0; i < arr.value().length; i++) {
                Array.set(ret,i,arr.value()[i]);
            }
            return ret;
        }

        if(field.getAnnotation(TOValue.DoubleArr.class) != null) {
            TOValue.DoubleArr arr = field.getAnnotation(TOValue.DoubleArr.class);
            if(field.getType().getComponentType() == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            Object ret = Array.newInstance(field.getType().getComponentType(),arr.value().length);
            for (int i = 0; i < arr.value().length; i++) {
                Array.set(ret,i,arr.value()[i]);
            }
            return ret;
        }

        if(field.getAnnotation(TOValue.ClassArr.class) != null) {
            TOValue.ClassArr arr = field.getAnnotation(TOValue.ClassArr.class);
            if(field.getType().getComponentType() == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            Object ret = Array.newInstance(field.getType().getComponentType(),arr.value().length);
            for (int i = 0; i < arr.value().length; i++) {
                Array.set(ret,i,arr.value()[i]);
            }
            return ret;
        }

        if(field.getAnnotation(TOValue.StringArr.class) != null) {
            TOValue.StringArr arr = field.getAnnotation(TOValue.StringArr.class);
            if(field.getType().getComponentType() == null)
                throw new RuntimeException("Error: could not generate array for "+TOValue.listToPath(path)+" of type "+field.getType()+" because it is not an array type");
            Object ret = Array.newInstance(field.getType().getComponentType(),arr.value().length);
            for (int i = 0; i < arr.value().length; i++) {
                Array.set(ret,i,stringAnnotationGen(path,field,fieldOwner,gen,arr.value()[i]));
            }
            return ret;
        }

        if(field.getAnnotation(TOValue.EnumArr.class) != null) {
            TOValue.EnumArr e = field.getAnnotation(TOValue.EnumArr.class);
            if (e.enumConstants().length != 0 && e.enumOrdinals().length != 0)
                throw new RuntimeException("@EnumArray in "+fieldOwner+" in path "+TOValue.listToPath(path)+" specifies both enum ordinals and constants. This is not allowed, only use one");
            Object ret = null;
            if (e.enumConstants().length != 0) {
                ret = Array.newInstance(field.getType().getComponentType(), e.enumConstants().length);
                Method m;
                try {
                    m = e.enumClass().getMethod("valueOf", String.class);
                    m.setAccessible(true);
                } catch (NoSuchMethodException e1) {
                    throw new RuntimeException("Could not find valueOf method, this should not have happened",e1);
                }
                for (int i = 0; i < e.enumConstants().length; i++) {
                    try {
                        Array.set(ret,i,m.invoke(null,e.enumConstants()[i]));
                    } catch (IllegalAccessException e1) {
                        throw new RuntimeException("This is impossible. Well almost, did you do something to java security?",e1);
                    } catch (InvocationTargetException e1) {
                        throw new RuntimeException("An enum constant for "+field+" could not be found");
                    }
                }
            } else if(e.enumOrdinals().length != 0) {
               ret = Array.newInstance(field.getType().getComponentType(),e.enumOrdinals().length);
                for (int i = 0; i < e.enumOrdinals().length; i++) {
                    try {
                        Array.set(ret, i, e.enumClass().getEnumConstants()[e.enumOrdinals()[i]]);
                    } catch (ArrayIndexOutOfBoundsException e1) {
                        throw new RuntimeException("Could not find enum ordinal "+e.enumOrdinals()[i]+" in "+e.enumClass()+" for "+field);
                    }
                }
            }
            if(ret != null) { //todo this code is not pretty.
                return ret;
            }
        }

        return defaultGen.generate(path,field,fieldOwner,gen);
    });



    private static final Pattern counterP = Pattern.compile("(?:[^\\\\]|^)(?:\\\\\\\\)*(\\{counter})");
    private static final Pattern uuidP = Pattern.compile("(?:[^\\\\]|^)(?:\\\\\\\\)*(\\{uuid})");
    private static final Pattern randomP = Pattern.compile("(?:[^\\\\]|^)(?:\\\\\\\\)*(\\{random [0-9]+ [0-9]+})");
    private static final Pattern nameP = Pattern.compile("(?:[^\\\\]|^)(?:\\\\\\\\)*(\\{name})");
    private static final Pattern fieldNameP = Pattern.compile("(?:[^\\\\]|^)(?:\\\\\\\\)*(\\{fieldName})");
    private static final Pattern pathP = Pattern.compile("(?:[^\\\\]|^)(?:\\\\\\\\)*(\\{path})");
    private static final Pattern timestampP = Pattern.compile("(?:[^\\\\]|^)(?:\\\\\\\\)*(\\{timestamp})");
    private static final Pattern classNameP = Pattern.compile("(?:[^\\\\]|^)(?:\\\\\\\\)*(\\{className})");
    private static final Pattern generatedP = Pattern.compile("(?:[^\\\\]|^)(?:\\\\\\\\)*(\\{generated [a-zA-Z_$0-9]+})");

    /*
         * {counter} injects the generators counter and increments it
         * {uuid} inserts a random uuid
         * {random [lower] [upper]} replaces by a random integer in the specified range, upper exclusive. Example: {random 0 10}
         * {name} inserts the test generators name
         * {fieldName} inserts the field name
         * {path} inserts the whole path
         * {timestamp} inserts the timestamp as returned by System.currentTimeMillis()
         * {className} inserts the simpleName of the class currently being generated (useful for annotations in generic superclasses)
         * {generated [type]} not yet available. Will insert a value generated by the (potentially custom) the named generator, like "firstname", "firstnameF", "firstnameM", "username", "password" etc
         *
     */
    private static String stringAnnotationGen(List<String> path, Field field, Class fieldOwner, TOGen gen, String fieldValue) {
        Matcher matcher = counterP.matcher(fieldValue);
        if(matcher.find()) {
            String a = fieldValue.substring(0,matcher.start(1));
            String b = fieldValue.substring(matcher.end(1));
            int c = gen.getCounterIncr();
            return stringAnnotationGen(path,field,fieldOwner,gen,a) + c + stringAnnotationGen(path,field,fieldOwner,gen,b);
        }

        matcher = uuidP.matcher(fieldValue);
        if(matcher.find()) {
            String a = fieldValue.substring(0,matcher.start(1));
            String b = fieldValue.substring(matcher.end(1));
            return stringAnnotationGen(path,field,fieldOwner,gen,a) + UUID.randomUUID().toString() + stringAnnotationGen(path,field,fieldOwner,gen,b);
        }

        matcher = randomP.matcher(fieldValue);
        if(matcher.find()) {
            String a = fieldValue.substring(0,matcher.start(1));
            String b = fieldValue.substring(matcher.end(1));
            String temp = fieldValue.substring(matcher.start(1),matcher.end(1)).substring("{random ".length());
            temp = temp.substring(0,temp.length() - 1);
            String[] numbersRaw = temp.split(" ");
            long lower, upper;
            try {
                lower = Long.parseLong(numbersRaw[0]);
                upper = Long.parseLong(numbersRaw[1]);
            } catch(NumberFormatException e) {
                throw new RuntimeException("Could not parse either "+numbersRaw[0]+" or "+numbersRaw[1]+", probably because it exceeds the bounds of a long");
            }
            return stringAnnotationGen(path,field,fieldOwner,gen,a) + longRandomGen(gen,lower,upper) + stringAnnotationGen(path,field,fieldOwner,gen,b);
        }

        matcher = nameP.matcher(fieldValue);
        if(matcher.find()) {
            String a = fieldValue.substring(0,matcher.start(1));
            String b = fieldValue.substring(matcher.end(1));
            return stringAnnotationGen(path,field,fieldOwner,gen,a) + gen.getName() + stringAnnotationGen(path,field,fieldOwner,gen,b);
        }

        matcher = fieldNameP.matcher(fieldValue);
        if(matcher.find()) {
            String a = fieldValue.substring(0,matcher.start(1));
            String b = fieldValue.substring(matcher.end(1));
            return stringAnnotationGen(path,field,fieldOwner,gen,a) + field.getName() + stringAnnotationGen(path,field,fieldOwner,gen,b);
        }

        matcher = pathP.matcher(fieldValue);
        if(matcher.find()) {
            String a = fieldValue.substring(0,matcher.start(1));
            String b = fieldValue.substring(matcher.end(1));
            return stringAnnotationGen(path,field,fieldOwner,gen,a) + TOValue.listToPath(path) + stringAnnotationGen(path,field,fieldOwner,gen,b);
        }

        matcher = timestampP.matcher(fieldValue);
        if(matcher.find()) {
            String a = fieldValue.substring(0,matcher.start(1));
            String b = fieldValue.substring(matcher.end(1));
            return stringAnnotationGen(path,field,fieldOwner,gen,a) + System.currentTimeMillis() + stringAnnotationGen(path,field,fieldOwner,gen,b);
        }

        matcher = classNameP.matcher(fieldValue);
        if(matcher.find()) {
            String a = fieldValue.substring(0,matcher.start(1));
            String b = fieldValue.substring(matcher.end(1));
            return stringAnnotationGen(path,field,fieldOwner,gen,a) + fieldOwner.getSimpleName() + stringAnnotationGen(path,field,fieldOwner,gen,b);
        }

        matcher = generatedP.matcher(fieldValue);
        if(matcher.find()) {
            String a = fieldValue.substring(0,matcher.start(1));
            String b = fieldValue.substring(matcher.end(1));
            String temp = fieldValue.substring(matcher.start(1),matcher.end(1)).substring("{generated ".length());
            //todo
            return stringAnnotationGen(path,field,fieldOwner,gen,a) + "[GENERATORS NOT AVAILABLE IN THIS VERSION OF TOGen]" + stringAnnotationGen(path,field,fieldOwner,gen,b);
        }
        return fieldValue.replaceAll("((?:[^\\\\]|^)(?:\\\\\\\\)*)\\\\([^\\\\])","$1$2").replaceAll("\\\\\\\\","\\\\");
    }

    private static Long longRandomGen(TOGen gen, long lower, long upper) {
        //noinspection ConstantConditions
        return gen.random.longs(1,lower,upper).findFirst().getAsLong();
    }


    private static Double doubleRandomGen(TOGen gen, double lower, double upper) {
        return gen.random.nextDouble() * (upper - lower) + lower;
    }


    private static Object doCast(Class targetType, Number source) {
        if(targetType == Byte.class || targetType == byte.class) {
            return source.byteValue();
        }
        if(targetType == Boolean.class || targetType == boolean.class) {
            return source.longValue() != 0;
        }
        if(targetType == Character.class || targetType == char.class) {
            return (char)source.intValue();
        }
        if(targetType == Short.class || targetType == short.class) {
            return source.shortValue();
        }
        if(targetType == Integer.class || targetType == int.class) {
            return source.intValue();
        }
        if(targetType == Long.class || targetType == long.class) {
            return source.longValue();
        }
        if(targetType == Float.class || targetType == float.class) {
            return source.floatValue();
        }
        if(targetType == Double.class || targetType == double.class) {
            return source.doubleValue();
        }
        throw new RuntimeException("Cannot assign "+source+" to "+targetType+" as that type is not one of the known number compatible primitives");
    }
}
