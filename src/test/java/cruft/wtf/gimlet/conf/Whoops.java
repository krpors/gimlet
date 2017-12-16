package cruft.wtf.gimlet.conf;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by krpors on 16-12-17.
 */
public class Whoops {

    @Test
    public void easd() {
        Node root = new Node("root");

        root.getChildren().add(new Node("Child 0"));
        root.getChildren().add(new Node("Child 1"));
        root.getChildren().add(new Node("Child 2"));

        root.getChildren().get(0).getChildren().add(new Node("grand child 0-0"));
        root.getChildren().get(0).getChildren().add(new Node("grand child 0-1"));
        root.getChildren().get(0).getChildren().add(new Node("grand child 0-2"));

        root.getChildren().get(1).getChildren().add(new Node("grand child 1-0"));
        root.getChildren().get(1).getChildren().add(new Node("grand child 1-1"));
        root.getChildren().get(1).getChildren().add(new Node("grand child 1-2"));

        root.getChildren().get(2).getChildren().add(new Node("grand child 2-0"));

        System.out.println(root.getName());
        for (Node r : root.getChildren()) {
            Queue<Node> q = new ArrayDeque<>();
            q.add(r);

            while(!q.isEmpty()) {

                System.out.println(q.poll().getName());
            }
        }
    }

    private static class Node {
        private String name;

        private List<Node> children = new ArrayList<>();

        public Node(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Node> getChildren() {
            return children;
        }

        public void setChildren(List<Node> children) {
            this.children = children;
        }
    }
}
