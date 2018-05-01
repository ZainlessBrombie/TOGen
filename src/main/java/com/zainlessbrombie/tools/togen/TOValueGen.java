package com.zainlessbrombie.tools.togen;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by mathis on 28.04.18 19:19.
 */
public interface TOValueGen<T> {
    T generate(List<String> path, Field field, Class<?> fieldOwner, TOGen gen);
}
