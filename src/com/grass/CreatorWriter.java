package com.grass;

import java.util.ArrayList;

import com.grass.model.Element;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.PsiStatement;

public class CreatorWriter extends WriteCommandAction.Simple {

    protected PsiFile mFile;
    protected Project mProject;
    protected PsiClass mClass;
    protected ArrayList<Element> mElements;
    protected PsiElementFactory mFactory;
    protected String mLayoutFileName;
    protected String mFieldNamePrefix;
    protected boolean mCreateHolder;

    public static final String APPLY_HOLDER = "holder.%s = (%s) view.findViewById(R.id.%s);\n";
    public static final String HOLDER_FIELD = "%s  %s;\n";

    public static final String APPLY_HOLDER_METHOD_ROOT_VIEW_STATEMENT = "holder.mRootView = view; \n";
    public static final String METHOD_CREATE_VIEW_HOLDER = "applyViewsToHolder";
    public static final String INNER_CLASS_VIEW_HOLDER = "ViewHolder";
    public static final String INNER_CLASS_VIEW_HOLDER_ROOT_VIEW_FIELD = "View mRootView; \n";

    protected static final Logger log = Logger.getInstance(CreatorWriter.class);

    public CreatorWriter(PsiFile file, PsiClass clazz, String command, ArrayList<Element> elements,
                         String layoutFileName, String fieldNamePrefix, boolean createHolder) {
        super(clazz.getProject(), command);

        mFile = file;
        mProject = clazz.getProject();
        mClass = clazz;
        mElements = elements;
        mFactory = JavaPsiFacade.getElementFactory(mProject);
        mLayoutFileName = layoutFileName;
        mFieldNamePrefix = fieldNamePrefix;
        mCreateHolder = createHolder;
        log.info("CreatorWriter: " + mProject.getName() + " " + mLayoutFileName + " " + mFieldNamePrefix);
    }

    @Override
    public void run() throws Throwable {
        logStatement();
        logInnerClass();
        generateViewholder();
        generateMethodApplyViewsToHolder();
    }

    protected void generateViewholder() {
        PsiClass innerClass = mClass.findInnerClassByName(INNER_CLASS_VIEW_HOLDER, false);
        if (innerClass != null) {
            PsiField[] allFields = innerClass.getAllFields();
            PsiField rootField = mFactory.createFieldFromText(INNER_CLASS_VIEW_HOLDER_ROOT_VIEW_FIELD, innerClass);
            if (!checkPsiFieldDuplicate(rootField, allFields)) {
                innerClass.add(rootField);
            }
            for (Element element : mElements) {
                String field = String.format(HOLDER_FIELD, element.name, element.fieldName);
                PsiField newField = mFactory.createFieldFromText(field, innerClass);
                boolean exist = checkPsiFieldDuplicate(newField, allFields);
                if (!exist) {
                    innerClass.add(newField);
                }
            }
        }
    }

    private void logInnerClass() {
        log.info("===========================logInnerClass========================================");
        PsiClass[] allInnerClasses = mClass.getAllInnerClasses();
        for (PsiClass innerClass : allInnerClasses) {
            log.info("innerClass: " + innerClass.getName() + " " + innerClass.getText());
        }
        log.info("===================================================================");
        PsiClass[] innerClasses = mClass.getInnerClasses();
        for (PsiClass innerClass : innerClasses) {
            log.info("innerClass: " + innerClass.getName() + " " + innerClass.getText());
        }

        PsiMethod[] allMethods = mClass.getAllMethods();
        for (PsiMethod method : allMethods) {
            log.info("method: " + method.getName() + " " + method.getText());
        }

        log.info("===========================logInnerClass end========================================");
    }

