package com.chuang.qapp.utils;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.dozer.MappingException;

public class DozerUtils {

    private static Mapper mapper = new DozerBeanMapper();

    private static <T> T map(Object source, Class<T> destinationClass, String mapId) throws MappingException {
        if (source == null) return null;
        return mapper.map(source, destinationClass, mapId);
    }

    public static <T> T map(Object source, Class<T> destinationClass) throws MappingException {
        if (source == null) return null;
        return mapper.map(source, destinationClass);
    }

    public static void map(Object source, Object destination) throws MappingException {
        if (source == null) return;
        mapper.map(source, destination);
    }
}
