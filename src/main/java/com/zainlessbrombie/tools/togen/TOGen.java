package com.zainlessbrombie.tools.togen;

import com.zainlessbrombie.tools.togen.testClasses.A;

import javax.validation.constraints.NotNull;
import java.lang.reflect.*;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Created by mathis on 27.04.18 13:09.
 */
public class TOGen {

    public static void main(String[] args) {
        System.out.println("starting test");
        A a = newGenerator("counter").withCounter(100).generateTO(A.class);
        System.out.println(a);
    }




    SecureRandom random = new SecureRandom(); // todo collection, set
    private List<String> path = new ArrayList<>();
    private Set<Field> history = new HashSet<>(); // for looping todo

    private static final Map<Class<?>,TOValueGen<?>> defaultGenerators = new HashMap<>();

    static {
        defaultGenerators.put(Map.class, ((path, field, fieldOwner, gen) -> new HashMap<>())); // todo
        defaultGenerators.put(List.class, ((path, field, fieldOwner, gen) -> new ArrayList<>())); // todo
        defaultGenerators.put(LocalDateTime.class, (path, field, fieldOwner, gen) -> LocalDateTime.now());
        defaultGenerators.put(ZonedDateTime.class, ((path, field, fieldOwner, gen) -> ZonedDateTime.now()));
        defaultGenerators.put(LocalTime.class, ((path, field, fieldOwner, gen) -> LocalTime.now()));
        defaultGenerators.put(LocalDate.class, ((path, field, fieldOwner, gen) -> LocalDate.now()));

        for(Class<?> primitive : DefaultGenerators.primitiveClasses) {
            defaultGenerators.put(primitive,DefaultGenerators.annotationGen);
        }
    }

    private Map<Class<?>,List<T2<TOGeneratorDecider,TOValueGen<?>>>> generators = new HashMap<>();

    private String name = "generated";

    private int counter = 0;



    public <T> T generateTO(Class<T> toGenerate) { //todo path for customgen support
        try {
            return generateInternal(toGenerate);
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not generate "+toGenerate+" because of an InstantiationException", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not generate "+toGenerate+" because of an IllegalAccessException. If this doesn't ring a bell this is probably an internal error. If so please open a git ticket if you have the time",e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Could not generate "+toGenerate+" because of an InvocationTargetException, meaning a Constructor threw an exception. Check the bottom of this stacktrace:",e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("This is not possible. You are dreaming. I hope. Unless your enum has no valueOf method. Haha.",e);
        }
    }

    private <T> T generateInternal(Class<T> toGen) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        int previousLength = path.size();
        List<Field> fields = fieldsOfClassAccessible(toGen);
        T ret = getConstructor(toGen).newInstance();


        for(Field field : fields) {
            try {
                path.add(field.getName());

                if(field.getAnnotation(TOValue.Null.class) != null) {
                    field.set(ret,null);
                    continue;
                }
                if(field.getAnnotation(TOValue.Ignore.class) != null) {
                    continue;
                }

                List<T2<TOGeneratorDecider,TOValueGen<?>>> customs = generators.getOrDefault(field.getType(),Collections.emptyList());
                Optional<TOValueGen<?>> max = customs.stream()
                        .map(t2 -> new T2<>(t2.o1.decide(path,field,toGen),t2.o2))
                        .max(Comparator.comparingInt(t2 -> t2.o1))
                        .filter(t2 -> t2.o1 > 0)
                        .map(t2 -> t2.o2);
                TOValueGen<?> temp = null;
                if(max.isPresent()) { // todo [custom] for path
                    field.set(ret,max.get().generate(path,field,toGen,this));
                } else if((temp = defaultGenerators.get(field.getType())) != null) {
                    field.set(ret,temp.generate(path,field,toGen,this));
                } else if(field.getType().getComponentType() != null) {
                    field.set(ret,DefaultGenerators.annotationGen.generate(path,field,toGen,this));
                } else if(field.getType().isEnum()) {
                    Object toSet;
                    if(field.getAnnotation(TOValue.Enum.class) != null) {
                        TOValue.Enum e = field.getAnnotation(TOValue.Enum.class);
                        if(e.enumOrdinal() == -1 && e.enumConstant().equals("")) {
                            throw new RuntimeException("Field "+ TOValue.listToPath(path)+" in "+toGen+" is annotated with @Enum but does not specify what member to use");
                        }
                        if(e.enumOrdinal() != -1 && !e.enumConstant().equals("")) {
                            throw new RuntimeException("FIeld "+ TOValue.listToPath(path)+" in "+toGen+" is annotated with @Enum and specifies a member in two ways. Only use one at a time");
                        }
                        if (e.enumOrdinal() != -1)
                            try {
                                toSet = field.getType().getEnumConstants()[e.enumOrdinal()];
                            } catch (ArrayIndexOutOfBoundsException exc) {throw new RuntimeException("Could not find ordinal"+e.enumOrdinal()+" in "+field.getType());}
                        else
                            try {
                                Method valueOf = e.enumClass().getMethod("valueOf",String.class);
                                valueOf.setAccessible(true);
                                toSet = valueOf.invoke(null,e.enumConstant());
                            } catch(InvocationTargetException exc) {throw new RuntimeException("Could not find enum constant "+e.enumConstant()+" in "+field.getType()+" for field "+field);}
                    } else {
                        toSet = field.getType().getEnumConstants().length == 0 ? null : field.getType().getEnumConstants()[0];
                    }
                    field.set(ret,toSet);
                } else {
                    field.set(ret,generateInternal(field.getType())); // todo loop
                }
            } finally {
                cutToLength(path,previousLength);
            }
        }
        return ret;
    }

    enum Test {
        A,B;
    }


    private static <T> Constructor<T> getConstructor(Class<T> c) {
        try {
            if((c.getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE)) != 0) {
                throw new RuntimeException("Cannot instantiate "+c+" because it is abstract or an interface. Add a generator for this class to use it. If you think there should be a default, consider opening a git ticket ;)");
            }
            Constructor<T> ret = c.getDeclaredConstructor();
            if((ret.getModifiers() & Modifier.PUBLIC) == 0) {
                if (ret.getDeclaredAnnotation(TOValue.UseConstructor.class) == null) // no annotation
                    throw new RuntimeException("The zero arg constructor on "+c+" is not public. If it is supposed to be used, use the @UseConstructor annotation on the constructor");
            }
            ret.setAccessible(true);
            return ret;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find zero argument constructor for "+c+". Note that argument constructors not supported in this version of TOGen yet.");
        }

    }

    /**
     * Cuts a list to the specified length
     */
    private static void cutToLength(List<?> l, int length) {
        if(length < 0)
            throw new RuntimeException("List \""+ Arrays.toString(l.toArray()) +"\" was attempted to be cut to "+length+" which logically does not work. Oops.");
        while(l.size() > length) {
            l.remove(l.size() - 1);
        }
    }
