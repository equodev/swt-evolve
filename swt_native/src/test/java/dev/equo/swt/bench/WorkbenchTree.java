package dev.equo.swt.bench;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.DartImage;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;

/**
 * The Eclipse workbench bench shape as a hardcoded live SWT widget tree (127 widgets),
 * GENERATED from resources/bench/workbench.json by dev.equo.swt.bench.gen.WorkbenchTreeGenerator.
 * Built once at bench startup; re-serialized through the production Serializer each iteration like
 * every other shape. Do not edit by hand — re-run the generator after re-capturing the JSON.
 */
final class WorkbenchTree {

    static DartControl build(Shell shell) {
        Widget[] w = new Widget[127];
        c0(shell, w);
        c1(w);
        c2(w);
        c3(w);
        c4(w);
        s0(w);
        s1(w);
        s2(w);
        s3(w);
        s4(w);
        s5(w);
        s6(w);
        s7(w);
        s8(w);
        s9(w);
        s10(w);
        s11(w);
        s12(w);
        s13(w);
        s14(w);
        s15(w);
        s16(w);
        s17(w);
        s18(w);
        s19(w);
        s20(w);
        s21(w);
        s22(w);
        s23(w);
        s24(w);
        s25(w);
        s26(w);
        s27(w);
        s28(w);
        s29(w);
        s30(w);
        s31(w);
        s32(w);
        k0(w);
        return (DartControl) w[0].getImpl();
    }

    private static void c0(Shell shell, Widget[] w) {
        w[0] = new Composite(shell, 570425344);
        w[1] = new Composite((Composite) w[0], 570425344);
        w[2] = new CTabFolder((Composite) w[1], 571476098);
        w[3] = new ToolBar((Composite) w[2], 578814208);
        w[4] = new ToolItem((ToolBar) w[3], 8);
        w[5] = new ToolBar((Composite) w[2], 578814208);
        w[6] = new ToolItem((ToolBar) w[5], 8);
        w[7] = new ToolItem((ToolBar) w[5], 8);
        w[8] = new Composite((Composite) w[2], 570425344);
        w[9] = new ToolBar((Composite) w[8], 578945344);
        w[10] = new ToolItem((ToolBar) w[9], 8);
        w[11] = new ToolItem((ToolBar) w[9], 32);
        w[12] = new ToolItem((ToolBar) w[9], 8);
        w[13] = new ToolItem((ToolBar) w[9], 2);
        w[14] = new ToolItem((ToolBar) w[9], 32);
        w[15] = new ToolBar((Composite) w[8], 578945280);
        w[16] = new ToolItem((ToolBar) w[15], 8);
        w[17] = new Composite((Composite) w[2], 570425344);
        w[18] = new Composite((Composite) w[17], 570425344);
        w[19] = new Composite((Composite) w[18], 570425344);
        w[20] = new Composite((Composite) w[19], 570425344);
        w[21] = new Composite((Composite) w[20], 570425344);
        w[22] = new Composite((Composite) w[21], 570425344);
        w[23] = new Link((Composite) w[22], 570425408);
        w[24] = new Composite((Composite) w[22], 570425344);
        w[25] = new Label((Composite) w[24], 570966016);
        w[26] = new Canvas((Composite) w[24], 33554496);
        w[27] = new Label((Composite) w[24], 570966016);
        w[28] = new Canvas((Composite) w[24], 33554496);
        w[29] = new Label((Composite) w[24], 570966016);
    }

    private static void c1(Widget[] w) {
        w[30] = new Canvas((Composite) w[24], 33554496);
        w[31] = new Label((Composite) w[24], 570966016);
        w[32] = new Canvas((Composite) w[24], 33554496);
        w[33] = new Label((Composite) w[24], 570966016);
        w[34] = new Canvas((Composite) w[24], 33554496);
        w[35] = new Tree((Composite) w[20], 570491650);
        w[36] = new Menu((Control) w[35]);
        w[37] = new CTabItem((CTabFolder) w[2], 64);
        w[38] = new CTabItem((CTabFolder) w[2], 64);
        w[39] = new Composite((Composite) w[1], 570425344);
        w[40] = new Composite((Composite) w[39], 570425344);
        w[41] = new CTabFolder((Composite) w[40], 571476098);
        w[42] = new ToolBar((Composite) w[41], 578814208);
        w[43] = new ToolItem((ToolBar) w[42], 8);
        w[44] = new Composite((Composite) w[41], 570425344);
        w[45] = new ToolBar((Composite) w[44], 578945280);
        w[46] = new ToolItem((ToolBar) w[45], 8);
        w[47] = new ToolBar((Composite) w[41], 578814208);
        w[48] = new ToolItem((ToolBar) w[47], 8);
        w[49] = new ToolItem((ToolBar) w[47], 8);
        w[50] = new Composite((Composite) w[41], 570425344);
        w[51] = new Composite((Composite) w[50], 570425344);
        w[52] = new Label((Composite) w[51], 570966016);
        w[53] = new Label((Composite) w[51], 570966016);
        w[54] = new Label((Composite) w[51], 571080704);
        w[55] = new Label((Composite) w[51], 570966016);
        w[56] = new Label((Composite) w[51], 571080704);
        w[57] = new Label((Composite) w[51], 570966016);
        w[58] = new Composite((Composite) w[41], 570425344);
        w[59] = new Label((Composite) w[58], 570966016);
    }

    private static void c2(Widget[] w) {
        w[60] = new CTabFolder((Composite) w[1], 571476098);
        w[61] = new ToolBar((Composite) w[60], 578814208);
        w[62] = new ToolItem((ToolBar) w[61], 8);
        w[63] = new ToolBar((Composite) w[60], 578814208);
        w[64] = new ToolItem((ToolBar) w[63], 8);
        w[65] = new ToolItem((ToolBar) w[63], 8);
        w[66] = new Composite((Composite) w[60], 570425344);
        w[67] = new ToolBar((Composite) w[66], 578945344);
        w[68] = new ToolItem((ToolBar) w[67], 32);
        w[69] = new ToolBar((Composite) w[66], 578945280);
        w[70] = new ToolItem((ToolBar) w[69], 8);
        w[71] = new Composite((Composite) w[60], 570425344);
        w[72] = new Composite((Composite) w[71], 570425344);
        w[73] = new Composite((Composite) w[72], 570425344);
        w[74] = new Composite((Composite) w[73], 570425344);
        w[75] = new Composite((Composite) w[74], 570425344);
        w[76] = new Label((Composite) w[75], 570966208);
        w[77] = new CTabItem((CTabFolder) w[60], 64);
        w[78] = new CTabItem((CTabFolder) w[60], 64);
        w[79] = new CTabFolder((Composite) w[1], 571476098);
        w[80] = new ToolBar((Composite) w[79], 578814208);
        w[81] = new ToolItem((ToolBar) w[80], 8);
        w[82] = new Composite((Composite) w[79], 570425344);
        w[83] = new ToolBar((Composite) w[82], 578945344);
        w[84] = new ToolItem((ToolBar) w[83], 8);
        w[85] = new ToolItem((ToolBar) w[83], 2);
        w[86] = new ToolItem((ToolBar) w[83], 32);
        w[87] = new ToolBar((Composite) w[82], 578945280);
        w[88] = new ToolItem((ToolBar) w[87], 8);
        w[89] = new ToolBar((Composite) w[79], 578814208);
    }

    private static void c3(Widget[] w) {
        w[90] = new ToolItem((ToolBar) w[89], 8);
        w[91] = new ToolItem((ToolBar) w[89], 8);
        w[92] = new Composite((Composite) w[79], 570425344);
        w[93] = new Composite((Composite) w[92], 570425344);
        w[94] = new Label((Composite) w[93], 570966016);
        w[95] = new Label((Composite) w[93], 570949898);
        w[96] = new Composite((Composite) w[93], 570425344);
        w[97] = new Tree((Composite) w[96], 570491650);
        w[98] = new TreeColumn((Tree) w[97], 16384);
        w[99] = new TreeColumn((Tree) w[97], 16384);
        w[100] = new TreeColumn((Tree) w[97], 16384);
        w[101] = new TreeColumn((Tree) w[97], 16384);
        w[102] = new TreeColumn((Tree) w[97], 16384);
        w[103] = new Menu((Control) w[97]);
        w[104] = new Text((Composite) w[97], 570441732);
        w[105] = new CTabItem((CTabFolder) w[79], 64);
        w[106] = new CTabItem((CTabFolder) w[79], 64);
        w[107] = new CTabFolder((Composite) w[1], 571476098);
        w[108] = new ToolBar((Composite) w[107], 578814208);
        w[109] = new ToolItem((ToolBar) w[108], 8);
        w[110] = new Composite((Composite) w[107], 570425344);
        w[111] = new ToolBar((Composite) w[110], 578945344);
        w[112] = new ToolBar((Composite) w[110], 578945280);
        w[113] = new ToolItem((ToolBar) w[112], 8);
        w[114] = new ToolBar((Composite) w[107], 578814208);
        w[115] = new ToolItem((ToolBar) w[114], 8);
        w[116] = new ToolItem((ToolBar) w[114], 8);
        w[117] = new Composite((Composite) w[107], 570425344);
        w[118] = new Composite((Composite) w[117], 570425344);
        w[119] = new Composite((Composite) w[118], 570425344);
    }

