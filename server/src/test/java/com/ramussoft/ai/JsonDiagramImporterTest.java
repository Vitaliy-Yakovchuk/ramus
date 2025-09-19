package com.ramussoft.ai;

import com.dsoft.pb.types.FRectangle;
import com.ramussoft.common.Engine;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
<<<<<<< ours
=======
import com.ramussoft.pb.data.SectorBorder;
>>>>>>> theirs
import com.ramussoft.pb.data.negine.NSector;
import com.ramussoft.pb.data.negine.NSectorBorder;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingPanel;
<<<<<<< ours
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.idef0.attribute.SectorPointPersistent;
import com.ramussoft.idef0.attribute.SectorPropertiesPersistent;
=======
>>>>>>> theirs
import com.ramussoft.server.EngineFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class JsonDiagramImporterTest {

    private EngineFactory engineFactory;
    private DataPlugin dataPlugin;
    private Function baseFunction;

    @Before
    public void setUp() {
        engineFactory = new EngineFactory();
        Engine engine = engineFactory.getJournaledEngine();
        dataPlugin = NDataPluginFactory.getDataPlugin(null, engine, engineFactory.getAccessRules());
        baseFunction = dataPlugin.getBaseFunction();
        baseFunction.setDecompositionType(MovingArea.DIAGRAM_TYPE_DFD);
    }

    @After
    public void tearDown() throws Exception {
        EngineFactory.closeConnection();
    }

    @Test
<<<<<<< ours
    public void importsBoxesStreamsAndArrows() throws Exception {
        String json = "{" +
                "\"streams\":[{" +
                "\"id\":7,\"name\":\"Material\",\"emptyName\":false" +
                "}]," +
                "\"elements\":[{" +
                "\"type\":\"box\",\"id\":12,\"name\":\"A1\"," +
                "\"parent\":{\"functionId\":0,\"diagramType\":1}," +
                "\"x\":120,\"y\":80,\"width\":140,\"height\":90" +
                "},{" +
                "\"type\":\"box\",\"id\":18,\"name\":\"A2\"," +
                "\"parent\":{\"functionId\":0,\"diagramType\":1}," +
                "\"x\":320,\"y\":80,\"width\":140,\"height\":90" +
                "},{" +
                "\"type\":\"arrow\",\"id\":34,\"name\":\"Материал\",\"sector\":{" +
                "\"functionId\":0,\"streamId\":7," +
                "\"start\":{\"functionId\":12,\"functionSide\":\"RIGHT\",\"crosspointId\":301,\"borderType\":\"FUNCTION\"}," +
                "\"end\":{\"functionId\":18,\"functionSide\":\"LEFT\",\"crosspointId\":301,\"borderType\":\"FUNCTION\"}," +
                "\"geometry\":{\"createState\":1,\"createPos\":0.5,\"showText\":1,\"alternativeText\":\"\",\"textAlignment\":0}," +
                "\"labelFrame\":{\"showText\":1,\"textX\":0.55,\"textY\":0.42,\"textWidth\":0.1,\"textHeight\":0.05,\"transparent\":0,\"showTilda\":0}," +
                "\"bendPoints\":[{" +
                "\"xOrdinateId\":1001,\"yOrdinateId\":2001,\"x\":180.0,\"y\":90.0,\"type\":\"MIDDLE\",\"position\":1" +
                "}]}}]}";
=======
    public void importsBoxesAndArrowsFromCompactJson() throws Exception {
        String json = "[" +
                "{\"type\":\"box\",\"id\":1,\"name\":\"A1\",\"father\":0,\"x\":120,\"y\":80,\"width\":140,\"height\":90}," +
                "{\"type\":\"box\",\"id\":2,\"name\":\"A2\",\"father\":0,\"x\":320,\"y\":80,\"width\":140,\"height\":90}," +
                "{\"type\":\"box\",\"id\":3,\"name\":\"A1.1\",\"father\":1,\"x\":140,\"y\":140,\"width\":120,\"height\":70}," +
                "{\"type\":\"box\",\"id\":4,\"name\":\"A1.2\",\"father\":1,\"x\":320,\"y\":140,\"width\":120,\"height\":70}," +
                "{\"type\":\"arrow\",\"id\":10,\"name\":\"Material\",\"father\":0,\"source\":1,\"sourceSide\":\"RIGHT\",\"target\":2,\"targetSide\":\"LEFT\"}," +
                "{\"type\":\"arrow\",\"id\":11,\"name\":\"External\",\"father\":0,\"source\":\"LEFT\",\"target\":1,\"targetSide\":\"LEFT\"}," +
                "{\"type\":\"arrow\",\"id\":12,\"name\":\"Detail\",\"father\":1,\"source\":3,\"sourceSide\":\"RIGHT\",\"target\":4,\"targetSide\":\"LEFT\"}," +
                "{\"type\":\"arrow\",\"id\":13,\"name\":\"Output\",\"father\":1,\"source\":4,\"sourceSide\":\"RIGHT\",\"target\":\"RIGHT\",\"targetSide\":\"RIGHT\"}" +
                "]";
>>>>>>> theirs

        JsonDiagramImporter importer = new JsonDiagramImporter(dataPlugin, baseFunction);
        JsonDiagramImporter.ImportResult result = importer.importDiagram(json);

        Map<Long, Function> functions = result.getFunctions();
<<<<<<< ours
        assertEquals(2, functions.size());

        Function box1 = functions.get(12L);
        assertNotNull(box1);
        assertEquals("A1", box1.getName());
=======
        assertEquals(4, functions.size());

        Function box1 = functions.get(1L);
        assertNotNull(box1);
        assertEquals("A1", box1.getName());
        assertEquals(MovingArea.DIAGRAM_TYPE_DFD, box1.getDecompositionType());
>>>>>>> theirs
        FRectangle bounds1 = box1.getBounds();
        assertEquals(120.0, bounds1.getX(), 0.001);
        assertEquals(80.0, bounds1.getY(), 0.001);
        assertEquals(140.0, bounds1.getWidth(), 0.001);
        assertEquals(90.0, bounds1.getHeight(), 0.001);
<<<<<<< ours
        assertEquals(MovingArea.DIAGRAM_TYPE_DFD, box1.getDecompositionType());

        Function box2 = functions.get(18L);
        assertNotNull(box2);
        assertEquals("A2", box2.getName());

        Map<Long, Stream> streams = result.getStreams();
        assertTrue(streams.containsKey(7L));
        Stream stream = streams.get(7L);
        assertEquals("Material", stream.getName());

        Sector sector = result.getSectors().get(34L);
        assertNotNull(sector);
        assertEquals(baseFunction, sector.getFunction());
        assertEquals(stream, sector.getStream());
        assertTrue(sector.isShowText());
        assertEquals(0, sector.getTextAligment());
        assertEquals(1, sector.getCreateState());
        assertEquals(0.5, sector.getCreatePos(), 0.0001);

        NSectorBorder start = (NSectorBorder) ((NSector) sector).getStart();
        assertEquals(box1, start.getFunction());
        assertEquals(MovingPanel.RIGHT, start.getFunctionType());
        Crosspoint crosspoint = start.getCrosspoint();
        assertNotNull(crosspoint);
        assertEquals(301L, crosspoint.getGlobalId());

        NSectorBorder end = (NSectorBorder) ((NSector) sector).getEnd();
        assertEquals(box2, end.getFunction());
        assertEquals(MovingPanel.LEFT, end.getFunctionType());
        assertEquals(crosspoint, end.getCrosspoint());

        SectorPropertiesPersistent properties = sector.getSectorProperties();
        assertEquals(0.55, properties.getTextX(), 0.0001);
        assertEquals(0.42, properties.getTextY(), 0.0001);
        assertEquals(0.1, properties.getTextWidth(), 0.0001);
        assertEquals(0.05, properties.getTextHieght(), 0.0001);

        assertEquals(1, sector.getSectorPointPersistents().size());
        SectorPointPersistent point = sector.getSectorPointPersistents().get(0);
        assertEquals(1001L, point.getXOrdinateId());
        assertEquals(2001L, point.getYOrdinateId());
        assertEquals(180.0, point.getXPosition(), 0.0001);
        assertEquals(90.0, point.getYPosition(), 0.0001);
        assertEquals(1, point.getPointType());
        assertEquals(1, point.getPosition());
    }
}

=======

        Function box3 = functions.get(3L);
        assertNotNull(box3);
        assertEquals("A1.1", box3.getName());
        assertEquals(MovingArea.DIAGRAM_TYPE_DFD, box3.getDecompositionType());

        Map<Long, Stream> streams = result.getStreams();
        assertEquals("Material", streams.get(10L).getName());
        assertEquals("External", streams.get(11L).getName());
        assertEquals("Detail", streams.get(12L).getName());
        assertEquals("Output", streams.get(13L).getName());

        Map<Long, Sector> sectors = result.getSectors();
        assertEquals(baseFunction, sectors.get(10L).getFunction());
        assertEquals(baseFunction, sectors.get(11L).getFunction());
        assertEquals(box1, sectors.get(12L).getFunction());
        assertEquals(box1, sectors.get(13L).getFunction());

        NSector sector10 = (NSector) sectors.get(10L);
        NSectorBorder start10 = sector10.getStart();
        assertEquals(box1, start10.getFunction());
        assertEquals(SectorBorder.TYPE_FUNCTION, start10.getBorderType());
        assertEquals(MovingPanel.RIGHT, start10.getFunctionType());
        NSectorBorder end10 = sector10.getEnd();
        assertEquals(functions.get(2L), end10.getFunction());
        assertEquals(SectorBorder.TYPE_FUNCTION, end10.getBorderType());
        assertEquals(MovingPanel.LEFT, end10.getFunctionType());

        NSector sector11 = (NSector) sectors.get(11L);
        NSectorBorder start11 = sector11.getStart();
        assertNull(start11.getFunction());
        assertEquals(SectorBorder.TYPE_BORDER, start11.getBorderType());
        assertEquals(MovingPanel.LEFT, start11.getFunctionType());
        NSectorBorder end11 = sector11.getEnd();
        assertEquals(box1, end11.getFunction());
        assertEquals(SectorBorder.TYPE_FUNCTION, end11.getBorderType());
        assertEquals(MovingPanel.LEFT, end11.getFunctionType());

        NSector sector12 = (NSector) sectors.get(12L);
        NSectorBorder start12 = sector12.getStart();
        assertEquals(box3, start12.getFunction());
        assertEquals(SectorBorder.TYPE_FUNCTION, start12.getBorderType());
        assertEquals(MovingPanel.RIGHT, start12.getFunctionType());
        NSectorBorder end12 = sector12.getEnd();
        assertEquals(functions.get(4L), end12.getFunction());
        assertEquals(SectorBorder.TYPE_FUNCTION, end12.getBorderType());
        assertEquals(MovingPanel.LEFT, end12.getFunctionType());

        NSector sector13 = (NSector) sectors.get(13L);
        NSectorBorder end13 = sector13.getEnd();
        assertNull(end13.getFunction());
        assertEquals(SectorBorder.TYPE_BORDER, end13.getBorderType());
        assertEquals(MovingPanel.RIGHT, end13.getFunctionType());
    }
}
>>>>>>> theirs
