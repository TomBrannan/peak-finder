
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * PlotWrapper: Encapsulates data for an opened plot's graph and settings
 * @author Tom
 */
public class PlotWrapper {

    private XYPlot plot = new XYPlot();
    private String filename;
    private File file;
    private int ignoreLines, xcol, ycol, approx;
    private boolean showPeaks, showApproximation, showBaseline, showTrendline, connectPeaks;
    private DataTable data, peaks, trendline, baselineGraph, approximation;
    private double baseline;
    private List<Point2D.Double> dataPoints, approxPoints, peakPoints;
    private double minGap, minWidth;
    private Color graphColor, approxColor, baselineColor;

    /**
     * Constructor: initializes all data for the PlotWrapper and generates 
     * the metadata (graph, best fit, etc.) for this file
     */
    public PlotWrapper(File file, int ignoreLines, int xcol, int ycol,
            boolean showPeaks, boolean showApproximation, boolean showBaseline,
            boolean showTrendline, boolean connectPeaks, double baseline,
            int approx, double minGap,
            double minWidth, Color graphColor, Color approxColor, Color baselineColor) {
        this.file = file;
        this.filename = file.getName();
        this.dataPoints = new ArrayList<>();
        this.approxPoints = new ArrayList<>();
        this.trendline = new DataTable();
        this.baselineGraph = new DataTable();
        this.ignoreLines = ignoreLines;
        this.xcol = xcol;
        this.ycol = ycol;
        this.connectPeaks = connectPeaks;
        this.showPeaks = showPeaks;
        this.showApproximation = showApproximation;
        this.showBaseline = showBaseline;
        this.showTrendline = showTrendline;
        this.baseline = baseline;
        this.approx = approx;
        this.minGap = minGap;
        this.minWidth = minWidth;
        this.graphColor = graphColor;
        this.approxColor = approxColor;
        this.baselineColor = baselineColor;
        generateData();
    }

    /**
     * Simple constructor for testing
     * @param filename the filename of the file to load
     */
    public PlotWrapper(String filename) {
        this.filename = filename;
        dataPoints = new ArrayList<>();
        approxPoints = new ArrayList<>();
        trendline = new DataTable();
        baselineGraph = new DataTable();
        ignoreLines = 6;
        xcol = 1;
        ycol = 2;
        connectPeaks = true;
        showPeaks = true;
        showApproximation = true;
        showBaseline = true;
        showTrendline = true;
        baseline = 1;
        approx = 4;
        minGap = 450;
        minWidth = 100;
        graphColor = Color.BLUE;
        approxColor = Color.RED;
        baselineColor = Color.GREEN;
        generateData();
    }

    /**
     * Empty constructor, may refactor to exclude this
     */
    public PlotWrapper() {

    }

    /**
     * Helper function to generate the metadata for a file.  
     * Parses the file, finds the approximation graph, then generates
     * the DataTables for peaks, baseline, and trendline.
     */
    private void generateData() {
        parseFile();
        getApproximation();
        refresh();
    }

    /**
     * Recalculates the peaks, baseline, and trendline.
     */
    public void refresh() {
        getPeaks();
        getBaseline();
        getTrendline();
        initPlot();
    }