    private static void c4(Widget[] w) {
        w[120] = new Browser((Composite) w[119], 570425344);
        w[121] = new Composite((Composite) w[107], 570425344);
        w[122] = new Composite((Composite) w[121], 570425344);
        w[123] = new Composite((Composite) w[122], 570425344);
        w[124] = new Browser((Composite) w[123], 570425344);
        w[125] = new CTabItem((CTabFolder) w[107], 64);
        w[126] = new CTabItem((CTabFolder) w[107], 64);
    }

    private static void s0(Widget[] w) {
        ((ToolItem) w[4]).setToolTipText("Show List");
        ((ToolItem) w[4]).setEnabled(true);
        ((ToolItem) w[4]).setImage(img(34, 18, 24, false, null));
        ((ToolBar) w[3]).setBounds(new Rectangle(128, 2, 0, 24));
        ((ToolBar) w[3]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[3]).setVisible(false);
        ((ToolBar) w[3]).setBackgroundMode(1);
        ((ToolBar) w[3]).setOrientation(33554432);
        ((ToolBar) w[3]).setDragDetect(true);
        ((ToolBar) w[3]).setTextDirection(33554432);
        ((ToolBar) w[3]).setEnabled(true);
        ((ToolBar) w[3]).setForeground(color(0, 0, 0, 255));
        ((ToolItem) w[6]).setToolTipText("Minimize");
        ((ToolItem) w[6]).setEnabled(true);
        ((ToolItem) w[6]).setImage(img(18, 18, 24, false, null));
        ((ToolItem) w[7]).setToolTipText("Maximize");
        ((ToolItem) w[7]).setEnabled(true);
        ((ToolItem) w[7]).setImage(img(18, 18, 24, false, null));
        ((ToolBar) w[5]).setBounds(new Rectangle(128, 2, 48, 24));
        ((ToolBar) w[5]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[5]).setVisible(true);
        ((ToolBar) w[5]).setBackgroundMode(1);
        ((ToolBar) w[5]).setOrientation(33554432);
        ((ToolBar) w[5]).setDragDetect(true);
        ((ToolBar) w[5]).setTextDirection(33554432);
        ((ToolBar) w[5]).setEnabled(true);
        ((ToolBar) w[5]).setForeground(color(0, 0, 0, 255));
        ((ToolItem) w[10]).setToolTipText("Collapse All  (⇧⌘Numpad_Divide)");
        ((ToolItem) w[10]).setEnabled(true);
        ((ToolItem) w[10]).setForeground(color(238, 238, 238, 255));
    }

    private static void s1(Widget[] w) {
        ((ToolItem) w[10]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[10]).setImage(img(16, 16, 32, true, "collapseall"));
        ((ToolItem) w[10]).setDisabledImage(img(16, 16, 32, true, "collapseall"));
        ((ToolItem) w[11]).setToolTipText("Link with Editor");
        ((ToolItem) w[11]).setEnabled(true);
        ((ToolItem) w[11]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[11]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[11]).setImage(img(16, 16, 32, true, "synced"));
        ((ToolItem) w[11]).setDisabledImage(img(16, 16, 32, true, "synced"));
        ((ToolItem) w[12]).setToolTipText("Select and deselect filters to apply to the content in the tree");
        ((ToolItem) w[12]).setEnabled(true);
        ((ToolItem) w[12]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[12]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[12]).setImage(img(16, 16, 32, true, "filter_ps"));
        ((ToolItem) w[12]).setDisabledImage(img(16, 16, 32, true, "filter_ps"));
        ((ToolItem) w[13]).setEnabled(true);
        ((ToolItem) w[13]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[13]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[14]).setToolTipText("Focus on Active Task (Alt+click to reveal filtered elements)");
        ((ToolItem) w[14]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[14]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[14]).setImage(img(16, 16, 32, false, "focus"));
        ((ToolItem) w[14]).setDisabledImage(img(16, 16, 32, false, "focus-disabled"));
        ((ToolBar) w[9]).setBounds(new Rectangle(0, 0, 102, 22));
        ((ToolBar) w[9]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[9]).setRedraw(true);
        ((ToolBar) w[9]).setVisible(true);
        ((ToolBar) w[9]).setOrientation(33554432);
        ((ToolBar) w[9]).setDragDetect(true);
        ((ToolBar) w[9]).setTextDirection(33554432);
    }

    private static void s2(Widget[] w) {
        ((ToolBar) w[9]).setEnabled(true);
        ((ToolBar) w[9]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[16]).setToolTipText("View Menu");
        ((ToolItem) w[16]).setEnabled(true);
        ((ToolItem) w[16]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[16]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[16]).setImage(img(16, 16, 32, true, "view_menu"));
        ((ToolBar) w[15]).setBounds(new Rectangle(102, 0, 24, 22));
        ((ToolBar) w[15]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[15]).setVisible(true);
        ((ToolBar) w[15]).setOrientation(33554432);
        ((ToolBar) w[15]).setDragDetect(true);
        ((ToolBar) w[15]).setTextDirection(33554432);
        ((ToolBar) w[15]).setEnabled(true);
        ((ToolBar) w[15]).setForeground(color(238, 238, 238, 255));
        ((Composite) w[8]).setBounds(new Rectangle(53, 27, 126, 22));
        ((Composite) w[8]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[8]).setRedraw(true);
        ((Composite) w[8]).setVisible(true);
        ((Composite) w[8]).setBackgroundMode(1);
        ((Composite) w[8]).setOrientation(33554432);
        ((Composite) w[8]).setDragDetect(true);
        ((Composite) w[8]).setTextDirection(33554432);
        ((Composite) w[8]).setEnabled(true);
        ((Composite) w[8]).setForeground(color(238, 238, 238, 255));
        ((Link) w[23]).setDragDetect(true);
        ((Link) w[23]).setText("There are no projects in your workspace.\nTo add a project:");
        ((Link) w[23]).setBounds(new Rectangle(5, 5, 165, 20));
        ((Link) w[23]).setBackground(color(47, 47, 47, 255));
        ((Link) w[23]).setTextDirection(33554432);
    }

    private static void s3(Widget[] w) {
        ((Link) w[23]).setEnabled(true);
        ((Link) w[23]).setForeground(color(170, 170, 170, 255));
        ((Link) w[23]).setLinkForeground(color(111, 197, 238, 255));
        ((Link) w[23]).setVisible(true);
        ((Link) w[23]).setOrientation(33554432);
        ((Label) w[25]).setAlignment(16384);
        ((Label) w[25]).setDragDetect(true);
        ((Label) w[25]).setBounds(new Rectangle(5, 9, 24, 20));
        ((Label) w[25]).setBackground(color(47, 47, 47, 255));
        ((Label) w[25]).setTextDirection(33554432);
        ((Label) w[25]).setEnabled(true);
        ((Label) w[25]).setForeground(color(170, 170, 170, 255));
        ((Label) w[25]).setVisible(true);
        ((Label) w[25]).setOrientation(33554432);
        ((Label) w[25]).setImage(img(16, 16, 32, true, "newpprj_wiz"));
        ((Canvas) w[26]).setBounds(new Rectangle(34, 5, 121, 28));
        ((Canvas) w[26]).setBackground(color(47, 47, 47, 255));
        ((Canvas) w[26]).setVisible(true);
        ((Canvas) w[26]).setOrientation(33554432);
        ((Canvas) w[26]).setDragDetect(true);
        ((Canvas) w[26]).setTextDirection(33554432);
        ((Canvas) w[26]).setEnabled(true);
        ((Canvas) w[26]).setForeground(color(170, 170, 170, 255));
        ((Label) w[27]).setAlignment(16384);
        ((Label) w[27]).setDragDetect(true);
        ((Label) w[27]).setBounds(new Rectangle(5, 42, 24, 20));
        ((Label) w[27]).setBackground(color(47, 47, 47, 255));
        ((Label) w[27]).setTextDirection(33554432);
        ((Label) w[27]).setEnabled(true);
        ((Label) w[27]).setForeground(color(170, 170, 170, 255));
    }

    private static void s4(Widget[] w) {
        ((Label) w[27]).setVisible(true);
        ((Label) w[27]).setOrientation(33554432);
        ((Label) w[27]).setImage(img(16, 16, 32, true, "newftrprj_wiz"));
        ((Canvas) w[28]).setBounds(new Rectangle(34, 38, 121, 28));
        ((Canvas) w[28]).setBackground(color(47, 47, 47, 255));
        ((Canvas) w[28]).setVisible(true);
        ((Canvas) w[28]).setOrientation(33554432);
        ((Canvas) w[28]).setDragDetect(true);
        ((Canvas) w[28]).setTextDirection(33554432);
        ((Canvas) w[28]).setEnabled(true);
        ((Canvas) w[28]).setForeground(color(170, 170, 170, 255));
        ((Label) w[29]).setAlignment(16384);
        ((Label) w[29]).setDragDetect(true);
        ((Label) w[29]).setBounds(new Rectangle(5, 75, 24, 20));
        ((Label) w[29]).setBackground(color(47, 47, 47, 255));
        ((Label) w[29]).setTextDirection(33554432);
        ((Label) w[29]).setEnabled(true);
        ((Label) w[29]).setForeground(color(170, 170, 170, 255));
        ((Label) w[29]).setVisible(true);
        ((Label) w[29]).setOrientation(33554432);
        ((Label) w[29]).setImage(img(16, 16, 32, true, "new_m2_project"));
        ((Canvas) w[30]).setBounds(new Rectangle(34, 71, 121, 28));
        ((Canvas) w[30]).setBackground(color(47, 47, 47, 255));
        ((Canvas) w[30]).setVisible(true);
        ((Canvas) w[30]).setOrientation(33554432);
        ((Canvas) w[30]).setDragDetect(true);
        ((Canvas) w[30]).setTextDirection(33554432);
        ((Canvas) w[30]).setEnabled(true);
        ((Canvas) w[30]).setForeground(color(170, 170, 170, 255));
        ((Label) w[31]).setAlignment(16384);
    }

