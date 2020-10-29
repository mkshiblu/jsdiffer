package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.FunctionDeclaration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CallTree {
    private CallTreeNode root;

    public CallTree(CallTreeNode root) {
        this.root = root;
    }

    public List<CallTreeNode> getNodesInBreadthFirstOrder() {
        List<CallTreeNode> nodes = new ArrayList<>();
        List<CallTreeNode> queue = new LinkedList<>();
        nodes.add(root);
        queue.add(root);
        while (!queue.isEmpty()) {
            CallTreeNode node = queue.remove(0);
            nodes.addAll(node.getChildren());
            queue.addAll(node.getChildren());
        }
        return nodes;
    }

    public boolean contains(FunctionDeclaration invokedOperation) {
        for (CallTreeNode node : getNodesInBreadthFirstOrder()) {
            if (node.getInvokedOperation().equals(invokedOperation)) {
                return true;
            }
        }
        return false;
    }
}
