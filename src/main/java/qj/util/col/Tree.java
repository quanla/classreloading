package qj.util.col;

import java.util.*;

import qj.util.Cols;
import qj.util.funct.F1;
import qj.util.funct.Fs;
import qj.util.funct.P1;

public class Tree<A> {
	public String name;
	public A value;
	public Tree<A> up;
	public List<Tree<A>> downs = new LinkedList<Tree<A>>();
	public Tree(A value) {
		this.value = value;
	}
	public Tree(String name, A value) {
		this.name = name;
		this.value = value;
	}
	public String path() {
		if (up==null) {
			return name==null ? "" : name;
		} else {
			return up.path() + "/" + name;
		}
	}

	
	public void replaceNode(Tree<A> replaced, Tree<A> newValue) {
		int i = 0;
		for (Iterator<Tree<A>> iterator = downs.iterator(); iterator.hasNext();i++) {
			Tree<A> node = iterator.next();
			if (node==replaced) {
				iterator.remove();
				downs.add(i, newValue);
				return;
			}
		}
		downs.add(newValue);
	}

	public void each(P1<A> p) {
		p.e(value);
		
		for (Tree<A> down : downs) {
			down.each(p);
		}
	}
	public void eachTree(P1<Tree<A>> p) {
		eachTree(Fs.f1(p, false));
	}
	public boolean eachTree(F1<Tree<A>,Boolean> f) {
		if (f.e(this)) return true;
		
		for (Tree<A> down : downs) {
			if (down.eachTree(f)) return true;
		}
		return false;
	}

	public void add(String name, A val) {
		add(new Tree<A>(name, val));
	}
	
	public void remove(A a) {
		for (Tree<A> tree : downs) {
			if (tree.value == a) {
				downs.remove(tree);
				return;
			}
		}
	}

	public void add(Tree<A> tree) {
		if (tree.up != null) {
			throw new RuntimeException("Tree already has upline");
		}
		tree.up = this;
		downs.add(tree);
	}
	public static <A> Tree<A> tree(A node, A... childrens) {
		Tree<A> tree = new Tree<A>(node);

		for (A child : childrens) {
			tree.add(null, child);
		}
		return tree;
	}
	
	public static <A> Tree<A> tree(A node, Tree<A>... childrens) {
		Tree<A> tree = new Tree<A>(node);

		for (Tree<A> child : childrens) {
			tree.add(child);
		}
		return tree;
	}
	
	public boolean contains(A target, Collection<A> activeNodes) {
		return contains(target, activeNodes, this);
	}

	public static <A> boolean contains(final A right, Collection<A> hadRights, Tree<A> rightTree) {
		if (Cols.isEmpty(hadRights)) {
			return false;
		}
		if (hadRights.contains(right)) {
			return true;
		}
		
		final boolean found[] = {false};
		eachActive(hadRights,rightTree, new F1<A,Boolean>() {public Boolean e(A hadRight) {
			if (hadRight.equals(right)) {
				found[0] = true;
				return false;
			}
			return true;
		}});
		return found[0];
	}

	/**
	 * 
	 * @param activeNodes
	 * @param rightTree
	 * @param f1
	 * @return goon
	 */
	private static <A> boolean eachActive(Collection<A> activeNodes,
			Tree<A> rightTree, F1<A, Boolean> f1) {
		if (activeNodes==null) {
			return false;
		}
		if (activeNodes.contains(rightTree.value)) {
			return eachNode(rightTree, f1);
		}

		for (Tree<A> downTree : rightTree.downs) {
			if (!eachActive(activeNodes,downTree, f1)) {
				return false;
			}
		}
		return true;
	}

	private static <A> boolean eachNode(Tree<A> rightTree,
			F1<A, Boolean> f1) {
		if (!f1.e(rightTree.value)) {
			return false;
		}
		
		for (Tree<A> downTree : rightTree.downs) {
			if (!eachNode(downTree, f1)) {
				return false;
			}
		}
		return true;
	}
	public Tree<A> getDown(String path) {
		if (path == null) {
			return this;
		}

		LinkedList<String> paths = parsePaths(path);
		
		return getTree(paths, this, false);
	}
	public static LinkedList<String> parsePaths(String path) {
		path = path.replaceFirst("^/", "");
		return new LinkedList<String>(Arrays.asList(path.split("/")));
	}
	public static <A> Tree<A> getTree(List<String> paths, Tree<A> tree, boolean force) {
		return getTree(paths, tree, force ? MODE_FORCE : MODE_NORMAL, 
				new F1<String,Tree<A>>() {public Tree<A> e(String name) {
					return new Tree<A>(name, null);
				}}
		);
	}

	public static final int MODE_NORMAL = 0;
	public static final int MODE_FORCE = 1;
	public static final int MODE_FALLBACK = 2;
	public static <A> Tree<A> getTree(List<String> paths, Tree<A> tree, int mode, F1<String,Tree<A>> constructor) {
		String name = paths.get(0);
		Tree<A> downTree = getDownTree(name,tree.downs);
		
		if (downTree == null) {
			if (MODE_FORCE == mode) {
				downTree = constructor.e(name);
				tree.downs.add(downTree);
				downTree.up = tree;
			} else if (MODE_FALLBACK == mode) {
				// Not found
				return tree;
			} else { // MODE_NORMAL
				return null;
			}
		}
		if (paths.size() == 1) {
			return downTree;
		} else {
			return getTree(paths.subList(1, paths.size()), downTree, mode,constructor);
		}
	}
	private static <A> Tree<A> getDownTree(String name, List<Tree<A>> downs) {
		for (Tree<A> down : downs) {
			if (down.name.equals(name)) {
				return down;
			}
		}
		return null;
	}
	public void remove(Tree<A> tree) {
		downs.remove(tree);
	}
	
	
	public void eachChild(int level, P1<Tree<A>> p) {
		if (level == 0) {
			p.e(this);
			return;
		}
		for (Tree<A> down : downs) {
			down.eachChild(level - 1, p);
		}
	}
	public boolean eachUp(F1<Tree<A>,Boolean> f) {
		if (this.up != null) {
			if (f.e(this.up)) {
				return true;
			}
			return this.up.eachUp(f);
		}
		return false;
	}
	public static <A> LinkedList<Tree<A>> getLine(Tree<A> upNode, Tree<A> downNode) {
		LinkedList<Tree<A>> ret = new LinkedList<Tree<A>>();
		
		Tree<A> node = downNode;
		while (true) {
			ret.addFirst(node);
			if (node.equals(upNode)) {
				break;
			}
			node = node.up;
			if (node == null) {
				throw new IllegalArgumentException("Can not find upNode");
			}
		}
		
		return ret;
	}
	
	
	public static <A> F1<Tree<A>,A> valF() {return new F1<Tree<A>, A>() {public A e(Tree<A> obj) {
		return obj.value;
	}};}
	public static <A> F1<Tree<A>,Collection<Tree<A>>> downsF() {return new F1<Tree<A>, Collection<Tree<A>>>() {public Collection<Tree<A>> e(Tree<A> obj) {
		return obj.downs;
	}};}
	
	@Override
	public String toString() {
		return "Tree (" + value + ")";
	}
}
