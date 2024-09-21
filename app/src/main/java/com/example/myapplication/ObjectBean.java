package com.example.myapplication;

import java.util.Objects;

/**
 * desc   :
 * e-mail : 1391324949@qq.com
 * date   : 2024/8/29 20:36
 * author : Roy
 * version: 1.0
 */
public class ObjectBean {

    private String name;
    private int id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectBean that = (ObjectBean) o;
        return id == that.id && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }
}
