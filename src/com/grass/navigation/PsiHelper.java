package com.grass.navigation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNameValuePair;

public class PsiHelper {

    private PsiHelper() {
    }

    public static boolean hasAnnotationWithValue(@NotNull final PsiModifierListOwner element, @NotNull final String
            annotation, @NotNull final String value) {
        final PsiAnnotation psiAnnotation = getAnnotation(element, annotation);
        if (psiAnnotation != null && annotation.equals(psiAnnotation.getQualifiedName())) {
            final PsiNameValuePair[] attributes = psiAnnotation.getParameterList().getAttributes();
            if (attributes.length > 0) {
                final PsiAnnotationMemberValue psiValue = attributes[0].getValue();
                if (psiValue != null && value.equals(psiValue.getText())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    public static PsiAnnotation getAnnotation(@NotNull final PsiElement element, @NotNull final String annotation) {
        if (element instanceof PsiModifierListOwner) {
            final PsiAnnotation[] annotations = AnnotationUtil.getAllAnnotations((PsiModifierListOwner) element, false, null);
            for (PsiAnnotation psiAnnotation : annotations) {
                if (annotation.equals(psiAnnotation.getQualifiedName())) {
                    return psiAnnotation;
                }
            }
        }

        return null;
    }
}
