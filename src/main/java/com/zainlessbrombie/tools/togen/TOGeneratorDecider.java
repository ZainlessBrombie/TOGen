package com.zainlessbrombie.tools.togen;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by mathis on 28.04.18 19:25.
 */
public interface TOGeneratorDecider {
    int decide(List<String> path, Field field, Class<?> fieldOwner);
}
