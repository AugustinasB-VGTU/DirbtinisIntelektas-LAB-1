package org.example;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * MazeSolver - Labirinto sprendimo programa naudojant BFS algoritmą.
 *
 * @author [Augustinas Bukas, Dovydas Peseckis ITnt24]
 * @date 2025-02-27
 */
public class Main extends JPanel {
    private static final int LANGELIO_DYDIS = 50, SIENA = 1, KELIAS = 0;
    private static int DYDIS = 18; // Labirinto dydis (gali būti keičiamas)
    private int[][] labirintas;
    private final Point pradzia = new Point(0, 0), pabaiga;
    private List<Point> sprendimoKelias;
    private Point agentas;
    private final int[][] kryptys = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
    private JLabel kelioIlgisLabel = new JLabel("Kelio ilgis: N/A");

    /**
     * Konstruktorius, kuris inicijuoja labirintą ir sukuria GUI.
     */
    public Main() {
        pabaiga = new Point(DYDIS - 1, DYDIS - 1);
        sukurtiLabirinta();
        sukurtiGUI();
    }

    /**
     * Sugeneruoja naują labirintą naudojant rekursyvinį gylio pirmą paieškos metodą (DFS).
     */
    private void sukurtiLabirinta() {
        labirintas = new int[DYDIS][DYDIS];
        for (int[] eilute : labirintas) Arrays.fill(eilute, SIENA);
        Stack<Point> stekas = new Stack<>();
        stekas.push(pradzia);
        Random rand = new Random();

        while (!stekas.isEmpty()) {
            Point dabartinis = stekas.peek();
            List<Point> kaimynai = new ArrayList<>();
            for (int[] k : kryptys) {
                int nx = dabartinis.x + k[0] * 2, ny = dabartinis.y + k[1] * 2;
                if (nx >= 0 && ny >= 0 && nx < DYDIS && ny < DYDIS && labirintas[nx][ny] == SIENA)
                    kaimynai.add(new Point(nx, ny));
            }
            if (!kaimynai.isEmpty()) {
                Point kitas = kaimynai.get(rand.nextInt(kaimynai.size()));
                labirintas[(dabartinis.x + kitas.x) / 2][(dabartinis.y + kitas.y) / 2] = KELIAS;
                labirintas[kitas.x][kitas.y] = KELIAS;
                stekas.push(kitas);
            } else stekas.pop();
        }

        // Užtikrina, kad išėjimas visada būtų atviras
        labirintas[pabaiga.x][pabaiga.y] = KELIAS;
        labirintas[pabaiga.x - 1][pabaiga.y] = KELIAS;

        agentas = new Point(pradzia);
        sprendimoKelias = null;
        kelioIlgisLabel.setText("Kelio ilgis: N/A");
        repaint();
    }

    /**
     * Randa trumpiausią kelią labirinte naudojant BFS algoritmą.
     */
    private void isprestisuBFS() {
        Queue<List<Point>> eile = new LinkedList<>();
        Set<Point> aplankyti = new HashSet<>();
        eile.add(Collections.singletonList(pradzia));

        while (!eile.isEmpty()) {
            List<Point> kelias = eile.poll();
            Point paskutinis = kelias.get(kelias.size() - 1);

            if (paskutinis.equals(pabaiga)) {
                sprendimoKelias = kelias;
                kelioIlgisLabel.setText("BFS kelio ilgis: " + kelias.size());
                judetiAgentui();
                return;
            }

            for (Point kitas : gautiKaimynus(paskutinis)) {
                if (aplankyti.add(kitas) && labirintas[kitas.x][kitas.y] == KELIAS) {
                    List<Point> naujasKelias = new ArrayList<>(kelias);
                    naujasKelias.add(kitas);
                    eile.add(naujasKelias);
                }
            }
        }
    }

    /**
     * Grąžina galimus kaimyninius taškus.
     * @param p Dabartinė taško vieta
     * @return Sąrašas galimų gretimų taškų
     */
    private List<Point> gautiKaimynus(Point p) {
        List<Point> kaimynai = new ArrayList<>();
        for (int[] k : kryptys) {
            int nx = p.x + k[0], ny = p.y + k[1];
            if (nx >= 0 && ny >= 0 && nx < DYDIS && ny < DYDIS) kaimynai.add(new Point(nx, ny));
        }
        return kaimynai;
    }

    /**
     * Perkelia agentą pagal rasto sprendimo kelią.
     */
    private void judetiAgentui() {
        if (sprendimoKelias == null || sprendimoKelias.isEmpty()) return; // Neleidžia NullPointerException

        Timer timer = new Timer(30, e -> {
            if (sprendimoKelias != null && !sprendimoKelias.isEmpty()) {
                agentas = sprendimoKelias.remove(0);
                repaint();
                if (agentas.equals(pabaiga)) ((Timer) e.getSource()).stop();
            } else {
                ((Timer) e.getSource()).stop();
            }
        });

        timer.start();
    }

    /**
     * Nupiešia labirintą, agentą ir jo judėjimo kelią.
     * @param g Grafinis komponentas piešimui
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < DYDIS; i++)
            for (int j = 0; j < DYDIS; j++) {
                g.setColor(labirintas[i][j] == SIENA ? Color.BLACK : Color.WHITE);
                g.fillRect(j * LANGELIO_DYDIS, i * LANGELIO_DYDIS, LANGELIO_DYDIS, LANGELIO_DYDIS);
                g.setColor(Color.GRAY);
                g.drawRect(j * LANGELIO_DYDIS, i * LANGELIO_DYDIS, LANGELIO_DYDIS, LANGELIO_DYDIS);
            }
        g.setColor(Color.GREEN);
        g.fillRect(pradzia.y * LANGELIO_DYDIS, pradzia.x * LANGELIO_DYDIS, LANGELIO_DYDIS, LANGELIO_DYDIS);
        g.setColor(Color.RED);
        g.fillRect(pabaiga.y * LANGELIO_DYDIS, pabaiga.x * LANGELIO_DYDIS, LANGELIO_DYDIS, LANGELIO_DYDIS);
        g.setColor(Color.YELLOW);
        g.fillRect(agentas.y * LANGELIO_DYDIS, agentas.x * LANGELIO_DYDIS, LANGELIO_DYDIS, LANGELIO_DYDIS);
    }

    /**
     * Sukuria pagrindinę vartotojo sąsają.
     */
    private void sukurtiGUI() {
        JFrame langas = new JFrame("Labirinto sprendėjas");
        langas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        langas.setSize(DYDIS * LANGELIO_DYDIS + 50, DYDIS * LANGELIO_DYDIS + 150);

        JButton bfsMygtukas = new JButton("Išspręsti (BFS)"), naujasLabirintasMygtukas = new JButton("Naujas labirintas");
        bfsMygtukas.addActionListener(e -> isprestisuBFS());
        naujasLabirintasMygtukas.addActionListener(e -> sukurtiLabirinta());

        JPanel mygtukuPanel = new JPanel();
        mygtukuPanel.add(bfsMygtukas);
        mygtukuPanel.add(naujasLabirintasMygtukas);
        mygtukuPanel.add(kelioIlgisLabel);

        langas.setLayout(new BorderLayout());
        langas.add(this, BorderLayout.CENTER);
        langas.add(mygtukuPanel, BorderLayout.SOUTH);
        langas.setVisible(true);
    }

    /**
     * Pagrindinis programos metodas.
     * @param args Komandinės eilutės argumentai (nenaudojami)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
