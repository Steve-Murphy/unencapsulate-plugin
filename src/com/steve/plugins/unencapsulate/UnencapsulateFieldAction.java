package com.steve.plugins.unencapsulate;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.ElementType;
import com.intellij.psi.impl.source.tree.java.ClassElement;
import com.intellij.psi.tree.TokenSet;

public class UnencapsulateFieldAction extends AnAction {
    private String myString;
    @Override
    public void update(AnActionEvent e) {
        ASTNode parentNode = getParentNode(e);
        if (parentNode == null || parentNode.getElementType() != ElementType.FIELD) {
            e.getPresentation().setEnabled(false);
        }
    }

    public void actionPerformed(AnActionEvent e) {
        ASTNode parentNode = getParentNode(e);
        if (parentNode == null || parentNode.getElementType() != ElementType.FIELD) {
            return;
        }
        final ASTNode fieldAccessModifierNode = parentNode.getFirstChildNode().getFirstChildNode();
        if (fieldAccessModifierNode.getElementType() == ElementType.PRIVATE_KEYWORD) {
            final Editor editor = e.getData(PlatformDataKeys.EDITOR);
            final int startOffset = fieldAccessModifierNode.getStartOffset();
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    editor.getDocument().replaceString(startOffset, startOffset + fieldAccessModifierNode.getTextLength(), "public");
                }
            });
        }

        ASTNode fieldIdentifier = parentNode.getLastChildNode().getTreePrev();
        String fieldName = fieldIdentifier.getText();
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        System.out.println("Changing field: " + fieldName);
        System.out.println("Getter shall be: " + getterName);
        System.out.println("Setter shall be: " + setterName);

        ClassElement classElement = (ClassElement) parentNode.getTreeParent();
        ASTNode methodElements[] = classElement.getChildren(TokenSet.create(ElementType.METHOD));


    }

    private ASTNode getParentNode(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null || editor == null)
            return null;

        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        if (elementAt == null || elementAt.getParent() == null || elementAt.getParent().getNode() == null)
            return null;

        return elementAt.getParent().getNode();
    }
}