    private static void s5(Widget[] w) {
        ((Label) w[31]).setDragDetect(true);
        ((Label) w[31]).setBounds(new Rectangle(5, 104, 24, 20));
        ((Label) w[31]).setBackground(color(47, 47, 47, 255));
        ((Label) w[31]).setTextDirection(33554432);
        ((Label) w[31]).setEnabled(true);
        ((Label) w[31]).setForeground(color(170, 170, 170, 255));
        ((Label) w[31]).setVisible(true);
        ((Label) w[31]).setOrientation(33554432);
        ((Label) w[31]).setImage(img(16, 16, 32, true, "new_wiz"));
        ((Canvas) w[32]).setBounds(new Rectangle(34, 104, 95, 20));
        ((Canvas) w[32]).setBackground(color(47, 47, 47, 255));
        ((Canvas) w[32]).setVisible(true);
        ((Canvas) w[32]).setOrientation(33554432);
        ((Canvas) w[32]).setDragDetect(true);
        ((Canvas) w[32]).setTextDirection(33554432);
        ((Canvas) w[32]).setEnabled(true);
        ((Canvas) w[32]).setForeground(color(170, 170, 170, 255));
        ((Label) w[33]).setAlignment(16384);
        ((Label) w[33]).setDragDetect(true);
        ((Label) w[33]).setBounds(new Rectangle(5, 129, 24, 20));
        ((Label) w[33]).setBackground(color(47, 47, 47, 255));
        ((Label) w[33]).setTextDirection(33554432);
        ((Label) w[33]).setEnabled(true);
        ((Label) w[33]).setForeground(color(170, 170, 170, 255));
        ((Label) w[33]).setVisible(true);
        ((Label) w[33]).setOrientation(33554432);
        ((Label) w[33]).setImage(img(16, 16, 32, true, "import_wiz"));
        ((Canvas) w[34]).setBounds(new Rectangle(34, 129, 91, 20));
        ((Canvas) w[34]).setBackground(color(47, 47, 47, 255));
        ((Canvas) w[34]).setVisible(true);
    }

    private static void s6(Widget[] w) {
        ((Canvas) w[34]).setOrientation(33554432);
        ((Canvas) w[34]).setDragDetect(true);
        ((Canvas) w[34]).setTextDirection(33554432);
        ((Canvas) w[34]).setEnabled(true);
        ((Canvas) w[34]).setForeground(color(170, 170, 170, 255));
        ((Composite) w[24]).setBounds(new Rectangle(10, 30, 160, 154));
        ((Composite) w[24]).setBackground(color(47, 47, 47, 255));
        ((Composite) w[24]).setVisible(true);
        ((Composite) w[24]).setOrientation(33554432);
        ((Composite) w[24]).setDragDetect(true);
        ((Composite) w[24]).setTextDirection(33554432);
        ((Composite) w[24]).setEnabled(true);
        ((Composite) w[24]).setForeground(color(170, 170, 170, 255));
        ((Composite) w[22]).setBounds(new Rectangle(5, 5, 175, 189));
        ((Composite) w[22]).setBackground(color(47, 47, 47, 255));
        ((Composite) w[22]).setVisible(true);
        ((Composite) w[22]).setOrientation(33554432);
        ((Composite) w[22]).setDragDetect(true);
        ((Composite) w[22]).setTextDirection(33554432);
        ((Composite) w[22]).setEnabled(true);
        ((Composite) w[22]).setForeground(color(170, 170, 170, 255));
        ((Composite) w[21]).setBounds(new Rectangle(0, 0, 180, 777));
        ((Composite) w[21]).setBackground(color(47, 47, 47, 255));
        ((Composite) w[21]).setVisible(true);
        ((Composite) w[21]).setBackgroundMode(2);
        ((Composite) w[21]).setOrientation(33554432);
        ((Composite) w[21]).setDragDetect(true);
        ((Composite) w[21]).setTextDirection(33554432);
        ((Composite) w[21]).setEnabled(true);
        ((Composite) w[21]).setForeground(color(170, 170, 170, 255));
    }

    private static void s7(Widget[] w) {
        ((Menu) w[36]).setEnabled(true);
        ((Menu) w[36]).setOrientation(33554432);
        ((Tree) w[35]).setBounds(new Rectangle(0, 0, 180, 777));
        ((Tree) w[35]).setHeaderBackground(color(0, 0, 0, 0));
        ((Tree) w[35]).setBackground(color(47, 47, 47, 255));
        ((Tree) w[35]).setRedraw(true);
        ((Tree) w[35]).setVisible(false);
        ((Tree) w[35]).setHeaderForeground(color(0, 0, 0, 0));
        ((Tree) w[35]).setOrientation(33554432);
        ((Tree) w[35]).setDragDetect(true);
        ((Tree) w[35]).setTextDirection(33554432);
        ((Tree) w[35]).setEnabled(true);
        ((Tree) w[35]).setForeground(color(170, 170, 170, 255));
        ((Tree) w[35]).setColumnOrder(new int[]{});
        ((Composite) w[20]).setBounds(new Rectangle(0, 0, 180, 777));
        ((Composite) w[20]).setBackground(color(47, 47, 47, 255));
        ((Composite) w[20]).setVisible(true);
        ((Composite) w[20]).setOrientation(33554432);
        ((Composite) w[20]).setDragDetect(true);
        ((Composite) w[20]).setTextDirection(33554432);
        ((Composite) w[20]).setEnabled(true);
        ((Composite) w[20]).setForeground(color(170, 170, 170, 255));
        ((Composite) w[19]).setBounds(new Rectangle(0, 0, 180, 777));
        ((Composite) w[19]).setBackground(color(47, 47, 47, 255));
        ((Composite) w[19]).setVisible(true);
        ((Composite) w[19]).setOrientation(33554432);
        ((Composite) w[19]).setDragDetect(true);
        ((Composite) w[19]).setTextDirection(33554432);
        ((Composite) w[19]).setEnabled(true);
        ((Composite) w[19]).setForeground(color(170, 170, 170, 255));
    }

    private static void s8(Widget[] w) {
        ((Composite) w[18]).setBounds(new Rectangle(0, 0, 180, 777));
        ((Composite) w[18]).setBackground(color(41, 41, 41, 255));
        ((Composite) w[18]).setRedraw(true);
        ((Composite) w[18]).setVisible(true);
        ((Composite) w[18]).setOrientation(33554432);
        ((Composite) w[18]).setDragDetect(true);
        ((Composite) w[18]).setTextDirection(33554432);
        ((Composite) w[18]).setEnabled(true);
        ((Composite) w[18]).setForeground(color(221, 221, 221, 255));
        ((Composite) w[17]).setBounds(new Rectangle(0, 32, 180, 777));
        ((Composite) w[17]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[17]).setVisible(true);
        ((Composite) w[17]).setOrientation(33554432);
        ((Composite) w[17]).setDragDetect(true);
        ((Composite) w[17]).setTextDirection(33554432);
        ((Composite) w[17]).setEnabled(true);
        ((Composite) w[17]).setForeground(color(238, 238, 238, 255));
        ((CTabItem) w[37]).setToolTipText("Workspace");
        ((CTabItem) w[37]).setText("Project Explorer");
        ((CTabItem) w[37]).setShowClose(true);
        ((CTabItem) w[37]).setImage(img(16, 16, 32, true, "resource_persp"));
        ((CTabItem) w[38]).setToolTipText("Plug-ins");
        ((CTabItem) w[38]).setText("Plug-ins");
        ((CTabItem) w[38]).setShowClose(true);
        ((CTabItem) w[38]).setImage(img(16, 16, 32, true, "plugin_depend"));
        ((CTabFolder) w[2]).setMinimizeVisible(true);
        ((CTabFolder) w[2]).setUnselectedImageVisible(true);
        ((CTabFolder) w[2]).setOrientation(33554432);
        ((CTabFolder) w[2]).setMinimumCharacters(1);
        ((CTabFolder) w[2]).setTextDirection(33554432);
    }

