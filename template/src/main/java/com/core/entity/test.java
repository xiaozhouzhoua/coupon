package com.core.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.StringJoiner;

@Getter
@Setter
public class test {

    private String name;

    private Integer age;



    @Override
    public String toString() {
        return new StringJoiner(", ", test.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("age=" + age)
                .toString();
    }

    public static void main(String[] args) {
        test test = new test();
        test.setAge(11);
        test.setName("jkl");
        System.out.println(test);
    }
}
