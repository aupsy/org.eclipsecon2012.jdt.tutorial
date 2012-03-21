package org.eclipsecon2012.jdt.tutorial.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
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
		if (project.isNatureEnabled(JavaCore.NATURE_ID)) {	// find only java projects
			message.append(project.getName());
			IJavaProject javaProject = JavaCore.create(project);
			for (IPackageFragment mypackage : javaProject.getPackageFragments()) {
				collectNamesForPackage(mypackage, message);
			}
		}
		
	}
	
	private void collectNamesForPackage(IPackageFragment mypackage, StringBuffer message) 
			throws JavaModelException {
		if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) { // deal with packages in source folders and ignore those in binary folders or jars
			String name = mypackage.getElementName();
			if (!name.isEmpty()) {
				message.append("\n-->\tPackage " + mypackage.getElementName());
			}
			else {
				message.append("\n-->\tDefault package");
			}
			for (ICompilationUnit compilationUnit : mypackage.getCompilationUnits()) {
				for (IType type : compilationUnit.getAllTypes()) {
					collectTypeName(type, message);
				}
			}
		}
	}
	
	private void collectTypeName(IType type, StringBuffer message) {
		message.append("\n\t+-> Type "+type.getElementName());
	}
}