package com.cheng.common.utils.reflect;

import com.cheng.common.core.text.Convert;
import com.cheng.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Date;

/**
 * 反射工具類. 提供呼叫getter/setter方法, 訪問私有變數, 呼叫私有方法, 取得泛型類型Class, 被AOP過的真實類等工具函數.
 *
 * @author cheng
 */
@SuppressWarnings("rawtypes")
public class ReflectUtils {
    private static final String SETTER_PREFIX = "set";

    private static final String GETTER_PREFIX = "get";

    private static final String CGLIB_CLASS_SEPARATOR = "$$";

    private static final Logger logger = LoggerFactory.getLogger(ReflectUtils.class);

    /**
     * 呼叫Getter方法.
     * 支援多級，如：物件名.物件名.方法
     */
    @SuppressWarnings("unchecked")
    public static <E> E invokeGetter(Object obj, String propertyName) {
        Object object = obj;
        for (String name : StringUtils.split(propertyName, ".")) {
            String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(name);
            object = invokeMethod(object, getterMethodName, new Class[]{}, new Object[]{});
        }
        return (E) object;
    }

    /**
     * 呼叫Setter方法, 僅匹配方法名。
     * 支援多級，如：物件名.物件名.方法
     */
    public static <E> void invokeSetter(Object obj, String propertyName, E value) {
        Object object = obj;
        String[] names = StringUtils.split(propertyName, ".");
        for (int i = 0; i < names.length; i++) {
            if (i < names.length - 1) {
                String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(names[i]);
                object = invokeMethod(object, getterMethodName, new Class[]{}, new Object[]{});
            } else {
                String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(names[i]);
                invokeMethodByName(object, setterMethodName, new Object[]{value});
            }
        }
    }

    /**
     * 直接讀取物件屬性值, 無視private/protected修飾符號, 不經過getter函數.
     */
    @SuppressWarnings("unchecked")
    public static <E> E getFieldValue(final Object obj, final String fieldName) {
        Field field = getAccessibleField(obj, fieldName);
        if (field == null) {
            logger.debug("在 [{}] 中，沒有找到 [{}] 欄位 ", obj.getClass(), fieldName);
            return null;
        }
        E result = null;
        try {
            result = (E) field.get(obj);
        } catch (IllegalAccessException e) {
            logger.error("不可能抛出的異常{}", e.getMessage());
        }
        return result;
    }

