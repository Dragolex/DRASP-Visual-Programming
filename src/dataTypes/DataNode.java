package dataTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import execution.handlers.InfoErrorHandler;


public class DataNode<T> {
    private List<DataNode<T>> children = new ArrayList<DataNode<T>>();
    private DataNode<T> parent = null;
    private T data = null;
    
    private boolean childrenHidden = false;
    private List<DataNode<T>> alwaysEmpty = new ArrayList<DataNode<T>>();

    // Constructors
    public DataNode(T data) {
        this.data = data;
    }

    public DataNode(T data, DataNode<T> parent) {
        this.data = data;
        this.parent = parent;
    }

    

	// Getters and setters
    public List<DataNode<T>> getChildren()
    {
    	if (!childrenHidden)
    		return(children);
    	else return(alwaysEmpty);
    	
    }

    public List<DataNode<T>> getChildrenAlways()
    {
   		return(children);    	
    }
    
    
    

    public DataNode<T> setParent(DataNode<T> parent) {
        this.parent = parent;
        parent.addChild(this);
        return(this);
    }
    
    public void addChild(DataNode<T> child) {
    	if (!children.contains(child))
    	{
	        this.children.add(child);
	        child.setParent(this);
    	}
    }
    
    public DataNode<T> addChild(T data) {
        DataNode<T> child = new DataNode<T>(data);
        addChild(child);
        return(child);
    }

	public void addChildAt(int ind, DataNode<T> newChild)
	{
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to add children to a node with hidden children. Mode: 0");
			return;
		}
		
