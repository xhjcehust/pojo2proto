package com.xiaohengjin;

import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

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
        lines.add("syntax = \"proto3\";\n");
        for (PsiClass psiClass: classList) {
            go(psiClass);
        }
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.write(StringUtils.join(lines, "\n"));
        writer.close();
        String protoContent = outputStream.toString();
        CopyPasteManager.getInstance().setContents(new StringSelection(protoContent));
    }

    public void go(PsiClass clazz) {
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
