package com.cheng.common.utils.bean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bean 工具類
 *
 * @author cheng
 */
public class BeanUtils extends org.springframework.beans.BeanUtils {
    /**
     * Bean方法名中屬性名開始的下標
     */
    private static final int BEAN_METHOD_PROP_INDEX = 3;

    /**
     * 匹配getter方法的正則表達式
     */
    private static final Pattern GET_PATTERN = Pattern.compile("get(\\p{javaUpperCase}\\w*)");

    /**
     * 匹配setter方法的正則表達式
     */
    private static final Pattern SET_PATTERN = Pattern.compile("set(\\p{javaUpperCase}\\w*)");

    /**
     * Bean屬性複製工具方法。
     *
     * @param dest 目標物件
     * @param src  來源物件
     */
    public static void copyBeanProp(Object dest, Object src) {
        try {
            copyProperties(src, dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取得物件的setter方法。
     *
     * @param obj 物件
     * @return 物件的setter方法列表
     */
    public static List<Method> getSetterMethods(Object obj) {
        // setter方法列表
        List<Method> setterMethods = new ArrayList<Method>();

        // 取得所有方法
        Method[] methods = obj.getClass().getMethods();

        // 尋找setter方法
        for (Method method : methods) {
            Matcher m = SET_PATTERN.matcher(method.getName());
            if (m.matches() && (method.getParameterTypes().length == 1)) {
                setterMethods.add(method);
            }
        }
        // 返回setter方法列表
        return setterMethods;
    }

    /**
     * 取得物件的getter方法。
     *
     * @param obj 物件
     * @return 物件的getter方法列表
     */

    public static List<Method> getGetterMethods(Object obj) {
        // getter方法列表
        List<Method> getterMethods = new ArrayList<Method>();
        // 取得所有方法
        Method[] methods = obj.getClass().getMethods();
        // 尋找getter方法
        for (Method method : methods) {
            Matcher m = GET_PATTERN.matcher(method.getName());
            if (m.matches() && (method.getParameterTypes().length == 0)) {
                getterMethods.add(method);
            }
        }
        // 返回getter方法列表
        return getterMethods;
    }

    /**
     * 檢查Bean方法名中的屬性名是否相等。<br>
     * 如getName()和setName()屬性名一樣，getName()和setAge()屬性名不一樣。
     *
     * @param m1 方法名1
     * @param m2 方法名2
     * @return 屬性名一樣返回true，否則返回false
     */

    public static boolean isMethodPropEquals(String m1, String m2) {
        return m1.substring(BEAN_METHOD_PROP_INDEX).equals(m2.substring(BEAN_METHOD_PROP_INDEX));
    }
}
