package com.application.areca.launcher.gui.composites;

import java.util.Iterator;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.application.areca.ResourceManager;
import com.application.areca.impl.FileSystemRecoveryTarget;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.ProcessorEditionWindow;
import com.application.areca.launcher.gui.ProcessorRepository;
import com.application.areca.launcher.gui.TargetEditionWindow;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.processor.Processor;
import com.application.areca.processor.ProcessorList;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4370643633314966344
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

This file is part of Areca.

    Areca is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Areca is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Areca; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
public class ProcessorsTable {

    private static final ResourceManager RM = ResourceManager.instance();

    protected Table tblProc;  
    protected Button btnAddProc;
    protected Button btnRemoveProc;
    protected Button btnModifyProc;

    protected boolean preprocess = true;
    protected final TargetEditionWindow main;

    public ProcessorsTable(Composite parent, TargetEditionWindow tge, boolean preprocess) {

        parent.setLayout(initLayout(4));
        this.main = tge;
        this.preprocess = preprocess;

        TableViewer viewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE);
        tblProc = viewer.getTable();
        GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        dt.heightHint = AbstractWindow.computeHeight(50);
        tblProc.setLayoutData(dt);
        
        TableColumn col1 = new TableColumn(tblProc, SWT.NONE);
        col1.setText(RM.getLabel("targetedition.proctable.type.label"));
        col1.setWidth(AbstractWindow.computeWidth(200));
        col1.setMoveable(true);

        TableColumn col2 = new TableColumn(tblProc, SWT.NONE);
        col2.setText(RM.getLabel("targetedition.proctable.parameters.label"));
        col2.setWidth(AbstractWindow.computeWidth(300));
        col2.setMoveable(true);

        tblProc.setHeaderVisible(true);
        tblProc.setLinesVisible(AbstractWindow.getTableLinesVisible());

        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                editCurrentProcessor();
            }
        });

        btnAddProc = new Button(parent, SWT.PUSH);
        btnAddProc.setText(RM.getLabel("targetedition.addprocaction.label"));
        btnAddProc.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                Processor newproc = showProcEditionFrame(null);
                if (newproc != null) {
                    addProcessor(newproc);
                    main.publicRegisterUpdate();                
                }
            }
        });

        btnModifyProc = new Button(parent, SWT.PUSH);
        btnModifyProc.setText(RM.getLabel("targetedition.editprocaction.label"));
        btnModifyProc.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                editCurrentProcessor();
            }
        });

        btnRemoveProc = new Button(parent, SWT.PUSH);
        btnRemoveProc.setText(RM.getLabel("targetedition.removeprocaction.label"));
        btnRemoveProc.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                if (tblProc.getSelectionIndex() != -1) {
                    int result = Application.getInstance().showConfirmDialog(
                            RM.getLabel("targetedition.removeprocaction.confirm.message"),
                            RM.getLabel("targetedition.confirmremoveproc.title"));

                    if (result == SWT.YES) {
                        tblProc.remove(tblProc.getSelectionIndex());
                        main.publicRegisterUpdate();                 
                    }
                }
            }
        });

        tblProc.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                updateProcListState();
            }
        });
    }

    private GridLayout initLayout(int nbCols) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.numColumns = nbCols;
        layout.marginHeight = 0;
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 10;
        return layout;
    }

    public void updateProcListState() {
        int index =  this.tblProc.getSelectionIndex();
        this.btnRemoveProc.setEnabled(index != -1);
        this.btnModifyProc.setEnabled(index != -1);       
    }


    private void editCurrentProcessor() {
        if (tblProc.getSelectionIndex() != -1) {
            TableItem item = tblProc.getItem(tblProc.getSelectionIndex());
            Processor proc = (Processor)item.getData();
            showProcEditionFrame(proc);
            updateProcessor(item, proc);
            main.publicRegisterUpdate();       
        }
    }

    private Processor showProcEditionFrame(Processor proc) {
        ProcessorEditionWindow frm = new ProcessorEditionWindow(proc, (FileSystemRecoveryTarget)main.getTarget(), preprocess);
        main.showDialog(frm);
        Processor prc = frm.getCurrentProcessor();
        return prc;
    }


    private void addProcessor(Processor proc) {
        TableItem item = new TableItem(tblProc, SWT.NONE);
        updateProcessor(item, proc);
    }

    private void updateProcessor(TableItem item, Processor proc) {
        item.setText(0, ProcessorRepository.getName(proc));
        item.setText(1, proc.getParametersSummary());
        item.setData(proc);
    }

    public Button getBtnAddProc() {
        return btnAddProc;
    }

    public Button getBtnModifyProc() {
        return btnModifyProc;
    }

    public Button getBtnRemoveProc() {
        return btnRemoveProc;
    }

    public Table getTblProc() {
        return tblProc;
    }

    public void addProcessors(ProcessorList list) {
        for (int i=0; i<tblProc.getItemCount(); i++) {
            list.addProcessor((Processor)tblProc.getItem(i).getData());
        }
    }

    public void setProcessors(ProcessorList list) {
        Iterator processors = list.iterator();
        int index = tblProc.getSelectionIndex();
        while (processors.hasNext()) {
            Processor proc = (Processor)processors.next();

            TableItem item = new TableItem(tblProc, SWT.NONE);
            item.setText(0, ProcessorRepository.getName(proc));
            item.setText(1, proc.getParametersSummary());
            item.setData(proc);
        } 
        if (index != -1) {
            tblProc.setSelection(index);
        }  
    }
}
