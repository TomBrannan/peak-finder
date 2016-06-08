
import de.erichseifert.gral.ui.InteractivePanel;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.Number;

/**
 * Launcher for the PeakFinder application
 * 
 * @author Tom
 */
public class PeakFinder {

    static File selectedFile;
    static List<PlotWrapper> openedPlots = new ArrayList<>();
    static int xcol = 1, ycol = 2, approx = 3, ignoreLines = 6,
            peaksFound = 0;
    static boolean showPeaks = true, showApprox = true, showBaseline = true,
            showTrendline = true, connectPeaks = true;
    static double baseline = 0;
    static double minGap = 400;
    static double minWidth = 90;

    static final Color GRAPH_COLOR = Color.BLUE;
    static final Color APPROX_COLOR = Color.RED;
    static final Color BASELINE_COLOR = Color.GREEN;

    static PlotWrapper selectedPlot = new PlotWrapper();
    static InteractivePanel graphPanel = new InteractivePanel(selectedPlot.getPlot());
    static final List<File> OPENED_FILES = new ArrayList<>();

    public static void main(String[] args) throws IOException, WriteException {
        //All components
        final DefaultListModel listModel = new DefaultListModel();

        final JCheckBox cbShowPeaks = new JCheckBox("Show Peaks");
        final JCheckBox cbShowApproximation = new JCheckBox("Show Approximation");
        final JCheckBox cbShowBaseline = new JCheckBox("Show Baseline");
        final JCheckBox cbShowTrendLine = new JCheckBox("Show Trend Line");
        final JCheckBox cbConnectPeaks = new JCheckBox("Connect peaks");
        final JSpinner spnIgnoreLines = new JSpinner(new SpinnerNumberModel(6, 0, 20, 1));
        final JSpinner spnMoveBaseline = new JSpinner(new SpinnerNumberModel(0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.1));
        final JSpinner spnMoveApproximation = new JSpinner(new SpinnerNumberModel(3, 0, 20, 1));
        final JSpinner spnMinimumGap = new JSpinner(new SpinnerNumberModel(400, 0, Double.POSITIVE_INFINITY, 10));
        final JSpinner spnMinimumWidth = new JSpinner(new SpinnerNumberModel(90, 0, Double.POSITIVE_INFINITY, 5));
        final JSpinner spnXColumn = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        final JSpinner spnYColumn = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));

        final JList lstFiles = new JList(listModel);
        final JScrollPane scrollPane = new JScrollPane(lstFiles);
        final JButton btnClearAll = new JButton("Remove All Files");
        final JButton btnOpenFile = new JButton("Add File(s)");
        final JButton btnUpdate = new JButton("Update");
        final JFileChooser fileChooser = new JFileChooser();
        final JPanel fileInfo = new JPanel();
        final JLabel lblIgnore1 = new JLabel("Ignore first");
        final JLabel lblIgnore2 = new JLabel("lines");
        final JLabel lblXColumn = new JLabel("X Column:");
        final JLabel lblYColumn = new JLabel("Y Column:");
        final JLabel lblApprox = new JLabel("Approximation:");
        final JLabel lblMinGap = new JLabel("Minimum Gap:");
        final JLabel lblMinWidth = new JLabel("Minimum Width:");
        final JLabel lblBaseline = new JLabel("Baseline: ");
        final JLabel lblPeaksFound = new JLabel("Peaks found: 0");
        final JButton btnExport = new JButton("Export all to Excel");
        final JPanel graphInfo = new JPanel();
        final JFrame frame = new JFrame("Peak Finder");
        frame.setLayout(null);
        frame.setSize(800, 685);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(20, 20);

        //Set locations and sizes
        scrollPane.setBounds(20, 36, 140, 150);
        btnOpenFile.setBounds(33, 9, 115, 22);
        btnUpdate.setBounds(23, 598, 135, 22);
        btnClearAll.setBounds(20, 190, 139, 22);
        btnExport.setBounds(23, 625, 135, 22);
        graphPanel.setBounds(180, 16, 623, 600);
        fileInfo.setBounds(7, 218, 165, 105);
        graphInfo.setBounds(7, 325, 165, 268);

        fileInfo.setLayout(null);
        lblIgnore1.setBounds(14, 20, 80, 22);
        spnIgnoreLines.setBounds(82, 20, 31, 22);
        lblIgnore2.setBounds(119, 20, 50, 22);
        fileInfo.add(lblIgnore1);
        fileInfo.add(spnIgnoreLines);
        fileInfo.add(lblIgnore2);
        lblXColumn.setBounds(29, 45, 70, 22);
        spnXColumn.setBounds(97, 45, 39, 22);
        lblYColumn.setBounds(29, 70, 70, 22);
        spnYColumn.setBounds(97, 70, 39, 22);
        fileInfo.add(lblXColumn);
        fileInfo.add(lblYColumn);
        fileInfo.add(spnXColumn);
        fileInfo.add(spnYColumn);

        graphInfo.setLayout(null);
        cbShowPeaks.setBounds(12, 20, 100, 22);
        cbShowApproximation.setBounds(12, 40, 146, 22);
        cbShowBaseline.setBounds(12, 60, 146, 22);
        cbShowTrendLine.setBounds(12, 80, 146, 22);
        cbConnectPeaks.setBounds(12, 100, 146, 22);
        lblBaseline.setBounds(12, 124, 80, 22);
        spnMoveBaseline.setBounds(80, 124, 70, 22);
        lblPeaksFound.setBounds(40, 235, 100, 22);
        lblApprox.setBounds(20, 150, 95, 22);
        spnMoveApproximation.setBounds(110, 150, 40, 22);
        lblMinGap.setBounds(20, 175, 100, 22);
        spnMinimumGap.setBounds(109, 175, 45, 22);
        lblMinWidth.setBounds(14, 199, 115, 22);
        spnMinimumWidth.setBounds(109, 201, 45, 22);
        graphInfo.add(cbShowPeaks);
        graphInfo.add(cbShowApproximation);
        graphInfo.add(cbShowBaseline);
        graphInfo.add(cbShowTrendLine);
        graphInfo.add(cbConnectPeaks);
        graphInfo.add(lblBaseline);
        graphInfo.add(spnMoveBaseline);
        graphInfo.add(lblPeaksFound);
        graphInfo.add(lblApprox);
        graphInfo.add(spnMoveApproximation);
        graphInfo.add(lblMinGap);
        graphInfo.add(lblMinWidth);
        graphInfo.add(spnMinimumGap);
        graphInfo.add(spnMinimumWidth);

        //Add all components
        frame.add(graphPanel);
        frame.add(btnClearAll);
        frame.add(scrollPane);
        frame.add(fileChooser);
        frame.add(btnOpenFile);
        frame.add(btnExport);
        frame.add(fileInfo);
        frame.add(graphInfo);
        frame.add(btnUpdate);

        graphPanel.setBorder(BorderFactory.createTitledBorder(""));
        fileInfo.setBorder(BorderFactory.createTitledBorder("File options"));
        graphInfo.setBorder(BorderFactory.createTitledBorder("Graph options"));
        fileChooser.setMultiSelectionEnabled(true);

        lstFiles.setBorder(BorderFactory.createEtchedBorder());
        lstFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cbShowPeaks.setSelected(true);
        cbShowApproximation.setSelected(true);
        cbShowBaseline.setSelected(true);
        cbShowTrendLine.setSelected(true);
        cbConnectPeaks.setSelected(true);

        btnClearAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.removeAllElements();
                OPENED_FILES.clear();
                selectedFile = null;
                openedPlots.clear();
                selectedPlot = null;
            }
        });

        lstFiles.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (listModel.isEmpty()) {
                        return;
                    }
                    int index = lstFiles.getSelectedIndex();
                    String fn = lstFiles.getSelectedValue().toString();
                    for (int i = 0; i < OPENED_FILES.size(); i++) {
                        if (OPENED_FILES.get(i).getName().equals(fn)) {
                            OPENED_FILES.remove(i);
                        }
                        if (openedPlots.get(i).getName().equals(fn)) {
                            openedPlots.remove(i);
                        }
                    }

                    listModel.remove(index);
                    index -= index == 0 ? 0 : 1;
                    lstFiles.setSelectedIndex(index);
                    lstFiles.revalidate();
                }
            }
        });

        lstFiles.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

                if (e.getValueIsAdjusting()) {
                    return;
                }
                String selectedFilename = (String) lstFiles.getSelectedValue();
                if (selectedFilename == null) {
                    return;
                }

                for (PlotWrapper pw : openedPlots) {
                    if (pw.getName().equals(selectedFilename)) {
                        //Set the selected plot and other controls
                        selectedFile = pw.getFile();
                        ignoreLines = pw.getIgnoreLines();
                        xcol = pw.getXCol();
                        ycol = pw.getYCol();
                        showPeaks = pw.getShowPeaks();
                        showApprox = pw.getShowApproximation();
                        showBaseline = pw.getShowBaseline();
                        showTrendline = pw.getShowTrendline();
                        connectPeaks = pw.getConnectPeaks();
                        baseline = pw.getBaselineValue();
                        approx = pw.getApprox();
                        minGap = pw.getMinGap();
                        minWidth = pw.getMinWidth();

                        spnIgnoreLines.setValue((Integer) ignoreLines);
                        spnXColumn.setValue((Integer) xcol);
                        spnYColumn.setValue((Integer) ycol);
                        cbShowPeaks.setSelected(showPeaks);
                        cbShowApproximation.setSelected(showApprox);
                        cbShowBaseline.setSelected(showBaseline);
                        cbShowTrendLine.setSelected(showTrendline);
                        cbConnectPeaks.setSelected(connectPeaks);
                        spnMoveBaseline.setValue((Double) baseline);
                        spnMoveApproximation.setValue((Integer) approx);
                        spnMinimumGap.setValue((Double) minGap);
                        spnMinimumWidth.setValue((Double) minWidth);

                        selectedPlot = new PlotWrapper(selectedFile, ignoreLines,
                                xcol, ycol, showPeaks, showApprox, showBaseline, showTrendline,
                                connectPeaks, baseline, approx, minGap, minWidth, GRAPH_COLOR,
                                APPROX_COLOR, BASELINE_COLOR);
                        openedPlots.remove(pw);
                        openedPlots.add(selectedPlot);
                        frame.remove(graphPanel);
                        graphPanel = new InteractivePanel(selectedPlot.getPlot());
                        graphPanel.setBounds(180, 16, 623, 600);
                        frame.add(graphPanel);
                        frame.repaint();
                        break;
                    }
                }

                frame.remove(graphPanel);
                graphPanel = new InteractivePanel(selectedPlot.getPlot());
                graphPanel.setBounds(180, 16, 623, 600);
                lblPeaksFound.setText("Peaks found: " + selectedPlot.getPeakPoints().size());
                frame.add(graphPanel);
                frame.repaint();
            }
        });

        btnOpenFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileChooser.setDialogTitle("Open");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setAcceptAllFileFilterUsed(true);
                fileChooser.showOpenDialog(frame);
                File[] selectedFiles = fileChooser.getSelectedFiles();
                String title = frame.getTitle();
                frame.setTitle("Please wait...");
                //Loop through all files that were selected
                for (File curr : selectedFiles) {
                    String filename = curr.getName();
                    if (OPENED_FILES.contains(curr)) {
                        continue;
                    }
                    OPENED_FILES.add(curr);
                    listModel.addElement(curr.getName());

                    boolean alreadyOpened = false;
                    for (int j = 0; j < openedPlots.size(); j++) {
                        if (openedPlots.get(j).getName().equals(filename)) {
                            alreadyOpened = true;
                            break;
                        }
                    }
                    if (!alreadyOpened) {
                        PlotWrapper pw = new PlotWrapper(curr, ignoreLines,
                                xcol, ycol, showPeaks, showApprox, showBaseline, showTrendline,
                                connectPeaks, baseline, approx, minGap, minWidth, GRAPH_COLOR,
                                APPROX_COLOR, BASELINE_COLOR);
                        openedPlots.add(pw);
                    }
                }
                frame.setTitle(title);
            }
        });

        btnUpdate.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                openedPlots.remove(selectedPlot);
                selectedPlot = new PlotWrapper(selectedFile, ignoreLines,
                        xcol, ycol, showPeaks, showApprox, showBaseline, showTrendline,
                        connectPeaks, baseline, approx, minGap, minWidth, GRAPH_COLOR,
                        APPROX_COLOR, BASELINE_COLOR);
                openedPlots.add(selectedPlot);

                frame.remove(graphPanel);
                graphPanel = new InteractivePanel(selectedPlot.getPlot());
                graphPanel.setBounds(180, 16, 623, 600);
                frame.add(graphPanel);
                frame.repaint();
                lblPeaksFound.setText("Peaks found: " + selectedPlot.getPeakPoints().size());
            }
        });

        spnIgnoreLines.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                ignoreLines = (Integer) spnIgnoreLines.getValue();
            }
        });
        spnXColumn.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                xcol = (Integer) spnXColumn.getValue();
            }
        });
        spnYColumn.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                ycol = (Integer) spnYColumn.getValue();
            }
        });
        spnMoveBaseline.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                baseline = (Double) spnMoveBaseline.getValue();
            }
        });
        spnMoveApproximation.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                approx = (Integer) spnMoveApproximation.getValue();
            }
        });
        spnMinimumGap.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                minGap = (Double) spnMinimumGap.getValue();
            }
        });
        spnMinimumWidth.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                minWidth = (Double) spnMinimumWidth.getValue();
            }
        });
        cbShowPeaks.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                showPeaks = cbShowPeaks.isSelected();
            }
        });
        cbShowApproximation.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                showApprox = cbShowApproximation.isSelected();
            }
        });
        cbShowBaseline.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                showBaseline = cbShowBaseline.isSelected();
            }
        });
        cbShowTrendLine.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                showTrendline = cbShowTrendLine.isSelected();
            }
        });
        cbConnectPeaks.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                connectPeaks = cbConnectPeaks.isSelected();
            }
        });

        btnExport.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String title = frame.getTitle();
                frame.setTitle("Please wait...");
                fileChooser.setCurrentDirectory(new File("."));
                fileChooser.setDialogTitle("Select a directory");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setAcceptAllFileFilterUsed(false);
                String path;
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    path = fileChooser.getSelectedFile().getAbsolutePath();
                } else {
                    return;
                }
                //Create and save an Excel spreadsheet (.xls) for each plot
                //Simply lists peaks one by one in the first column
                for (PlotWrapper pw : openedPlots) {
                    try {
                        WritableWorkbook book = Workbook.createWorkbook(
                                new File(path + "/" + pw.getSimpleName() + ".xls"));
                        WritableSheet sheet = book.createSheet("sheet 1", 0);
                        int i = 0;
                        for (Point2D.Double d : pw.getPeakPoints()) {
                            Number n = new Number(0, i, d.y);
                            sheet.addCell(n);
                            i++;
                        }
                        book.write();
                        book.close();
                    } catch (IOException | WriteException ex) {
                        Logger.getLogger(PeakFinder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                frame.setTitle(title);
            }
        });
        frame.show();
    }

}