    private static void s9(Widget[] w) {
        ((CTabFolder) w[2]).setEnabled(true);
        ((CTabFolder) w[2]).setHighlightEnabled(true);
        ((CTabFolder) w[2]).setTabPosition(128);
        ((CTabFolder) w[2]).setMaximizeVisible(true);
        ((CTabFolder) w[2]).setBounds(new Rectangle(0, 0, 180, 809));
        ((CTabFolder) w[2]).setBackground(color(72, 72, 76, 255));
        ((CTabFolder) w[2]).setSelectionForeground(color(255, 255, 255, 255));
        ((CTabFolder) w[2]).setVisible(true);
        ((CTabFolder) w[2]).setBorderVisible(true);
        ((CTabFolder) w[2]).setMRUVisible(true);
        ((CTabFolder) w[2]).setDragDetect(true);
        ((CTabFolder) w[2]).setSelectedImageVisible(true);
        ((CTabFolder) w[2]).setForeground(color(187, 187, 187, 255));
        ((CTabFolder) w[2]).setSimple(true);
        ((CTabFolder) w[2]).setTabHeight(24);
        ((CTabFolder) w[2]).setSelectionBarThickness(2);
        ((ToolItem) w[43]).setToolTipText("Show List");
        ((ToolItem) w[43]).setEnabled(true);
        ((ToolItem) w[43]).setImage(img(34, 18, 24, false, null));
        ((ToolBar) w[42]).setBounds(new Rectangle(350, 2, 0, 24));
        ((ToolBar) w[42]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[42]).setVisible(false);
        ((ToolBar) w[42]).setBackgroundMode(1);
        ((ToolBar) w[42]).setOrientation(33554432);
        ((ToolBar) w[42]).setDragDetect(true);
        ((ToolBar) w[42]).setTextDirection(33554432);
        ((ToolBar) w[42]).setEnabled(true);
        ((ToolBar) w[42]).setForeground(color(0, 0, 0, 255));
        ((ToolItem) w[46]).setToolTipText("View Menu");
        ((ToolItem) w[46]).setEnabled(true);
    }

    private static void s10(Widget[] w) {
        ((ToolItem) w[46]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[46]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[46]).setImage(img(16, 16, 32, true, "view_menu"));
        ((ToolBar) w[45]).setBounds(new Rectangle(0, 0, 0, 0));
        ((ToolBar) w[45]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[45]).setVisible(false);
        ((ToolBar) w[45]).setOrientation(33554432);
        ((ToolBar) w[45]).setDragDetect(true);
        ((ToolBar) w[45]).setTextDirection(33554432);
        ((ToolBar) w[45]).setEnabled(true);
        ((ToolBar) w[45]).setForeground(color(238, 238, 238, 255));
        ((Composite) w[44]).setBounds(new Rectangle(350, 2, 0, 24));
        ((Composite) w[44]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[44]).setVisible(false);
        ((Composite) w[44]).setBackgroundMode(1);
        ((Composite) w[44]).setOrientation(33554432);
        ((Composite) w[44]).setDragDetect(true);
        ((Composite) w[44]).setTextDirection(33554432);
        ((Composite) w[44]).setEnabled(true);
        ((Composite) w[44]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[48]).setToolTipText("Minimize");
        ((ToolItem) w[48]).setEnabled(true);
        ((ToolItem) w[48]).setImage(img(18, 18, 24, false, null));
        ((ToolItem) w[49]).setToolTipText("Maximize");
        ((ToolItem) w[49]).setEnabled(true);
        ((ToolItem) w[49]).setImage(img(18, 18, 24, false, null));
        ((ToolBar) w[47]).setBounds(new Rectangle(350, 2, 48, 24));
        ((ToolBar) w[47]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[47]).setVisible(true);
        ((ToolBar) w[47]).setBackgroundMode(1);
    }

    private static void s11(Widget[] w) {
        ((ToolBar) w[47]).setOrientation(33554432);
        ((ToolBar) w[47]).setDragDetect(true);
        ((ToolBar) w[47]).setTextDirection(33554432);
        ((ToolBar) w[47]).setEnabled(true);
        ((ToolBar) w[47]).setForeground(color(0, 0, 0, 255));
        ((Label) w[52]).setAlignment(16384);
        ((Label) w[52]).setDragDetect(true);
        ((Label) w[52]).setBounds(new Rectangle(4, 15, 358, 264));
        ((Label) w[52]).setBackground(color(72, 72, 76, 255));
        ((Label) w[52]).setTextDirection(33554432);
        ((Label) w[52]).setEnabled(true);
        ((Label) w[52]).setForeground(color(238, 238, 238, 255));
        ((Label) w[52]).setVisible(true);
        ((Label) w[52]).setOrientation(33554432);
        ((Label) w[52]).setImage(img(350, 260, 32, true, "onboarding_plugins"));
        ((Label) w[53]).setAlignment(16384);
        ((Label) w[53]).setDragDetect(true);
        ((Label) w[53]).setText("Open a file or drop files here to open them.");
        ((Label) w[53]).setBounds(new Rectangle(38, 288, 290, 20));
        ((Label) w[53]).setBackground(color(72, 72, 76, 255));
        ((Label) w[53]).setTextDirection(33554432);
        ((Label) w[53]).setEnabled(true);
        ((Label) w[53]).setForeground(color(238, 238, 238, 255));
        ((Label) w[53]).setVisible(true);
        ((Label) w[53]).setOrientation(33554432);
        ((Label) w[54]).setAlignment(131072);
        ((Label) w[54]).setDragDetect(true);
        ((Label) w[54]).setText("Find Actions");
        ((Label) w[54]).setBounds(new Rectangle(89, 317, 90, 20));
        ((Label) w[54]).setBackground(color(72, 72, 76, 255));
    }

    private static void s12(Widget[] w) {
        ((Label) w[54]).setTextDirection(33554432);
        ((Label) w[54]).setEnabled(true);
        ((Label) w[54]).setForeground(color(238, 238, 238, 255));
        ((Label) w[54]).setVisible(true);
        ((Label) w[54]).setOrientation(33554432);
        ((Label) w[55]).setAlignment(16384);
        ((Label) w[55]).setDragDetect(true);
        ((Label) w[55]).setText("⌘3");
        ((Label) w[55]).setBounds(new Rectangle(188, 317, 25, 20));
        ((Label) w[55]).setBackground(color(72, 72, 76, 255));
        ((Label) w[55]).setTextDirection(33554432);
        ((Label) w[55]).setEnabled(true);
        ((Label) w[55]).setForeground(color(238, 238, 238, 255));
        ((Label) w[55]).setVisible(true);
        ((Label) w[55]).setOrientation(33554432);
        ((Label) w[56]).setAlignment(131072);
        ((Label) w[56]).setDragDetect(true);
        ((Label) w[56]).setText("Open Type");
        ((Label) w[56]).setBounds(new Rectangle(98, 335, 81, 19));
        ((Label) w[56]).setBackground(color(72, 72, 76, 255));
        ((Label) w[56]).setTextDirection(33554432);
        ((Label) w[56]).setEnabled(true);
        ((Label) w[56]).setForeground(color(238, 238, 238, 255));
        ((Label) w[56]).setVisible(true);
        ((Label) w[56]).setOrientation(33554432);
        ((Label) w[57]).setAlignment(16384);
        ((Label) w[57]).setDragDetect(true);
        ((Label) w[57]).setText("⇧⌘T");
        ((Label) w[57]).setBounds(new Rectangle(188, 335, 33, 19));
        ((Label) w[57]).setBackground(color(72, 72, 76, 255));
    }

    private static void s13(Widget[] w) {
        ((Label) w[57]).setTextDirection(33554432);
        ((Label) w[57]).setEnabled(true);
        ((Label) w[57]).setForeground(color(238, 238, 238, 255));
        ((Label) w[57]).setVisible(true);
        ((Label) w[57]).setOrientation(33554432);
        ((Composite) w[51]).setBounds(new Rectangle(15, 106, 368, 360));
        ((Composite) w[51]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[51]).setVisible(true);
        ((Composite) w[51]).setOrientation(33554432);
        ((Composite) w[51]).setDragDetect(true);
        ((Composite) w[51]).setTextDirection(33554432);
        ((Composite) w[51]).setEnabled(true);
        ((Composite) w[51]).setForeground(color(238, 238, 238, 255));
        ((Composite) w[50]).setBounds(new Rectangle(2, 30, 398, 572));
        ((Composite) w[50]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[50]).setVisible(true);
        ((Composite) w[50]).setOrientation(33554432);
        ((Composite) w[50]).setDragDetect(true);
        ((Composite) w[50]).setTextDirection(33554432);
        ((Composite) w[50]).setEnabled(true);
        ((Composite) w[50]).setForeground(color(0, 0, 0, 255));
        ((Label) w[59]).setAlignment(16384);
        ((Label) w[59]).setDragDetect(true);
        ((Label) w[59]).setBounds(new Rectangle(0, 0, 0, 0));
        ((Label) w[59]).setBackground(color(72, 72, 76, 255));
        ((Label) w[59]).setTextDirection(33554432);
        ((Label) w[59]).setEnabled(true);
        ((Label) w[59]).setForeground(color(238, 238, 238, 255));
        ((Label) w[59]).setVisible(true);
        ((Label) w[59]).setOrientation(33554432);
    }

    private static void s14(Widget[] w) {
        ((Composite) w[58]).setBounds(new Rectangle(0, 0, 0, 0));
        ((Composite) w[58]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[58]).setVisible(true);
        ((Composite) w[58]).setOrientation(33554432);
        ((Composite) w[58]).setDragDetect(true);
        ((Composite) w[58]).setTextDirection(33554432);
        ((Composite) w[58]).setEnabled(true);
        ((Composite) w[58]).setForeground(color(0, 0, 0, 255));
        ((CTabFolder) w[41]).setMinimizeVisible(true);
        ((CTabFolder) w[41]).setUnselectedImageVisible(true);
        ((CTabFolder) w[41]).setUnselectedCloseVisible(true);
        ((CTabFolder) w[41]).setOrientation(33554432);
        ((CTabFolder) w[41]).setMinimumCharacters(15);
        ((CTabFolder) w[41]).setTextDirection(33554432);
        ((CTabFolder) w[41]).setEnabled(true);
        ((CTabFolder) w[41]).setHighlightEnabled(true);
        ((CTabFolder) w[41]).setTabPosition(128);
        ((CTabFolder) w[41]).setMaximizeVisible(true);
        ((CTabFolder) w[41]).setBounds(new Rectangle(0, 0, 402, 604));
        ((CTabFolder) w[41]).setBackground(color(72, 72, 76, 255));
        ((CTabFolder) w[41]).setSelectionForeground(color(0, 0, 0, 255));
        ((CTabFolder) w[41]).setVisible(true);
        ((CTabFolder) w[41]).setBorderVisible(true);
        ((CTabFolder) w[41]).setMRUVisible(true);
        ((CTabFolder) w[41]).setDragDetect(true);
        ((CTabFolder) w[41]).setSelectedImageVisible(true);
        ((CTabFolder) w[41]).setForeground(color(187, 187, 187, 255));
        ((CTabFolder) w[41]).setSimple(true);
        ((CTabFolder) w[41]).setTabHeight(24);
        ((CTabFolder) w[41]).setSelectionBarThickness(2);
    }

