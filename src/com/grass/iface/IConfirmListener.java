package com.grass.iface;

import java.util.ArrayList;

import com.grass.model.Element;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public interface IConfirmListener {

    public void onConfirm(Project project, Editor editor, ArrayList<Element> elements, String fieldNamePrefix, boolean createHolder);
}
