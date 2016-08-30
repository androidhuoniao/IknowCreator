package com.grass.navigation;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicate;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;

public class NavigationMarkerProvider implements LineMarkerProvider {

    private static final Predicate<PsiElement> IS_FIELD_IDENTIFIER = new Predicate<PsiElement>() {
        @Override
        public boolean apply(@Nullable PsiElement element) {
            return element != null && element instanceof PsiIdentifier && element.getParent() instanceof PsiField;
        }
    };

    private static final Predicate<PsiElement> IS_METHOD_IDENTIFIER = new Predicate<PsiElement>() {
        @Override
        public boolean apply(@Nullable PsiElement element) {
            return element != null && element instanceof PsiIdentifier && element.getParent() instanceof PsiMethod;
        }
    };

    /**
     * Check if element is a method annotated with <em>@OnClick</em> or a field annotated with
     * <em>@InjectView</em> and create corresponding navigation link.
     *
     * @return a {@link com.intellij.codeInsight.daemon.GutterIconNavigationHandler} for the
     * appropriate type, or null if we don't care about it.
     */
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {


        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements, @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {
        // empty
    }

    @Nullable
    private LineMarkerInfo getNavigationLineMarker(@NotNull final PsiIdentifier element) {

        return null;
    }



//    private class ClassMemberProcessor implements Processor<PsiMember> {
//        private final String resourceId;
//        private final ButterKnifeLink link;
//        private PsiMember resultMember;
//
//        public ClassMemberProcessor(@NotNull final String resourceId, @NotNull final ButterKnifeLink link) {
//            this.resourceId = resourceId;
//            this.link = link;
//        }
//
//        @Override
//        public boolean process(PsiMember psiMember) {
//            if (hasAnnotationWithValue(psiMember, link.dstAnnotation, resourceId)) {
//                resultMember = psiMember;
//                return false;
//            }
//            return true;
//        }
//
//        @Nullable
//        public PsiMember getResultMember() {
//            return resultMember;
//        }
//    }
}