    private static void s15(Widget[] w) {
        ((Composite) w[40]).setBounds(new Rectangle(0, 0, 402, 604));
        ((Composite) w[40]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[40]).setVisible(true);
        ((Composite) w[40]).setOrientation(33554432);
        ((Composite) w[40]).setDragDetect(true);
        ((Composite) w[40]).setTextDirection(33554432);
        ((Composite) w[40]).setEnabled(true);
        ((Composite) w[40]).setForeground(color(238, 238, 238, 255));
        ((Composite) w[39]).setBounds(new Rectangle(184, 0, 402, 604));
        ((Composite) w[39]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[39]).setVisible(true);
        ((Composite) w[39]).setOrientation(33554432);
        ((Composite) w[39]).setDragDetect(true);
        ((Composite) w[39]).setTextDirection(33554432);
        ((Composite) w[39]).setEnabled(true);
        ((Composite) w[39]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[62]).setToolTipText("Show List");
        ((ToolItem) w[62]).setEnabled(true);
        ((ToolItem) w[62]).setImage(img(34, 18, 24, false, null));
        ((ToolBar) w[61]).setBounds(new Rectangle(38, 2, 40, 24));
        ((ToolBar) w[61]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[61]).setVisible(true);
        ((ToolBar) w[61]).setBackgroundMode(1);
        ((ToolBar) w[61]).setOrientation(33554432);
        ((ToolBar) w[61]).setDragDetect(true);
        ((ToolBar) w[61]).setTextDirection(33554432);
        ((ToolBar) w[61]).setEnabled(true);
        ((ToolBar) w[61]).setForeground(color(0, 0, 0, 255));
        ((ToolItem) w[64]).setToolTipText("Minimize");
        ((ToolItem) w[64]).setEnabled(true);
    }

    private static void s16(Widget[] w) {
        ((ToolItem) w[64]).setImage(img(18, 18, 24, false, null));
        ((ToolItem) w[65]).setToolTipText("Maximize");
        ((ToolItem) w[65]).setEnabled(true);
        ((ToolItem) w[65]).setImage(img(18, 18, 24, false, null));
        ((ToolBar) w[63]).setBounds(new Rectangle(82, 2, 48, 24));
        ((ToolBar) w[63]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[63]).setVisible(true);
        ((ToolBar) w[63]).setBackgroundMode(1);
        ((ToolBar) w[63]).setOrientation(33554432);
        ((ToolBar) w[63]).setDragDetect(true);
        ((ToolBar) w[63]).setTextDirection(33554432);
        ((ToolBar) w[63]).setEnabled(true);
        ((ToolBar) w[63]).setForeground(color(0, 0, 0, 255));
        ((ToolItem) w[68]).setToolTipText("Focus on Active Task (Alt+click to reveal filtered elements)");
        ((ToolItem) w[68]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[68]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[68]).setImage(img(16, 16, 32, false, "focus"));
        ((ToolItem) w[68]).setDisabledImage(img(16, 16, 32, false, "focus-disabled"));
        ((ToolBar) w[67]).setBounds(new Rectangle(0, 0, 24, 22));
        ((ToolBar) w[67]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[67]).setRedraw(true);
        ((ToolBar) w[67]).setVisible(true);
        ((ToolBar) w[67]).setOrientation(33554432);
        ((ToolBar) w[67]).setDragDetect(true);
        ((ToolBar) w[67]).setTextDirection(33554432);
        ((ToolBar) w[67]).setEnabled(true);
        ((ToolBar) w[67]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[70]).setToolTipText("View Menu");
        ((ToolItem) w[70]).setEnabled(true);
        ((ToolItem) w[70]).setForeground(color(238, 238, 238, 255));
    }

    private static void s17(Widget[] w) {
        ((ToolItem) w[70]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[70]).setImage(img(16, 16, 32, true, "view_menu"));
        ((ToolBar) w[69]).setBounds(new Rectangle(24, 0, 24, 22));
        ((ToolBar) w[69]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[69]).setVisible(true);
        ((ToolBar) w[69]).setOrientation(33554432);
        ((ToolBar) w[69]).setDragDetect(true);
        ((ToolBar) w[69]).setTextDirection(33554432);
        ((ToolBar) w[69]).setEnabled(true);
        ((ToolBar) w[69]).setForeground(color(238, 238, 238, 255));
        ((Composite) w[66]).setBounds(new Rectangle(85, 27, 48, 22));
        ((Composite) w[66]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[66]).setRedraw(true);
        ((Composite) w[66]).setVisible(true);
        ((Composite) w[66]).setBackgroundMode(1);
        ((Composite) w[66]).setOrientation(33554432);
        ((Composite) w[66]).setDragDetect(true);
        ((Composite) w[66]).setTextDirection(33554432);
        ((Composite) w[66]).setEnabled(true);
        ((Composite) w[66]).setForeground(color(238, 238, 238, 255));
        ((Label) w[76]).setAlignment(16384);
        ((Label) w[76]).setDragDetect(true);
        ((Label) w[76]).setText("There is no active editor that provides an outline.");
        ((Label) w[76]).setBounds(new Rectangle(5, 5, 124, 562));
        ((Label) w[76]).setBackground(color(47, 47, 47, 255));
        ((Label) w[76]).setTextDirection(33554432);
        ((Label) w[76]).setEnabled(true);
        ((Label) w[76]).setForeground(color(170, 170, 170, 255));
        ((Label) w[76]).setVisible(true);
        ((Label) w[76]).setOrientation(33554432);
    }

    private static void s18(Widget[] w) {
        ((Composite) w[75]).setBounds(new Rectangle(0, 0, 134, 572));
        ((Composite) w[75]).setBackground(color(47, 47, 47, 255));
        ((Composite) w[75]).setVisible(true);
        ((Composite) w[75]).setOrientation(33554432);
        ((Composite) w[75]).setDragDetect(true);
        ((Composite) w[75]).setTextDirection(33554432);
        ((Composite) w[75]).setEnabled(true);
        ((Composite) w[75]).setForeground(color(170, 170, 170, 255));
        ((Composite) w[74]).setBounds(new Rectangle(0, 0, 134, 572));
        ((Composite) w[74]).setBackground(color(47, 47, 47, 255));
        ((Composite) w[74]).setVisible(true);
        ((Composite) w[74]).setOrientation(33554432);
        ((Composite) w[74]).setDragDetect(true);
        ((Composite) w[74]).setTextDirection(33554432);
        ((Composite) w[74]).setEnabled(true);
        ((Composite) w[74]).setForeground(color(170, 170, 170, 255));
        ((Composite) w[73]).setBounds(new Rectangle(0, 0, 134, 572));
        ((Composite) w[73]).setBackground(color(47, 47, 47, 255));
        ((Composite) w[73]).setVisible(true);
        ((Composite) w[73]).setOrientation(33554432);
        ((Composite) w[73]).setDragDetect(true);
        ((Composite) w[73]).setTextDirection(33554432);
        ((Composite) w[73]).setEnabled(true);
        ((Composite) w[73]).setForeground(color(170, 170, 170, 255));
        ((Composite) w[72]).setBounds(new Rectangle(0, 0, 134, 572));
        ((Composite) w[72]).setBackground(color(41, 41, 41, 255));
        ((Composite) w[72]).setRedraw(true);
        ((Composite) w[72]).setVisible(true);
        ((Composite) w[72]).setOrientation(33554432);
        ((Composite) w[72]).setDragDetect(true);
    }

