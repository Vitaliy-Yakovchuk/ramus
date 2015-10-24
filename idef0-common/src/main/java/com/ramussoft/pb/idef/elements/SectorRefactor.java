package com.ramussoft.pb.idef.elements;

import static com.ramussoft.pb.data.AbstractSector.equalsStreams;

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.dsoft.utils.DataLoader;
import com.dsoft.utils.DataSaver;
import com.dsoft.utils.Options;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.SectorBorder;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.data.negine.NSector;
import com.ramussoft.pb.data.negine.NSectorBorder;
import com.ramussoft.pb.idef.elements.PaintSector.SplitSectorType;
import com.ramussoft.pb.idef.visual.IDEF0Object;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingLabel;
import com.ramussoft.pb.idef.visual.MovingPanel;
import com.ramussoft.pb.idef.visual.MovingText;

/**
 * Клас для перебудови послідовності точок, при переключенні секторів, для
 * побудови секторів, та переміщені елементів.
 *
 * @author Яковчу В.В.
 */

public class SectorRefactor {

    /**
     * Поточна версія типу даних про сектори відображення.
     */

    public static int BIN_VERSION = 2;

    private Function function = null;

    private final Vector<PaintSector> sectors = new Vector<PaintSector>();

    private final Vector<MovingText> texts = new Vector<MovingText>();

    /**
     * Константа, яка означає початок сектора.
     */

    public static final int TYPE_START = 0;

    /**
     * Константа, яка означає кінець сектора.
     */

    public static final int TYPE_END = 1;

    /**
     * Довжина обрубка.
     */

    public static final double PART_SECTOR_LENGTH = MovingArea.getWidth(Options
            .getInteger("PART_SECTOR_LENGTH", 30));

    /**
     * Посилання на клас з налаштуваннями відображення.
     */

    private MovingArea movingArea = null;

    /**
     * Початкова точка сектора.
     */

    private PerspectivePoint pointA = null;

    /**
     * Кінцева точка сектора.
     */

    private PerspectivePoint pointB = null;

    /**
     * Сектор, над яким можливо здійснювати зміни.
     */

    private PaintSector sector = null;

    /**
     * Значення типу останнього знасення точки.
     */

    private int type = -1;

    private Sector lastC = null;

    private int loadVersion;

    /**
     * Конструктор, по замовчуванню.
     *
     * @param movingArea Посилання наобласть відображення.
     */

    public SectorRefactor(final MovingArea movingArea) {
        this.movingArea = movingArea;
    }

    /**
     * Клас для збереження інформації про потенційну можливу точку.
     *
     * @author ZDD
     */

    public static class PerspectivePoint {

        public PaintSector.Pin pin = null;

        public double x;

        public double y;

        public Point point = null;

        public Crosspoint crosspoint = null;

        public int functionType = -1;

        public int borderType = -1;

        public Function function = null;

        public int pointTypa = SectorBorder.TYPE_SPOT;

        /**
         * TYPE_START, TYPE_END
         */
        public int type;

        public PaintSector sector;

        public void setFunction(final Function function, final int functionType) {
            assert function != null;
            assert functionType >= 0;
            this.function = function;
            this.functionType = functionType;
        }

        public double getX() {
            if (functionType >= 0)
                return point.getX();
            return x;
        }

        public double getY() {
            if (functionType >= 0)
                return point.getY();
            return y;
        }

    }

    ;

    /**
     * Задає значення сектора, для якого необхідно внести зміни.
     *
     * @param sector
     */

    public void setSector(final PaintSector sector) {
        this.sector = sector;
    }

    public static Stream cloneStream(final Stream stream,
                                     DataPlugin dataPlugin, Sector sector) {
        if (stream == null)
            return null;

        if (!stream.isEmptyName())
            return stream;

        final Stream res = (Stream) dataPlugin.createRow(
                dataPlugin.getBaseStream(), true);
        res.setRows(stream.getAdded());
        res.setEmptyName(true);
        return res;
    }

    /**
     * Створює новий сектор по переданим точкам, новостворений сектор можна
     * отримати методом getSector().
     */

    public void createNewSector() {

        Stream stream = null;

        boolean start = true;

        PaintSector fromSector = null;

        if (pointA.pin != null) {
            fromSector = pointA.pin.getSector();
            if (fromSector.getEnd() == null) {
                return;
            }
            if (pointB.pin != null) {
                final Sector s = pointB.pin.getSector().getSector();
                if (s.isConnectedOnFunction(fromSector.getSector()))
                    return;
            }
            stream = fromSector.getStream();
        }

        if (pointB.pin != null) {
            fromSector = pointB.pin.getSector();
            if (fromSector.getStart() == null) {
                return;
            }
            if (stream == null) {
                stream = fromSector.getStream();
                start = false;
            }
        }

        Sector s1 = createMiss(pointA);
        Sector s2 = createMiss(pointB);
        final Sector s = getDataPlugin().createSector();
        s.setFunction(function);
        copyToSectorBorder(s.getStart(), pointA);
        s.getStart().setCrosspointA(pointA.crosspoint);
        s.getStart().commit();
        copyToSectorBorder(s.getEnd(), pointB);
        s.getEnd().setCrosspointA(pointB.crosspoint);
        s.getEnd().commit();
        final PaintSector sector = new PaintSector(s, pointA.point,
                pointB.point, movingArea);
        addSector(sector);
        if (stream != null && fromSector != null) {
            s.setStream(cloneStream(stream, movingArea.dataPlugin, s),
                    ReplaceStreamType.CHILDREN);
            s.loadRowAttributes(fromSector.getSector(), start);
            s.setVisualAttributes(fromSector.getSector().getVisualAttributes());
        }

        if (fromSector != null) {
            if (s1 != null)
                s1.setVisualAttributes(fromSector.getSector()
                        .getVisualAttributes());
            if (s2 != null)
                s2.setVisualAttributes(fromSector.getSector()
                        .getVisualAttributes());
        }
        if (fromSector == null && stream == null
                && (pointA.sector != null || pointB.sector != null)) {
            PaintSector ps = pointA.sector;
            if (ps == null)
                ps = pointB.sector;

            if (s1 != null)
                s1.setVisualAttributes(ps.getSector().getVisualAttributes());
            if (s2 != null)
                s2.setVisualAttributes(ps.getSector().getVisualAttributes());
            if (ps.getStream() != null)
                s.setStream(ps.getStream(), ReplaceStreamType.CHILDREN);
        }

        sector.tryRemovePin(movingArea);
        sector.savePointOrdinates();
        //sector.saveVisual();
        lightSaveToFunction();
        this.sector = sector;
    }

    private Sector createMiss(final PerspectivePoint point) {
        if (point.functionType >= 0) {
            point.crosspoint = getDataPlugin().createCrosspoint();
            double inPos;

            if (point.functionType == MovingPanel.LEFT
                    || point.functionType == MovingPanel.RIGHT)
                inPos = point.point.getY();
            else
                inPos = point.point.getX();

            return createPartIn(point.crosspoint, point.function,
                    point.functionType, inPos, point.type == TYPE_END);
        } else if (point.borderType == -1) {// Tochka na inshomu sektory

            final PaintSector.Pin pin = point.pin;
            final double x = point.x;
            final double y = point.y;

            double oX;
            double oY;
            double opX;
            double opY;
            final boolean start = point.type == TYPE_START;
            if (start) {
                oX = pointA.x;
                oY = pointA.y;
                opX = pointB.getX();
                opY = pointB.getY();
            } else {
                opX = pointA.getX();
                opY = pointA.getY();
                oX = pointB.x;
                oY = pointB.y;
            }

            final Point p = new Point();
            if (pin.isNearStart(x, y) && pin.isFirst()) {
                final Crosspoint crosspoint = pin.getSector().getStart();
                if (crosspoint.isCanAddOut() && point.type == TYPE_START
                        || crosspoint.isCanAddIn() && point.type == TYPE_END) {
                    final int type = pin.getStart().isCanConnected(opX, opY);
                    if (type >= 0) {
                        p.setType(type);
                        p.setXOrdinate(pin.getStart().getXOrdinate());
                        p.setYOrdinate(pin.getStart().getYOrdinate());
                        point.crosspoint = crosspoint;
                        point.point = p;
                        return null;
                    }
                }
            } else if (pin.isNearEnd(x, y) && pin.isEnd()) {
                final Crosspoint crosspoint = pin.getSector().getEnd();
                if (crosspoint.isCanAddOut() && point.type == TYPE_START
                        || crosspoint.isCanAddIn() && point.type == TYPE_END) {
                    final int type = pin.getEnd().isCanConnected(opX, opY);
                    if (type >= 0) {
                        p.setType(type);
                        p.setXOrdinate(pin.getEnd().getXOrdinate());
                        p.setYOrdinate(pin.getEnd().getYOrdinate());
                        point.crosspoint = crosspoint;
                        point.point = p;
                        return null;
                    }
                }
            }

            final PaintSector sector = pin.getSector();

            final SplitSectorType sst = sector.splitSector(pin, new FloatPoint(
                    point.x, point.y), oX, oY, opX, opY, start);

            final PaintSector aSector = sst.paintSector;
            point.crosspoint = sector.getEnd();
            point.point = new Point(sector.getEndPoint().getXOrdinate(), sector
                    .getEndPoint().getYOrdinate());
            point.point.setType(sst.type);
            tryRemoveText(aSector);
            tryRemoveText(sector);
            addSector(aSector);

        } else {// TOCHKA NA KRAU
            createBorderPoints(point, function);
        }
        return null;
    }

