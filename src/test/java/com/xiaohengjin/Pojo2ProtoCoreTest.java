package com.xiaohengjin;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class Pojo2ProtoCoreTest {
    @Test
    public void testEnumString(){
        Pojo2ProtoCore pojo2ProtoCore = new Pojo2ProtoCore(Collections.emptyList());
        assertEquals("\tTEST_ENUM_VALUE1 = 1;", pojo2ProtoCore.getFieldNameForEnum("TestEnum", "Value1", 1));
        assertEquals("\tTEST_ENUM_VALUE1 = 1;", pojo2ProtoCore.getFieldNameForEnum("TestEnum", "VALUE1", 1));
        assertEquals("\tTEST_ENUM_SOME_VALUE = 1;", pojo2ProtoCore.getFieldNameForEnum("TestEnum", "SomeValue", 1));
        assertEquals("\tTEST_ENUM_SOME_VALUE = 1;", pojo2ProtoCore.getFieldNameForEnum("TestEnum", "SOME_VALUE", 1));

        // This is really a bug, but cannot do much about such inconsistent naming of enum values.
        assertEquals("\tTEST_ENUM_S_O_M_E__VALUE = 1;", pojo2ProtoCore.getFieldNameForEnum("TestEnum", "SOME_Value", 1));
        assertEquals("\tTEST_ENUM_SOME_VALUE = 1;", pojo2ProtoCore.getFieldNameForEnum("TestEnum", "some_value", 1));

        // Double underscore gets added if the enum name is inconsistent
        assertEquals("\tTEST_ENUM_SOME__VALUE = 1;", pojo2ProtoCore.getFieldNameForEnum("TestEnum", "Some_Value", 1));
        assertEquals("\tTEST_ENUM_SOMEVALUE = 1;", pojo2ProtoCore.getFieldNameForEnum("TestEnum", "somevalue", 1));
        assertEquals("\tTEST_ENUM_SOME_VALUE = 1;", pojo2ProtoCore.getFieldNameForEnum("TestEnum", "someValue", 1));
    }
}