		children.add(ind, newChild);
		newChild.setParent(this);
	}
    
	public void addChildAt(int ind, T element)
	{
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to add children to a node with hidden children. Mode: 1");
			return;
		}
		
		DataNode<T> newChild = new DataNode(element);
		children.add(ind, newChild);
		newChild.setParent(this);
	}


	public void addChildBefore(DataNode<T> origChild, DataNode<T> newChild)
	{
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to add children to a node with hidden children. Mode: 2");
			return;
		}

		
		int ind = children.indexOf(origChild);
		
		if (ind == -1)
		{
			if (origChild == this)
			{
				if (!isRoot())
					parent.addChildBefore(this, newChild);
				else
					InfoErrorHandler.printMinorExecutionErrorMessage("Tried to insert a child into a DataNode before its root.");
			}
			else
				InfoErrorHandler.printMinorExecutionErrorMessage("Tried to insert a child before a non-existing DataNode.");
		}
		else
			addChildAt(ind, newChild);
	}
	
	
	
	public DataNode<T> getChildBefore(DataNode<T> origChild)
	{
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to add children to a node with hidden children. Mode: 3");
			return(null);
		}

		
		int ind = children.indexOf(origChild);
		
		if (ind == -1)
		{
			if (origChild == this)
			{
				if (!isRoot())
					return(parent.getChildBefore(this));
				else
					InfoErrorHandler.printMinorExecutionErrorMessage("Tried to get a DataNode before its root.");
			}
			else
				InfoErrorHandler.printMinorExecutionErrorMessage("Tried to insert a child before a non-existing DataNode.");
		}
		else
			return(children.get(Math.max(0, ind-1)));
		
		return(null);
	}

	
	
	
	
	public void addChildAtTreeIndex(int ind, T element, int addAsSubChildMode)
	{
		addChildAtTreeIndex(ind, new DataNode<T>(element), addAsSubChildMode);
	}
	
	public void addChildAtTreeIndex(int ind, DataNode<T> newChild, int addAsSubChildMode)
	{
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to add children to a node with hidden children. Mode: 4");
			return;
		}

		
		if (ind == 0)
		{
			if (!isRoot())
				parent.addChildBefore(this, newChild);
			else
				if (addAsSubChildMode == 1)
					this.addChildAt(0, newChild);
				else
				if (addAsSubChildMode == 2)
					this.addChildAt(children.size(), newChild);
				else
					InfoErrorHandler.printMinorExecutionErrorMessage("Tried to insert a child before its root.");
			return;
		}
		
		if (ind == 1)
		{
			if (addAsSubChildMode == 0)
				this.addChildAt(0, newChild); // Add to the right position

			if (addAsSubChildMode == 1)
				if (!isLeaf())
					children.get(0).addChildAt(0, newChild);
			
			if (addAsSubChildMode == -1)
				if (!isRoot())
					this.parent.addChildBefore(this, newChild);
			
			return;
		}
		
			
		if (addChildAtTreeIndex(ind, newChild, addAsSubChildMode, 1, 0) >= 0)
			addChild(newChild);
		
	}

	private int addChildAtTreeIndex(int ind, DataNode<T> newChild, int addAsSubChildMode, int count, int depth)
	{
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to add children to a node with hidden children. Mode: 5  Children: " + children.size());
			return(0);
		}
		

		for(int i = 0; i < children.size(); i++)
		{
			
			if (ind == (count+depth))
			{
				if (addAsSubChildMode == 0)
					this.addChildAt(i, newChild); // Add to the right position
				else
				if (addAsSubChildMode == 1)
					children.get(i).addChildAt(0, newChild);
				else
				if (addAsSubChildMode == 2)
					children.get(i).addChildAt(children.get(i).getChildrenAlways().size(), newChild);
				
				return(-1000);
				
			}
			
			
			if (!children.get(i).hasChildrenHidden())
				count = children.get(i).addChildAtTreeIndex(ind, newChild, addAsSubChildMode, count, depth+1)+1;
			else
				count++;
		}
		

		
		if (addAsSubChildMode == -1)
			if (ind == (count+depth))
				if (!isRoot())
				{				
					if ((children.size() > 0) && (children.get(children.size()-1).hasChildrenHidden()))
						this.addChild(newChild);
					else
						this.parent.addChild(newChild);

					return(-1000);				
				}

		
		return(count);

	}
	

	
	
	
	

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf()
    {
    	//if (childrenHidden)
    		//return(true);
    	
        if(this.children.size() == 0) 
            return true;
        else 
            return false;
    }

    public void removeParent() {
        this.parent = null;
    }

	public DataNode<T> getParent()
	{
		return(this.parent);
	}
		
	/*
	public List<DataNode<T>> getChildrenAllLinearly()
	{
		List<DataNode<T>> linearList = new ArrayList<DataNode<T>>();
		
		for(int i = 0; i < children.size(); i++)
			linearList.add(children.get(i));
		
		return(linearList);
	}
	
	private void getChildrenAllLinearly(List<DataNode<T>> linearList)
	{
		if (this.isLeaf())
			linearList.add((DataNode<T>) this);
		else
		{
			for(int i = 0; i < children.size(); i++)
				linearList.add(children.get(i));
		}
		
	}
	*/

	public void printAll()
	{
		printAll("--");
	}
	
	public void printAll(String preStr)
	{
		if (data == null)
			InfoErrorHandler.printDirectMessage(preStr + " EMPTY NODE");		
		else
			InfoErrorHandler.printDirectMessage(preStr + " " + data.toString());		
		
		if (!isLeaf())
		{
			for(int i = 0; i < children.size(); i++)
				children.get(i).printAll(preStr + preStr);
		}
	}
	
	public void applyToChildrenTotal(T[] dat, Runnable task, boolean includeHidden)
	{
		/*
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to apply to a node with hidden children. Mode: 0");
			return;
		}*/

		
		if (data != null)
		{
			dat[0] = data;
			task.run();
		}
		
		if (!isLeaf() && (!childrenHidden || includeHidden))
		{
			for(int i = 0; i < children.size(); i++)
				children.get(i).applyToChildrenTotal(dat, task, includeHidden);
		}
	}
	
	public void applyToChildrenTotal(T[] dat, Integer[] depth, Runnable task, boolean includeHidden)
	{
		/*
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to apply to a node with hidden children. Mode: 1");
			return;
		}*/
		
		if (depth[0] == null)
			depth[0] = 0;
				
		if (data != null)
		{
			dat[0] = data;
			task.run();
		}
		
		if ((!isLeaf()) && (!childrenHidden || includeHidden))
		{
			for(int i = 0; i < children.size(); i++)
			{
				depth[0] += 1;
				children.get(i).applyToChildrenTotal(dat, depth, task, includeHidden);
				depth[0] -= 1;
			}
		}
	}	
	
	public void applyToChildrenTotal(T[] dat, DataNode[] node, Runnable task, boolean includeHidden)
	{
		/*
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to apply to a node with hidden children. Mode: 2");
			return;
		}*/
		
		if (data != null)
		{
			dat[0] = data;
			node[0] = this;
			task.run();
		}
		
		if (!isLeaf() && (!childrenHidden || includeHidden))
		{
			for(int i = 0; i < children.size(); i++)
				children.get(i).applyToChildrenTotal(dat, node, task, includeHidden);
		}
		
	}
	
	public void applyToChildrenTotal(DataNode[] node, Runnable task, boolean includeHidden)
	{
		/*
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to apply to a node with hidden children. Mode: 2");
			return;
		}*/
		
		if (data != null)
		{
			node[0] = this;
			task.run();
		}
		
		if (!isLeaf() && (!childrenHidden || includeHidden))
		{
			for(int i = 0; i < children.size(); i++)
				children.get(i).applyToChildrenTotal(node, task, includeHidden);
		}
		
	}

	
	public void applyToChildrenTotal(T[] dat, DataNode[] node, Integer[] depth, Runnable task, boolean includeHidden)
	{
		/*
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to apply to a node with hidden children. Mode: 1");
			return;
		}*/
		
		if (depth[0] == null)
			depth[0] = 0;
				
		if (data != null)
		{
			dat[0] = data;
			node[0] = this;
			task.run();
		}
		
		if ((!isLeaf()) && (!childrenHidden || includeHidden))
		{
			for(int i = 0; i < children.size(); i++)
			{
				depth[0] += 1;
				children.get(i).applyToChildrenTotal(dat, node, depth, task, includeHidden);
				depth[0] -= 1;
			}
		}
	}
	
	
	public void selfreplaceTotalNodes(T[] dat, Callable<T> task, boolean includeHidden)
	{
		if (data != null)
		{
			dat[0] = data;
			
			try {
				data = task.call();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		if (!isLeaf() && (!childrenHidden || includeHidden))
		{
			for(int i = 0; i < children.size(); i++)
				children.get(i).selfreplaceTotalNodes(dat, task, includeHidden);
		}
	}

	
	public int countChildrenAsTree()
	{
		if (isLeaf()) return(0);
		return(countVisibleChildren(this));
	}
	
	private int countVisibleChildren(DataNode<T> root)
	{
		if (root.isLeaf())
			return(1);
		
		int count = 0;
		
		List<DataNode<T>> childs = root.getChildren();
		
		for(int i = 0; i < childs.size(); i++)
		{
			//if (childs.get(i).getData().isExpandedOneGUI())
			count += countVisibleChildren(childs.get(i));
		}
			
		return(count+1);
	}
	
	public void removeChild(DataNode<T> nodeToRemove)
	{
		if (childrenHidden)
		{
			InfoErrorHandler.callBugError("Trying to remove child from a node with hidden children. Mode: 0");
			return;
		}
		
		if (children.contains(nodeToRemove))
			children.remove(nodeToRemove);
	}
	public void removeChildAlways(DataNode<T> nodeToRemove)
	{		
		if (children.contains(nodeToRemove))
			children.remove(nodeToRemove);
	}
	
	
	
	public boolean hasChildrenHidden()
	{
		return(childrenHidden);
	}
	public void hideChildren()
	{
		childrenHidden = true;	
	}
	public void unhideChildren()
	{
		childrenHidden = false;	
	}

	public void setChildren(List<DataNode<T>> childrenToInsert)
	{
		children = childrenToInsert;
		for (DataNode<T> child: children)
			child.setParent(this);
	}


	public void removeAllChildren()
	{
		children.clear();
	}
	

	public int getDepth()
	{
		int d = 0;
		DataNode<T> n = this;
		
		while(!n.isRoot())
		{
			n = n.getParent();
			d++;
		}
		
		return(d);
	}
	
	
	public DataNode<T> getRoot()
	{
		DataNode<T> trueParent = this;
		while(!trueParent.isRoot())
			trueParent = trueParent.getParent();
		
		return(trueParent);
	}

	public void replaceChild(DataNode<T> oldNode, DataNode<T> newNode)
	{
		children.set(children.indexOf(oldNode), newNode);
		newNode.parent = this;
	}

	public int indexInTree(T element)
	{
		DataNode[] dat = new DataNode[1];
		Integer[] ind = new Integer[1];
		ind[0] = 0;
		
		Integer[] res = new Integer[1];
		res[0] = -1;
		
		applyToChildrenTotal(dat, () -> { if (dat[0].getData() == element) res[0] = ind[0]; ind[0]++; }, true);
		
		return(res[0]);
	}
	
	public void removeAnywhereInTree(T element)
	{
		DataNode[] dat = new DataNode[1];
		applyToChildrenTotal(dat, () -> { if (dat[0].getData() == element) dat[0].getParent().removeChild(dat[0]); }, true);
	}

	public DataNode<ProgramElement> getNodeOfInTree(T element)
	{
		DataNode[] dat = new DataNode[1];
		DataNode[] res = new DataNode[1];
		res[0] = null;
		applyToChildrenTotal(dat, () -> { if (dat[0].getData() == element) res[0] = dat[0]; }, true);
		
		return(res[0]);
	}	
	
	
	

	
}