    private void createBorderPoints(PerspectivePoint point, Function function) {
        int type = point.borderType;
        Ordinate xO = new Ordinate(Ordinate.TYPE_X);
        Ordinate yO = new Ordinate(Ordinate.TYPE_Y);
        Point p = new Point(xO, yO);

        if (type == MovingPanel.RIGHT) {
            xO.setPosition(movingArea.getDoubleWidth() - MovingArea.PART_SPACE);
            yO.setPosition(point.y);
            p.setType(Ordinate.TYPE_X);
        } else {
            if (type == MovingPanel.LEFT) {
                xO.setPosition(MovingArea.PART_SPACE);
                yO.setPosition(point.y);
                p.setType(Ordinate.TYPE_X);
            } else if (type == MovingPanel.TOP) {
                xO.setPosition(point.x);
                yO.setPosition(MovingArea.PART_SPACE + MovingArea.TOP_PART);
                p.setType(Ordinate.TYPE_Y);
            } else if (type == MovingPanel.BOTTOM) {
                xO.setPosition(point.x);
                yO.setPosition(movingArea.getDoubleHeight()
                        - MovingArea.PART_SPACE - MovingArea.BOTTOM_PART);
                p.setType(Ordinate.TYPE_Y);
            }
        }
        point.point = p;
        /*if (function.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
            double x = xO.getPosition();
			double y = yO.getPosition();

			if (type == MovingPanel.RIGHT) {
				xO.setPosition(movingArea.getDoubleWidth()
						- MovingArea.PART_SPACE
						- Options.getInteger("DEFAULT_DFDS_ROLE_WIDTH", 20));
				yO.setPosition(point.y);
				p.setType(Ordinate.TYPE_X);
				y = yO.getPosition()
						- Options.getInteger("DEFAULT_DFDS_ROLE_HEIGHT", 12)
						/ 2;
				x = xO.getPosition();
			} else {
				if (type == MovingPanel.LEFT) {
					xO.setPosition(MovingArea.PART_SPACE
							+ Options.getInteger("DEFAULT_DFDS_ROLE_WIDTH", 20));
					yO.setPosition(point.y);
					p.setType(Ordinate.TYPE_X);
					y = yO.getPosition()
							- Options
									.getInteger("DEFAULT_DFDS_ROLE_HEIGHT", 12)
							/ 2;
					x = xO.getPosition()
							- Options.getInteger("DEFAULT_DFDS_ROLE_WIDTH", 20);
				} else if (type == MovingPanel.TOP) {
					xO.setPosition(point.x);
					yO.setPosition(MovingArea.PART_SPACE
							+ MovingArea.TOP_PART
							+ Options
									.getInteger("DEFAULT_DFDS_ROLE_HEIGHT", 12));
					p.setType(Ordinate.TYPE_Y);
					x = xO.getPosition()
							- Options.getInteger("DEFAULT_DFDS_ROLE_WIDTH", 20)
							/ 2;
					y = yO.getPosition()
							- Options
									.getInteger("DEFAULT_DFDS_ROLE_HEIGHT", 12);
				} else if (type == MovingPanel.BOTTOM) {
					xO.setPosition(point.x);
					yO.setPosition(movingArea.getDoubleHeight()
							- MovingArea.PART_SPACE
							- MovingArea.BOTTOM_PART
							- Options
									.getInteger("DEFAULT_DFDS_ROLE_HEIGHT", 12));
					p.setType(Ordinate.TYPE_Y);
					x = xO.getPosition()
							- Options.getInteger("DEFAULT_DFDS_ROLE_WIDTH", 20)
							/ 2;
					y = yO.getPosition();
				}
			}

			point.functionType = MovingPanel.getOpposite(point.borderType);
			point.borderType = -1;
			point.crosspoint = getDataPlugin().createCrosspoint();

			point.function = movingArea.createFunctionalObject(x, y,
					Function.TYPE_DFDS_ROLE, function);
			movingArea.setPanels();

		} else*/
        {
            if (point.crosspoint == null)
                point.crosspoint = getDataPlugin().createCrosspoint();
        }

    }

    private boolean isBorder(final SectorBorder border) {
        return border.getFunctionType() >= 0 || border.getBorderType() >= 0;
    }

    private void tryRemoveText(final PaintSector s) {
        if (s.getStream() == null)
            return;
        if (s.getAlternativeText() != null || !s.getStream().isEmptyName())
            return;
        if (isBorder(s.getSector().getStart())
                || isBorder(s.getSector().getEnd()))
            return;
        s.setShowText(false);
    }

    private void copyToSectorBorder(NSectorBorder border, PerspectivePoint point) {
        border.setBorderTypeA(point.borderType);
        border.setFunctionA(point.function);
        border.setFunctionTypeA(point.functionType);
    }

    /**
     * Змінює сектор переданий методом setSector(Sector sector).
     *
     * @return <code>true</code>, якщо зміна відбулась,<br>
     * <code>false</code>, якщо зміна не відбулась.
     */

    public boolean changeSector() {
        Crosspoint crosspoint;
        final PerspectivePoint point = getLastPoint();
        if (point.functionType == -1 && point.borderType == -1) {
            if (point.pin.getSector().getStart() == null
                    || point.pin.getSector().getEnd() == null
                    || point.pin.getSector().equals(sector))
                return false;
        }

        Sector inner = sector.getSector();
        for (PaintSector s : sectors)
            if (s.getSector().equals(inner)) {
                this.sector = s;
                break;
            }

        lastC = null;
        NSectorBorder sb;
        if (point.type == TYPE_START) {
            sb = sector.getSector().getStart();
            crosspoint = sector.getStart();
            pointB = new PerspectivePoint();
            pointB.x = sector.getEndPoint().getX();
            pointB.y = sector.getEndPoint().getY();
        } else {
            sb = sector.getSector().getEnd();
            crosspoint = sector.getEnd();
            pointA = new PerspectivePoint();
            pointA.x = sector.getStartPoint().getX();
            pointA.y = sector.getStartPoint().getY();
        }

        List<Sector> oldConnectedSectors = new ArrayList<Sector>();
        if (crosspoint != null) {
            for (Sector s : crosspoint.getIns())
                if (s != sector.getSector())
                    oldConnectedSectors.add(s);
            for (Sector s : crosspoint.getOuts())
                if (s != sector.getSector())
                    oldConnectedSectors.add(s);
        }

        Sector s = null;
        boolean createMiss = true;
        if (point.function != null) {
            Function oldF = sb.getFunction();
            if (oldF != null && oldF.equals(point.function)) {
                if (function.getDecompositionType() >= 0
                        || point.function.getType() >= Function.TYPE_EXTERNAL_REFERENCE)
                    createMiss = false;
                else {
                    createMiss = point.functionType != sb.getFunctionType();
                }
            }
        }
        if (createMiss)
            s = createMiss(point);
        else
            point.crosspoint = sb.getCrosspoint();

        if (s != null)
            s.setVisualAttributes(sector.getSector().getVisualAttributes());

        if (type == TYPE_START) {
            copyToSectorBorder(sb, pointA);
            sector.setStart(point.crosspoint, point.point, createMiss);
            sector.getSector().getStart().commit();
        } else {
            copyToSectorBorder(sector.getSector().getEnd(), pointB);
            sector.setEnd(point.crosspoint, point.point, createMiss);
            sector.getSector().getEnd().commit();
        }
        if (sector.getStream() != null) {
            if (lastC != null)
                lastC.setStream(
                        cloneStream(sector.getStream(), movingArea.dataPlugin,
                                lastC), ReplaceStreamType.CHILDREN);
        }
        sector.setCorrectRows();
        sector.regeneratePoints();
        sector.tryRemovePin(movingArea);
        Hashtable<Long, Sector> sectorHash = movingArea.getDataPlugin()
                .getSectorHash();

        for (Sector sector : oldConnectedSectors) {
            if (sectorHash.get(((NSector) sector).getElementId()) != null) {
                fixOwners(sector, getDataPlugin());
            }
        }
        saveToFunction();
        return true;
    }

