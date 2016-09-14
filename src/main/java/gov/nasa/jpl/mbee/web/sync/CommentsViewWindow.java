/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.web.sync;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Deprecated
public class CommentsViewWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JPanel comments;
    private int commentCount = 0;

    public CommentsViewWindow(String title) {
        super(title);
        comments = commentsPanel();
        getContentPane().add(scrollPane(comments), BorderLayout.CENTER);
        getContentPane().add(buttonPanel(), BorderLayout.PAGE_END);

        setSize(800, 400);
        setMaximumSize(new Dimension(800, 400));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(Application.getInstance().getMainFrame());
    }

    private JPanel commentsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder());
        return panel;
    }

    private JScrollPane scrollPane(JPanel content) {
        JScrollPane scrollPane = new JScrollPane(comments, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private JPanel buttonPanel() {
        final JButton closeButton = new JButton("Close");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(Box.createHorizontalGlue());
        panel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                CommentsViewWindow.this.dispose();
            }
        });
        return panel;
    }

    public void addComment(Comment c, Stereotype s) {
        addComment(c.getBody(), CommentUtil.timestamp(c, s), CommentUtil.author(c, s));
    }

    public void addComment(String body, String time, String author) {
        if (++commentCount > 1) {
            comments.add(Box.createRigidArea(new Dimension(0, 0)));
            comments.add(new JSeparator());
        }
        comments.add(Box.createRigidArea(new Dimension(0, 0)));
        comments.add(new CommentPanel(body.trim(), time, author));
    }

    public void noComments() {
        comments.add(new NoContentPanel());
    }

    public int getCommentCount() {
        return commentCount;
    }

    /**
     * Panel for one comment.
     */
    private class CommentPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        CommentPanel(String body, String date, String author) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(LEFT_ALIGNMENT);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder());

            add(headerPanel(date, author));
            add(textPane(body));
        }

        private JPanel headerPanel(String date, String author) {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));

            JLabel authorLabel = new JLabel(author);
            authorLabel.setBorder(BorderFactory.createEmptyBorder());
            authorLabel.setBackground(Color.WHITE);
            panel.add(bold(authorLabel));

            JLabel timestampLabel = new JLabel("on " + date.replace(" ", " at "));
            timestampLabel.setBorder(BorderFactory.createEmptyBorder());
            timestampLabel.setBackground(Color.WHITE);
            panel.add(timestampLabel);
            return panel;
        }

        private JTextPane textPane(String body) {
            JTextPane textPane = new JTextPane();
            textPane.setEditable(false);
            textPane.setContentType("text/html");
            textPane.setText(body);
            textPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
            return textPane;
        }

        private JLabel bold(JLabel label) {
            Font f = new Font(label.getFont().getName(), Font.BOLD, label.getFont().getSize());
            label.setFont(f);
            return label;
        }
    }

    /**
     * Use this when there are no comments to display.
     */
    private class NoContentPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        NoContentPanel() {
            setAlignmentX(CENTER_ALIGNMENT);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder());

            JLabel label = new JLabel("(no comments)");
            label.setBorder(BorderFactory.createEmptyBorder(20, 200, 50, 200));
            add(italic(label));
        }

        private JLabel italic(JLabel label) {
            Font f = new Font(label.getFont().getName(), Font.ITALIC, label.getFont().getSize());
            label.setFont(f);
            return label;
        }
    }

    /**
     * This is useful for testing the look and feel of the comments view without
     * starting up MagicDraw.
     *
     * @param args
     */
    public static void main(String[] args) {
        CommentsViewWindow frame = new CommentsViewWindow("Comments");

        frame.addComment("<html>This is the <b>third</b> comment</html>", "2012-03-24 01:02:03", "dlam");
        frame.addComment("Second comment", "2012-03-24 01:02:03", "cldelp");

        // for (int i = 0; i < 100; i++) {
        // frame.addComment("Filler", "whenever", "nobody");
        // }

        frame.addComment("First comment\n"
                + "sdcscd sdcsdn sdcsdcsdc sdcsdcsdcs sdcsdcsdcsdcsdc sdcsdcsdcsdcsdcscd sdcsdcsdsdc\n"
                + "sdcsdcsdcsdc\n", "2012-03-24 01:02:03", "dnoble");

        if (frame.commentCount == 0) {
            frame.noComments();
        }
        frame.pack();
        frame.setVisible(true);
    }
}