    private static void s19(Widget[] w) {
        ((Composite) w[72]).setTextDirection(33554432);
        ((Composite) w[72]).setEnabled(true);
        ((Composite) w[72]).setForeground(color(221, 221, 221, 255));
        ((Composite) w[71]).setBounds(new Rectangle(0, 32, 134, 572));
        ((Composite) w[71]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[71]).setVisible(true);
        ((Composite) w[71]).setOrientation(33554432);
        ((Composite) w[71]).setDragDetect(true);
        ((Composite) w[71]).setTextDirection(33554432);
        ((Composite) w[71]).setEnabled(true);
        ((Composite) w[71]).setForeground(color(238, 238, 238, 255));
        ((CTabItem) w[77]).setToolTipText("Outline");
        ((CTabItem) w[77]).setText("Outline");
        ((CTabItem) w[77]).setShowClose(true);
        ((CTabItem) w[77]).setImage(img(16, 16, 32, true, "outline_co"));
        ((CTabItem) w[78]).setText("Task List");
        ((CTabItem) w[78]).setShowClose(true);
        ((CTabItem) w[78]).setImage(img(16, 16, 32, true, "task-list"));
        ((CTabFolder) w[60]).setMinimizeVisible(true);
        ((CTabFolder) w[60]).setUnselectedImageVisible(true);
        ((CTabFolder) w[60]).setOrientation(33554432);
        ((CTabFolder) w[60]).setMinimumCharacters(1);
        ((CTabFolder) w[60]).setTextDirection(33554432);
        ((CTabFolder) w[60]).setEnabled(true);
        ((CTabFolder) w[60]).setHighlightEnabled(true);
        ((CTabFolder) w[60]).setTabPosition(128);
        ((CTabFolder) w[60]).setMaximizeVisible(true);
        ((CTabFolder) w[60]).setBounds(new Rectangle(590, 0, 134, 604));
        ((CTabFolder) w[60]).setBackground(color(72, 72, 76, 255));
        ((CTabFolder) w[60]).setSelectionForeground(color(255, 255, 255, 255));
    }

    private static void s20(Widget[] w) {
        ((CTabFolder) w[60]).setVisible(true);
        ((CTabFolder) w[60]).setBorderVisible(true);
        ((CTabFolder) w[60]).setMRUVisible(true);
        ((CTabFolder) w[60]).setDragDetect(true);
        ((CTabFolder) w[60]).setSelectedImageVisible(true);
        ((CTabFolder) w[60]).setForeground(color(187, 187, 187, 255));
        ((CTabFolder) w[60]).setSimple(true);
        ((CTabFolder) w[60]).setTabHeight(24);
        ((CTabFolder) w[60]).setSelectionBarThickness(2);
        ((ToolItem) w[81]).setToolTipText("Show List");
        ((ToolItem) w[81]).setEnabled(true);
        ((ToolItem) w[81]).setImage(img(34, 18, 24, false, null));
        ((ToolBar) w[80]).setBounds(new Rectangle(410, 2, 0, 24));
        ((ToolBar) w[80]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[80]).setVisible(false);
        ((ToolBar) w[80]).setBackgroundMode(1);
        ((ToolBar) w[80]).setOrientation(33554432);
        ((ToolBar) w[80]).setDragDetect(true);
        ((ToolBar) w[80]).setTextDirection(33554432);
        ((ToolBar) w[80]).setEnabled(true);
        ((ToolBar) w[80]).setForeground(color(0, 0, 0, 255));
        ((ToolItem) w[84]).setToolTipText("&Filters...");
        ((ToolItem) w[84]).setEnabled(true);
        ((ToolItem) w[84]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[84]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[84]).setImage(img(16, 16, 32, true, "filter_ps"));
        ((ToolItem) w[84]).setDisabledImage(img(16, 16, 32, true, "filter_ps"));
        ((ToolItem) w[85]).setEnabled(true);
        ((ToolItem) w[85]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[85]).setBackground(color(72, 72, 76, 255));
    }

    private static void s21(Widget[] w) {
        ((ToolItem) w[86]).setToolTipText("Focus on Active Task");
        ((ToolItem) w[86]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[86]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[86]).setImage(img(16, 16, 32, false, "focus"));
        ((ToolItem) w[86]).setDisabledImage(img(16, 16, 32, false, "focus-disabled"));
        ((ToolBar) w[83]).setBounds(new Rectangle(0, 0, 54, 22));
        ((ToolBar) w[83]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[83]).setRedraw(true);
        ((ToolBar) w[83]).setVisible(true);
        ((ToolBar) w[83]).setOrientation(33554432);
        ((ToolBar) w[83]).setDragDetect(true);
        ((ToolBar) w[83]).setTextDirection(33554432);
        ((ToolBar) w[83]).setEnabled(true);
        ((ToolBar) w[83]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[88]).setToolTipText("View Menu");
        ((ToolItem) w[88]).setEnabled(true);
        ((ToolItem) w[88]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[88]).setBackground(color(72, 72, 76, 255));
        ((ToolItem) w[88]).setImage(img(16, 16, 32, true, "view_menu"));
        ((ToolBar) w[87]).setBounds(new Rectangle(54, 0, 24, 22));
        ((ToolBar) w[87]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[87]).setVisible(true);
        ((ToolBar) w[87]).setOrientation(33554432);
        ((ToolBar) w[87]).setDragDetect(true);
        ((ToolBar) w[87]).setTextDirection(33554432);
        ((ToolBar) w[87]).setEnabled(true);
        ((ToolBar) w[87]).setForeground(color(238, 238, 238, 255));
        ((Composite) w[82]).setBounds(new Rectangle(410, 2, 78, 24));
        ((Composite) w[82]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[82]).setRedraw(true);
    }

    private static void s22(Widget[] w) {
        ((Composite) w[82]).setVisible(true);
        ((Composite) w[82]).setBackgroundMode(1);
        ((Composite) w[82]).setOrientation(33554432);
        ((Composite) w[82]).setDragDetect(true);
        ((Composite) w[82]).setTextDirection(33554432);
        ((Composite) w[82]).setEnabled(true);
        ((Composite) w[82]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[90]).setToolTipText("Minimize");
        ((ToolItem) w[90]).setEnabled(true);
        ((ToolItem) w[90]).setImage(img(18, 18, 24, false, null));
        ((ToolItem) w[91]).setToolTipText("Maximize");
        ((ToolItem) w[91]).setEnabled(true);
        ((ToolItem) w[91]).setImage(img(18, 18, 24, false, null));
        ((ToolBar) w[89]).setBounds(new Rectangle(488, 2, 48, 24));
        ((ToolBar) w[89]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[89]).setVisible(true);
        ((ToolBar) w[89]).setBackgroundMode(1);
        ((ToolBar) w[89]).setOrientation(33554432);
        ((ToolBar) w[89]).setDragDetect(true);
        ((ToolBar) w[89]).setTextDirection(33554432);
        ((ToolBar) w[89]).setEnabled(true);
        ((ToolBar) w[89]).setForeground(color(0, 0, 0, 255));
        ((Label) w[94]).setToolTipText("0 items");
        ((Label) w[94]).setAlignment(16384);
        ((Label) w[94]).setDragDetect(true);
        ((Label) w[94]).setText("0 items");
        ((Label) w[94]).setBounds(new Rectangle(0, 0, 540, 20));
        ((Label) w[94]).setBackground(color(47, 47, 47, 255));
        ((Label) w[94]).setTextDirection(33554432);
        ((Label) w[94]).setEnabled(true);
    }

    private static void s23(Widget[] w) {
        ((Label) w[94]).setForeground(color(170, 170, 170, 255));
        ((Label) w[94]).setVisible(true);
        ((Label) w[94]).setOrientation(33554432);
        ((Label) w[95]).setAlignment(16384);
        ((Label) w[95]).setDragDetect(true);
        ((Label) w[95]).setBounds(new Rectangle(0, 20, 540, 0));
        ((Label) w[95]).setBackground(color(47, 47, 47, 255));
        ((Label) w[95]).setTextDirection(33554432);
        ((Label) w[95]).setEnabled(true);
        ((Label) w[95]).setForeground(color(170, 170, 170, 255));
        ((Label) w[95]).setVisible(true);
        ((Label) w[95]).setOrientation(33554432);
        ((TreeColumn) w[98]).setToolTipText("Description");
        ((TreeColumn) w[98]).setAlignment(16384);
        ((TreeColumn) w[98]).setText("Description");
        ((TreeColumn) w[98]).setWidth(300);
        ((TreeColumn) w[98]).setMoveable(true);
        ((TreeColumn) w[98]).setResizable(true);
        ((TreeColumn) w[99]).setToolTipText("Resource");
        ((TreeColumn) w[99]).setAlignment(16384);
        ((TreeColumn) w[99]).setText("Resource");
        ((TreeColumn) w[99]).setWidth(90);
        ((TreeColumn) w[99]).setMoveable(true);
        ((TreeColumn) w[99]).setResizable(true);
        ((TreeColumn) w[100]).setToolTipText("Path");
        ((TreeColumn) w[100]).setAlignment(16384);
        ((TreeColumn) w[100]).setText("Path");
        ((TreeColumn) w[100]).setWidth(120);
        ((TreeColumn) w[100]).setMoveable(true);
        ((TreeColumn) w[100]).setResizable(true);
    }

    private static void s24(Widget[] w) {
        ((TreeColumn) w[101]).setToolTipText("Location");
        ((TreeColumn) w[101]).setAlignment(16384);
        ((TreeColumn) w[101]).setText("Location");
        ((TreeColumn) w[101]).setWidth(90);
        ((TreeColumn) w[101]).setMoveable(true);
        ((TreeColumn) w[101]).setResizable(true);
        ((TreeColumn) w[102]).setToolTipText("Type");
        ((TreeColumn) w[102]).setAlignment(16384);
        ((TreeColumn) w[102]).setText("Type");
        ((TreeColumn) w[102]).setWidth(90);
        ((TreeColumn) w[102]).setMoveable(true);
        ((TreeColumn) w[102]).setResizable(true);
        ((Menu) w[103]).setEnabled(true);
        ((Menu) w[103]).setOrientation(33554432);
        ((Text) w[104]).setFont(font(".AppleSystemUIFont", 11, 0));
        ((Text) w[104]).setBounds(new Rectangle(0, 0, 0, 0));
        ((Text) w[104]).setBackground(color(47, 47, 47, 255));
        ((Text) w[104]).setMessage("");
        ((Text) w[104]).setEditable(true);
        ((Text) w[104]).setVisible(false);
        ((Text) w[104]).setOrientation(33554432);
        ((Text) w[104]).setDragDetect(true);
        ((Text) w[104]).setTextLimit(2147483647);
        ((Text) w[104]).setTextDirection(33554432);
        ((Text) w[104]).setDoubleClickEnabled(true);
        ((Text) w[104]).setEnabled(true);
        ((Text) w[104]).setForeground(color(170, 170, 170, 255));
        ((Text) w[104]).setTabs(8);
        ((Tree) w[97]).setBounds(new Rectangle(0, 0, 540, 66));
        ((Tree) w[97]).setHeaderBackground(color(0, 0, 0, 0));
    }

