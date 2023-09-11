package org.openmbee.mdk.generator;

import org.openmbee.mdk.model.Container;
import org.openmbee.mdk.model.DocGenElement;
import org.openmbee.mdk.model.Document;
import org.openmbee.mdk.model.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * process document model to set those with option "useSectionNameAsTitle"
 * should probably be redone as a visitor or include in the transformation to DB
 * classes
 *
 * @author dlam
 */
public class PostProcessor {

    private Stack<String> titles;

    public PostProcessor() {
        titles = new Stack<String>();
    }

    public void process(Document d) {
        titles.push(d.getTitle());
        processContainer(d);
        titles.pop();
    }

    private void processContainer(Container c) {
        for (DocGenElement de : c.getChildren()) {
            if (de instanceof Container) {
                String title = ((Container) de).getTitle();
                if ((title == null || title.isEmpty()) && de.getUseContextNameAsTitle()) {
                    ((Container) de).setTitle(titles.peek());
                }
                titles.push(((Container) de).getTitle());
                processContainer((Container) de);
                titles.pop();
            }
            else if (de instanceof Query) {
                List<String> title = ((Query) de).getTitles();
                if ((title == null || title.isEmpty()) && de.getUseContextNameAsTitle()) {
                    title = new ArrayList<String>();
                    title.add(titles.peek());
                    ((Query) de).setTitles(title);
                }
            }
        }
    }
}