    public void setUndoPoint() {
        if (movingArea.getPanel() == null)
            return;
        if (movingArea.isUserTransactionStarted()) {
            movingArea.getPanel().getFrame().refreshActions(null);
            lightSaveToFunction();
            movingArea.commitUserTransaction();
        } else {
            // byte[] bs = function.getSectorData();
            // byte[] bs2 = getSectorData();
            // if (!Arrays.equals(bs, bs2)) {
            movingArea.startUserTransaction();

            saveToFunction();

            movingArea.commitUserTransaction();
            // }
        }
    }

    public void setUndoPointSaveAll() {
        if (movingArea.getPanel() == null)
            return;
        if (movingArea.isUserTransactionStarted()) {
            movingArea.getPanel().getFrame().refreshActions(null);
            saveToFunction();
            movingArea.commitUserTransaction();
        } else {
            // byte[] bs = function.getSectorData();
            // byte[] bs2 = getSectorData();
            // if (!Arrays.equals(bs, bs2)) {
            movingArea.startUserTransaction();

            saveToFunction();

            movingArea.commitUserTransaction();
            // }
        }
    }

    public PerspectivePoint getPoint(final int type) {
        if (type == TYPE_START)
            return pointA;
        else
            return pointB;
    }

    /**
     * Повертає значення останньої створеної PerspectivePoint.
     *
     * @return Повертає pointA або pointB, в залежності від типу зміни, яка
     * відбувається.
     */

    private PerspectivePoint getLastPoint() {
        return getPoint(type);
    }

    /**
     * Метод, який завантажує секторну структуру функціонального блоку, якщо
     * інформація вже раніше була завантажена з функціонального блоку, то вона
     * автоматично в нього зберігається.
     *
     * @param function Функціональний блок, з якого завантажаться дані.
     * @param save     Параметр, який вказує, чи зберігати останню завантажену
     *                 функцію.
     */