    private static void s25(Widget[] w) {
        ((Tree) w[97]).setBackground(color(72, 72, 76, 255));
        ((Tree) w[97]).setRedraw(true);
        ((Tree) w[97]).setVisible(true);
        ((Tree) w[97]).setHeaderForeground(color(0, 0, 0, 0));
        ((Tree) w[97]).setOrientation(33554432);
        ((Tree) w[97]).setHeaderVisible(true);
        ((Tree) w[97]).setDragDetect(true);
        ((Tree) w[97]).setSortDirection(128);
        ((Tree) w[97]).setTextDirection(33554432);
        ((Tree) w[97]).setEnabled(true);
        ((Tree) w[97]).setLinesVisible(true);
        ((Tree) w[97]).setForeground(color(170, 170, 170, 255));
        ((Composite) w[96]).setBounds(new Rectangle(0, 20, 540, 66));
        ((Composite) w[96]).setBackground(color(47, 47, 47, 255));
        ((Composite) w[96]).setVisible(true);
        ((Composite) w[96]).setOrientation(33554432);
        ((Composite) w[96]).setDragDetect(true);
        ((Composite) w[96]).setTextDirection(33554432);
        ((Composite) w[96]).setEnabled(true);
        ((Composite) w[96]).setForeground(color(170, 170, 170, 255));
        ((Composite) w[93]).setBounds(new Rectangle(0, 0, 540, 86));
        ((Composite) w[93]).setBackground(color(41, 41, 41, 255));
        ((Composite) w[93]).setRedraw(true);
        ((Composite) w[93]).setVisible(true);
        ((Composite) w[93]).setOrientation(33554432);
        ((Composite) w[93]).setDragDetect(true);
        ((Composite) w[93]).setTextDirection(33554432);
        ((Composite) w[93]).setEnabled(true);
        ((Composite) w[93]).setForeground(color(221, 221, 221, 255));
        ((Composite) w[92]).setBounds(new Rectangle(0, 32, 540, 86));
    }

    private static void s26(Widget[] w) {
        ((Composite) w[92]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[92]).setVisible(true);
        ((Composite) w[92]).setOrientation(33554432);
        ((Composite) w[92]).setDragDetect(true);
        ((Composite) w[92]).setTextDirection(33554432);
        ((Composite) w[92]).setEnabled(true);
        ((Composite) w[92]).setForeground(color(238, 238, 238, 255));
        ((CTabItem) w[105]).setFont(font(".AppleSystemUIFont", 11, 0));
        ((CTabItem) w[105]).setText("Problems");
        ((CTabItem) w[105]).setShowClose(true);
        ((CTabItem) w[105]).setImage(img(16, 16, 32, true, "problems_view"));
        ((CTabItem) w[106]).setText("Target Platform State");
        ((CTabItem) w[106]).setShowClose(true);
        ((CTabItem) w[106]).setImage(img(16, 16, 32, true, "target_profile_xml_obj"));
        ((CTabFolder) w[79]).setMinimizeVisible(true);
        ((CTabFolder) w[79]).setUnselectedImageVisible(true);
        ((CTabFolder) w[79]).setOrientation(33554432);
        ((CTabFolder) w[79]).setMinimumCharacters(1);
        ((CTabFolder) w[79]).setTextDirection(33554432);
        ((CTabFolder) w[79]).setEnabled(true);
        ((CTabFolder) w[79]).setHighlightEnabled(true);
        ((CTabFolder) w[79]).setTabPosition(128);
        ((CTabFolder) w[79]).setMaximizeVisible(true);
        ((CTabFolder) w[79]).setBounds(new Rectangle(184, 608, 540, 118));
        ((CTabFolder) w[79]).setBackground(color(72, 72, 76, 255));
        ((CTabFolder) w[79]).setSelectionForeground(color(255, 255, 255, 255));
        ((CTabFolder) w[79]).setVisible(true);
        ((CTabFolder) w[79]).setBorderVisible(true);
        ((CTabFolder) w[79]).setMRUVisible(true);
        ((CTabFolder) w[79]).setDragDetect(true);
    }

    private static void s27(Widget[] w) {
        ((CTabFolder) w[79]).setSelectedImageVisible(true);
        ((CTabFolder) w[79]).setForeground(color(187, 187, 187, 255));
        ((CTabFolder) w[79]).setSimple(true);
        ((CTabFolder) w[79]).setTabHeight(24);
        ((CTabFolder) w[79]).setSelectionBarThickness(2);
        ((ToolItem) w[109]).setToolTipText("Show List");
        ((ToolItem) w[109]).setEnabled(true);
        ((ToolItem) w[109]).setImage(img(34, 18, 24, false, null));
        ((ToolBar) w[108]).setBounds(new Rectangle(464, 2, 0, 24));
        ((ToolBar) w[108]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[108]).setVisible(false);
        ((ToolBar) w[108]).setBackgroundMode(1);
        ((ToolBar) w[108]).setOrientation(33554432);
        ((ToolBar) w[108]).setDragDetect(true);
        ((ToolBar) w[108]).setTextDirection(33554432);
        ((ToolBar) w[108]).setEnabled(true);
        ((ToolBar) w[108]).setForeground(color(0, 0, 0, 255));
        ((ToolBar) w[111]).setBounds(new Rectangle(0, 0, 24, 22));
        ((ToolBar) w[111]).setBackground(color(41, 41, 41, 255));
        ((ToolBar) w[111]).setRedraw(true);
        ((ToolBar) w[111]).setVisible(true);
        ((ToolBar) w[111]).setOrientation(33554432);
        ((ToolBar) w[111]).setDragDetect(true);
        ((ToolBar) w[111]).setTextDirection(33554432);
        ((ToolBar) w[111]).setEnabled(true);
        ((ToolBar) w[111]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[113]).setToolTipText("View Menu");
        ((ToolItem) w[113]).setEnabled(true);
        ((ToolItem) w[113]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[113]).setBackground(color(72, 72, 76, 255));
    }

    private static void s28(Widget[] w) {
        ((ToolItem) w[113]).setImage(img(16, 16, 32, true, "view_menu"));
        ((ToolBar) w[112]).setBounds(new Rectangle(0, 0, 0, 0));
        ((ToolBar) w[112]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[112]).setVisible(false);
        ((ToolBar) w[112]).setOrientation(33554432);
        ((ToolBar) w[112]).setDragDetect(true);
        ((ToolBar) w[112]).setTextDirection(33554432);
        ((ToolBar) w[112]).setEnabled(true);
        ((ToolBar) w[112]).setForeground(color(238, 238, 238, 255));
        ((Composite) w[110]).setBounds(new Rectangle(464, 2, 24, 24));
        ((Composite) w[110]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[110]).setRedraw(true);
        ((Composite) w[110]).setVisible(true);
        ((Composite) w[110]).setBackgroundMode(1);
        ((Composite) w[110]).setOrientation(33554432);
        ((Composite) w[110]).setDragDetect(true);
        ((Composite) w[110]).setTextDirection(33554432);
        ((Composite) w[110]).setEnabled(true);
        ((Composite) w[110]).setForeground(color(238, 238, 238, 255));
        ((ToolItem) w[115]).setToolTipText("Minimize");
        ((ToolItem) w[115]).setEnabled(true);
        ((ToolItem) w[115]).setImage(img(18, 18, 24, false, null));
        ((ToolItem) w[116]).setToolTipText("Maximize");
        ((ToolItem) w[116]).setEnabled(true);
        ((ToolItem) w[116]).setImage(img(18, 18, 24, false, null));
        ((ToolBar) w[114]).setBounds(new Rectangle(488, 2, 48, 24));
        ((ToolBar) w[114]).setBackground(color(72, 72, 76, 255));
        ((ToolBar) w[114]).setVisible(true);
        ((ToolBar) w[114]).setBackgroundMode(1);
        ((ToolBar) w[114]).setOrientation(33554432);
    }

