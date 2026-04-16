import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class SortingVisualizer extends JFrame {

    // ── Constants ──────────────────────────────────────────────
    private static final int BAR_AREA_HEIGHT = 300;
    private static final Color COL_UNSORTED   = new Color(55,  138, 221);  // blue
    private static final Color COL_COMPARING  = new Color(239, 159,  39);  // orange
    private static final Color COL_SWAPPING   = new Color(226,  75,  74);  // red
    private static final Color COL_SORTED     = new Color( 29, 158, 117);  // green
    private static final Color COL_PIVOT      = new Color(212,  83, 126);  // pink
    private static final Color BG             = new Color(250, 250, 248);
    private static final Color PANEL_BG       = new Color(244, 243, 240);

    // ── State ──────────────────────────────────────────────────
    private int[]        array;
    private List<Step>   steps      = new ArrayList<>();
    private int          stepIndex  = 0;
    private Timer        animTimer;
    private String       currentAlgo = "Bubble Sort";
    private int          comparisons = 0;
    private int          swaps       = 0;

    // ── UI Components ──────────────────────────────────────────
    private BarPanel     barPanel;
    private JLabel       lblComparisons, lblSwaps, lblStatus, lblStep;
    private JButton      btnSort, btnReset;
    private JSlider      speedSlider, sizeSlider;
    private JTable       complexityTable;

    // ══════════════════════════════════════════════════════════
    //  Data class representing one animation frame
    // ══════════════════════════════════════════════════════════
    static class Step {
        int[]    arr;
        int[]    comparing;
        int[]    swapping;
        int[]    sorted;
        int[]    pivot;
        String   message;

        Step(int[] arr, int[] comparing, int[] swapping,
             int[] sorted, int[] pivot, String message) {
            this.arr       = arr.clone();
            this.comparing = comparing;
            this.swapping  = swapping;
            this.sorted    = sorted;
            this.pivot     = pivot;
            this.message   = message;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Custom panel that draws the bars
    // ══════════════════════════════════════════════════════════
    class BarPanel extends JPanel {
        private int[]   paintArr      = new int[0];
        private int[]   paintComparing = {};
        private int[]   paintSwapping  = {};
        private int[]   paintSorted    = {};
        private int[]   paintPivot     = {};

        BarPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(200, 200, 195), 1));
            setPreferredSize(new Dimension(800, BAR_AREA_HEIGHT));
        }

        void update(Step s) {
            paintArr       = s.arr;
            paintComparing = s.comparing;
            paintSwapping  = s.swapping;
            paintSorted    = s.sorted;
            paintPivot     = s.pivot;
            repaint();
        }

        void setArray(int[] a) {
            paintArr       = a.clone();
            paintComparing = new int[]{};
            paintSwapping  = new int[]{};
            paintSorted    = new int[]{};
            paintPivot     = new int[]{};
            repaint();
        }

        private boolean contains(int[] arr, int v) {
            for (int x : arr) if (x == v) return true;
            return false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (paintArr == null || paintArr.length == 0) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int n      = paintArr.length;
            int w      = getWidth();
            int h      = getHeight();
            int max    = 1;
            for (int v : paintArr) if (v > max) max = v;

            int gap     = Math.max(1, (int)(w * 0.006));
            int barW    = Math.max(4, (w - gap * (n + 1)) / n);
            int startX  = (w - (barW + gap) * n + gap) / 2;

            for (int i = 0; i < n; i++) {
                int barH = (int)((double) paintArr[i] / max * (h - 20));
                int x    = startX + i * (barW + gap);
                int y    = h - barH - 4;

                Color c = COL_UNSORTED;
                if      (contains(paintPivot,     i)) c = COL_PIVOT;
                else if (contains(paintSwapping,  i)) c = COL_SWAPPING;
                else if (contains(paintComparing, i)) c = COL_COMPARING;
                else if (contains(paintSorted,    i)) c = COL_SORTED;

                g2.setColor(c);
                g2.fillRoundRect(x, y, barW, barH, 4, 4);
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Constructor – build the UI
    // ══════════════════════════════════════════════════════════
    public SortingVisualizer() {
        setTitle("Sorting Algorithm Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BG);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(860, 680));

        generateArray();
    }

    // ── Top: algorithm buttons ─────────────────────────────────
    private JPanel buildTopPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
        p.setBackground(BG);
        p.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 218, 212)));

        String[] algos = {"Bubble Sort","Selection Sort","Insertion Sort",
                          "Merge Sort","Quick Sort"};
        ButtonGroup bg = new ButtonGroup();
        for (String name : algos) {
            JToggleButton btn = new JToggleButton(name);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (name.equals("Bubble Sort")) btn.setSelected(true);
            btn.addActionListener(e -> {
                currentAlgo = name;
                stopAnimation();
                updateComplexityHighlight();
                generateArray();
            });
            bg.add(btn);
            p.add(btn);
        }
        return p;
    }

    // ── Center: bars + description ─────────────────────────────
    private JPanel buildCenterPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(12, 16, 8, 16));

        // description label
        lblStep = new JLabel(getDescription("Bubble Sort"));
        lblStep.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblStep.setForeground(new Color(100, 98, 94));
        lblStep.setBorder(new CompoundBorder(
            new MatteBorder(1,1,1,1, new Color(220,218,212)),
            new EmptyBorder(8,12,8,12)));
        lblStep.setBackground(PANEL_BG);
        lblStep.setOpaque(true);
        p.add(lblStep, BorderLayout.NORTH);

        barPanel = new BarPanel();
        p.add(barPanel, BorderLayout.CENTER);

        p.add(buildLegend(), BorderLayout.SOUTH);
        return p;
    }

    // ── Legend ─────────────────────────────────────────────────
    private JPanel buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        p.setBackground(BG);
        String[][] items = {
            {"Unsorted",   "#378ADD"},
            {"Comparing",  "#EF9F27"},
            {"Swapping",   "#E24B4A"},
            {"Pivot",      "#D4537E"},
            {"Sorted",     "#1D9E75"}
        };
        for (String[] item : items) {
            JPanel dot = new JPanel();
            dot.setPreferredSize(new Dimension(10, 10));
            dot.setBackground(Color.decode(item[1]));
            JLabel lbl = new JLabel(item[0]);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lbl.setForeground(new Color(120, 118, 110));
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            row.setBackground(BG);
            row.add(dot); row.add(lbl);
            p.add(row);
        }
        return p;
    }

    // ── Bottom: controls + stats + complexity table ────────────
    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(BG);
        p.setBorder(new MatteBorder(1,0,0,0, new Color(220,218,212)));

        // controls row
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controls.setBackground(BG);

        btnSort = new JButton("Sort");
        btnSort.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnSort.setBackground(new Color(24, 95, 165));
        btnSort.setForeground(Color.WHITE);
        btnSort.setFocusPainted(false);
        btnSort.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSort.addActionListener(e -> toggleSort());

        btnReset = new JButton("New array");
        btnReset.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> { stopAnimation(); generateArray(); });

        speedSlider = new JSlider(1, 5, 3);
        speedSlider.setBackground(BG);
        sizeSlider  = new JSlider(8, 30, 16);
        sizeSlider.setBackground(BG);
        sizeSlider.addChangeListener(e -> { if (!sizeSlider.getValueIsAdjusting()) { stopAnimation(); generateArray(); }});

        JLabel lblSpd = new JLabel("Speed");
        lblSpd.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSpd.setForeground(new Color(120,118,110));
        JLabel lblSz = new JLabel("Size");
        lblSz.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSz.setForeground(new Color(120,118,110));

        lblComparisons = new JLabel("Comparisons: 0");
        lblComparisons.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblSwaps       = new JLabel("Swaps: 0");
        lblSwaps.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblStatus      = new JLabel("");
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setForeground(new Color(29,158,117));

        controls.add(btnSort); controls.add(btnReset);
        controls.add(Box.createHorizontalStrut(10));
        controls.add(lblSpd); controls.add(speedSlider);
        controls.add(Box.createHorizontalStrut(6));
        controls.add(lblSz);  controls.add(sizeSlider);
        controls.add(Box.createHorizontalStrut(16));
        controls.add(lblComparisons); controls.add(lblSwaps);
        controls.add(lblStatus);

        p.add(controls, BorderLayout.NORTH);
        p.add(buildComplexityTable(), BorderLayout.CENTER);
        return p;
    }

    // ── Complexity table ───────────────────────────────────────
    private JScrollPane buildComplexityTable() {
        String[] cols = {"Algorithm","Best","Average","Worst","Space","Stable"};
        Object[][] data = {
            {"Bubble Sort",    "O(n)",        "O(n²)",       "O(n²)",       "O(1)",      "Yes"},
            {"Selection Sort", "O(n²)",       "O(n²)",       "O(n²)",       "O(1)",      "No"},
            {"Insertion Sort", "O(n)",        "O(n²)",       "O(n²)",       "O(1)",      "Yes"},
            {"Merge Sort",     "O(n log n)",  "O(n log n)",  "O(n log n)",  "O(n)",      "Yes"},
            {"Quick Sort",     "O(n log n)",  "O(n log n)",  "O(n²)",       "O(log n)",  "No"},
        };
        complexityTable = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                String rowAlgo = (String) getModel().getValueAt(row, 0);
                if (rowAlgo.equals(currentAlgo)) {
                    c.setBackground(new Color(230, 242, 255));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248,248,245));
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        };
        complexityTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        complexityTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        complexityTable.setRowHeight(24);
        complexityTable.setShowGrid(false);
        complexityTable.setIntercellSpacing(new Dimension(0, 0));
        complexityTable.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(complexityTable);
        sp.setBorder(new MatteBorder(1,0,0,0, new Color(220,218,212)));
        sp.setPreferredSize(new Dimension(800, 145));
        return sp;
    }

    // ══════════════════════════════════════════════════════════
    //  Array generation
    // ══════════════════════════════════════════════════════════
    private void generateArray() {
        int n = sizeSlider.getValue();
        array = new int[n];
        Random rnd = new Random();
        for (int i = 0; i < n; i++) array[i] = rnd.nextInt(90) + 10;
        comparisons = 0; swaps = 0;
        updateStats();
        lblStatus.setText("");
        if (lblStep != null) lblStep.setText(getDescription(currentAlgo));
        barPanel.setArray(array);
    }

    // ══════════════════════════════════════════════════════════
    //  Animation control
    // ══════════════════════════════════════════════════════════
    private void toggleSort() {
        if (animTimer != null && animTimer.isRunning()) {
            animTimer.stop();
            btnSort.setText("Resume");
            return;
        }
        if (stepIndex == 0 || stepIndex >= steps.size()) {
            steps = generateSteps(array.clone());
            stepIndex = 0;
            comparisons = 0; swaps = 0;
        }
        btnSort.setText("Pause");
        int delay = getDelay();
        animTimer = new Timer(delay, e -> {
            if (stepIndex >= steps.size()) {
                ((Timer)e.getSource()).stop();
                btnSort.setText("Sort");
                lblStatus.setText("Done!");
                return;
            }
            Step s = steps.get(stepIndex++);
            if (s.comparing.length > 0) comparisons++;
            if (s.swapping.length  > 0) swaps++;
            updateStats();
            lblStep.setText(s.message);
            barPanel.update(s);
        });
        animTimer.setDelay(delay);
        animTimer.start();
    }

    private void stopAnimation() {
        if (animTimer != null) animTimer.stop();
        stepIndex = 0; steps.clear();
        btnSort.setText("Sort");
        lblStatus.setText("");
        comparisons = 0; swaps = 0;
        updateStats();
    }

    private int getDelay() {
        int[] map = {500, 250, 120, 50, 15};
        return map[speedSlider.getValue() - 1];
    }

    private void updateStats() {
        lblComparisons.setText("Comparisons: " + comparisons);
        lblSwaps.setText("   Swaps: " + swaps);
    }

    private void updateComplexityHighlight() {
        if (complexityTable != null) complexityTable.repaint();
        if (lblStep != null) lblStep.setText(getDescription(currentAlgo));
    }

    // ══════════════════════════════════════════════════════════
    //  Step generators – one per algorithm
    // ══════════════════════════════════════════════════════════
    private List<Step> generateSteps(int[] a) {
        switch (currentAlgo) {
            case "Bubble Sort":    return bubbleSteps(a);
            case "Selection Sort": return selectionSteps(a);
            case "Insertion Sort": return insertionSteps(a);
            case "Merge Sort":     return mergeSteps(a);
            case "Quick Sort":     return quickSteps(a);
            default:               return new ArrayList<>();
        }
    }

    // ── Bubble Sort ────────────────────────────────────────────
    private List<Step> bubbleSteps(int[] a) {
        List<Step> s = new ArrayList<>();
        int n = a.length;
        Set<Integer> sorted = new LinkedHashSet<>();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                s.add(new Step(a, new int[]{j,j+1}, new int[]{},
                    toArr(sorted), new int[]{},
                    "Comparing index " + j + " (" + a[j] + ") and " + (j+1) + " (" + a[j+1] + ")"));
                if (a[j] > a[j+1]) {
                    int tmp = a[j]; a[j] = a[j+1]; a[j+1] = tmp;
                    s.add(new Step(a, new int[]{}, new int[]{j,j+1},
                        toArr(sorted), new int[]{},
                        "Swapped! " + a[j] + " and " + a[j+1]));
                }
            }
            sorted.add(n - 1 - i);
            s.add(new Step(a, new int[]{}, new int[]{},
                toArr(sorted), new int[]{}, "Pass " + (i+1) + " complete"));
        }
        sorted.add(0);
        s.add(allSorted(a, n));
        return s;
    }

    // ── Selection Sort ─────────────────────────────────────────
    private List<Step> selectionSteps(int[] a) {
        List<Step> s = new ArrayList<>();
        int n = a.length;
        Set<Integer> sorted = new LinkedHashSet<>();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                s.add(new Step(a, new int[]{minIdx,j}, new int[]{},
                    toArr(sorted), new int[]{},
                    "Finding min: comparing " + a[minIdx] + " at " + minIdx + " with " + a[j] + " at " + j));
                if (a[j] < a[minIdx]) minIdx = j;
            }
            if (minIdx != i) {
                int tmp = a[i]; a[i] = a[minIdx]; a[minIdx] = tmp;
                s.add(new Step(a, new int[]{}, new int[]{i,minIdx},
                    toArr(sorted), new int[]{}, "Placed minimum " + a[i] + " at index " + i));
            }
            sorted.add(i);
        }
        sorted.add(n - 1);
        s.add(allSorted(a, n));
        return s;
    }

    // ── Insertion Sort ─────────────────────────────────────────
    private List<Step> insertionSteps(int[] a) {
        List<Step> s = new ArrayList<>();
        int n = a.length;
        Set<Integer> sorted = new LinkedHashSet<>();
        sorted.add(0);
        for (int i = 1; i < n; i++) {
            int key = a[i], j = i - 1;
            s.add(new Step(a, new int[]{i}, new int[]{},
                toArr(sorted), new int[]{}, "Inserting " + key + " into sorted portion"));
            while (j >= 0 && a[j] > key) {
                s.add(new Step(a, new int[]{j,j+1}, new int[]{},
                    toArr(sorted), new int[]{}, a[j] + " > " + key + ", shift right"));
                a[j+1] = a[j]; a[j] = key;
                s.add(new Step(a, new int[]{}, new int[]{j,j+1},
                    toArr(sorted), new int[]{}, "Shifted " + a[j+1] + " right"));
                j--;
            }
            sorted.add(i);
            s.add(new Step(a, new int[]{}, new int[]{},
                toArr(sorted), new int[]{}, key + " inserted at index " + (j+1)));
        }
        s.add(allSorted(a, n));
        return s;
    }

    // ── Merge Sort ─────────────────────────────────────────────
    private List<Step> mergeSteps(int[] a) {
        List<Step> s = new ArrayList<>();
        mergeSortHelper(a, 0, a.length - 1, s);
        s.add(allSorted(a, a.length));
        return s;
    }

    private void mergeSortHelper(int[] a, int l, int r, List<Step> s) {
        if (l >= r) return;
        int m = (l + r) / 2;
        mergeSortHelper(a, l, m, s);
        mergeSortHelper(a, m+1, r, s);
        merge(a, l, m, r, s);
    }

    private void merge(int[] a, int l, int m, int r, List<Step> s) {
        int[] left  = Arrays.copyOfRange(a, l, m+1);
        int[] right = Arrays.copyOfRange(a, m+1, r+1);
        int i=0, j=0, k=l;
        while (i < left.length && j < right.length) {
            s.add(new Step(a, new int[]{l+i, m+1+j}, new int[]{},
                new int[]{}, new int[]{}, "Merging: comparing " + left[i] + " and " + right[j]));
            if (left[i] <= right[j]) a[k++] = left[i++];
            else                     a[k++] = right[j++];
            s.add(new Step(a, new int[]{}, new int[]{k-1},
                new int[]{}, new int[]{}, "Placed " + a[k-1] + " at index " + (k-1)));
        }
        while (i < left.length)  { a[k++] = left[i++];  s.add(new Step(a,new int[]{},new int[]{k-1},new int[]{},new int[]{},"Placed "+a[k-1])); }
        while (j < right.length) { a[k++] = right[j++]; s.add(new Step(a,new int[]{},new int[]{k-1},new int[]{},new int[]{},"Placed "+a[k-1])); }
    }

    // ── Quick Sort ─────────────────────────────────────────────
    private List<Step> quickSteps(int[] a) {
        List<Step> s = new ArrayList<>();
        quickSortHelper(a, 0, a.length - 1, s);
        s.add(allSorted(a, a.length));
        return s;
    }

    private void quickSortHelper(int[] a, int l, int r, List<Step> s) {
        if (l >= r) return;
        int p = partition(a, l, r, s);
        quickSortHelper(a, l, p-1, s);
        quickSortHelper(a, p+1, r, s);
    }

    private int partition(int[] a, int l, int r, List<Step> s) {
        int pivot = a[r];
        s.add(new Step(a, new int[]{}, new int[]{}, new int[]{}, new int[]{r},
            "Pivot = " + pivot + " at index " + r));
        int i = l - 1;
        for (int j = l; j < r; j++) {
            s.add(new Step(a, new int[]{j,r}, new int[]{}, new int[]{}, new int[]{r},
                "Comparing " + a[j] + " with pivot " + pivot));
            if (a[j] <= pivot) {
                i++;
                if (i != j) {
                    int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
                    s.add(new Step(a, new int[]{}, new int[]{i,j}, new int[]{}, new int[]{r},
                        "Swapped " + a[i] + " and " + a[j]));
                }
            }
        }
        int tmp = a[i+1]; a[i+1] = a[r]; a[r] = tmp;
        s.add(new Step(a, new int[]{}, new int[]{i+1,r}, new int[]{}, new int[]{},
            "Pivot " + pivot + " placed at index " + (i+1)));
        return i+1;
    }

    // ── Helpers ────────────────────────────────────────────────
    private int[] toArr(Set<Integer> set) {
        return set.stream().mapToInt(Integer::intValue).toArray();
    }

    private Step allSorted(int[] a, int n) {
        int[] all = new int[n];
        for (int i = 0; i < n; i++) all[i] = i;
        return new Step(a, new int[]{}, new int[]{}, all, new int[]{}, "Sorted!");
    }

    private String getDescription(String algo) {
        switch (algo) {
            case "Bubble Sort":    return "Bubble sort repeatedly compares adjacent elements and swaps them if out of order. Largest values bubble to the end each pass.";
            case "Selection Sort": return "Selection sort finds the minimum element from the unsorted part and places it at the front repeatedly, building a sorted section.";
            case "Insertion Sort": return "Insertion sort picks each element and inserts it into its correct position in the already-sorted portion, like sorting playing cards.";
            case "Merge Sort":     return "Merge sort divides the array in half recursively, then merges halves in sorted order. Guarantees O(n log n) time always.";
            case "Quick Sort":     return "Quick sort picks a pivot, partitions smaller values left and larger values right, then recursively sorts each partition.";
            default:               return "";
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Main
    // ══════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new SortingVisualizer().setVisible(true);
        });
    }
}