package com.grass.action;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jetbrains.annotations.NotNull;

import com.grass.CreatorWriter;
import com.grass.common.Utils;
import com.grass.form.EntryList;
import com.grass.iface.ICancelListener;
import com.grass.iface.IConfirmListener;
import com.grass.model.Element;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;

/**
 * Created by baidu on 16/8/29.
 */
public class CreatorAction extends BaseGenerateAction implements IConfirmListener, ICancelListener {

    protected JFrame mDialog;
    protected static final Logger log = Logger.getInstance(CreatorAction.class);

    @SuppressWarnings("unused")
    public CreatorAction() {
        super(null);
    }

    @SuppressWarnings("unused")
    public CreatorAction(CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    protected boolean isValidForClass(PsiClass targetClass) {
        log.info("isValidForClass " + targetClass.getName());
        return super.isValidForClass(targetClass);
    }

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        log.info("isValidForFile: " + project.getName() + " editor: " + editor.toString() +
                " psfile: " + file.getName());
        return super.isValidForFile(project, editor, file);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);

        actionPerformedImpl(project, editor);
    }

    @Override
    public void actionPerformedImpl(Project project, Editor editor) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiFile layout = Utils.getLayoutFileFromCaret(editor, file);

        if (layout == null) {
            Utils.showErrorNotification(project, "No layout found");
            return; // no layout found
        }

        log.info("Layout file: " + layout.getVirtualFile() + " file: " + file.getName());

        log.info("--------------------------------------------------------------------------");
        ArrayList<Element> elements = Utils.getIDsFromLayout(layout);
        if (!elements.isEmpty()) {
            for (Element element : elements) {
                log.info("element: " + element.toString());
            }
            showDialog(project, editor, elements);
        } else {
            Utils.showErrorNotification(project, "No IDs found in layout");
        }
    }

    protected void showDialog(Project project, Editor editor, ArrayList<Element> elements) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (file == null) {
            return;
        }
        log.info("showDialog: file: " + file.getName());
        PsiClass clazz = getTargetClass(editor, file);

        if (clazz == null) {
            return;
        }

        log.info("showDialog: clazz: " + clazz.getName());
        // get parent classes and check if it's an adapter
        boolean createHolder = false;

        // get already generated injections
        ArrayList<String> ids = new ArrayList<String>();

        EntryList panel = new EntryList(project, editor, elements, ids, createHolder, this, this);

        mDialog = new JFrame();
        mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mDialog.getRootPane().setDefaultButton(panel.getConfirmButton());
        mDialog.getContentPane().add(panel);
        mDialog.pack();
        mDialog.setLocationRelativeTo(null);
        mDialog.setVisible(true);
    }

    @Override
    public void onCancel() {
        closeDialog();
    }

    @Override
    public void onConfirm(Project project, Editor editor, ArrayList<Element> elements, String fieldNamePrefix,
                          boolean createHolder) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (file == null) {
            return;
        }
        PsiFile layout = Utils.getLayoutFileFromCaret(editor, file);

        closeDialog();

        if (Utils.getInjectCount(elements) > 0) {
            new CreatorWriter(file, getTargetClass(editor, file), "Generate Creator", elements, layout.getName(),
                    fieldNamePrefix, createHolder).execute();
        } else { // just notify user about no element selected
            Utils.showInfoNotification(project, "No injection was selected");
        }
    }

    protected void closeDialog() {
        if (mDialog == null) {
            return;
        }

        mDialog.setVisible(false);
        mDialog.dispose();
    }
}