    private static void s29(Widget[] w) {
        ((ToolBar) w[114]).setDragDetect(true);
        ((ToolBar) w[114]).setTextDirection(33554432);
        ((ToolBar) w[114]).setEnabled(true);
        ((ToolBar) w[114]).setForeground(color(0, 0, 0, 255));
        ((Browser) w[120]).setBounds(new Rectangle(0, 0, 0, 0));
        ((Browser) w[120]).setBackground(color(49, 53, 56, 255));
        ((Browser) w[120]).setUrl("https://www.eclipse.org/setups/notification/java26/?product-id=org.eclipse.epp.package.rcp.product&product-name=Eclipse%20IDE&application-id=org.eclipse.ui.ide.workbench&bundle-id=org.eclipse.epp.package.rcp&bundle-version=4.39.0.20260305-0817&java.vendor=Eclipse%20Adoptium&java.version=21.0.10&color=%23aaaaaaff&background-color=%232f2f2fff");
        ((Browser) w[120]).setVisible(false);
        ((Browser) w[120]).setOrientation(33554432);
        ((Browser) w[120]).setDragDetect(true);
        ((Browser) w[120]).setJavascriptEnabled(true);
        ((Browser) w[120]).setTextDirection(33554432);
        ((Browser) w[120]).setEnabled(true);
        ((Browser) w[120]).setForeground(color(204, 204, 204, 255));
        ((Composite) w[119]).setBounds(new Rectangle(0, 0, 0, 0));
        ((Composite) w[119]).setBackground(color(47, 47, 47, 255));
        ((Composite) w[119]).setVisible(true);
        ((Composite) w[119]).setOrientation(33554432);
        ((Composite) w[119]).setDragDetect(true);
        ((Composite) w[119]).setTextDirection(33554432);
        ((Composite) w[119]).setEnabled(true);
        ((Composite) w[119]).setForeground(color(170, 170, 170, 255));
        ((Composite) w[118]).setBounds(new Rectangle(0, 0, 0, 0));
        ((Composite) w[118]).setBackground(color(41, 41, 41, 255));
        ((Composite) w[118]).setRedraw(true);
        ((Composite) w[118]).setVisible(true);
        ((Composite) w[118]).setOrientation(33554432);
        ((Composite) w[118]).setDragDetect(true);
        ((Composite) w[118]).setTextDirection(33554432);
        ((Composite) w[118]).setEnabled(true);
    }

    private static void s30(Widget[] w) {
        ((Composite) w[118]).setForeground(color(221, 221, 221, 255));
        ((Composite) w[117]).setBounds(new Rectangle(0, 32, 0, 0));
        ((Composite) w[117]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[117]).setVisible(false);
        ((Composite) w[117]).setOrientation(33554432);
        ((Composite) w[117]).setDragDetect(true);
        ((Composite) w[117]).setTextDirection(33554432);
        ((Composite) w[117]).setEnabled(true);
        ((Composite) w[117]).setForeground(color(0, 0, 0, 255));
        ((Browser) w[124]).setBounds(new Rectangle(0, 0, 540, 47));
        ((Browser) w[124]).setBackground(color(49, 53, 56, 255));
        ((Browser) w[124]).setUrl("https://www.eclipse.org/setups/notification/milestone?product-id=org.eclipse.epp.package.rcp.product&product-name=Eclipse%20IDE&application-id=org.eclipse.ui.ide.workbench&bundle-id=org.eclipse.epp.package.rcp&bundle-version=4.39.0.20260305-0817&java.vendor=Eclipse%20Adoptium&java.version=21.0.10&color=%23aaaaaaff&background-color=%232f2f2fff");
        ((Browser) w[124]).setVisible(false);
        ((Browser) w[124]).setOrientation(33554432);
        ((Browser) w[124]).setDragDetect(true);
        ((Browser) w[124]).setJavascriptEnabled(true);
        ((Browser) w[124]).setTextDirection(33554432);
        ((Browser) w[124]).setEnabled(true);
        ((Browser) w[124]).setForeground(color(204, 204, 204, 255));
        ((Composite) w[123]).setBounds(new Rectangle(0, 0, 540, 47));
        ((Composite) w[123]).setBackground(color(47, 47, 47, 255));
        ((Composite) w[123]).setVisible(true);
        ((Composite) w[123]).setOrientation(33554432);
        ((Composite) w[123]).setDragDetect(true);
        ((Composite) w[123]).setTextDirection(33554432);
        ((Composite) w[123]).setEnabled(true);
        ((Composite) w[123]).setForeground(color(170, 170, 170, 255));
        ((Composite) w[122]).setBounds(new Rectangle(0, 0, 540, 47));
        ((Composite) w[122]).setBackground(color(41, 41, 41, 255));
        ((Composite) w[122]).setRedraw(true);
    }

    private static void s31(Widget[] w) {
        ((Composite) w[122]).setVisible(true);
        ((Composite) w[122]).setOrientation(33554432);
        ((Composite) w[122]).setDragDetect(true);
        ((Composite) w[122]).setTextDirection(33554432);
        ((Composite) w[122]).setEnabled(true);
        ((Composite) w[122]).setForeground(color(221, 221, 221, 255));
        ((Composite) w[121]).setBounds(new Rectangle(0, 32, 540, 47));
        ((Composite) w[121]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[121]).setVisible(true);
        ((Composite) w[121]).setOrientation(33554432);
        ((Composite) w[121]).setDragDetect(true);
        ((Composite) w[121]).setTextDirection(33554432);
        ((Composite) w[121]).setEnabled(true);
        ((Composite) w[121]).setForeground(color(238, 238, 238, 255));
        ((CTabItem) w[125]).setText("Notification");
        ((CTabItem) w[125]).setShowClose(true);
        ((CTabItem) w[125]).setImage(img(16, 16, 32, true, "notification"));
        ((CTabItem) w[126]).setText("Notification");
        ((CTabItem) w[126]).setShowClose(true);
        ((CTabItem) w[126]).setImage(img(16, 16, 32, true, "notification"));
        ((CTabFolder) w[107]).setMinimizeVisible(true);
        ((CTabFolder) w[107]).setUnselectedImageVisible(true);
        ((CTabFolder) w[107]).setOrientation(33554432);
        ((CTabFolder) w[107]).setMinimumCharacters(1);
        ((CTabFolder) w[107]).setTextDirection(33554432);
        ((CTabFolder) w[107]).setEnabled(true);
        ((CTabFolder) w[107]).setHighlightEnabled(true);
        ((CTabFolder) w[107]).setTabPosition(128);
        ((CTabFolder) w[107]).setMaximizeVisible(true);
        ((CTabFolder) w[107]).setBounds(new Rectangle(184, 730, 540, 79));
    }

    private static void s32(Widget[] w) {
        ((CTabFolder) w[107]).setBackground(color(41, 41, 41, 255));
        ((CTabFolder) w[107]).setRedraw(true);
        ((CTabFolder) w[107]).setSelectionForeground(color(255, 255, 255, 255));
        ((CTabFolder) w[107]).setVisible(true);
        ((CTabFolder) w[107]).setBorderVisible(true);
        ((CTabFolder) w[107]).setMRUVisible(true);
        ((CTabFolder) w[107]).setDragDetect(true);
        ((CTabFolder) w[107]).setSelectedImageVisible(true);
        ((CTabFolder) w[107]).setForeground(color(221, 221, 221, 255));
        ((CTabFolder) w[107]).setSimple(true);
        ((CTabFolder) w[107]).setTabHeight(24);
        ((CTabFolder) w[107]).setSelectionBarThickness(2);
        ((Composite) w[1]).setBounds(new Rectangle(0, 0, 724, 809));
        ((Composite) w[1]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[1]).setVisible(true);
        ((Composite) w[1]).setOrientation(33554432);
        ((Composite) w[1]).setDragDetect(true);
        ((Composite) w[1]).setTextDirection(33554432);
        ((Composite) w[1]).setEnabled(true);
        ((Composite) w[1]).setForeground(color(238, 238, 238, 255));
        ((Composite) w[0]).setBounds(new Rectangle(0, 0, 724, 809));
        ((Composite) w[0]).setBackground(color(72, 72, 76, 255));
        ((Composite) w[0]).setVisible(true);
        ((Composite) w[0]).setOrientation(33554432);
        ((Composite) w[0]).setDragDetect(true);
        ((Composite) w[0]).setTextDirection(33554432);
        ((Composite) w[0]).setEnabled(true);
        ((Composite) w[0]).setForeground(color(238, 238, 238, 255));
    }

    private static void k0(Widget[] w) {
        ((Tree) w[35]).setMenu((Menu) w[36]);
        ((CTabItem) w[37]).setControl((Control) w[17]);
        ((CTabFolder) w[2]).setTopRight((Control) w[8]);
        ((CTabFolder) w[41]).setTopRight((Control) w[44]);
        ((CTabItem) w[77]).setControl((Control) w[71]);
        ((CTabFolder) w[60]).setTopRight((Control) w[66]);
        ((Tree) w[97]).setMenu((Menu) w[103]);
        ((CTabItem) w[105]).setControl((Control) w[92]);
        ((CTabFolder) w[79]).setTopRight((Control) w[82]);
        ((CTabItem) w[125]).setControl((Control) w[117]);
        ((CTabItem) w[126]).setControl((Control) w[121]);
        ((CTabFolder) w[107]).setTopRight((Control) w[110]);
    }

    private static Color color(int r, int g, int b, int a) { return new Color(r, g, b, a); }

    private static Font font(String name, int height, int style) {
        return new Font(Mocks.device(), name, height, style);
    }

    private static Image img(int width, int height, int depth, boolean alpha, String filename) {
        PaletteData pal = depth == 16 ? new PaletteData(0x7C00, 0x3E0, 0x1F) : new PaletteData(0xFF0000, 0xFF00, 0xFF);
        if (depth != 16 && depth != 24 && depth != 32) depth = 24;
        ImageData data = new ImageData(width, height, depth, pal);
        if (alpha) data.alphaData = new byte[width * height];
        Image image = new Image(Mocks.device(), data);
        if (filename != null) ((DartImage) image.getImpl())._filename(filename);
        return image;
    }
}
