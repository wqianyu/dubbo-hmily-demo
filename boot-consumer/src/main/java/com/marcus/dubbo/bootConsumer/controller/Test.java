package com.marcus.dubbo.bootConsumer.controller;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        Integer array[] = {
                610700,
                13,
                14,
                15,
                21,
                22,
                23,
                610200,
                31,
                32,
                33,
                34,
                35,
                36,
                37,
                41,
                42,
                43,
                44,
                45,
                46,
                50,
                51,
                610100,
                53,
                54,
                611000,
                52,
                62,
                63,
                64,
                65,
                610500,
                71,
                12,
                81,
                82,
                610900,
                610400,
                610800,
                610300
        };
        List<Integer> list = Lists.newArrayList(array);
        Collections.sort(list);
        System.out.println(list);
        System.out.println(StringUtils.join(list, ","));
    }
}
