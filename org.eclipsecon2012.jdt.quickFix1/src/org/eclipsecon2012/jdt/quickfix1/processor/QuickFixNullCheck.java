package org.eclipsecon2012.jdt.quickfix1.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;

public class QuickFixNullCheck implements IQuickFixProcessor {

	@Override
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		return IProblem.PotentialNullLocalVariableReference == problemId;
	}

	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		HashSet<Integer> handledProblems= new HashSet<Integer>(locations.length);
		ArrayList<IJavaCompletionProposal> resultingCollections= new ArrayList<IJavaCompletionProposal>();
		for (int i= 0; i < locations.length; i++) {
			IProblemLocation curr= locations[i];
			Integer id= new Integer(curr.getProblemId());
			if (handledProblems.add(id)) {
				process(context, curr, resultingCollections);
			}
		}
		return resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
	}
	
	private void process(final IInvocationContext context,
			final IProblemLocation problem,
			final ArrayList<IJavaCompletionProposal> proposals)
			throws CoreException {
		final int id = problem.getProblemId();
		if (id == 0) { // no proposals for none-problem locations
			return;
		}
		switch (id) {
			// 1. Which proposal(s) do we want. Call a method to handle each proposal.
			case IProblem.PotentialNullLocalVariableReference:
				QuickFixNullCheck.addNullCheckProposal(context, problem, proposals);
				break;
		}
	}
	
	public static void addNullCheckProposal(IInvocationContext context, IProblemLocation problem, Collection<IJavaCompletionProposal> proposals){
		ICompilationUnit cu= context.getCompilationUnit();
		ASTNode selectedNode= problem.getCoveredNode(context.getASTRoot());
		AST ast = selectedNode.getAST();
		
		ASTRewrite rewrite = ASTRewrite.create(ast);
		String label= "add null check";
		
		// 2. add sanity checks and AST modifications
		if (!(selectedNode instanceof SimpleName)) {
			return;
		}
		
		// 2a. What new AST nodes do we want? What AST node do we want to remove?
		
		// 2b. Create skeletal ifStatement, a Block to be added as the 'then' statement and an expression to be added
		// as the condition for the ifStatement. Set the if's condition and then statement

		// create IfStatement
		IfStatement ifStatement = ast.newIfStatement();
				
		// create condition
		InfixExpression exp = ast.newInfixExpression();
		
		// create a block and add the dereference statement into that block
		Block block = ast.newBlock();
		
		ifStatement.setExpression(exp);
		
		ifStatement.setThenStatement(block);
		
		// 2c. Set the proper operands and operator of the if's condition expression
		SimpleName name = (SimpleName) selectedNode;
		exp.setOperator(InfixExpression.Operator.NOT_EQUALS);
		SimpleName operandName = ast.newSimpleName(name.getIdentifier());
		exp.setLeftOperand(operandName);
		NullLiteral nullLiteral = ast.newNullLiteral();
		exp.setRightOperand(nullLiteral);
		
		// 2d. Add the earlier dereferencing statement into the if's then block
		Statement blockSt = (Statement)rewrite.createMoveTarget(name.getParent().getParent());
		List<Statement> blockStatements = block.statements();
		blockStatements.add(blockSt);
		
		// 2e. replace the dereferencing statement with the if statement using the 'rewrite' object.
		rewrite.replace(name.getParent().getParent(), ifStatement, null);
		
		ASTRewriteCorrectionProposal proposal= new ASTRewriteCorrectionProposal(label, cu, rewrite, 6);
			
		proposals.add(proposal);
	}

}
