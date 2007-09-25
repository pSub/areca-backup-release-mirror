package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.application.areca.ResourceManager;
import com.application.areca.impl.policy.FTPFileSystemPolicy;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.ftp.SecuredSocketFactory;
import com.myJava.util.Utilitaire;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 7453350623295719521
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
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
public class FTPEditionWindow 
extends AbstractWindow {
    
    private static final ResourceManager RM = ResourceManager.instance();
    private static final String TITLE = RM.getLabel("ftpedition.dialog.title");
    private static String[] PROTOCOLS;
    
    static {
        String[] protocols = FrameworkConfiguration.getInstance().getSSEProtocols();
        PROTOCOLS = new String[protocols.length + 1];
        PROTOCOLS[0] = "";
        for (int i=0; i<protocols.length; i++) {
            PROTOCOLS[i+1] = protocols[i];
        }
    }
    
    private FTPFileSystemPolicy currentPolicy;  
    private Thread currentRunningTest = null;
    
    protected Text txtHost;
    protected Text txtPort;
    protected Button chkPassiv;
    protected Combo cboProtocol;
    protected Text txtLogin;
    protected Text txtPassword;
    protected Text txtRemoteDir;
    protected Button chkImplicit;
    protected Button btnTest;
    protected Button btnSave;
    protected Button btnCancel;
    protected Combo cboProtection;
    
    public FTPEditionWindow(FTPFileSystemPolicy currentPolicy) {
        super();
        this.currentPolicy = currentPolicy;
    }

    protected Control createContents(Composite parent) {
        Composite ret = new Composite(parent, SWT.NONE);
        ret.setLayout(new GridLayout(1, false));

        CTabFolder tabs = new CTabFolder(ret, SWT.BORDER);
        tabs.setSimple(Application.SIMPLE_SUBTABS);
        tabs.setLayout(new FillLayout());
        GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true);
        tabs.setLayoutData(dt);

        CTabItem itm1 = new CTabItem(tabs, SWT.NONE);
        Application.setTabLabel(itm1, RM.getLabel("ftpedition.main.title"), false);
        itm1.setControl(getMainPanel(tabs));

        CTabItem itm2 = new CTabItem(tabs, SWT.NONE);
        Application.setTabLabel(itm2, RM.getLabel("ftpedition.ftps.label"), false);
        itm2.setControl(getFTPsPanel(tabs));
        
        buildSaveComposite(ret);
        initValues();
        
        ret.pack();
        return ret;
    }
    
    private void initValues() {
        if (this.currentPolicy != null) {            
            this.txtHost.setText(currentPolicy.getRemoteServer());
            this.txtPort.setText("" + currentPolicy.getRemotePort());
            this.chkPassiv.setSelection(currentPolicy.isPassivMode());
            this.chkImplicit.setSelection(currentPolicy.isImplicit());
            
            int index = -1;
            for (int i=0; i<PROTOCOLS.length; i++) {
                if (PROTOCOLS[i].equals(currentPolicy.getProtocol())) {
                    index = i;
                    break;
                }
            }
            this.cboProtocol.select(index);
            
            index = 0;
            for (int i=0; i<SecuredSocketFactory.PROTECTIONS.length; i++) {
                if (SecuredSocketFactory.PROTECTIONS[i].equals(currentPolicy.getProtection())) {
                    index = i;
                    break;
                }
            }
            this.cboProtection.select(index);
            
            this.txtLogin.setText(currentPolicy.getLogin());
            this.txtPassword.setText(currentPolicy.getPassword());
            this.txtRemoteDir.setText(currentPolicy.getRemoteDirectory());
        } else {
            this.txtPort.setText("" + FTPFileSystemPolicy.DEFAULT_PORT);
            this.cboProtection.select(0);
        }
    }
    
    private GridLayout initLayout(int nbCols) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 10;
        layout.numColumns = nbCols;
        layout.marginHeight = 10;
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 10;
        return layout;
    }
    
    private Composite getMainPanel(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(initLayout(1));
        
        Group grpServer = new Group(composite, SWT.NONE);
        grpServer.setText(RM.getLabel("ftpedition.servergroup.label"));
        grpServer.setLayout(new GridLayout(3, false));
        grpServer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        Label lblHost = new Label(grpServer, SWT.NONE);
        lblHost.setText(RM.getLabel("ftpedition.host.label"));
        txtHost = new Text(grpServer, SWT.BORDER);
        GridData dt = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        dt.widthHint = computeWidth(300);
        txtHost.setLayoutData(dt);
        monitorControl(txtHost);
        
        Label lblPort = new Label(grpServer, SWT.NONE);
        lblPort.setText(RM.getLabel("ftpedition.port.label"));
        txtPort = new Text(grpServer, SWT.BORDER);
        txtPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        monitorControl(txtPort);
        
        chkPassiv = new Button(grpServer, SWT.CHECK);
        chkPassiv.setText(RM.getLabel("ftpedition.passiv.label") + " ");
        chkPassiv.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        monitorControl(chkPassiv);
        
        Label lblRemoteDir = new Label(grpServer, SWT.NONE);
        lblRemoteDir.setText(RM.getLabel("ftpedition.dir.label"));
        txtRemoteDir = new Text(grpServer, SWT.BORDER);
        txtRemoteDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        monitorControl(txtRemoteDir);
        
        new Label(composite, SWT.NONE);
        
        Group grpAuthent = new Group(composite, SWT.NONE);
        grpAuthent.setText(RM.getLabel("ftpedition.authentgroup.label"));
        grpAuthent.setLayout(new GridLayout(2, false));
        grpAuthent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        Label lblLogin = new Label(grpAuthent, SWT.NONE);
        lblLogin.setText(RM.getLabel("ftpedition.login.label"));
        txtLogin = new Text(grpAuthent, SWT.BORDER);
        txtLogin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        monitorControl(txtLogin);
        
        Label lblPassword = new Label(grpAuthent, SWT.NONE);
        lblPassword.setText(RM.getLabel("ftpedition.password.label"));
        txtPassword = new Text(grpAuthent, SWT.BORDER);
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        monitorControl(txtPassword);
        
        return composite;
    }
    
    private Composite getFTPsPanel(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(initLayout(3));

        Label lblFTPs = new Label(composite, SWT.NONE);
        lblFTPs.setText(RM.getLabel("ftpedition.secured.label"));
        cboProtocol = new Combo(composite, SWT.READ_ONLY);
        cboProtocol.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        for (int i=0; i<PROTOCOLS.length; i++) {
            cboProtocol.add(PROTOCOLS[i]);
        }
        monitorControl(cboProtocol);
        
        chkImplicit = new Button(composite, SWT.CHECK);
        chkImplicit.setText(RM.getLabel("ftpedition.implicit.label"));
        chkImplicit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        monitorControl(chkImplicit);
        
        Label lblProtection = new Label(composite, SWT.NONE);
        lblProtection.setText(RM.getLabel("ftpedition.protection.label"));
        cboProtection = new Combo(composite, SWT.READ_ONLY);
        cboProtection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        for (int i=0; i<SecuredSocketFactory.PROTECTIONS.length; i++) {
            cboProtection.add(SecuredSocketFactory.PROTECTIONS[i]);
        }
        monitorControl(cboProtection);
        
        return composite;
    }
    
    private void buildSaveComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        composite.setLayout(new GridLayout(3, false));
        
        btnTest = new Button(composite, SWT.PUSH);
        btnTest.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        btnTest.setText(RM.getLabel("ftpedition.test.label"));
        btnTest.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                final FTPFileSystemPolicy policy = new FTPFileSystemPolicy();
                initPolicy(policy);
                
                Runnable rn = new Runnable() {
                    public void run() {
                        testFTP(policy);    
                    }
                };
                Thread th = new Thread(rn, "FTP Test #" + Utilitaire.getRndLong());
                th.setDaemon(true);
                registerCurrentRunningTest(th);
                th.start();
            }
        });
        
        btnSave = new Button(composite, SWT.PUSH);
        btnSave.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        btnSave.setText(RM.getLabel("common.save.label"));
        btnSave.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                registerCurrentRunningTest(null);
                saveChanges();
            }
        });
        
        btnCancel = new Button(composite, SWT.PUSH);
        btnCancel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        btnCancel.setText(RM.getLabel("common.cancel.label"));
        btnCancel.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                registerCurrentRunningTest(null);
                cancelChanges();
            }
        });
    }
    
    public String getTitle() {
        return TITLE;
    }
    
    private void registerCurrentRunningTest(Thread th) {
        try {
            if (this.currentRunningTest != null) {
                this.currentRunningTest.interrupt();
            }
        } catch (Exception e) {
            Logger.defaultLogger().error(e);
        }
        this.currentRunningTest = th;
    }
    
    protected void testFTP(FTPFileSystemPolicy policy) {
        SecuredRunner.execute(new Runnable() {
            public void run() {
                btnTest.setEnabled(false);
            }
        });

        try {
            policy.validate(true);
            SecuredRunner.execute(new Runnable() {
                public void run() {
                    application.showInformationDialog(RM.getLabel("ftpedition.test.success"), RM.getLabel("ftpedition.test.title"), false);
                }
            });
        } catch (final Throwable e) {
            SecuredRunner.execute(new Runnable() {
                public void run() {
                    application.showWarningDialog(RM.getLabel("ftpedition.test.failure", new Object[] {e.getMessage()}), RM.getLabel("ftpedition.test.title"), false);
                }
            });
        } finally {
            SecuredRunner.execute(new Runnable() {
                public void run() {
                    btnTest.setEnabled(true);
                }
            });
        }
    }

    protected boolean checkBusinessRules() {   
        this.resetErrorState(txtHost);
        this.resetErrorState(txtPort);
        this.resetErrorState(txtRemoteDir);
        this.resetErrorState(txtLogin);
        this.resetErrorState(txtPassword);
        
        if (! check(txtHost)) {
            return false;
        }
        
        if (! check(txtPort)) {
            return false;
        }

        try {
            Integer.parseInt(txtPort.getText());
        } catch (Throwable e) {
            this.setInError(txtPort);
            return false;
        }

        if (! check(txtRemoteDir)) {
            return false;
        }
        
        if (! check(txtLogin)) {
            return false;
        }
        
        if (! check(txtPassword)) {
            return false;
        }

        return true;
    }

    private boolean check(Text fld) {
        if (fld.getText() == null || fld.getText().trim().length() == 0) {
            this.setInError(fld);
            return false;
        }
        return true;
    }
    
    protected void saveChanges() {
        if (this.currentPolicy == null) {
            this.currentPolicy = new FTPFileSystemPolicy();
        }
        initPolicy(this.currentPolicy);
        
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        btnSave.setEnabled(rulesSatisfied);
        btnTest.setEnabled(rulesSatisfied);
    }
    
    protected void initPolicy(FTPFileSystemPolicy policy) {
        policy.setLogin(txtLogin.getText());
        policy.setPassivMode(chkPassiv.getSelection());
        policy.setImplicit(chkImplicit.getSelection());

        if (cboProtocol.getSelectionIndex() != -1) {
            String protocol = (String)cboProtocol.getItem(cboProtocol.getSelectionIndex());
            policy.setProtocol(protocol);
        } else {
            policy.setProtocol(null);
        }
        
        if (cboProtection.getSelectionIndex() != -1) {
            String protection = (String)cboProtection.getItem(cboProtection.getSelectionIndex());
            policy.setProtection(protection);
        } else {
            policy.setProtection(null);
        }
        
        policy.setPassword(txtPassword.getText());
        policy.setRemoteDirectory(txtRemoteDir.getText());
        policy.setRemotePort(Integer.parseInt(txtPort.getText()));
        policy.setRemoteServer(txtHost.getText());
    }

    public FTPFileSystemPolicy getCurrentPolicy() {
        return currentPolicy;
    }
}
