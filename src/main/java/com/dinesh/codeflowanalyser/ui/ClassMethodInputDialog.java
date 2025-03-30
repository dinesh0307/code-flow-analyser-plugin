package com.dinesh.codeflowanalyser.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ClassMethodInputDialog extends DialogWrapper {
    private final JBTextField classNameField = new JBTextField(30);
    private final JBTextField methodNameField = new JBTextField(30);

    public ClassMethodInputDialog(Project project) {
        super(project);
        setTitle("Enter Class and Method Information");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(new JBLabel("Class Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(classNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel("Method Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(methodNameField, gbc);

        return panel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (classNameField.getText().trim().isEmpty()) {
            return new ValidationInfo("Class name cannot be empty", classNameField);
        }
        if (methodNameField.getText().trim().isEmpty()) {
            return new ValidationInfo("Method name cannot be empty", methodNameField);
        }
        return null;
    }

    public String getClassName() {
        return classNameField.getText().trim();
    }

    public String getMethodName() {
        return methodNameField.getText().trim();
    }
}
