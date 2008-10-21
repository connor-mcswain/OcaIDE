package ocaml.launching;

import java.io.File;

import ocaml.OcamlPlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Implements a properties page that will appear amongst the O'Caml launch configuration tabs and that will
 * allow the user to configure the name of the executable, the name of the project, and the command-line
 * arguments of the executable.
 */
public class OcamlLaunchTab extends AbstractLaunchConfigurationTab {

    public static final String ATTR_RUNPATH = "attr_ocaml_run_full_path";

    public static final String ATTR_BYTEPATH = "attr_ocaml_byte_full_path";

	public static final String ATTR_PROJECTNAME = "attr_ocaml_launch_project_name";

	public static final String ATTR_ARGS = "attr_ocaml_launch_args";

	public static final String ATTR_REMOTE_DEBUG_ENABLE = "attr_ocaml_remote_debug_enable";

	public static final String ATTR_REMOTE_DEBUG_PORT = "attr_ocaml_remote_debug_port";
	
	public static final String ATTR_SCRIPTPATH = "attr_ocaml_script_full_path";

	public static final boolean DEFAULT_REMOTE_DEBUG_ENABLE = false;

	public static final String DEFAULT_REMOTE_DEBUG_PORT = "8000";

	Button buttonRemoteDebugEnable;

	Composite composite;

    Text textRunPath;

	Text textBytePath;

	Text textProjectName;

	Text textArguments;

	Text textRemoteDebugPort;
	
	Text textScriptPath;

	public void createControl(Composite parent) {

		composite = new Composite(parent, SWT.NONE);
		setControl(composite);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);

        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        layoutData.widthHint = 200;

		Label label1 = new Label(composite, SWT.NONE);
		label1.setText("Project name:");
		// dummy label, because we have two columns
		new Label(composite, SWT.NONE);
		textProjectName = new Text(composite, SWT.BORDER);
		new Label(composite, SWT.NONE);

