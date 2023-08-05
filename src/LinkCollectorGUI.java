import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.formdev.flatlaf.FlatDarkLaf; // Import FlatLaf

public class LinkCollectorGUI {
    public static void main(String[] args) {
        try {
            // Set the FlatLaf look and feel
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            // Your existing GUI code
            JFrame frame = new JFrame("Link Collector");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ImageIcon icon = new ImageIcon("src/icon.png"); // Replace "icon.png" with the path to your icon image

            // Set the icon for the JFrame
            frame.setIconImage(icon.getImage());

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

            JPanel urlPanel = new JPanel();
            urlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5)); // Center the components with spacing
            JLabel urlLabel = new JLabel("Enter URL:");
            JTextField urlField = new JTextField(30);
            urlPanel.add(urlLabel);
            urlPanel.add(urlField);

            JButton searchButton = new JButton("Search");
            searchButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the search button
            JTextArea resultArea = new JTextArea(20, 40); // Adjust width of the result area
            resultArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(resultArea);

            // Add some spacing between components
            mainPanel.add(Box.createVerticalStrut(20));
            mainPanel.add(urlPanel);
            mainPanel.add(Box.createVerticalStrut(20));
            mainPanel.add(searchButton);
            mainPanel.add(Box.createVerticalStrut(20));
            mainPanel.add(scrollPane);
            mainPanel.add(Box.createVerticalStrut(10)); // Add a bit of space below the scroll pane

            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String url = urlField.getText();

                    // Disable the search button during the search
                    searchButton.setEnabled(false);

                    JProgressBar progressBar = new JProgressBar();
                    progressBar.setIndeterminate(true); // Set the progress bar to indeterminate mode
                    mainPanel.add(progressBar);
                    mainPanel.revalidate();
                    mainPanel.repaint();

                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            try {
                                Map<String, String> matchedLinks = new HashMap<>();
                                Set<String> seenLinks = new HashSet<>();
                                boolean continueSearching = true;
                                int pageNum = 1;

                                while (continueSearching) {
                                    int lastIndex = url.lastIndexOf("=");
                                    String pageUrl = url.substring(0, lastIndex) + "=" + pageNum;
                                    Document document = Jsoup.connect(pageUrl).get();

                                    Elements links = document.select("a[href*=profile.php?id=]");

                                    if (!links.isEmpty()) {
                                        for (Element link : links) {
                                            String linkUrl = link.attr("abs:href"); // Get the absolute URL of the link
                                            String linkName = link.text(); // Get the text content of the link
                                            if (seenLinks.contains(linkUrl)) {
                                                continueSearching = false; // Stop searching if link is a duplicate
                                                break;
                                            } else {
                                                matchedLinks.put(linkName, linkUrl);
                                                seenLinks.add(linkUrl);
                                            }
                                        }
                                    } else {
                                        continueSearching = false; // Stop searching if no links with "member.php?u=" are found on the current page
                                    }

                                    if (continueSearching) {
                                        pageNum++;
                                    }
                                }

                                StringBuilder resultBuilder = new StringBuilder();
                                for (Map.Entry<String, String> entry : matchedLinks.entrySet()) {
                                    resultBuilder.append(entry.getKey()).append("\n");
                                }

                                resultArea.setText(resultBuilder.toString());
                                System.out.println("Links matching the criteria found.");

                            } catch (IOException ex) {
                                ex.printStackTrace();
                                resultArea.setText("Error: " + ex.getMessage());
                            }

                            return null;
                        }

                        @Override
                        protected void done() {
                            // Enable the search button when the search is done
                            searchButton.setEnabled(true);

                            // Remove the progress bar after the search is done
                            mainPanel.remove(progressBar);
                            mainPanel.revalidate();
                            mainPanel.repaint();
                        }
                    };

                    // Start the SwingWorker to perform the search in the background
                    worker.execute();
                }
            });

            // Add the main panel to the frame
            frame.add(mainPanel);

            frame.pack();
            frame.setVisible(true);
        });
    }
}