    /**
     * 直接設定物件屬性值, 無視private/protected修飾符號, 不經過setter函數.
     */
    public static <E> void setFieldValue(final Object obj, final String fieldName, final E value) {
        Field field = getAccessibleField(obj, fieldName);
        if (field == null) {
            // throw new IllegalArgumentException("在 [" + obj.getClass() + "] 中，沒有找到 [" + fieldName + "] 欄位 ");
            logger.debug("在 [{}] 中，沒有找到 [{}] 欄位 ", obj.getClass(), fieldName);
            return;
        }
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            logger.error("不可能抛出的異常: {}", e.getMessage());
        }
    }

    /**
     * 直接呼叫物件方法, 無視private/protected修飾符號.
     * 用於一次性呼叫的情況，否則應使用getAccessibleMethod()函數取得Method後反複呼叫.
     * 同時匹配方法名+參數類型，
     */
    @SuppressWarnings("unchecked")
    public static <E> E invokeMethod(final Object obj, final String methodName, final Class<?>[] parameterTypes,
                                     final Object[] args) {
        if (obj == null || methodName == null) {
            return null;
        }
        Method method = getAccessibleMethod(obj, methodName, parameterTypes);
        if (method == null) {
            logger.debug("在 [{}] 中，沒有找到 [{}] 方法 ", obj.getClass(), methodName);
            return null;
        }
        try {
            return (E) method.invoke(obj, args);
        } catch (Exception e) {
            String msg = "method: " + method + ", obj: " + obj + ", args: " + Arrays.toString(args);
            throw convertReflectionExceptionToUnchecked(msg, e);
        }
    }

    /**
     * 直接呼叫物件方法, 無視private/protected修飾符號，
     * 用於一次性呼叫的情況，否則應使用getAccessibleMethodByName()函數取得Method後反複呼叫.
     * 只匹配函數名，如果有多個同名函數呼叫第一個。
     */
    @SuppressWarnings("unchecked")
    public static <E> E invokeMethodByName(final Object obj, final String methodName, final Object[] args) {
        Method method = getAccessibleMethodByName(obj, methodName, args.length);
        if (method == null) {
            // 如果為空不報錯，直接返回空。
            logger.debug("在 [{}] 中，沒有找到 [{}] 方法 ", obj.getClass(), methodName);
            return null;
        }
        try {
            // 類型轉換（將參數數據類型轉換為目標方法參數類型）
            Class<?>[] cs = method.getParameterTypes();
            for (int i = 0; i < cs.length; i++) {
                if (args[i] != null && !args[i].getClass().equals(cs[i])) {
                    if (cs[i] == String.class) {
                        args[i] = Convert.toStr(args[i]);
                        if (StringUtils.endsWith((String) args[i], ".0")) {
                            args[i] = StringUtils.substringBefore((String) args[i], ".0");
                        }
                    } else if (cs[i] == Integer.class) {
                        args[i] = Convert.toInt(args[i]);
                    } else if (cs[i] == Long.class) {
                        args[i] = Convert.toLong(args[i]);
                    } else if (cs[i] == Double.class) {
                        args[i] = Convert.toDouble(args[i]);
                    } else if (cs[i] == Float.class) {
                        args[i] = Convert.toFloat(args[i]);
                    } else if (cs[i] == Date.class) {
                        if (args[i] instanceof String) {
                            args[i] = DateUtils.parseDate(args[i]);
                        } else {
                            args[i] = DateUtil.getJavaDate((Double) args[i]);
                        }
                    } else if (cs[i] == boolean.class || cs[i] == Boolean.class) {
                        args[i] = Convert.toBool(args[i]);
                    }
                }
            }
            return (E) method.invoke(obj, args);
        } catch (Exception e) {
            String msg = "method: " + method + ", obj: " + obj + ", args: " + Arrays.toString(args);
            throw convertReflectionExceptionToUnchecked(msg, e);
        }
    }

    /**
     * 循環向上轉型, 取得物件的DeclaredField, 並強制設定為可訪問.
     * 如向上轉型到Object仍無法找到, 返回null.
     */
    public static Field getAccessibleField(final Object obj, final String fieldName) {
        // 為空不報錯。直接返回 null
        if (obj == null) {
            return null;
        }
        Validate.notBlank(fieldName, "fieldName can't be blank");
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                makeAccessible(field);
                return field;
            } catch (NoSuchFieldException e) {
                continue;
            }
        }
        return null;
    }

    /**
     * 循環向上轉型, 取得物件的DeclaredMethod,並強制設定為可訪問.
     * 如向上轉型到Object仍無法找到, 返回null.
     * 匹配函數名+參數類型。
     * 用於方法需要被多次呼叫的情況. 先使用本函數先取得Method,然後呼叫Method.invoke(Object obj, Object... args)
     */
    public static Method getAccessibleMethod(final Object obj, final String methodName,
                                             final Class<?>... parameterTypes) {
        // 為空不報錯。直接返回 null
        if (obj == null) {
            return null;
        }
        Validate.notBlank(methodName, "methodName can't be blank");
        for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
            try {
                Method method = searchType.getDeclaredMethod(methodName, parameterTypes);
                makeAccessible(method);
                return method;
            } catch (NoSuchMethodException e) {
                continue;
            }
        }
        return null;
    }

    /**
     * 循環向上轉型, 取得物件的DeclaredMethod,並強制設定為可訪問.
     * 如向上轉型到Object仍無法找到, 返回null.
     * 只匹配函數名。
     * 用於方法需要被多次呼叫的情況. 先使用本函數先取得Method,然後呼叫Method.invoke(Object obj, Object... args)
     */
    public static Method getAccessibleMethodByName(final Object obj, final String methodName, int argsNum) {
        // 為空不報錯。直接返回 null
        if (obj == null) {
            return null;
        }
        Validate.notBlank(methodName, "methodName can't be blank");
        for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
            Method[] methods = searchType.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && method.getParameterTypes().length == argsNum) {
                    makeAccessible(method);
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * 改變private/protected的方法為public，尽量不呼叫實際改動的語句，避免JDK的SecurityManager抱怨。
     */
    @SuppressWarnings("deprecation")
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    /**
     * 改變private/protected的成員變數為public，尽量不呼叫實際改動的語句，避免JDK的SecurityManager抱怨。
     */
    @SuppressWarnings("deprecation")
    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
                || Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    /**
     * 通過反射, 取得Class定義中聲明的泛型參數的類型, 註意泛型必須定義在父類處
     * 如無法找到, 返回Object.class.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassGenricType(final Class clazz) {
        return getClassGenricType(clazz, 0);
    }

    /**
     * 通過反射, 取得Class定義中聲明的父類的泛型參數的類型.
     * 如無法找到, 返回Object.class.
     */
    public static Class getClassGenricType(final Class clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            logger.debug(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            logger.debug("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
                    + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            logger.debug(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class) params[index];
    }

    public static Class<?> getUserClass(Object instance) {
        if (instance == null) {
            throw new RuntimeException("Instance must not be null");
        }
        Class clazz = instance.getClass();
        if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && !Object.class.equals(superClass)) {
                return superClass;
            }
        }
        return clazz;

    }

    /**
     * 將反射時的checked exception轉換為unchecked exception.
     */
    public static RuntimeException convertReflectionExceptionToUnchecked(String msg, Exception e) {
        if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
                || e instanceof NoSuchMethodException) {
            return new IllegalArgumentException(msg, e);
        } else if (e instanceof InvocationTargetException) {
            return new RuntimeException(msg, ((InvocationTargetException) e).getTargetException());
        }
        return new RuntimeException(msg, e);
    }
}