        textProjectName.setLayoutData(layoutData);
        textProjectName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setDirty(true);
                updateLaunchConfigurationDialog();
            }
        });

		Label label2 = new Label(composite, SWT.NONE);
		label2.setText("Executable file to run:");
		// dummy label, because we have two columns
		new Label(composite, SWT.NONE);
		textRunPath = new Text(composite, SWT.BORDER);

        textRunPath.setLayoutData(layoutData);
        textRunPath.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setDirty(true);
                updateLaunchConfigurationDialog();
            }
        });

        Button buttonBrowseRun = new Button(composite, SWT.PUSH);
        buttonBrowseRun.setText("Browse...");
        buttonBrowseRun.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String path = browse();
                if (path != null) {
                    textRunPath.setText(path);
                }
            }
        });

		Label label3 = new Label(composite, SWT.NONE);
        label3.setText("Bytecode file to load into debugger (if different from executable):");
        // dummy label, because we have two columns
        new Label(composite, SWT.NONE);
        textBytePath = new Text(composite, SWT.BORDER);

		textBytePath.setLayoutData(layoutData);
        textBytePath.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setDirty(true);
                updateLaunchConfigurationDialog();
            }
        });

        Button buttonBrowseByte = new Button(composite, SWT.PUSH);
        buttonBrowseByte.setText("Browse...");
        buttonBrowseByte.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String path = browse();
                if (path != null) {
                    textBytePath.setText(path);
                }
            }
        });

		Label label4 = new Label(composite, SWT.NONE);
		label4.setText("Command line arguments (separated by spaces)\n"
				+ "You can use \" \" and \\ to quote strings");
		// dummy label, because we have two columns
		new Label(composite, SWT.NONE);
		textArguments = new Text(composite, SWT.BORDER);
		textArguments.setLayoutData(layoutData);
		textArguments.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});
		// dummy label, because we have two columns
		new Label(composite, SWT.NONE);

		// remote debug settings.
		Label labelRemoteDebug = new Label(composite, SWT.NONE);
		labelRemoteDebug.setText("Remote debugging:");
		new Label(composite, SWT.NONE);
		
		// remote debug enable.
		buttonRemoteDebugEnable = new Button(composite, SWT.CHECK);
		buttonRemoteDebugEnable.setText("Enable remote debugging");
		buttonRemoteDebugEnable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
				updateLaunchConfigurationDialog();
				textRemoteDebugPort.setEnabled(buttonRemoteDebugEnable.getSelection());
			}
		});
		
		new Label(composite, SWT.NONE);

		// remote debug port.
		Label labelRemoteDebugPort = new Label(composite, SWT.NONE);
		labelRemoteDebugPort.setText("Remote port:");
		new Label(composite, SWT.NONE);
		textRemoteDebugPort = new Text(composite, SWT.BORDER);
		textRemoteDebugPort.setLayoutData(layoutData);
		textRemoteDebugPort.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});
		textRemoteDebugPort.setEnabled(false);
		
		new Label(composite, SWT.NONE);

		// script file
		Label labelScriptFile = new Label(composite, SWT.NONE);
		labelScriptFile.setText("Script file to execute in the debugger:");
		// dummy label, because we have two columns
		new Label(composite, SWT.NONE);
		textScriptPath = new Text(composite, SWT.BORDER);
		textScriptPath.setLayoutData(layoutData);
		textScriptPath.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});

        Button buttonBrowseScript = new Button(composite, SWT.PUSH);
        buttonBrowseScript.setText("Browse...");
        buttonBrowseScript.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String path = browse();
                if (path != null) {
                    textScriptPath.setText(path);
                }
            }
        });

        new Label(composite, SWT.NONE);
	}

	/** Browse button was clicked */
	protected String browse() {
		FileDialog fileDialog = new FileDialog(composite.getShell());
		try {
			fileDialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
		} catch (Exception e) {
			OcamlPlugin.logError("ocaml plugin error", e);
		}

		return fileDialog.open();
	}

	/** This method was copied from the JDT plug-in */
	protected void updateLaunchConfigurationDialog() {
		if (getLaunchConfigurationDialog() != null) {
			/*
			 * order is important here due to the call to refresh the tab viewer in updateButtons() which
			 * ensures that the messages are up to date
			 */
			getLaunchConfigurationDialog().updateButtons();
			getLaunchConfigurationDialog().updateMessage();
		}
	}

	public String getName() {
		return "Ocaml launch configuration";
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		String bytepath = null;
		String runpath = null;
		String project = null;
		String args = null;
		boolean remoteDebugEnable = false;
		String remoteDebugPort = null;
		String scriptpath = null;
		try {
			runpath = configuration.getAttribute(ATTR_RUNPATH, "");
            bytepath = configuration.getAttribute(ATTR_BYTEPATH, "");
			project = configuration.getAttribute(ATTR_PROJECTNAME, "");
			args = configuration.getAttribute(ATTR_ARGS, "");
			remoteDebugEnable = configuration.getAttribute(ATTR_REMOTE_DEBUG_ENABLE, DEFAULT_REMOTE_DEBUG_ENABLE);
			remoteDebugPort = configuration.getAttribute(ATTR_REMOTE_DEBUG_PORT, DEFAULT_REMOTE_DEBUG_PORT);
			scriptpath = configuration.getAttribute(ATTR_SCRIPTPATH, "");
		} catch (CoreException e) {
			OcamlPlugin.logError("ocaml plugin error", e);
			runpath = "";
            bytepath = "";
			project = "";
			args = "";
			remoteDebugEnable = DEFAULT_REMOTE_DEBUG_ENABLE;
			remoteDebugPort = DEFAULT_REMOTE_DEBUG_PORT;
			scriptpath = "";
		}

		textBytePath.setText(bytepath);
		textRunPath.setText(runpath);
		textProjectName.setText(project);
		textArguments.setText(args);
		buttonRemoteDebugEnable.setSelection(remoteDebugEnable);
		textRemoteDebugPort.setText(remoteDebugPort);
		textRemoteDebugPort.setEnabled(remoteDebugEnable);
		textScriptPath.setText(scriptpath);
		setDirty(false);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(ATTR_RUNPATH, textRunPath.getText().trim());
		configuration.setAttribute(ATTR_BYTEPATH, textBytePath.getText().trim());
		configuration.setAttribute(ATTR_PROJECTNAME, textProjectName.getText());
		configuration.setAttribute(ATTR_ARGS, textArguments.getText());
		configuration.setAttribute(ATTR_REMOTE_DEBUG_ENABLE, buttonRemoteDebugEnable.getSelection());
		configuration.setAttribute(ATTR_REMOTE_DEBUG_PORT, textRemoteDebugPort.getText());
		configuration.setAttribute(ATTR_SCRIPTPATH, textScriptPath.getText());
		setDirty(false);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(ATTR_RUNPATH, "");
		configuration.setAttribute(ATTR_BYTEPATH, "");
		configuration.setAttribute(ATTR_PROJECTNAME, "");
		configuration.setAttribute(ATTR_ARGS, "");
		configuration.setAttribute(ATTR_REMOTE_DEBUG_ENABLE, DEFAULT_REMOTE_DEBUG_ENABLE);
		configuration.setAttribute(ATTR_REMOTE_DEBUG_PORT, DEFAULT_REMOTE_DEBUG_PORT);
		configuration.setAttribute(ATTR_SCRIPTPATH, "");
		setDirty(false);
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		String bytepath = null;
		String runpath = null;
		String project = null;
		String remoteDebugPort = null;
		String scriptpath = null;
		try {
            runpath = launchConfig.getAttribute(ATTR_RUNPATH, "");
			bytepath = launchConfig.getAttribute(ATTR_BYTEPATH, "");
			project = launchConfig.getAttribute(ATTR_PROJECTNAME, "");
			remoteDebugPort = launchConfig.getAttribute(ATTR_REMOTE_DEBUG_PORT, DEFAULT_REMOTE_DEBUG_PORT);
            scriptpath = launchConfig.getAttribute(ATTR_SCRIPTPATH, "");
		} catch (CoreException e) {
			OcamlPlugin.logError("ocaml plugin error", e);
			bytepath = "";
			runpath = "";
			project = "";
			remoteDebugPort = DEFAULT_REMOTE_DEBUG_PORT;
			scriptpath = "";
		}

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		boolean bFound = false;
		for (IProject p : projects)
			if (p.getName().equals(project)) {
				bFound = true;
				break;
			}

		if (!bFound) {
			setErrorMessage("Invalid project name: " + project);
			return false;
		}

		File runfile = new File(runpath);
		if (! (runfile.exists() && runfile.isFile())) {
			setErrorMessage("Invalid executable: " + runpath);
			return false;
		}

		if (bytepath.length() > 0) {
    		File bytefile = new File(bytepath);
    
    		if (! (bytefile.exists() && bytefile.isFile())) {
    		    setErrorMessage("Invalid bytecode file: " + bytepath);
    		    return false;
            }
		}

		int remoteDebugPortValue = -1;
		try {
			remoteDebugPortValue = Integer.parseInt(remoteDebugPort);
		} catch (NumberFormatException e) {/* Do nothing */}
		if (remoteDebugPortValue < 1 || remoteDebugPortValue > 65535) {
			setErrorMessage("Invalid remote debug port: " + remoteDebugPort);
			return false;
		}

		if (scriptpath.length() > 0) {
    		File scriptfile = new File(scriptpath);
    
    		if (! (scriptfile.exists() && scriptfile.isFile())) {
    		    setErrorMessage("Invalid script file: " + scriptpath);
    		    return false;
            }
		}

		// Success.
		setErrorMessage(null);
		return true;
	}
}