    protected void generateMethodApplyViewsToHolder() {
        log.info("=============================generateMethodApplyViewsToHolder======================================");
        PsiMethod[] methods = mClass.findMethodsByName(METHOD_CREATE_VIEW_HOLDER, false);
        log.info("methods length: " + methods.length);
        PsiMethod applyMethod = null;
        if (methods.length == 1) {
            applyMethod = methods[0];
        } else {
            log.info("------------------没有发现" + METHOD_CREATE_VIEW_HOLDER + "方法");
            return;
        }
        PsiCodeBlock body = applyMethod.getBody();
        PsiStatement[] statements = body.getStatements();
        for (PsiStatement statement : statements) {
            if (statement instanceof PsiReturnStatement) {
                PsiReturnStatement returnSt = (PsiReturnStatement) statement;
                ArrayList<PsiStatement> stmList = createApplyViewsToHolderMethodStatements();
                for (PsiStatement psiStatement : stmList) {
                    boolean exist = checkStatementDuplicate(psiStatement, statements);
                    if (!exist) {
                        body.addBefore(psiStatement, returnSt);
                    }
                }
                break;
            }
        }
        log.info("=============================generateMethodApplyViewsToHolder end "
                + "======================================");
    }

    private boolean checkPsiFieldDuplicate(PsiField toSt, PsiField[] array) {
        if (toSt == null) {
            return true;
        }
        for (PsiField psiField : array) {
            if (toSt.getName().equals(psiField.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkStatementDuplicate(PsiStatement toSt, PsiStatement[] array) {
        if (toSt == null) {
            return true;
        }

        for (PsiStatement psiStatement : array) {
            if (toSt.getText().equals(psiStatement.getText())) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<PsiStatement> createApplyViewsToHolderMethodStatements() {
        ArrayList<PsiStatement> list = new ArrayList<>();
        PsiStatement rootViewSt = mFactory.createStatementFromText(APPLY_HOLDER_METHOD_ROOT_VIEW_STATEMENT,
                mClass);
        list.add(rootViewSt);
        for (Element element : mElements) {
            String newSt = String.format(APPLY_HOLDER, element.fieldName, element.name, element.id);
            PsiStatement statment = mFactory.createStatementFromText(newSt.toString(),
                    mClass);
            list.add(statment);
        }
        for (PsiStatement ps : list) {
            log.info("createApplyViewsToHolderMethod: " + ps.getText());
        }
        return list;
    }

    protected void logStatement() {
        log.info("=================================logStatement==================================");
        PsiMethod[] methods = mClass.findMethodsByName(METHOD_CREATE_VIEW_HOLDER, false);
        PsiMethod createViewHolderMethod = null;
        if (methods.length == 1) {
            createViewHolderMethod = methods[0];
        } else {
            return;
        }

        PsiElement firstChild = createViewHolderMethod.getFirstChild();
        PsiElement lastChild = createViewHolderMethod.getLastChild();
        if (firstChild != null && lastChild != null) {
            log.info("firstchild: " + firstChild.getText());
            log.info("lastchild: " + lastChild.getText());
        }
        log.info("======================================");
        PsiElement[] children = createViewHolderMethod.getChildren();
        for (PsiElement child : children) {
            log.info("child: " + child.getText());
        }

        log.info("======================================");

        PsiReference reference = createViewHolderMethod.getReference();
        if (reference != null) {
            log.info("refrence: " + reference.getCanonicalText());
        }

        log.info("======================================");

        PsiCodeBlock body = createViewHolderMethod.getBody();
        PsiStatement[] statements = body.getStatements();
        for (PsiStatement statement : statements) {
            log.info("statement: " + statement.toString() + " " + statement.getText());
            PsiElement[] childrens = statement.getChildren();
            for (PsiElement psiElement : childrens) {
                log.info("---statement child: " + psiElement.getText());
            }
        }

        PsiElement firstChild1 = body.getFirstChild();
        if (firstChild1 != null) {
            log.info("firstChild1: " + firstChild1.getText());
        }
        PsiElement lastChild1 = body.getLastChild();
        if (lastChild1 != null) {
            log.info("lastChild1: " + lastChild1.getText());
        }
        PsiElement firstBodyElement = body.getFirstBodyElement();
        if (firstBodyElement != null) {
            log.info("fb: " + firstBodyElement.getText());
        }
        PsiElement lastBodyElement = body.getLastBodyElement();
        if (lastBodyElement != null) {
            log.info("lb: " + lastBodyElement.getText());
        }
        log.info("=================================logStatement end==================================");
    }
}