    public void loadFromFunction(final Function function, final boolean save) {
        if (save && this.function != null)
            saveToFunction(this.function);

        sectors.clear();
        texts.clear();
        this.function = function;
        if (function == null)
            return;
        ((NFunction) function).clearSectorsBuffer();
        final byte[] bs = function.getSectorData();
        if (bs == null)
            return;
        final ByteArrayInputStream in = new ByteArrayInputStream(bs);

        try {
            final DataLoader.MemoryData memoryData = new DataLoader.MemoryData();
            final int ver = DataLoader.readInteger(in);// read version
            this.loadVersion = ver;
            if (ver < 2 && ver > 0) {
                int n = DataLoader.readInteger(in);
                for (int i = 0; i < n; i++) {
                    final PaintSector sector = PaintSector.loadFromStream(in,
                            ver, movingArea, memoryData, getDataPlugin());
                    if (sector != null)
                        addSector(sector);
                }
            } else {
                for (Sector sector : function.getSectors()) {
                    final PaintSector paintSector = PaintSector.loadFromSector(
                            ver, movingArea, memoryData, getDataPlugin(),
                            sector);
                    if (paintSector != null) {
                        if (sector.getCreateState() == Sector.STATE_IN)
                            createInPaintPart(sector);
                        else if (sector.getCreateState() == Sector.STATE_OUT)
                            createOutPaintPart(sector);
                        else
                            addSector(paintSector);
                    }
                }
            }

            int n = DataLoader.readInteger(in);
            for (int i = 0; i < n; i++) {
                final MovingText text = movingArea.createText();
                text.setFont(DataLoader.readFont(in, memoryData));
                text.setColor(DataLoader.readColor(in, memoryData));
                text.setBounds(DataLoader.readFRectangle(in));
                text.setText(DataLoader.readString(in));
                texts.add(text);
            }
            in.close();
            HashSet<PaintSector> hashSet = new HashSet<PaintSector>();
            for (int i = 0; i < getSectorsCount(); i++) {
                getSector(i).createTexts(hashSet);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public Function getFunction() {
        return function;
    }

    /**
     * Метод зберігає секторну структуру в функціональний блок, з якого була
     * завантажена інформація про сектори.
     */

    public void saveToFunction() {
        if (function != null) {
            saveToFunction(function);
            function.setSystemRevDate(new Date());
            function.setRevDate(new Date());
        }
    }

    public void lightSaveToFunction() {
        if (loadVersion < 2) {
            saveToFunction();
            return;
        }
        if (function != null) {
            lightSaveToFunction(function);
            function.setSystemRevDate(new Date());
            function.setRevDate(new Date());
        }
    }

    public byte[] getSectorData() {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final DataLoader.MemoryData memoryData = new DataLoader.MemoryData();
            DataSaver.saveInteger(out, BIN_VERSION);
            for (int i = 0; i < getSectorsCount(); i++) {
                final PaintSector sector = getSector(i);
                PaintSector.save(sector, memoryData, getDataPlugin()
                        .getEngine());
            }
            DataSaver.saveInteger(out, texts.size());
            for (int i = 0; i < texts.size(); i++) {
                DataSaver.saveFont(out, getText(i).getFont(), memoryData);
                DataSaver.saveColor(out, getText(i).getColor(), memoryData);
                DataSaver.saveFRectangle(out, getText(i).getBounds());
                DataSaver.saveString(out, getText(i).getText());
            }
            return out.toByteArray();
        } catch (final IOException e) {
            return null;
        }
    }

    public byte[] getLightSectorData() {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final DataLoader.MemoryData memoryData = new DataLoader.MemoryData();
            DataSaver.saveInteger(out, BIN_VERSION);

            DataSaver.saveInteger(out, texts.size());
            for (int i = 0; i < texts.size(); i++) {
                DataSaver.saveFont(out, getText(i).getFont(), memoryData);
                DataSaver.saveColor(out, getText(i).getColor(), memoryData);
                DataSaver.saveFRectangle(out, getText(i).getBounds());
                DataSaver.saveString(out, getText(i).getText());
            }
            return out.toByteArray();
        } catch (final IOException e) {
            return null;
        }
    }

    public void saveToFunction(Function function) {
        if (function == null)
            function = getDataPlugin().getBaseFunction();

        byte[] data = getSectorData();

        function.setSectorData(data);
    }

    public void lightSaveToFunction(Function function) {
        if (function == null)
            function = getDataPlugin().getBaseFunction();

        byte[] data = getLightSectorData();
        byte[] bs = function.getSectorData();
        if (Arrays.equals(bs, data))
            return;

        function.setSectorData(data);
    }

    public int getSectorsCount() {
        return sectors.size();
    }

    public Vector<PaintSector> getSectors() {
        return sectors;
    }

    public PaintSector getSector(final int i) {
        return sectors.get(i);
    }

    public void addSector(final PaintSector res) {
        if (res.getSector() != null)
            sectors.add(res);
    }

    /**
     * Медод повертає набір секторів, протилежних до точки перетену.
     *
     * @param sector Сектор відображення, для якого необхідно знайти протилежні.
     * @param point  Точка - перхрестя.
     * @return Масив секторів, які є пролежними до переданого.
     */

    public PaintSector[] getOppozite(final PaintSector sector,
                                     final Crosspoint point) {
        final Sector[] sectors = point.getOppozite(sector.getSector());
        final Vector<PaintSector> r = new Vector<PaintSector>();
        for (int i = 0; i < getSectorsCount(); i++) {
            final PaintSector s = getSector(i);
            final Sector sec = s.getSector();
            for (final Sector element : sectors)
                if (sec.equals(element))
                    r.add(s);
        }
        final PaintSector[] rs = new PaintSector[r.size()];
        for (int i = 0; i < rs.length; i++)
            rs[i] = r.get(i);
        return rs;
    }

    public void removeSector(final PaintSector sector) {
        Iterator<PaintSector> i = sectors.iterator();
        while (i.hasNext()) {
            if (((NSector) i.next().getSector()).getElementId() == ((NSector) sector
                    .getSector()).getElementId())
                i.remove();
        }
    }

    private boolean isIn(final PaintSector sector, final Sector[] sectors) {
        final Sector s = sector.getSector();
        for (final Sector element : sectors)
            if (s.equals(element))
                return true;
        return false;
    }

    public void getStreamedSectors(final Sector sector, final Crosspoint point,
                                   final HashSet v, final boolean streamed) {
        if (point == null)
            return;
        final Sector[] sectors = point.getOppozite(sector);
        for (int i = 0; i < getSectorsCount(); i++) {
            final PaintSector s = getSector(i);
            if (isIn(s, sectors)) {
                if (streamed)
                    if (sector.getStream() == null
                            || !equalsStreams(sector.getStream(), s.getStream()))
                        continue;
                if (!v.contains(s)
                        && s.getFunction().equals(sector.getFunction())) {
                    v.add(s);
                    getStreamedSectors(s, v);
                }
            }
        }
    }

    /**
     * Додає в масив сектори відображення, які пов’язані між собою одним потоком
     * і знаходяться на одному функціональному блоці.
     *
     * @param sector - сектор, для якого будуть знайдені пов’язані.
     * @param v      Вектор в який будуть додані сектори відображення.
     */

    public void getStreamedSectors(final PaintSector sector, final HashSet v) {
        getStreamedSectors(sector.getSector(), sector.getStart(), v, true);
        getStreamedSectors(sector.getSector(), sector.getEnd(), v, true);
        if (!v.contains(sector))
            v.add(sector);
    }

    /**
     * Метод повертає всі пов’язані сектори на функціонпальному блоці.
     *
     * @param v Вектор, кути будуть додані пов’язані функціональні блоки.
     */

    public void getConnectedOnFunction(final HashSet v) {
        getStreamedSectors(sector.getSector(), sector.getStart(), v, false);
        getStreamedSectors(sector.getSector(), sector.getEnd(), v, false);
    }

    public void setPoint(final PerspectivePoint pp) {
        if (pp.x < 5)
            pp.x = 5;
        if (pp.x > movingArea.CLIENT_WIDTH - 5)
            pp.x = movingArea.CLIENT_WIDTH - 5;
        if (pp.y < 5)
            pp.y = 5;
        if (pp.y > movingArea.CLIENT_HEIGHT - 5)
            pp.y = movingArea.CLIENT_HEIGHT - 5;

        if (pp.type == TYPE_START)
            pointA = pp;
        else
            pointB = pp;
        type = pp.type;
    }

    public PaintSector getSector() {
        return sector;
    }

    /**
     * Медод, який повертає значення x останньою точки яка була змінена.
     */

    public double getX() {
        PerspectivePoint last;
        if (movingArea.getRealState() == MovingArea.END_POINT_ADDING)
            last = getLastPoint();
        else {
            if (movingArea.getPointChangingType() == TYPE_START)
                return sector.getEndPoint().getX();
            else
                return sector.getStartPoint().getX();
        }
        if (last.point == null)
            return last.x;
        else
            return last.point.getX();
    }

    /**
     * Метод, який повертає значення y останньою точки яка була змінена.
     */

    public double getY() {
        PerspectivePoint last;
        if (movingArea.getRealState() == MovingArea.END_POINT_ADDING)
            last = getLastPoint();
        else {
            if (movingArea.getPointChangingType() == TYPE_START)
                return sector.getEndPoint().getY();
            else
                return sector.getStartPoint().getY();
        }
        if (last.point == null)
            return last.y;
        else
            return last.point.getY();
    }

    /**
     * Метод тунулює сектор.
     *
     * @param sector Сектор, який необхідно протунулювати.
     * @param start  <code>true</code>, якщо тунулюється початок,
     *               <code>false</code>, якщо тунулюється кінець.
     * @return
     */

    public Sector createSectorOnIn(final PaintSector sector, final boolean start) {
        if (start) {
            return createSectorOnIn(sector.getStart(), sector.getSector()
                    .getStart(), sector, start);
        } else {
            return createSectorOnIn(sector.getEnd(), sector.getSector()
                    .getEnd(), sector, start);
        }
    }

    public Sector createSectorOnIn(final Crosspoint point,
                                   final SectorBorder border, final PaintSector sector, boolean start) {
        Function function = border.getFunction();
        int borderType = border.getBorderType();
        if (function != null && function.getType() == Function.TYPE_DFDS_ROLE) {
            function = null;
            borderType = MovingText.getOpposite(border.getFunctionType());
        }
        double pos;
        Sector s;
        Point bPoint;

        Function par = sector.getFunction();

        if (start)
            bPoint = sector.getStartPoint();
        else
            bPoint = sector.getEndPoint();

        if (function != null) {

            if ((border.getFunctionType() == MovingPanel.RIGHT)
                    || (border.getFunctionType() == MovingPanel.LEFT))
                pos = bPoint.getY() / movingArea.CLIENT_HEIGHT
                        * par.getBounds().getHeight() + par.getBounds().getY();
            else
                pos = bPoint.getX() / movingArea.CLIENT_WIDTH
                        * par.getBounds().getWidth() + par.getBounds().getX();
            s = createPartIn(point, function, border.getFunctionType(), pos,
                    !start);
        } else {
            if ((borderType == MovingPanel.RIGHT)
                    || (borderType == MovingPanel.LEFT))
                pos = bPoint.getY() / movingArea.CLIENT_HEIGHT
                        * par.getBounds().getHeight() + par.getBounds().getY();
            else
                pos = sector.getStartPoint().getX() / movingArea.CLIENT_WIDTH
                        * par.getBounds().getWidth() + par.getBounds().getX();
            s = createPartOut(point, this.function, borderType, pos, !start);
        }
        if (s != null) {
            s.setStream(
                    cloneStream(sector.getSector().getStream(),
                            movingArea.dataPlugin, s),
                    ReplaceStreamType.CHILDREN);
            ((NSector) s).storeData();
            s.setVisualAttributes(s.getVisualAttributes());
        }
        return s;
    }

    /**
     * Метод свторює сектор зверху на функціональному блоці.
     *
     * @param point        Точка на краю активного функціоналоноко блоку (констук якого
     *                     показується).
     * @param function     Активний функціональний блок.
     * @param functionType Край, на якому знаходиться тока.
     */

    private Sector createPartOut(final Crosspoint point,
                                 final Function function, final int functionType,
                                 final double outPos, boolean start) {
        if (functionType < 0) {
            JOptionPane.showMessageDialog(null, "Не виправлена "
                    + "помилка тунулювання.");
            return null;
        }
        final Function par = (Function) function.getParent();
        if (par == null)
            return null;

        //PerspectivePoint ppPoint = null;
        final Sector sector = getDataPlugin().createSector();
        sector.setFunction(par);

	/*	if (par.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
			ppPoint = new PerspectivePoint();
			ppPoint.x = function.getBounds().getX() + outPos
					/ movingArea.CLIENT_WIDTH * function.getBounds().getWidth();
			ppPoint.y = outPos;
			ppPoint.borderType = functionType;
			createBorderPoints(ppPoint, par);
		}*/

        final Crosspoint op = getDataPlugin().createCrosspoint();
        if (start) {
            sector.getStart().setCrosspointA(point);
            sector.getStart().setFunctionA(function);
            sector.getStart().setFunctionTypeA(functionType);
            sector.getEnd().setCrosspointA(op);
			/*if (par.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
				sector.getEnd().setFunctionA(ppPoint.function);
				sector.getEnd().setFunctionTypeA(
						MovingPanel.getOpposite(functionType));
			} else*/
            sector.getEnd().setBorderTypeA(functionType);
        } else {
            sector.getEnd().setCrosspointA(point);
            sector.getEnd().setFunctionA(function);
            sector.getEnd().setFunctionTypeA(functionType);
            sector.getStart().setCrosspointA(op);
			/*if (par.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
				sector.getStart().setFunctionA(ppPoint.function);
				sector.getStart().setFunctionTypeA(
						MovingPanel.getOpposite(functionType));
			} else*/
            sector.getStart().setBorderTypeA(functionType);
        }
        sector.getStart().commit();
        sector.getEnd().commit();
        PaintSector.save(createOutPaintPart(sector, outPos),
                new DataLoader.MemoryData(), movingArea.getDataPlugin()
                        .getEngine());
        return sector;
    }

    /**
     * Метод створює сектор всередені функціонального блоку і зазаначає, що для
     * нього не має сектору відображення, тому такий буде створено автоматично
     * згодом.
     *
     * @param point      Точка в яку буде під’єднаний сектор - обрубок.
     * @param borderType сторона з якої виходить, входить сектор
     */

    public Sector createPartIn(final Crosspoint point, final Function function,
                               final int borderType, final double inPos, boolean start) {
		/*if (this.function.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS
				&& function.getType() < Function.TYPE_EXTERNAL_REFERENCE) {
			final Sector sector = getDataPlugin().createSector();
			double dX;
			double dY;
			Point a;
			Point b;
			if (borderType == MovingText.RIGHT) {
				dX = movingArea.CLIENT_WIDTH - 40;
				dY = inPos;
			} else if (borderType == MovingText.LEFT) {
				dX = 20;
				dY = inPos;
			} else if (borderType == MovingText.BOTTOM) {
				dY = movingArea.CLIENT_HEIGHT - 40;
				dX = inPos;
			} else {
				dY = 20;
				dX = inPos;
			}
			Function function2 = movingArea.createFunctionalObject(dX, dY,
					Function.TYPE_DFDS_ROLE, function);
			FRectangle bs = function2.getBounds();

			if (borderType == MovingText.RIGHT) {
				dX = bs.getLeft();
				dY = bs.getCenter().getY();
				a = new Point(new Ordinate(Ordinate.TYPE_X), new Ordinate(
						Ordinate.TYPE_Y));
				a.setX(dX);
				a.setY(dY);
				b = new Point(new Ordinate(Ordinate.TYPE_X), a.getYOrdinate());
				b.setX(dX - PART_SECTOR_LENGTH);
			} else if (borderType == MovingText.LEFT) {
				dX = bs.getRight();
				dY = bs.getCenter().getY();
				a = new Point(new Ordinate(Ordinate.TYPE_X), new Ordinate(
						Ordinate.TYPE_Y));
				a.setX(dX);
				a.setY(dY);
				b = new Point(new Ordinate(Ordinate.TYPE_X), a.getYOrdinate());
				b.setX(dX + PART_SECTOR_LENGTH);
			} else if (borderType == MovingText.BOTTOM) {
				dX = bs.getCenter().getX();
				dY = bs.getTop();
				a = new Point(new Ordinate(Ordinate.TYPE_X), new Ordinate(
						Ordinate.TYPE_Y));
				a.setX(dX);
				a.setY(dY);
				b = new Point(a.getXOrdinate(), new Ordinate(Ordinate.TYPE_Y));
				b.setY(dY - PART_SECTOR_LENGTH);
			} else {
				dX = bs.getCenter().getX();
				dY = bs.getBottom();
				a = new Point(new Ordinate(Ordinate.TYPE_X), new Ordinate(
						Ordinate.TYPE_Y));
				a.setX(dX);
				a.setY(dY);
				b = new Point(a.getXOrdinate(), new Ordinate(Ordinate.TYPE_Y));
				b.setY(dY + PART_SECTOR_LENGTH);
			}
			PaintSector paintSector;
			if (start) {
				sector.getStart().setFunctionTypeA(
						MovingText.getOpposite(borderType));
				sector.getStart().setFunctionA(function2);
				sector.getStart().setCrosspointA(point);
				sector.getStart().commit();
				paintSector = new PaintSector(movingArea);
				paintSector.setPoints(new Point[] { a, b });
			} else {
				sector.getEnd().setFunctionTypeA(
						MovingText.getOpposite(borderType));
				sector.getEnd().setFunctionA(function2);
				sector.getEnd().setCrosspointA(point);
				sector.getEnd().commit();
				paintSector = new PaintSector(movingArea);
				paintSector.setPoints(new Point[] { b, a });
			}
			sector.setFunction(function);
			paintSector.setSector(sector);
			PaintSector.save(paintSector, new DataLoader.MemoryData(),
					movingArea.getDataPlugin().getEngine());
			lastC = sector;

			return sector;
		} else */
        {
            final Sector sector = getDataPlugin().createSector();
            sector.setCreateState(Sector.STATE_IN, inPos);
            if (start) {
                sector.getStart().setBorderTypeA(borderType);
                sector.getStart().setCrosspointA(point);
                sector.getStart().commit();
            } else {
                sector.getEnd().setBorderTypeA(borderType);
                sector.getEnd().setCrosspointA(point);
                sector.getEnd().commit();
            }
            sector.setFunction(function);
            PaintSector.save(createInPaintPart(sector, inPos),
                    new DataLoader.MemoryData(), movingArea.getDataPlugin()
                            .getEngine());
            lastC = sector;
            return sector;
        }
    }

    private PaintSector createInPaintPart(final Sector sector, double position) {
        if (sector.getStart().getCrosspoint() == null) {// -->

            final Point a = new Point();
            final Point b = new Point();
            switch (sector.getEnd().getBorderType()) {
                case MovingPanel.TOP: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(position);
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setY(MovingArea.PART_SPACE);
                    a.setY(MovingArea.PART_SPACE + PART_SECTOR_LENGTH);
                }
                break;
                case MovingPanel.BOTTOM: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(position);
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE);
                    a.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE
                            - PART_SECTOR_LENGTH);
                }
                break;
                case MovingPanel.RIGHT: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(position);
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(movingArea.getDoubleWidth() - PART_SECTOR_LENGTH
                            - MovingArea.PART_SPACE);
                    b.setX(movingArea.getDoubleWidth() - MovingArea.PART_SPACE);
                }
                break;
                case MovingPanel.LEFT: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(position);
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(PART_SECTOR_LENGTH + MovingArea.PART_SPACE);
                    b.setX(MovingArea.PART_SPACE);
                }
                break;
            }

            // sector.setVisualAttributes(opps[0].getVisualAttributes());
            final PaintSector ps = new PaintSector(sector, a, b, movingArea);
            return ps;
        } else {
            final Point a = new Point();
            final Point b = new Point();
            switch (sector.getStart().getBorderType()) {
                case MovingPanel.TOP: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(position);
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(MovingArea.PART_SPACE);
                    b.setY(MovingArea.PART_SPACE + PART_SECTOR_LENGTH);
                }
                break;
                case MovingPanel.BOTTOM: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(position);
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE);
                    b.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE
                            - PART_SECTOR_LENGTH);
                }
                break;
                case MovingPanel.RIGHT: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(position);
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(movingArea.getDoubleWidth() - MovingArea.PART_SPACE);
                    b.setX(movingArea.getDoubleWidth() - MovingArea.PART_SPACE
                            - PART_SECTOR_LENGTH);
                }
                break;
                case MovingPanel.LEFT: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(position);
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(MovingArea.PART_SPACE);
                    b.setX(MovingArea.PART_SPACE + PART_SECTOR_LENGTH);
                }
                break;
            }
            // sector.setVisualAttributes(opps[0].getVisualAttributes());
            final PaintSector ps = new PaintSector(sector, a, b, movingArea);
            return ps;
        }
    }

    /**
     * Метод, який створює відображення для сектора, для якого відображення не
     * має, це стосується частин і т. д.
     *
     * @param sector Сектор, для якого буде створено відображення.
     */

    @SuppressWarnings("unused")
    private void createPainted(final Sector sector) {
        try {
            if (sector.getCreateState() == Sector.STATE_IN)
                createInPaintPart(sector);
            else if (sector.getCreateState() == Sector.STATE_OUT)
                createOutPaintPart(sector);
        } catch (NullPointerException e) {
            sector.remove();
            e.printStackTrace();
        }
    }

    /**
     * Метод, який створює відображення для сектора на верху.
     *
     * @param sector Сектор, для якого буде створено відображення.
     */

    private void createOutPaintPart(final Sector sector) {
        final double pos = sector.getCreatePos();
        final Point a = new Point();
        final Point b = new Point();
        final FloatPoint p = new FloatPoint();

        boolean start = sector.getStart().getType() == SectorBorder.TYPE_BORDER;
        int endBorderType = sector.getEnd().getBorderType();
        int startBorderType = sector.getStart().getBorderType();

		/*FRectangle role = null;

		if (function.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
			start = false;
			if (sector.getStart().getFunction() != null
					&& sector.getStart().getFunction().getType() == Function.TYPE_DFDS_ROLE) {
				start = true;
				startBorderType = MovingPanel.getOpposite(sector.getStart()
						.getFunctionType());
				role = sector.getStart().getFunction().getBounds();
			} else {
				endBorderType = MovingPanel.getOpposite(sector.getEnd()
						.getFunctionType());
				role = sector.getEnd().getFunction().getBounds();
			}
		}*/

        if (start) {
            switch (startBorderType) {
                case MovingPanel.TOP: {
                    p.setX(pos);
                    final FloatPoint st = getOutCoordinate(sector.getEnd()
                            .getFunction(), p, MovingPanel.TOP);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(st.getX());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(MovingArea.PART_SPACE);
                    b.setY(st.getY());
                }
                break;
                case MovingPanel.BOTTOM: {
                    p.setX(pos);
                    final FloatPoint st = getOutCoordinate(sector.getEnd()
                            .getFunction(), p, MovingPanel.BOTTOM);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(st.getX());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE);
                    b.setY(st.getY());
                }
                break;
                case MovingPanel.LEFT: {
                    p.setY(pos);
                    final FloatPoint st = getOutCoordinate(sector.getEnd()
                            .getFunction(), p, MovingPanel.LEFT);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(st.getY());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(MovingArea.PART_SPACE);
                    b.setX(st.getX());
                }
                break;
                default: {
                    p.setY(pos);
                    final FloatPoint st = getOutCoordinate(sector.getEnd()
                            .getFunction(), p, MovingPanel.RIGHT);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(st.getY());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setX(st.getX());
                    a.setX(movingArea.getDoubleWidth() - MovingArea.PART_SPACE);
                }
                break;
            }
			/*if (function.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
				switch (startBorderType) {
				case MovingPanel.TOP: {
					a.setY(role.getBottom());
					a.setX(role.getCenter().getX());
				}
					break;
				case MovingPanel.BOTTOM: {
					a.setY(role.getTop());
					a.setX(role.getCenter().getX());
				}
					break;
				case MovingPanel.LEFT: {
					a.setY(role.getCenter().getY());
					a.setX(role.getRight());
				}
					break;
				default: {
					a.setY(role.getCenter().getY());
					a.setX(role.getLeft());
				}
					break;
				}
			}*/
        } else {
            switch (endBorderType) {
                case MovingPanel.RIGHT: {
                    p.setY(pos);
                    final FloatPoint st = getOutCoordinate(sector.getStart()
                            .getFunction(), p, MovingPanel.RIGHT);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(st.getY());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(st.getX());
                    b.setX(movingArea.getDoubleWidth() - MovingArea.PART_SPACE);
                }
                break;
                case MovingPanel.LEFT: {
                    p.setY(pos);
                    final FloatPoint st = getOutCoordinate(sector.getStart()
                            .getFunction(), p, MovingPanel.LEFT);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(st.getY());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(st.getX());
                    b.setX(MovingArea.PART_SPACE);
                }
                break;
                case MovingPanel.TOP: {
                    p.setX(pos);
                    final FloatPoint st = getOutCoordinate(sector.getStart()
                            .getFunction(), p, MovingPanel.TOP);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(st.getX());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(st.getY());
                    b.setY(MovingArea.PART_SPACE);
                }
                break;
                case MovingPanel.BOTTOM: {
                    p.setX(pos);
                    final FloatPoint st = getOutCoordinate(sector.getStart()
                            .getFunction(), p, MovingPanel.BOTTOM);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(st.getX());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(st.getY());
                    b.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE);
                }
                break;
            }

			/*if (function.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
				switch (endBorderType) {
				case MovingPanel.TOP: {
					b.setY(role.getBottom());
					b.setX(role.getCenter().getX());
				}
					break;
				case MovingPanel.BOTTOM: {
					b.setY(role.getTop());
					b.setX(role.getCenter().getX());
				}
					break;
				case MovingPanel.LEFT: {
					b.setY(role.getCenter().getY());
					b.setX(role.getRight());
				}
					break;
				default: {
					b.setY(role.getCenter().getY());
					b.setX(role.getLeft());
				}
					break;
				}
			}*/
        }
        addSector(new PaintSector(sector, a, b, movingArea));
    }

    private PaintSector createOutPaintPart(final Sector sector, double pos) {
        final Point a = new Point();
        final Point b = new Point();
        final FloatPoint p = new FloatPoint();

        boolean start = sector.getStart().getType() == SectorBorder.TYPE_BORDER;
        int endBorderType = sector.getEnd().getBorderType();
        int startBorderType = sector.getStart().getBorderType();

		/*FRectangle role = null;

		if (function.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
			start = false;
			if (sector.getStart().getFunction() != null
					&& sector.getStart().getFunction().getType() == Function.TYPE_DFDS_ROLE) {
				start = true;
				startBorderType = MovingPanel.getOpposite(sector.getStart()
						.getFunctionType());
				role = sector.getStart().getFunction().getBounds();
			} else {
				endBorderType = MovingPanel.getOpposite(sector.getEnd()
						.getFunctionType());
				role = sector.getEnd().getFunction().getBounds();
			}
		}*/

        if (start) {
            switch (startBorderType) {
                case MovingPanel.TOP: {
                    p.setX(pos);
                    final FloatPoint st = getOutCoordinate(sector.getEnd()
                            .getFunction(), p, MovingPanel.TOP);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(st.getX());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(MovingArea.PART_SPACE);
                    b.setY(st.getY());
                }
                break;
                case MovingPanel.BOTTOM: {
                    p.setX(pos);
                    final FloatPoint st = getOutCoordinate(sector.getEnd()
                            .getFunction(), p, MovingPanel.BOTTOM);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(st.getX());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE);
                    b.setY(st.getY());
                }
                break;
                case MovingPanel.LEFT: {
                    p.setY(pos);
                    final FloatPoint st = getOutCoordinate(sector.getEnd()
                            .getFunction(), p, MovingPanel.LEFT);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(st.getY());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(MovingArea.PART_SPACE);
                    b.setX(st.getX());
                }
                break;
                default: {
                    p.setY(pos);
                    final FloatPoint st = getOutCoordinate(sector.getEnd()
                            .getFunction(), p, MovingPanel.RIGHT);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(st.getY());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setX(st.getX());
                    a.setX(movingArea.getDoubleWidth() - MovingArea.PART_SPACE);
                }
                break;
            }
			/*if (function.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
				switch (startBorderType) {
				case MovingPanel.TOP: {
					a.setY(role.getBottom());
					a.setX(role.getCenter().getX());
				}
					break;
				case MovingPanel.BOTTOM: {
					a.setY(role.getTop());
					a.setX(role.getCenter().getX());
				}
					break;
				case MovingPanel.LEFT: {
					a.setY(role.getCenter().getY());
					a.setX(role.getRight());
				}
					break;
				default: {
					a.setY(role.getCenter().getY());
					a.setX(role.getLeft());
				}
					break;
				}
			}*/
        } else {
            switch (endBorderType) {
                case MovingPanel.RIGHT: {
                    p.setY(pos);
                    final FloatPoint st = getOutCoordinate(sector.getStart()
                            .getFunction(), p, MovingPanel.RIGHT);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(st.getY());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(st.getX());
                    b.setX(movingArea.getDoubleWidth() - MovingArea.PART_SPACE);
                }
                break;
                case MovingPanel.LEFT: {
                    p.setY(pos);
                    final FloatPoint st = getOutCoordinate(sector.getStart()
                            .getFunction(), p, MovingPanel.LEFT);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(st.getY());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(st.getX());
                    b.setX(MovingArea.PART_SPACE);
                }
                break;
                case MovingPanel.TOP: {
                    p.setX(pos);
                    final FloatPoint st = getOutCoordinate(sector.getStart()
                            .getFunction(), p, MovingPanel.TOP);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(st.getX());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(st.getY());
                    b.setY(MovingArea.PART_SPACE);
                }
                break;
                case MovingPanel.BOTTOM: {
                    p.setX(pos);
                    final FloatPoint st = getOutCoordinate(sector.getStart()
                            .getFunction(), p, MovingPanel.BOTTOM);
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(st.getX());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(st.getY());
                    b.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE);
                }
                break;
            }

			/*if (function.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
				switch (endBorderType) {
				case MovingPanel.TOP: {
					b.setY(role.getBottom());
					b.setX(role.getCenter().getX());
				}
					break;
				case MovingPanel.BOTTOM: {
					b.setY(role.getTop());
					b.setX(role.getCenter().getX());
				}
					break;
				case MovingPanel.LEFT: {
					b.setY(role.getCenter().getY());
					b.setX(role.getRight());
				}
					break;
				default: {
					b.setY(role.getCenter().getY());
					b.setX(role.getLeft());
				}
					break;
				}
			}*/
        }
        return new PaintSector(sector, a, b, movingArea);
    }

    private FloatPoint getOutCoordinate(final Function function,
                                        final FloatPoint in, final int borderType) {
        final FloatPoint res = new FloatPoint();
        final FRectangle b = function.getBounds();
        switch (borderType) {
            case MovingPanel.LEFT: {
                res.setX(b.getLeft());
                res.setY(getOutPos(in.getY(), b.getHeight(), b.getTop(),
                        movingArea.getDoubleHeight()));
            }
            break;
            case MovingPanel.RIGHT: {
                res.setX(b.getRight());
                res.setY(getOutPos(in.getY(), b.getHeight(), b.getTop(),
                        movingArea.getDoubleHeight()));
            }
            break;
            case MovingPanel.TOP: {
                res.setX(getOutPos(in.getX(), b.getWidth(), b.getLeft(),
                        movingArea.getDoubleWidth()));
                res.setY(b.getTop());
            }
            break;
            case MovingPanel.BOTTOM: {
                res.setX(getOutPos(in.getX(), b.getWidth(), b.getLeft(),
                        movingArea.getDoubleWidth()));
                res.setY(b.getBottom());
            }
            break;
        }

        return res;
    }

    private double getOutPos(final double inPos, final double outWidth,
                             final double outPos, final double inWidth) {
        return outPos + outWidth * inPos / inWidth;
    }

    /**
     * Мотод, який створює відображення всередені.
     *
     * @param sector Сектор для якого буде створено відображення.
     */

    private void createInPaintPart(final Sector sector) {
        if (sector.getStart().getCrosspoint() == null) {// -->
            final Sector[] opps = sector.getEnd().getCrosspoint()
                    .getOppozite(sector);
            if (opps.length == 0) {
                // sector.remove();
                return;
            }
            final Point a = new Point();
            final Point b = new Point();
            switch (sector.getEnd().getBorderType()) {
                case MovingPanel.TOP: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(sector.getCreatePos());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setY(MovingArea.PART_SPACE);
                    a.setY(MovingArea.PART_SPACE + PART_SECTOR_LENGTH);
                }
                break;
                case MovingPanel.BOTTOM: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(sector.getCreatePos());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE);
                    a.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE
                            - PART_SECTOR_LENGTH);
                }
                break;
                case MovingPanel.RIGHT: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(sector.getCreatePos());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(movingArea.getDoubleWidth() - PART_SECTOR_LENGTH
                            - MovingArea.PART_SPACE);
                    b.setX(movingArea.getDoubleWidth() - MovingArea.PART_SPACE);
                }
                break;
                case MovingPanel.LEFT: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(sector.getCreatePos());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(PART_SECTOR_LENGTH + MovingArea.PART_SPACE);
                    b.setX(MovingArea.PART_SPACE);
                }
                break;
            }

            // sector.setVisualAttributes(opps[0].getVisualAttributes());
            final PaintSector ps = new PaintSector(sector, a, b, movingArea);
            addSector(ps);
        } else {
            final Sector[] opps = sector.getStart().getCrosspoint()
                    .getOppozite(sector);
            if (opps.length == 0) {
                // sector.remove();
                return;
            }
            final Point a = new Point();
            final Point b = new Point();
            switch (sector.getStart().getBorderType()) {
                case MovingPanel.TOP: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(sector.getCreatePos());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(MovingArea.PART_SPACE);
                    b.setY(MovingArea.PART_SPACE + PART_SECTOR_LENGTH);
                }
                break;
                case MovingPanel.BOTTOM: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_X);
                    o.setPosition(sector.getCreatePos());
                    a.setXOrdinate(o);
                    b.setXOrdinate(o);
                    a.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    b.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    a.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE);
                    b.setY(movingArea.getDoubleHeight() - MovingArea.PART_SPACE
                            - PART_SECTOR_LENGTH);
                }
                break;
                case MovingPanel.RIGHT: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(sector.getCreatePos());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(movingArea.getDoubleWidth() - MovingArea.PART_SPACE);
                    b.setX(movingArea.getDoubleWidth() - MovingArea.PART_SPACE
                            - PART_SECTOR_LENGTH);
                }
                break;
                case MovingPanel.LEFT: {
                    final Ordinate o = new Ordinate(Ordinate.TYPE_Y);
                    o.setPosition(sector.getCreatePos());
                    a.setYOrdinate(o);
                    b.setYOrdinate(o);
                    a.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    b.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    a.setX(MovingArea.PART_SPACE);
                    b.setX(MovingArea.PART_SPACE + PART_SECTOR_LENGTH);
                }
                break;
            }
            if (opps.length == 0) {
                sector.remove();
            } else {
                // sector.setVisualAttributes(opps[0].getVisualAttributes());
                final PaintSector ps = new PaintSector(sector, a, b, movingArea);
                addSector(ps);
            }
        }
    }

    public void addText(final MovingText text) {
        texts.add(text);
    }

    public void removeText(final MovingText text) {
        texts.remove(text);
    }

    public MovingText getText(final int i) {
        return texts.get(i);
    }

    public int getTextCount() {
        return texts.size();
    }

    public Vector<MovingText> getTexts() {
        return texts;
    }

    public MovingArea getMovingArea() {
        return movingArea;
    }

    public PaintSector getPaintSector(final Sector sector) {
        for (int i = 0; i < sectors.size(); i++) {
            if (sectors.get(i).getSector().equals(sector))
                return sectors.get(i);
        }
        return null;
    }

    public int getPaintSectorIndex(final Sector sector) {
        for (int i = 0; i < sectors.size(); i++) {
            if (sectors.get(i).getSector().equals(sector))
                return i;
        }
        return -1;
    }

    public void setFunction(final Function function) {
        this.function = function;
    }

    /**
     * @return the dataPlugin
     */
    private DataPlugin getDataPlugin() {
        return movingArea.dataPlugin;
    }

    public void fixNoNameBug() {

        for (PaintSector ps : sectors) {
            try {
                fixNoNameBug(ps.getSector().getStart().getCrosspoint(),
                        ps.getSector());
                fixNoNameBug(ps.getSector().getEnd().getCrosspoint(),
                        ps.getSector());
            } catch (Exception e) {
                // this code should never execute, this try was added just in
                // case.
                e.printStackTrace();
            }
        }
    }

    private void fixNoNameBug(Crosspoint crosspoint, Sector sector) {
        if (crosspoint != null) {
            if (crosspoint.isDLevel()) {
                Sector[] oppozite = crosspoint.getOppozite(sector);
                if (oppozite.length == 1) {
                    Sector opp = oppozite[0];
                    Stream s = opp.getStream();
                    ((NSector) sector).setThisStream(s);
                }
            }
        }
    }

    public void updatePageSize(boolean updateFonts, boolean updateZoom,
                               double percent, Function function) {
        if (updateZoom) {
            function.setBounds(function.getBounds().zoom(percent));
        }

        if (updateFonts) {
            Font font = function.getFont();
            if (font != null) {
                function.setFont(new Font(font.getName(), font.getStyle(),
                        (int) (font.getSize() * percent)));
            }
        }
        if (function.getChildCount() == 0)
            return;
        if (updateZoom) {
            loadFromFunction(function, false);
            for (MovingText text : texts) {
                if (text instanceof IDEF0Object) {

                } else {
                    // text.setBounds(text.getBounds().zoom(percent));
                }
            }

            HashSet<Ordinate> ordinates = new HashSet<Ordinate>();

            for (PaintSector paintSector : sectors) {
                for (int i = 0; i < paintSector.getPointCount(); i++) {
                    Point point = paintSector.getPoint(i);
                    if (!ordinates.contains(point.getXOrdinate()))
                        ordinates.add(point.getXOrdinate());
                    if (!ordinates.contains(point.getYOrdinate()))
                        ordinates.add(point.getYOrdinate());
                }
                MovingLabel movingLabel = paintSector.getText();
                if (movingLabel != null) {
                    movingLabel
                            .setBounds(movingLabel.getBounds().zoom(percent));
                    Font font = movingLabel.getFont();
                    movingLabel.setFont(new Font(font.getName(), font
                            .getStyle(), (int) (font.getSize() * percent)));
                }
                Font font = paintSector.getFont();
                if (font != null) {
                    paintSector.setFont(new Font(font.getName(), font
                            .getStyle(), (int) (font.getSize() * percent)));
                    paintSector.saveVisual();
                }
            }

            for (Ordinate o : ordinates) {
                o.setPosition(percent * o.getPosition());
            }

            for (PaintSector paintSector : sectors) {
                for (int i = 0; i < paintSector.getPointCount(); i++) {
                    if (paintSector.getSector().getEnd().getBorderType() == Point.BOTTOM) {
                        Point point = paintSector.getEndPoint();
                        point.getYOrdinate().setPosition(
                                movingArea.CLIENT_HEIGHT - 7);
                    } else if (paintSector.getSector().getStart()
                            .getBorderType() == Point.BOTTOM) {
                        Point point = paintSector.getStartPoint();
                        point.getYOrdinate().setPosition(
                                movingArea.CLIENT_HEIGHT - 7);
                    }

                    if (paintSector.getSector().getEnd().getBorderType() == Point.TOP) {
                        Point point = paintSector.getEndPoint();
                        point.getYOrdinate().setPosition(7);
                    } else if (paintSector.getSector().getStart()
                            .getBorderType() == Point.TOP) {
                        Point point = paintSector.getStartPoint();
                        point.getYOrdinate().setPosition(7);
                    }

                    if (paintSector.getSector().getEnd().getBorderType() == Point.LEFT) {
                        Point point = paintSector.getEndPoint();
                        point.getXOrdinate().setPosition(7);
                    } else if (paintSector.getSector().getStart()
                            .getBorderType() == Point.LEFT) {
                        Point point = paintSector.getStartPoint();
                        point.getXOrdinate().setPosition(7);
                    }

                    if (paintSector.getSector().getEnd().getBorderType() == Point.RIGHT) {
                        Point point = paintSector.getEndPoint();
                        point.getXOrdinate().setPosition(
                                movingArea.CLIENT_WIDTH - 7);
                    } else if (paintSector.getSector().getStart()
                            .getBorderType() == Point.RIGHT) {
                        Point point = paintSector.getStartPoint();
                        point.getXOrdinate().setPosition(
                                movingArea.CLIENT_WIDTH - 7);
                    }

                }
            }

            saveToFunction();
        }
    }

    public void fixOwners() {
        if (function.getDecompositionType() != MovingArea.DIAGRAM_TYPE_DFDS)
            return;
        fixOwners(sector.getSector(), movingArea.getDataPlugin());
    }

    public static void fixOwners(Sector sector, DataPlugin dataPlugin) {
        fixEndOwners(sector, dataPlugin);
        fixStartOwners(sector, dataPlugin);
    }

    private static void fixEndOwners(Sector sector, DataPlugin dataPlugin) {
        Function function2 = sector.getStart().getFunction();
        if (function2 != null && function2.getType() == Function.TYPE_DFDS_ROLE)
            fillFromOwners(function2, dataPlugin);

        Crosspoint crosspoint = sector.getEnd().getCrosspoint();
        if (crosspoint != null) {
            for (Sector sector2 : crosspoint.getOppozite(sector))
                fixEndOwners(sector2, dataPlugin);
        }
    }

    private static void fixStartOwners(Sector sector, DataPlugin dataPlugin) {
        Function function2 = sector.getEnd().getFunction();
        if (function2 != null && function2.getType() == Function.TYPE_DFDS_ROLE)
            fillFromOwners(function2, dataPlugin);

        Crosspoint crosspoint = sector.getStart().getCrosspoint();
        if (crosspoint != null) {
            for (Sector sector2 : crosspoint.getOppozite(sector))
                fixStartOwners(sector2, dataPlugin);
        }
    }

    private static void fillFromOwners(Function function, DataPlugin dataPlugin) {
        Function par = (Function) function.getParent();
        if (par == null)
            return;
        Vector<Sector> ends = new Vector<Sector>();
        Vector<Sector> starts = new Vector<Sector>();
        for (Sector sector : par.getSectors()) {
            NSectorBorder start = sector.getStart();
            if (function.equals(start.getFunction())
                    && start.getCrosspoint() != null) {
                if (start.getCrosspoint().isDLevel()) {
                    for (Sector sector2 : start.getCrosspoint().getOppozite(
                            sector))
                        starts.add(sector2);
                }
            }
            NSectorBorder end = sector.getEnd();
            if (function.equals(end.getFunction())
                    && end.getCrosspoint() != null) {
                if (end.getCrosspoint().isDLevel()) {
                    for (Sector sector2 : end.getCrosspoint().getOppozite(
                            sector))
                        ends.add(sector2);
                }
            }
        }
        if (ends.size() == 0 && starts.size() == 0)
            return;
        HashSet<Row> owners = new HashSet<Row>();
        boolean[] hasFunctionOwners = new boolean[]{false};
        for (Sector sector : starts)
            fillStartFromOwners(sector, owners, dataPlugin, hasFunctionOwners);
        for (Sector sector : ends)
            fillEndFromOwners(sector, owners, dataPlugin, hasFunctionOwners);
        if (!hasFunctionOwners[0] && owners.size() == 0)
            return;
        Stream stream = (Stream) dataPlugin.findRowByGlobalId(function
                .getLink());
        if (stream == null) {
            if (owners.size() > 0) {
                stream = (Stream) dataPlugin.createRow(
                        dataPlugin.getBaseStream(), true);
                function.setLink(stream.getElement().getId());
                stream.setRows(owners.toArray(new Row[owners.size()]));
            }
        } else {
            stream.setRows(owners.toArray(new Row[owners.size()]));
        }
    }

    private static void fillStartFromOwners(Sector sector, HashSet<Row> owners,
                                            DataPlugin dataPlugin, boolean[] hasFunctionOwners) {
        Crosspoint crosspoint = sector.getStart().getCrosspoint();
        Function function2 = sector.getStart().getFunction();
        if (function2 != null) {
            if (function2.getType() != Function.TYPE_DFDS_ROLE) {
                Row row = function2.getOwner();
                if (row != null && !owners.contains(row)) {
                    owners.add(row);
                    hasFunctionOwners[0] = true;
                }
            } else {
                Stream stream = (Stream) dataPlugin.findRowByGlobalId(function2
                        .getLink());
                if (stream != null)
                    for (Row row : stream.getAdded()) {
                        if (row != null && !owners.contains(row))
                            owners.add(row);
                    }
            }
        }
        if (crosspoint != null)
            for (Sector sector2 : crosspoint.getOppozite(sector))
                fillStartFromOwners(sector2, owners, dataPlugin,
                        hasFunctionOwners);
    }

    private static void fillEndFromOwners(Sector sector, HashSet<Row> owners,
                                          DataPlugin dataPlugin, boolean[] hasFunctionOwners) {
        Crosspoint crosspoint = sector.getEnd().getCrosspoint();
        Function function2 = sector.getEnd().getFunction();
        if (function2 != null) {
            if (function2.getType() != Function.TYPE_DFDS_ROLE) {
                Row row = function2.getOwner();
                if (row != null && !owners.contains(row)) {
                    owners.add(row);
                    hasFunctionOwners[0] = true;
                }
            } else {
                Stream stream = (Stream) dataPlugin.findRowByGlobalId(function2
                        .getLink());
                if (stream != null)
                    for (Row row : stream.getAdded()) {
                        if (row != null && !owners.contains(row))
                            owners.add(row);
                    }
            }
        }
        if (crosspoint != null)
            for (Sector sector2 : crosspoint.getOppozite(sector))
                fillEndFromOwners(sector2, owners, dataPlugin,
                        hasFunctionOwners);
    }

    public static void copyOwnersFrom(Function function, DataPlugin dataPlugin) {
        Function par = (Function) function.getParent();
        HashSet<Function> roles = new HashSet<Function>();
        for (Sector sector : par.getSectors()) {
            if (function.equals(sector.getStart().getFunction())) {
                getEndRoles(sector, roles, dataPlugin);
                getStartRoles(sector, roles, dataPlugin);
            }
            if (function.equals(sector.getEnd().getFunction())) {
                getStartRoles(sector, roles, dataPlugin);
                getEndRoles(sector, roles, dataPlugin);
            }
        }

        Stream stream = (Stream) dataPlugin.findRowByGlobalId(function
                .getLink());

        Row[] owners;
        if (stream == null)
            owners = new Row[]{};
        else
            owners = stream.getAdded();

        for (Function function2 : roles) {
            stream = (Stream) dataPlugin.findRowByGlobalId(function2.getLink());
            if (stream == null) {
                if (owners.length > 0) {
                    stream = (Stream) dataPlugin.createRow(
                            dataPlugin.getBaseStream(), true);
                    function2.setLink(stream.getElement().getId());
                    stream.setRows(owners);
                }
            } else {
                stream.setRows(owners);
            }
        }
    }

    private static void getEndRoles(Sector sector, HashSet<Function> roles,
                                    DataPlugin dataPlugin) {
        Function function2 = sector.getEnd().getFunction();
        if (function2 != null && function2.getType() == Function.TYPE_DFDS_ROLE)
            if (!roles.contains(function2))
                roles.add(function2);
        function2 = sector.getStart().getFunction();
        if (function2 != null && function2.getType() == Function.TYPE_DFDS_ROLE)
            if (!roles.contains(function2))
                roles.add(function2);
        if (sector.getEnd().getCrosspoint() != null) {
            for (Sector sector2 : sector.getEnd().getCrosspoint()
                    .getOppozite(sector))
                getEndRoles(sector2, roles, dataPlugin);
        }
    }

    private static void getStartRoles(Sector sector, HashSet<Function> roles,
                                      DataPlugin dataPlugin) {
        Function function2 = sector.getStart().getFunction();
        if (function2 != null && function2.getType() == Function.TYPE_DFDS_ROLE)
            if (!roles.contains(function2))
                roles.add(function2);
        function2 = sector.getEnd().getFunction();
        if (function2 != null && function2.getType() == Function.TYPE_DFDS_ROLE)
            if (!roles.contains(function2))
                roles.add(function2);
        if (sector.getStart().getCrosspoint() != null) {
            for (Sector sector2 : sector.getStart().getCrosspoint()
                    .getOppozite(sector))
                getStartRoles(sector2, roles, dataPlugin);
        }
    }
}
