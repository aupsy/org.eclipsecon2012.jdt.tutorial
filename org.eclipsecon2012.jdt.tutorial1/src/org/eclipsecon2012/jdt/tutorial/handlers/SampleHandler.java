package org.eclipsecon2012.jdt.tutorial.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class SampleHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell shell = window.getShell();

		try {
			StringBuffer message = new StringBuffer();

			ISelection selection = window.getSelectionService().getSelection();
			collectNamesFromSelection(selection, message);
			if (message.length() > 0) {
				MessageDialog.openInformation(
						shell,
						"Infos",
						message.toString());
				return null;
			}

			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			
			// 3a. get list of all projects in the workspace
			
			// 3b. print the list of projects, packages, types in the dialog
			
		} catch (CoreException ex) {
			throw new ExecutionException("Could not collect names", ex);
		}
		return null;
	}

	private void collectNamesFromSelection(ISelection selection, StringBuffer message) 
			throws CoreException {
		if (selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection)selection).getFirstElement();
			if (first instanceof IJavaProject) {
				collectNamesForProject(((IJavaProject)first).getProject(), message);
			} else if (first instanceof IPackageFragment) {
				collectNamesForPackage((IPackageFragment)first, message);
			} else if (first instanceof IType) {
				collectTypeName((IType)first, message);
			}
			
			// 4. What happens if you click on the "src" folder and then use the command? What's missing?
		}
	}

	private void collectNamesForProject(IProject project, StringBuffer message) 
			throws CoreException {
		// 1a. Check if this is a Java project. If yes, proceed.
		
		// 1b. Append its name to the message
		
		// 1c. Find out all the package fragments by calling collectNamesForPackage(..)
		
	}
	
	private void collectNamesForPackage(IPackageFragment mypackage, StringBuffer message) 
			throws JavaModelException {
		// 2a. Check if this is a source package. If yes, proceed. Why is this step required?
		
		// 2b. Append its name to the message
		
		// 2c. Find out all types in this package and call collectTypeName(..)
	}
	
	private void collectTypeName(IType type, StringBuffer message) {
		message.append("\n\t+-> Type "+type.getElementName());
	}
}