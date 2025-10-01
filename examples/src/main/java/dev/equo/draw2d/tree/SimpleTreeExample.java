package dev.equo.draw2d.tree;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;

import dev.equo.draw2d.AbstractExample;

public class SimpleTreeExample extends AbstractExample {

	public static void main(String[] args) {
		new SimpleTreeExample().run();
	}

	@Override
	protected IFigure createContents() {
		getFigureCanvas().setBackground(ColorConstants.white);
		TreeRoot root = new TreeRoot(new PageNode("Graph Root")); //$NON-NLS-1$
		root.setAlignment(PositionConstants.LEFT);

		TreeBranch branch = new TreeBranch(new PageNode("Child 1")); //$NON-NLS-1$
		root.addBranch(branch);

		branch = new TreeBranch(new PageNode("Child 2"), TreeBranch.STYLE_HANGING); //$NON-NLS-1$
		root.addBranch(branch);
		branch.addBranch(new TreeBranch(new PageNode("Child 1"))); //$NON-NLS-1$

		return root;
	}

}
