package gov.nasa.jpl.mbee.web.sync;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class CommentsViewWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JPanel      comments;
    private int               commentCount     = 0;

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
