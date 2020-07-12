package com.xiaohengjin;

import java.util.List;

import com.google.common.collect.Lists;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;


/**
 * @author xiaohengjin <xhjcehust@qq.com>
 * Created on 2020-07-10
 */
public class Pojo2ProtoAction extends AnAction {

    private static final NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("pojo2proto.NotificationGroup", NotificationDisplayType.BALLOON, true);

    @Override
    public void actionPerformed(AnActionEvent event) {
        try {
            actionPerformedInternal(event);
        } catch (Exception e) {
            Notification error = NOTIFICATION_GROUP.createNotification("convert to proto failed.", NotificationType.ERROR);
            Project project = event.getData(PlatformDataKeys.PROJECT);
            Notifications.Bus.notify(error, project);
        }
    }

    public void actionPerformedInternal(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        PsiElement psiFile = event.getData(CommonDataKeys.PSI_FILE);
        List<PsiClass> classList = getClasses(psiFile);

        new Pojo2ProtoCore(classList).start();
        String message = "Convert to proto success, copied to clipboard.";
        Notification success = NOTIFICATION_GROUP.createNotification(message, NotificationType.INFORMATION);
        Notifications.Bus.notify(success, project);
    }

    public static List<PsiClass> getClasses(PsiElement element) {
        List<PsiClass> elements = Lists.newArrayList();
        List<PsiClass> classElements = PsiTreeUtil.getChildrenOfTypeAsList(element, PsiClass.class);
        elements.addAll(classElements);
        for (PsiClass classElement : classElements) {
            //这里用了递归的方式获取内部类
            elements.addAll(getClasses(classElement));
        }
        return elements;
    }
}