// todo enums?

    /**
     * Get all non final and non static fields of a class and its superclasses + make them accessible
     */
    private static List<Field> fieldsOfClassAccessible(Class<?> c) {
        List<Field> ret = new ArrayList<>();
        ret.addAll(Arrays.asList(c.getDeclaredFields()));
        while((c = c.getSuperclass()) != null && c != Object.class) // todo test if object check works
            ret.addAll(Arrays.asList(c.getDeclaredFields()));
        ret.forEach(field -> field.setAccessible(true));
        return ret;
    }
    private static void fieldAdd(List<Field> l, Field[] fields) { // internal filtering for static & final
        if(l instanceof ArrayList)
            ((ArrayList) l).ensureCapacity(l.size() + fields.length);
        for (Field field : fields) {
            if((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) == 0) {
                l.add(field);
            }
        }
    }



    private TOGen() {

    }

    public static TOGen newGenerator() {
        return new TOGen();
    }

    public static TOGen newGenerator(String generatorName) {
        return newGenerator().withName(generatorName);
    }

    // todo class null reserved for later purposes
    public <T> TOGen withGenerator(@NotNull Class<T> forWhat, @NotNull TOGeneratorDecider condition, @NotNull TOValueGen<T> generator) {
        Objects.requireNonNull(forWhat,"Class forWhat must not be null. Note that class being null is reserved for later use");
        Objects.requireNonNull(forWhat,"Function condition must not be null");
        Objects.requireNonNull(forWhat,"Value generator generator must not be null");
        generators.computeIfAbsent(forWhat,c -> new ArrayList<>()).add(new T2<TOGeneratorDecider,TOValueGen<?>>(condition,generator));
        return this;
    }

    public <T> TOGen withGenerator(@NotNull Class<T> forWhat, TOGeneratorPredicate condition, @NotNull TOValueGen<T> generator) {
        return withGenerator(forWhat,(TOGeneratorDecider) (path,field,owner) -> (condition == null || condition.useGenerator(path,field,owner)) ? 1 : 0, generator);
    }

    public TOGen withName(String generatorName) {
        this.name = generatorName;
        return this;
    }

    public TOGen withCounter(int counter) {
        this.counter = counter;
        return this;
    }

    public String getName() {
        return name;
    }

    public int getCounter() {
        return counter;
    }

    public int getCounterIncr() {
        return counter++;
    }

    private static class T2<T1,T2> {
        private T1 o1;
        private T2 o2;

        public T2() {
        }

        public T2(T1 o1, T2 o2) {
            this.o1 = o1;
            this.o2 = o2;
        }
    }

    public static <T> T defaultGenerate(Class<T> forWhat) {
       return newGenerator("defaultGen").generateTO(forWhat);
    }

    // todo remember that paths are not unique
}
