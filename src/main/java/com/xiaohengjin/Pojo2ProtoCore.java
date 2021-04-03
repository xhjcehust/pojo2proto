package com.xiaohengjin;

import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CaseFormat;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;

/**
 * @author xiaohengjin <xhjcehust@qq.com>
 * Created on 2020-06-29
 */
public class Pojo2ProtoCore {

    private List<String> lines;

    private final List<PsiClass> classList;

    public Pojo2ProtoCore(List<PsiClass> classList) {
        this.classList = classList;
        this.lines = new ArrayList<>();
    }

    public void start() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (PsiClass psiClass: classList) {
            go(psiClass);
        }
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.write(StringUtils.join(lines, "\n"));
        writer.close();
        String protoContent = outputStream.toString();
        CopyPasteManager.getInstance().setContents(new StringSelection(protoContent));
    }

    private void go(PsiClass clazz) {
        if (clazz.isEnum()) {
            goEnum(clazz);
        } else {
            goClass(clazz);
        }
    }

    private void goClass(PsiClass clazz) {
        String className = clazz.getName();
        List<String> protoContentList = new ArrayList<>();
        protoContentList.add("message " + convertClassName(className) + " {");
        PsiField[] fields = clazz.getAllFields();
        int index = 1;
        for (PsiField field: fields) {
            PsiModifierList modifierList = field.getModifierList();
            if (modifierList == null || modifierList.hasModifierProperty(PsiModifier.STATIC)) {
                continue;
            }
            String underscoreName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
            String fieldStr = "\t";
            fieldStr += getProtoType(field.getType());
            fieldStr += String.format(" %s = %d;", underscoreName, index++);
            protoContentList.add(fieldStr);
        }

        protoContentList.add("}\n\n");
        System.out.println(StringUtils.join(protoContentList, "\n"));
        lines.addAll(protoContentList);
    }

    private void goEnum(PsiClass clazz) {
        String className = clazz.getName();
        List<String> protoContentList = new ArrayList<>();
        protoContentList.add("enum " + convertClassName(className) + " {");
        PsiField[] fields = clazz.getFields();
        int index = 0;
        // As per protobuf convention - enums should have a NONE field.
        protoContentList.add(getFieldNameForEnum(className, "NONE", index++));
        for (PsiField field : fields) {
            // Enum values are copied as-is
            protoContentList.add(getFieldNameForEnum(className, field.getName(), index++));
        }

        protoContentList.add("}\n\n");
        System.out.println(StringUtils.join(protoContentList, "\n"));
        lines.addAll(protoContentList);
    }

    @VisibleForTesting
    String getFieldNameForEnum(String enumName, String fieldName, int index) {
        enumName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, enumName);
        if (!fieldName.toUpperCase().equals(fieldName)) {
            // The field name is not in the prescribed UPPER_UNDERSCORE case, do convert.
            fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldName);
        }

        String fieldStr = "\t";
        fieldStr += enumName + "_" + fieldName;
        fieldStr = String.format("%s = %d;", fieldStr, index);
        return fieldStr;
    }

    private String getProtoType(PsiType type) {
        if (type instanceof PsiClassReferenceType) {
            PsiClassReferenceType pt = (PsiClassReferenceType) type;
            String className = pt.getReference().getQualifiedName();
            PsiType[] parameterTypes = pt.getParameters();
            if (parameterTypes.length > 0) {
                if (className.equals(List.class.getName())) {
                    return "repeated " + getProtoType(parameterTypes[0]);
                } else if (className.equals(Map.class.getName())) {
                    String keyProtoType = getProtoType(parameterTypes[0]);
                    String valProtoType = getProtoType(parameterTypes[1]);
                    return String.format("map<%s, %s>", keyProtoType, valProtoType);
                }
            }

            if (Long.class.getName().equals(className)) {
                return "uint64";
            } else if (Integer.class.getName().equals(className)) {
                return "uint32";
            } else if (String.class.getName().equals(className)) {
                return "string";
            } else if (Double.class.getName().equals(className)) {
                return "double";
            } else if (Float.class.getName().equals(className)) {
                return "float";
            } else if (Boolean.class.getName().equals(className)) {
                return "bool";
            }
            return convertClassName(type.getPresentableText());
        } else if (PsiPrimitiveType.LONG.equals(type)) {
            return "uint64";
        } else if (PsiPrimitiveType.INT.equals(type)) {
            return "uint32";
        } else if (PsiPrimitiveType.SHORT.equals(type)) {
            return "uint32";
        } else if (PsiPrimitiveType.DOUBLE.equals(type)) {
            return "double";
        } else if (PsiPrimitiveType.FLOAT.equals(type)) {
            return "float";
        } else if (PsiPrimitiveType.BOOLEAN.equals(type)) {
            return "bool";
        }
        //PsiArrayType
        System.err.println(type.getCanonicalText() + " is not recognized");
        return type.getCanonicalText();
    }

    private static String convertClassName(String className) {
        return className;
        //TODO: add some rule of class name convert
    }
}
