package haven.automation;


import haven.*;

public class LightWithTorch implements Runnable {
    private GameUI gui;
    private Gob gob;
    private static final int TIMEOUT_ACT = 3000;
    private static final int TIMEOUT_FINISH = 4000;
    private static final int PROG_ACT_DELAY = 8;
    private static final int PROG_FINISH_DELAY = 70;

    public LightWithTorch(GameUI gui) {
        this.gui = gui;
    }

    @Override
    public void run() {
        synchronized (gui.map.glob.oc) {
            for (Gob gob : gui.map.glob.oc) {
                Resource res = gob.getres();
                if (res != null &&
                        (res.name.equals("gfx/terobjs/oven") ||
                        res.name.equals("gfx/terobjs/smelter") ||
                        res.name.equals("gfx/terobjs/steelcrucible") ||
                        res.name.equals("gfx/terobjs/kiln"))) {
                    if (this.gob == null)
                        this.gob = gob;
                    else if (gob.rc.dist(gui.map.player().rc) < this.gob.rc.dist(gui.map.player().rc))
                        this.gob = gob;
                }
            }
        }

        if (gob == null) {
            gui.error("No ovens/smelters/steelboxes/kilns found.");
            return;
        }

        Equipory e = gui.getequipory();
        WItem l = e.quickslots[6];
        WItem r = e.quickslots[7];

        boolean noltorch = true;
        boolean nortorch = true;

        if (l != null) {
            String lname = l.item.getname();
            if (lname.contains("Lit Torch"))
                noltorch = false;
        }
        if (r != null) {
            String rname = r.item.getname();
            if (rname.contains("Lit Torch"))
                nortorch = false;
        }

        if (noltorch && nortorch) {
            gui.error("No lit torch is equipped.");
            return;
        }

        WItem w = e.quickslots[noltorch ? 7 : 6];
        w.mousedown(new Coord(w.sz.x / 2, w.sz.y / 2), 1);

        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            return;
        }

        gui.map.wdgmsg("itemact", Coord.z, gob.rc, 0, 0, (int) gob.id, gob.rc, 0, -1);

        int timeout = 0;
        while (gui.prog == -1) {
            timeout += PROG_ACT_DELAY;
            if (timeout >= TIMEOUT_ACT) {
                gui.error("Oops something went wrong. Timeout when trying to light with torch.");
                e.wdgmsg("drop", noltorch ? 7 : 6);
                return;
            }
            try {
                Thread.sleep(PROG_ACT_DELAY);
            } catch (InterruptedException ie) {
                e.wdgmsg("drop", noltorch ? 7 : 6);
                return;
            }
        }

        timeout = 0;
        while (gui.prog != -1) {
            timeout += PROG_FINISH_DELAY;
            if (timeout >= TIMEOUT_FINISH) {
                gui.error("Oops something went wrong. Timeout when trying to light with torch.");
                e.wdgmsg("drop", noltorch ? 7 : 6);
                return;
            }
            try {
                Thread.sleep(PROG_FINISH_DELAY);
            } catch (InterruptedException ie) {
                e.wdgmsg("drop", noltorch ? 7 : 6);
                return;
            }
        }
        e.wdgmsg("drop", noltorch ? 7 : 6);
    }
}
