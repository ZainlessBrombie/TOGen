package com.zainlessbrombie.tools.togen;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by mathis on 28.04.18 19:25.
 */
public interface TOGeneratorPredicate {
    boolean useGenerator(List<String> path, Field field, Class<?> fieldOwner);
}