    /**
     * Populates the data DataTable from a file
     */
    private void parseFile() {
        data = new DataTable();
        try {
            if (file == null) {
                file = new File(filename);
            }
            Scanner scan = new Scanner(file);
            for (int i = 0; i < ignoreLines; i++) {
                scan.nextLine();
            }
            while (scan.hasNextLine()) {
                double xVal, yVal;
                if (xcol < ycol) {
                    for (int i = 0; i < xcol - 1; i++) {
                        scan.nextDouble();
                    }
                    xVal = scan.nextDouble();
                    for (int i = 0; i < ycol - xcol - 1; i++) {
                        scan.nextDouble();
                    }
                    yVal = scan.nextDouble();
                } else {
                    for (int i = 0; i < ycol - 1; i++) {
                        scan.nextDouble();
                    }
                    yVal = scan.nextDouble();
                    for (int i = 0; i < xcol - ycol - 1; i++) {
                        scan.nextDouble();
                    }
                    xVal = scan.nextDouble();
                }
                scan.nextLine();
                Point2D.Double point = new Point2D.Double(xVal, yVal);
                dataPoints.add(point);
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File Not Found: " + filename);
        }
        data = populateDataTable(dataPoints);

    }

    /**
     * Gets the approximation graph from the data
     */
    private void getApproximation() {
        approximation = new DataTable();
        approxPoints = localSmooth(dataPoints, approx);
        approximation = populateDataTable(approxPoints);
    }

    /**
     * Gets the peaks (points) from the data
     */
    private void getPeaks() {
        peaks = new DataTable();
        peakPoints = findPeaksGapDouble();
        peaks = populateDataTable(peakPoints);
    }

    ///Getters and Setters
    public List<Point2D.Double> getPeakPoints() {
        return peakPoints;
    }

    public double getMinWidth() {
        return minWidth;
    }

    public double getMinGap() {
        return minGap;
    }

    public double getBaselineValue() {
        return baseline;
    }

    public boolean getConnectPeaks() {
        return connectPeaks;
    }

    public boolean getShowTrendline() {
        return showTrendline;
    }

    public boolean getShowBaseline() {
        return showBaseline;
    }

    public boolean getShowApproximation() {
        return showApproximation;
    }

    public boolean getShowPeaks() {
        return showPeaks;
    }

    public int getApprox() {
        return approx;
    }

    public int getYCol() {
        return ycol;
    }

    public int getXCol() {
        return xcol;
    }

    public int getIgnoreLines() {
        return ignoreLines;
    }

    public File getFile() {
        return file;
    }
    
    public XYPlot getPlot() {
        return plot;
    }

    public String getName() {
        return filename;
    }

    public String getSimpleName() {
        return filename.substring(0, filename.length() - 4);
    }

    private void getBaseline() {
        double leftVal = dataPoints.get(0).x;
        double rightVal = dataPoints.get(dataPoints.size() - 1).x;
        List<Point2D.Double> temp = new ArrayList<>();
        temp.add(new Point2D.Double(leftVal, baseline));
        temp.add(new Point2D.Double(rightVal, baseline));
        baselineGraph = populateDataTable(temp);
    }

    public void setDrawingProperties(boolean showPeaks, boolean showApproximation,
            boolean showBaseline, boolean showTrendline, boolean connectPeaks,
            Color graphColor, Color approxColor, Color baselineColor) {
        this.showPeaks = showPeaks;
        this.showApproximation = showApproximation;
        this.showBaseline = showBaseline;
        this.showTrendline = showTrendline;
        this.connectPeaks = connectPeaks;
        this.graphColor = graphColor;
        this.approxColor = approxColor;
        this.baselineColor = baselineColor;
        initPlot();
    }

    public void setFileProcessingInfo(int ig, int xc, int yc) {
        ignoreLines = ig;
        xcol = xc;
        ycol = yc;
    }

    public void setApprox(int approx) {
        this.approx = approx;
    }

    public void setBaseline(double baseline) {
        this.baseline = baseline;
    }

    public void setMinGapWidth(double gap, double width) {
        minGap = gap;
        minWidth = width;
    }

    /**
     * Gets the trendline - a linear best-fit line through the peak points
     */
    private void getTrendline() {
        double xsum = 0, ysum = 0, xysum = 0, x2sum = 0;
        for (Point2D.Double d : peakPoints) {
            xsum += d.x;
            ysum += d.y;
            xysum += d.x * d.y;
            x2sum += d.x * d.x;
        }
        double n = peakPoints.size();
        double numerator = xysum - (xsum * ysum) / n;
        double denominator = x2sum - xsum * xsum / n;
        double slope = numerator / denominator;
        double xbar = xsum / n;
        double ybar = ysum / n;
        double yintercept = ybar - slope * xbar;
        List<Point2D.Double> bestfit = new ArrayList<>();
        for (Point2D.Double d : peakPoints) {
            bestfit.add(new Point2D.Double(d.x, slope * d.x + yintercept));
        }
        trendline = populateDataTable(bestfit);
    }

    /**
     * Initialize the plot (after all DataTables have been calculated)
     */
    private void initPlot() {
        plot = new XYPlot();
        plot.add(data); //always have data
        DefaultLineRenderer2D lineRendererGraph = new DefaultLineRenderer2D();
        DefaultLineRenderer2D lineRendererPeaks = new DefaultLineRenderer2D();
        DefaultLineRenderer2D lineRendererApprox = new DefaultLineRenderer2D();
        DefaultLineRenderer2D lineRendererBaseline = new DefaultLineRenderer2D();
        DefaultLineRenderer2D lineRendererTrendline = new DefaultLineRenderer2D();
        lineRendererGraph.setColor(graphColor);
        lineRendererPeaks.setColor(approxColor);
        lineRendererApprox.setColor(approxColor);
        lineRendererBaseline.setColor(baselineColor);
        lineRendererTrendline.setColor(baselineColor);
        plot.setLineRenderer(data, lineRendererGraph);
        plot.setPointRenderer(data, null);
        plot.getLineRenderer(data).setColor(graphColor);
        if (showPeaks) {
            plot.add(peaks);
            plot.getPointRenderer(peaks).setColor(approxColor);
            if (connectPeaks) {
                plot.setLineRenderer(peaks, lineRendererPeaks);
            }
        }
        if (showApproximation) {
            plot.add(approximation);
            plot.setLineRenderer(approximation, lineRendererApprox);
            plot.setPointRenderer(approximation, null);
        }
        if (showBaseline) {
            plot.add(baselineGraph);
            plot.setLineRenderer(baselineGraph, lineRendererBaseline);
            plot.setPointRenderer(baselineGraph, null);
        }
        if (showTrendline) {
            plot.add(trendline);
            plot.setLineRenderer(trendline, lineRendererTrendline);
            plot.setPointRenderer(trendline, null);
        }
    }

    /**
     * Does two passes over the data: one left-to-right, another right-to-left.
     * Each pass attempts to find all peaks within the data set.
     * A single pass will usually work, but peaks close to the beginning or
     * end of the data are sometimes missed (which is why another pass in the
     * opposite direction is used).  
     * @return A list of the peaks (highest global relative y-values) in the data
     */
    private List<Point2D.Double> findPeaksGapDouble() {
        List<Point2D.Double> peaksLR = new ArrayList<>();
        List<Point2D.Double> peaksRL = new ArrayList<>();
        Point2D.Double prev = approxPoints.get(0);
        boolean first = true;
        boolean foundWideAscension = false;

        for (int i = 0; i < approxPoints.size() - 1; i++) {
            Point2D.Double firstPoint = approxPoints.get(i);
            Point2D.Double secondPoint = approxPoints.get(i + 1);
            if (ascendingOver(firstPoint, secondPoint, baseline) || descendingOver(firstPoint, secondPoint, baseline)) {
                if (ascendingOver(firstPoint, secondPoint, baseline)) {
                    if (first) {
                        first = false;
                        prev = firstPoint;
                    } else if (firstPoint.x - prev.x >= minGap) {
                        prev = firstPoint;
                        foundWideAscension = true;
                    }
                }
                if (descendingOver(firstPoint, secondPoint, baseline)) {
                    if (first) {
                        first = false;
                        if (secondPoint.x - prev.x >= minGap) {
                            addUnique(peaksLR, findMaxBetween(approxPoints, prev, secondPoint));
                            prev = secondPoint;
                        }
                    } else if (secondPoint.x - prev.x >= minGap
                            || foundWideAscension && (secondPoint.x - prev.x >= minWidth)) {
                        foundWideAscension = false;
                        addUnique(peaksLR, findMaxBetween(approxPoints, prev, secondPoint));
                        prev = secondPoint;
                    }
                }
            }
        }
        prev = approxPoints.get(approxPoints.size() - 1);
        first = true;
        foundWideAscension = false;
        for (int i = approxPoints.size() - 1; i > 1; i--) {
            Point2D.Double firstPoint = approxPoints.get(i);
            Point2D.Double secondPoint = approxPoints.get(i - 1);
            if (ascendingOver(firstPoint, secondPoint, baseline) || descendingOver(firstPoint, secondPoint, baseline)) {
                if (ascendingOver(firstPoint, secondPoint, baseline)) {
                    if (first) {
                        first = false;
                        prev = firstPoint;
                    } else if (prev.x - firstPoint.x >= minGap) {
                        prev = firstPoint;
                        foundWideAscension = true;
                    }
                }
                if (descendingOver(firstPoint, secondPoint, baseline)) {
                    if (first) {
                        first = false;
                        if (prev.x - secondPoint.x >= minGap) {
                            addUnique(peaksRL, findMaxBetween(approxPoints, secondPoint, prev));
                            prev = secondPoint;
                        }
                    } else if (prev.x - secondPoint.x >= minGap
                            || foundWideAscension && (prev.x - secondPoint.x >= minWidth)) {
                        foundWideAscension = false;
                        addUnique(peaksRL, findMaxBetween(approxPoints, secondPoint, prev));
                        prev = secondPoint;
                    }
                }
            }
        }
        List<Point2D.Double> peaksFinal = new ArrayList<>();
        List<Point2D.Double> bigger, smaller;
        if (!peaksLR.isEmpty()) {
            addUnique(peaksFinal, peaksLR.get(0));
        }
        if (peaksLR.size() > peaksRL.size()) {
            bigger = peaksLR;
            smaller = peaksRL;
        } else {
            bigger = peaksRL;
            smaller = peaksLR;
        }
        for (Point2D.Double pt : bigger) {
            if (smaller.contains(pt)) {
                addUnique(peaksFinal, pt);
            }
        }
        CoordinateComparator coordCompare = new CoordinateComparator();
        Collections.sort(peaksFinal, coordCompare);

        return peaksFinal;
    }

    /**
     * Finds the maximum point along a curve within a certain region.
     * @param smoothedData A list of points to search
     * @param left The left boundary (leftmost point) of the region
     * @param right The right boundary (rightmost point) of the region
     * @return The point with the highest y-value within the region
     */
    public static Point2D.Double findMaxBetween(List<Point2D.Double> smoothedData, Point2D.Double left, Point2D.Double right) {
        Point2D.Double max = left;
        for (int i = smoothedData.indexOf(left); i <= smoothedData.indexOf(right); i++) {
            if (smoothedData.get(i).y > max.y) {
                max = smoothedData.get(i);
            }
        }
        return max;
    }

    /**
     * Adds a point to a list of points if the list doesn't already contain it.
     * Should really be changed to use a Set instead of a List.
     * @param data The list of points
     * @param point The point to be added
     */
    public static void addUnique(List<Point2D.Double> data, Point2D.Double point) {
        for (Point2D.Double p : data) {
            if (p.x == point.x && p.y == point.y) {
                return;
            }
        }
        data.add(point);
    }
    
    /**
     * Checks if a region ascends over a certain y-value.
     * @param left The left point of the region
     * @param right The right point of the region
     * @param y The y-value to check
     * @return true if the region ascends over the y-value.
     */
    public static boolean ascendingOver(Point2D.Double left, Point2D.Double right, double y) {
        //Left must be strictly less than right
        if (left.y >= right.y) {
            return false;
        }

        //Then it must cross the line.  Both y is impossible because
        //Of the first statement, but one may be y.
        return left.y <= y && right.y >= y;
    }

    /**
     * Checks if a region descends over a certain y-value.
     * @param left The left point of the region
     * @param right The right point of the region
     * @param y The y-value to check
     * @return true if the region descends over the y-value.
     */
    public static boolean descendingOver(Point2D.Double left, Point2D.Double right, double y) {
        //Left must be strictly larger than right
        if (left.y <= right.y) {
            return false;
        }

        //Then it must cross the line.  Both y is impossible because
        //Of the first statement, but one may be y.
        return left.y >= y && right.y <= y;
    }

    /**
     * Creates a smoothed list of points from a source list and a difference amount.
     * The difference amount determines how large of a chunk is examined in each
     * iteration.  For each iteration, an averaged point is added to the list.
     * @param data The source data
     * @param diff The chunk size, amount to be averaged per point
     * @return The smoothed data
     */
    public static List<Point2D.Double> localSmooth(List<Point2D.Double> data, int diff) {
        List<Point2D.Double> smoothed = new ArrayList<>();
        for (int i = diff; i < data.size() - diff; i++) {
            double value = 0;
            for (int j = i - diff; j <= i + diff; j++) {
                value += data.get(j).y;
            }
            value /= (2 * diff + 1);
            Point2D.Double newPoint = new Point2D.Double(data.get(i).x, value);
            smoothed.add(newPoint);
        }
        return smoothed;
    }

    /**
     * Generates a DataTable given a list of points
     * @param values The list of points
     * @return A DataTable populated with the given values
     */
    private DataTable populateDataTable(List<Point2D.Double> values) {
        DataTable dt = new DataTable(Double.class, Double.class);
        for (Point2D.Double d : values) {
            dt.add(d.x, d.y);
        }
        return dt;
    }

}
