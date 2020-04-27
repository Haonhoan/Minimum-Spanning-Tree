package app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import structures.Arc;
import structures.Graph;
import structures.PartialTree;
import structures.Vertex;
import structures.MinHeap;

/**
 * Stores partial trees in a circular linked list
 * 
 */
public class PartialTreeList implements Iterable<PartialTree> {
    
	/**
	 * Inner class - to build the partial tree circular linked list 
	 * 
	 */
	public static class Node {
		/**
		 * Partial tree
		 */
		public PartialTree tree;
		
		/**
		 * Next node in linked list
		 */
		public Node next;
		
		/**
		 * Initializes this node by setting the tree part to the given tree,
		 * and setting next part to null
		 * 
		 * @param tree Partial tree
		 */
		public Node(PartialTree tree) {
			this.tree = tree;
			next = null;
		}
	}

	/**
	 * Pointer to last node of the circular linked list
	 */
	private Node rear;
	
	/**
	 * Number of nodes in the CLL
	 */
	private int size;
	
	/**
	 * Initializes this list to empty
	 */
    public PartialTreeList() {
    	rear = null;
    	size = 0;
    }

    /**
     * Adds a new tree to the end of the list
     * 
     * @param tree Tree to be added to the end of the list
     */
    public void append(PartialTree tree) {
    	Node ptr = new Node(tree);
    	if (rear == null) {
    		ptr.next = ptr;
    	} else {
    		ptr.next = rear.next;
    		rear.next = ptr;
    	}
    	rear = ptr;
    	size++;
    }

    /**
	 * Initializes the algorithm by building single-vertex partial trees
	 * 
	 * @param graph Graph for which the MST is to be found
	 * @return The initial partial tree list
	 */
	public static PartialTreeList initialize(Graph graph) {
		PartialTreeList list = new PartialTreeList();
		
		for(int i = 0; i < graph.vertices.length; i++) {
			PartialTree vrtx = new PartialTree(graph.vertices[i]);
			Vertex tempVrtx = graph.vertices[i];
			
			while(tempVrtx.neighbors != null) {
				Arc a = new Arc(tempVrtx, tempVrtx.neighbors.vertex, tempVrtx.neighbors.weight);
				vrtx.getArcs().insert(a);
				tempVrtx.neighbors = tempVrtx.neighbors.next;
			}
			
			list.append(vrtx);
			
		}
		
		return list;
	}
	
	/**
	 * Executes the algorithm on a graph, starting with the initial partial tree list
	 * for that graph
	 * 
	 * @param ptlist Initial partial tree list
	 * @return Array list of all arcs that are in the MST - sequence of arcs is irrelevant
	 */
	public static ArrayList<Arc> execute(PartialTreeList ptlist) {
		ArrayList<Arc> L = new ArrayList<Arc>();
		
		while(ptlist.size() > 1) { 
			PartialTree T = ptlist.remove();
			
			MinHeap<Arc> P = new MinHeap<Arc>(); 
			P =	T.getArcs(); 
			Arc low = P.deleteMin();
			
			while(low != null) {
				Vertex v1 = low.getv1();
				Vertex v2 = low.getv2();
				
				PartialTree removeTree = ptlist.removeTreeContaining(v1); 
				
				if(removeTree != null) {
					T.merge(removeTree);
					L.add(low);
					ptlist.append(T);
					
					break;
				}
				else{
					removeTree = ptlist.removeTreeContaining(v2);
				}
				
				if(removeTree != null) {
					T.merge(removeTree);
					L.add(low);
					ptlist.append(T);
					
					break;
				}
				
				low = P.deleteMin();
			}
			
		}

		return L;
	}
	
    /**
     * Removes the tree that is at the front of the list.
     * 
     * @return The tree that is removed from the front
     * @throws NoSuchElementException If the list is empty
     */
    public PartialTree remove() 
    throws NoSuchElementException {
    			
    	if (rear == null) {
    		throw new NoSuchElementException("list is empty");
    	}
    	PartialTree ret = rear.next.tree;
    	if (rear.next == rear) {
    		rear = null;
    	} else {
    		rear.next = rear.next.next;
    	}
    	size--;
    	return ret;
    		
    }

    /**
     * Removes the tree in this list that contains a given vertex.
     * 
     * @param vertex Vertex whose tree is to be removed
     * @return The tree that is removed
     * @throws NoSuchElementException If there is no matching tree
     */
    public PartialTree removeTreeContaining(Vertex vertex) 
    throws NoSuchElementException {
    		if(rear == null) {
    			throw new NoSuchElementException();
    		}
    		
    		PartialTree remove = null;
    		boolean available = false;
    		Node temp = rear;
    		
    		do {
    			PartialTree tree = temp.tree;
    			available = checkVrtx(tree, vertex);
    			
    			if(available == true) {
    				remove = tree;
    				removeTree(temp.next, temp);
    				break;
    			}
    			
    			temp = temp.next;
    		} while(temp != rear);
    		
    		if(remove == null) {
    			return null;
    		}
    		return remove;
     }
    
    private boolean checkVrtx (PartialTree tree, Vertex vrtx) {
    	while(vrtx != null)
        {
            if(vrtx.equals(tree.getRoot())){
                return true;
            }
            if(vrtx.equals(vrtx.parent)){
                return false;
            }
        
            vrtx = vrtx.parent;
        }
    	return false;
    }
    
    private void removeTree(Node current, Node prev) {
    	Node node = prev;
        while (prev.next != node) {
        	prev = prev.next;
        }

        if (current == node && prev == node) {
        	rear = null;
        	size--;
        }
        else if (current == prev) {
        	if (node == rear) {                      
        		rear = rear.next;
        	}
            
        	node.next.next = node.next;            
        	size--;
        }
        else {
        	if (node == rear) {     
        		rear = prev;
        	}
               
        	prev.next = current;
        	size--;
        }            
    }
    /**
     * Gives the number of trees in this list
     * 
     * @return Number of trees
     */
    public int size() {
    	return size;
    }
    
    /**
     * Returns an Iterator that can be used to step through the trees in this list.
     * The iterator does NOT support remove.
     * 
     * @return Iterator for this list
     */
    public Iterator<PartialTree> iterator() {
    	return new PartialTreeListIterator(this);
    }
    
    private class PartialTreeListIterator implements Iterator<PartialTree> {
    	
    	private PartialTreeList.Node ptr;
    	private int rest;
    	
    	public PartialTreeListIterator(PartialTreeList target) {
    		rest = target.size;
    		ptr = rest > 0 ? target.rear.next : null;
    	}
    	
    	public PartialTree next() 
    	throws NoSuchElementException {
    		if (rest <= 0) {
    			throw new NoSuchElementException();
    		}
    		PartialTree ret = ptr.tree;
    		ptr = ptr.next;
    		rest--;
    		return ret;
    	}
    	
    	public boolean hasNext() {
    		return rest != 0;
    	}
    	
    	public void remove() 
    	throws UnsupportedOperationException {
    		throw new UnsupportedOperationException();
    	}
    	
    }
}


