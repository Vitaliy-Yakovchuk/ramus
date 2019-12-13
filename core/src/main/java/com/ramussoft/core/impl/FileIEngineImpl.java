package com.ramussoft.core.impl;

import java.io.BufferedOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileLock;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.TransformerConfigurationException;

import com.ramussoft.common.*;
import com.ramussoft.common.persistent.Persistent;
import org.xml.sax.SAXException;

import com.ramussoft.common.attribute.AttributePlugin;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;

public class FileIEngineImpl extends IEngineImpl {

    private static final String APPLICATION_METADATA = "data/application_metadata.xml";

    public static final String SEQUENCES = "data/sequences.xml";

    /**
     * @param id
     * @param template
     * @param factory
     * @param file
     * Can be <code>null</code>
     * @throws ClassNotFoundException
     */

    private ZipFile zFile;

    private Hashtable<String, File> extractedFiles = new Hashtable<String, File>();

    private ArrayList<String> deletedPaths = new ArrayList<String>();

    private String fileTmpPath;

    private String tmpPath;

    private File file;

    private FileLock lock;

    private File fLock;

    private RandomAccessFile oLock;

    private boolean clearSessionPath = true;

    public FileIEngineImpl(int id, JDBCTemplate template, PluginFactory factory)
            throws ClassNotFoundException, ZipException, IOException {
        super(id, template, "ramus_", factory);
        String sessions = getSessionsPath();
        tmpPath = sessions + File.separator + System.currentTimeMillis()
                + Math.round(Math.random() * 1000);
        new File(tmpPath).mkdirs();
        lockSession();
        fileTmpPath = tmpPath + File.separator + "cache";
    }

    @Override
    protected void initStartBranch(JDBCTemplate template) {
        super.initStartBranch(template);
    }

    public static String getSessionsPath() {
        String tmpPath = System.getProperty("user.home");
        if (tmpPath == null) {
            System.err.println("Fatal error, tmp path not found!");
            System.exit(1);
        }
        if (!tmpPath.endsWith(File.separator))
            tmpPath += File.separator;
        return new OptionsDirectoryGetter() {
            @Override
            public String getProgramName() {
                String name = System.getProperty("user.ramus.application.name");
                if (name != null)
                    return name;
                return Metadata.getApplicationName();
            }
        }.getDirectoryName()
                + File.separator + "sessions";
    }

    public FileIEngineImpl(int id, JDBCTemplate template,
                           PluginFactory factory, String tmpPath)
            throws ClassNotFoundException, ZipException, IOException {
        super(id, template, "ramus_", factory);
        this.tmpPath = tmpPath;
        if (tmpPath == null) {
            return;
        }
        fileTmpPath = tmpPath + File.separator + "cache";
        lockSession();
    }

    private void lockSession() throws FileNotFoundException, IOException {
        fLock = new File(tmpPath + File.separator + ".lock");
        oLock = new RandomAccessFile(fLock, "rw");
        lock = oLock.getChannel().lock();
    }

    public void recoveryStreams() {
        File file = new File(fileTmpPath);
        if (file.exists()) {
            recoveryStreams(file.getAbsolutePath(), file);
        }
    }

    private void recoveryStreams(String prefix, File file) {
        for (File f : file.listFiles()) {
            if ((f.isDirectory()) && (!f.getName().equals(".."))
                    && (!f.getName().equals("."))) {
                recoveryStreams(prefix, f);
            }
            if (f.isFile()) {
                String key = f.getAbsolutePath().substring(prefix.length());
                if (!key.startsWith(File.separator))
                    key = "/" + key;
                extractedFiles.put(key.replace('\\', '/'), f);
            }
        }
    }

    public void open(File file, boolean ignoreFileVersion) throws ZipException,
            IOException, FileVersionException {
        if (zFile != null) {
            throw new RuntimeException("Engine has opened file " + this.file);
        }
        this.file = file;
        String copy = tmpPath + File.separator + "source.rms";
        copy(file, new File(copy));
        zFile = new ZipFile(file);
        FileMetadata metadata = null;
        if (!ignoreFileVersion) {
            metadata = checkFileVersion();
        }
        loadSequences();

        loadTable("", "branches");
        loadTable("", "attributes_data_metadata");
        loadTable("", "attributes_history");
        loadTable("", "qualifiers_history");
        loadTable("", "formulas_data_metadata");
        loadTable("", "formula_dependences_data_metadata");

        loadTable("", "qualifiers");
        loadTable("", "attributes");
        loadTable("", "qualifiers_attributes");
        loadTable("", "elements");
        // loadTable("", "persistents");
        // loadTable("", "persistent_fields");
        loadTable("", "application_preferencies");
        loadTable("", "streams");
        loadTable("", "formulas");
        loadTable("", "formula_dependences");
        openPersistentTables();

        if (metadata != null)
            deleteOrphanFileAttachments(metadata);
    }

    private void deleteOrphanFileAttachments(FileMetadata metadata) {
        if (isOlderVersion("2.0.1", metadata.getApplicationVersion())) {
            HashSet<String> streams = new HashSet<String>(Arrays.asList(getStreamNames()));
            for (Qualifier qualifier : getQualifiers()) {
                for (Attribute attr : qualifier.getAttributes())
                    if (attr.getAttributeType().equals(new AttributeType("Core", "File"))) {
                        for (Element element : getElements(qualifier.getId())) {
                            String path = "/elements/" + element.getId() + "/" + attr.getId()
                                    + "/Core/file";
                            if (streams.contains(path)) {
                                List<Persistent>[] data = getBinaryAttribute(element.getId(), attr.getId());
                                if (data.length == 1 && data[0].isEmpty()) {
                                    deleteStream(path);
                                }
                            }
                        }
                    }
            }
        }
    }

    private void loadSequences() throws IOException {
        ZipEntry entry = new ZipEntry(SEQUENCES);
        InputStream is = zFile.getInputStream(entry);
        Connection c = template.getConnection();
        try {
            if (is != null) {
                Properties ps = new Properties();
                ps.loadFromXML(is);
                Statement st = c.createStatement();
                Enumeration<Object> keys = ps.keys();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    try {
                        st.execute("DROP SEQUENCE " + prefix + key + ";");

                    } catch (SQLException e) {

                    }
                    st.execute("CREATE SEQUENCE " + prefix + key + " START "
                            + ps.getProperty(key) + ";");
                }
                st.close();
                is.close();
            }
            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private FileMetadata checkFileVersion() throws FileVersionException, IOException {
        String[] names = getAllPluginNames(factory.getPlugins());
        FileMetadata metadata = loadFileMetadata();
        if (metadata == null)
            return null;
        if (isOlderVersion(metadata.getFileOpenMinimumVersion())) {
            throw new FileMinimumVersionException(
                    metadata.getFileOpenMinimumVersion());
        }
        String[] filePlugins = metadata.getPlugins();
        if (filePlugins != null) {
            for (String fp : filePlugins) {
                boolean p = true;
                for (String name : names) {
                    if (name.equals(fp)) {
                        p = false;
                        break;
                    }
                }
                if (p) {
                    throw new FileVersionException(names, filePlugins, fp);
                }
            }
        }
        return metadata;
    }

    private boolean isOlderVersion(String minimumVersion) {
        String currentVersion = Metadata.getApplicationVersion();
        return isOlderVersion(minimumVersion, currentVersion);
    }

    public static boolean isOlderVersion(String minimumVersion,
                                         String currentVersion) {
        if (minimumVersion == null)
            return false;
        StringTokenizer current = new StringTokenizer(currentVersion, ".");
        StringTokenizer minimum = new StringTokenizer(minimumVersion, ".");
        while (true) {
            if (minimum.hasMoreElements()) {
                if (!current.hasMoreElements())
                    return true;
                int minimumPart = Integer.parseInt(minimum.nextToken());
                int currentPart = Integer.parseInt(current.nextToken());
                if (minimumPart > currentPart)
                    return true;
                else if (minimumPart < currentPart)
                    return false;
            } else
                return false;
        }
    }

    private FileMetadata loadFileMetadata() throws IOException {
        ZipEntry entry = new ZipEntry(APPLICATION_METADATA);
        InputStream is = zFile.getInputStream(entry);
        if (is != null) {

            Properties ps = new Properties();
            ps.loadFromXML(is);

            FileMetadata fileMetadata = new FileMetadata(ps);
            is.close();
            return fileMetadata;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private PluginName[] getPluginNames(List<Plugin> plugins) {
        List<PluginName> data = new ArrayList<PluginName>(plugins.size());
        List<AttributeType> types = template
                .query("SELECT attribute_type_plugin_name, attribute_type_name from "
                                + prefix
                                + "attributes GROUP BY attribute_type_plugin_name, attribute_type_name",
                        new RowMapper() {

                            @Override
                            public Object mapRow(ResultSet rs, int rowNum)
                                    throws SQLException {
                                return new AttributeType(
                                        rs.getString(1).trim(), rs.getString(2)
                                        .trim());
                            }

                        });

        for (int i = 0; i < plugins.size(); i++) {
            Plugin plugin = plugins.get(i);
            String name = plugin.getName();
            if (plugin instanceof AttributePlugin) {
                AttributePlugin attributePlugin = (AttributePlugin) plugin;
                AttributeType type = new AttributeType(
                        attributePlugin.getName(),
                        attributePlugin.getTypeName());
                if (types.indexOf(type) >= 0) {
                    name = "Attribute." + name + "."
                            + attributePlugin.getTypeName();
                    data.add(new PluginName(plugin, name));
                }
            } else {
                data.add(new PluginName(plugin, name));
            }
        }
        return data.toArray(new PluginName[data.size()]);
    }

    private class PluginName {
        public PluginName(Plugin plugin, String name) {
            this.plugin = plugin;
            this.name = name;
        }

        Plugin plugin;

        String name;
    }

    private String[] getAllPluginNames(List<Plugin> plugins) {
        String[] data = new String[plugins.size()];
        for (int i = 0; i < plugins.size(); i++) {
            Plugin plugin = plugins.get(i);
            String name = plugin.getName();
            if (plugin instanceof AttributePlugin)
                name = "Attribute." + name + "."
                        + ((AttributePlugin) plugin).getTypeName();
            data[i] = name;
        }
        return data;
    }

    private void copy(File source, File destination) throws IOException {
        if (source.equals(destination))// we are recovering some file
            return;
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(destination);
        byte[] bs = new byte[1024 * 256];
        int r;
        while ((r = fis.read(bs)) > 0) {
            fos.write(bs, 0, r);
        }
        fis.close();
        fos.close();
        writeFileNameToLock(source);
    }

    private void writeFileNameToLock(File file) throws IOException,
            UnsupportedEncodingException {
        oLock.write(file.getAbsolutePath().getBytes("UTF-8"));
    }

    private void loadTable(String dir, String tableName) {
        ZipEntry ze = new ZipEntry("data/" + dir + tableName + ".xml");
        try {
            InputStream stream = zFile.getInputStream(ze);
            if (stream != null) {
                XMLToTable toTable = new XMLToTable(template, stream,
                        tableName, prefix);
                toTable.load();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void openPersistentTables() {
        List<PersistentInfo> list = template.query("SELECT * FROM " + prefix
                + "persistents", new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new PersistentInfo(rs.getString("PLUGIN_NAME").trim(),
                        rs.getString("TABLE_NAME").trim());
            }
        });
        for (PersistentInfo info : list) {
            loadTable(info.plugin + "/", info.table.substring(prefix.length()));
        }

    }

    public void saveToFile(File file) throws IOException {
        File out = new File(file.getAbsolutePath() + ".part");
        saveToFileA(out);
        if (zFile != null)
            zFile.close();
        if (file.exists())
            if (!file.delete()) {
                zFile = new ZipFile(this.file);
                throw new IOException("Can not delete existsing file " + file);
            }
        if (!out.renameTo(file)) {
            zFile = new ZipFile(this.file);
            throw new IOException("Can not set name of file " + file);
        }
        this.file = file;
        zFile = new ZipFile(this.file);
        if (oLock != null) {
            oLock.seek(0);
            oLock.setLength(0);
            writeFileNameToLock(file);
        }
    }

    private void saveToFileA(File out) throws IOException {
        ZipOutputStream zos = saveToFileNotCloseFile(out);
        zos.close();
    }

    public ZipOutputStream saveToFileNotCloseFile(File out)
            throws FileNotFoundException, IOException {
        FileOutputStream stream = new FileOutputStream(out);
        return writeToStream(stream);
    }

    @SuppressWarnings("unchecked")
    public ZipOutputStream writeToStream(OutputStream stream)
            throws IOException {

        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
                stream));

        ZipEntry ze = new ZipEntry(APPLICATION_METADATA);

        zos.putNextEntry(ze);

        Properties ps = createMetadata();

        ps.storeToXML(zos, "Ramus file metadata");

        ze = new ZipEntry(SEQUENCES);

        zos.putNextEntry(ze);

        ps = new Properties();
        for (Plugin p : factory.getPlugins()) {
            for (String key : p.getSequences()) {
                ps.setProperty(key, Long.toString(nextValue(key)));
            }
        }
        ps.storeToXML(zos, "Sequence list file");

        saveTable("", "application_preferencies", zos);
        saveTable("", "attributes", zos);
        saveTable("", "elements", zos);
        saveTable("", "qualifiers", zos);
        saveTable("", "persistents", zos);
        saveTable("", "persistent_fields", zos);
        saveTable("", "qualifiers_attributes", zos);
        saveTable("", "streams", zos);
        saveTable("", "formulas", zos);
        saveTable("", "formula_dependences", zos);

        saveBranches("", "branches", zos);

        saveTable("", "attributes_history", zos);
        saveTable("", "qualifiers_history", zos);
        saveTable("", "formulas_data_metadata", zos);
        saveTable("", "formula_dependences_data_metadata", zos);
        saveTable("", "attributes_data_metadata", zos);

        savePersistentTables(zos);

        for (Entry<String, File> entry : extractedFiles.entrySet()) {
            String path = entry.getKey();
            if (path.startsWith("/data")) {
                System.out.println("WARNiNG: file will not be save: " + path
                        + ", becource it started with /data");
                continue;
            }
            File file = entry.getValue();
            zos.putNextEntry(createZipEntry(path));
            FileInputStream is = new FileInputStream(file);
            copyStreamA(is, zos);
            zos.closeEntry();
            is.close();
        }

        if (zFile != null) {
            Enumeration<ZipEntry> zes = (Enumeration<ZipEntry>) zFile.entries();
            while (zes.hasMoreElements()) {
                ZipEntry e = zes.nextElement();
                if ((e.getName().startsWith("/data"))
                        || (e.getName().startsWith("data"))
                        || (deletedPaths.indexOf(e.getName()) >= 0)
                        || (deletedPaths.indexOf("/" + e.getName()) >= 0)
                        || (extractedFiles.get(e.getName()) != null)
                        || (extractedFiles.get("/" + e.getName()) != null)) {

                } else {
                    InputStream is = zFile.getInputStream(e);
                    zos.putNextEntry(new ZipEntry(e.getName()));
                    copyStreamA(is, zos);
                    zos.closeEntry();
                    is.close();
                }
            }
        }
        return zos;
    }

    private Properties createMetadata() {
        Properties ps = new Properties();
        PluginName[] plugins = getPluginNames(factory.getPlugins());
        ps.setProperty("ApplicationName", Metadata.getApplicationName());
        ps.setProperty("ApplicationVersion", Metadata.getApplicationVersion());
        ps.setProperty("CurrentTimeMillis",
                Long.toString(System.currentTimeMillis()));
        ps.setProperty("CurrentDateTime", new Date().toString());
        ps.setProperty("FileOpenMinimumVersion",
                Metadata.getFileOpenMinimumVersion());
        int i = 0;
        int id = 0;
        for (PluginName s : plugins) {
            if (plugins[i].plugin.isCriticatToOpenFile()) {
                ps.setProperty("Plugin_" + id, s.name);
                id++;
            }
            i++;
        }
        ps.setProperty("PluginCount", Integer.toString(id));
        return ps;
    }

    private ZipEntry createZipEntry(String path) {
        if (path.startsWith("/"))
            return new ZipEntry(path.substring(1));
        return new ZipEntry(path);
    }

    public static class PersistentInfo {

        public PersistentInfo(String plugin, String table) {
            this.plugin = plugin;
            this.table = table;
        }

        public String plugin;
        public String table;
    }

    @SuppressWarnings("unchecked")
    private void savePersistentTables(ZipOutputStream zos) throws IOException {
        List<PersistentInfo> list = template.query("SELECT * FROM " + prefix
                + "persistents", new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new PersistentInfo(rs.getString("PLUGIN_NAME").trim(),
                        rs.getString("TABLE_NAME").trim());
            }
        });
        for (PersistentInfo info : list) {
            saveTable(info.plugin + "/", info.table.substring(prefix.length()),
                    zos);
        }
    }

    private void saveTable(String dir, String fileName, ZipOutputStream zos)
            throws IOException {
        zos.putNextEntry(new ZipEntry("data/" + dir + fileName + ".xml"));
        TableToXML toXML = new TableToXML(template, zos, fileName, prefix);
        try {
            toXML.store();
            zos.closeEntry();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private void saveBranches(String dir, String fileName, ZipOutputStream zos)
            throws IOException {
        zos.putNextEntry(new ZipEntry("data/" + dir + fileName + ".xml"));
        TableToXML toXML = new TableToXML(template, zos, fileName, prefix) {
            @Override
            protected boolean resultSetNext(ResultSet rs) throws SQLException {
                boolean next = super.resultSetNext(rs);
                if (next)
                    if (rs.getLong("branch_id") == 0l) {
                        next = super.resultSetNext(rs);
                    }
                return next;
            }
        };
        try {
            toXML.store();
            zos.closeEntry();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод фізично видаляє дані, і нічого не знає про версії даних.
     */
    @Override
    protected boolean deleteStreamBytes(String path) {
        File file;
        if ((file = extractedFiles.get(path)) != null) {
            boolean r = file.delete();
            if (!r)
                return false;
        }
        deletedPaths.add(path);
        extractedFiles.remove(path);
        return true;
    }

    @Override
    public byte[] getStream(String path) {
        String path2;
        path2 = getRealPath(path);
        if (path2 == null)
            return null;
        return getStreamFromFile(path2);
    }

    private String getRealPath(String path) {
        String path2;
        if (!path.startsWith("/elements/"))
            path2 = path;
        else {
            long branchId = getActiveBranchId();
            List<Long> list = template
                    .query("SELECT * FROM "
                                    + prefix
                                    + "streams WHERE STREAM_ID=? AND created_branch_id IN "
                                    + "(SELECT MAX(created_branch_id) FROM "
                                    + prefix
                                    + "streams WHERE STREAM_ID=? AND created_branch_id<=?) AND removed_branch_id >?",
                            new RowMapper() {

                                @Override
                                public Object mapRow(ResultSet rs, int rowNum)
                                        throws SQLException {
                                    return rs.getLong("created_branch_id");
                                }
                            }, new Object[]{path, path, branchId, branchId},
                            false);
            if (list.size() == 0) {
                return null;
            } else if (list.size() > 1) {
                System.err.println("Error with streams branch " + path + " "
                        + list);
            }
            long sb = list.get(0);
            if (sb == 0l)
                path2 = path;
            else
                path2 = "/branches/" + sb + path;
        }
        return path2;
    }

    /**
     * Метод просто зчитує дані і нічого не знає про версії даних.
     */
    public byte[] getStreamFromFile(String path) {
        if (deletedPaths.indexOf(path) >= 0)
            return null;
        try {
            File file;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if ((file = extractedFiles.get(path)) != null) {
                copyStream(new FileInputStream(file), out);
                return out.toByteArray();
            } else if (zFile != null) {
                String ep = path;
                if (ep.startsWith("/"))
                    ep = path.substring(1);
                ZipEntry ze = zFile.getEntry(ep);
                if (ze == null) {
                    return null;
                }
                InputStream inputStream = zFile.getInputStream(ze);
                if (inputStream == null)
                    return null;
                copyStream(inputStream, out);
                file = new File(fileTmpPath + path);
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(out.toByteArray());
                fos.close();
                extractedFiles.put(path, file);
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Метод обробляє безпосередній запис, але не відповідає за роботу з
     * версіями даних.
     */
    @Override
    protected void writeStream(String path, byte[] bytes) {
        if (tmpPath == null)
            return;
        deletedPaths.remove(path);
        File file = new File(fileTmpPath + path);
        try {
            File f = file.getParentFile();
            if (!f.exists())
                f.mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
            extractedFiles.put(path, file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteStream(String path) {
        long branchId = getActiveBranch();
        List<Long> list = template
                .query("SELECT * FROM "
                                + prefix
                                + "streams WHERE STREAM_ID=? AND created_branch_id IN "
                                + "(SELECT MAX(created_branch_id) FROM "
                                + prefix
                                + "streams WHERE STREAM_ID=? AND created_branch_id<=?) AND removed_branch_id>?",
                        new RowMapper() {

                            @Override
                            public Object mapRow(ResultSet rs, int rowNum)
                                    throws SQLException {
                                return rs.getLong("created_branch_id");
                            }
                        }, new Object[]{path, path, branchId, branchId},
                        false);
        if (list.size() > 0) {
            long storedBranchId = list.get(0);
            if (storedBranchId == branchId) {
                try {
                    return deleteStreamBytes(path);
                } finally {
                    template.update(
                            "DELETE FROM "
                                    + prefix
                                    + "streams WHERE STREAM_ID=? AND created_branch_id=?",
                            new Object[]{path, branchId}, false);
                }
            } else {
                template.update(
                        "UPDATE "
                                + prefix
                                + "streams SET removed_branch_id=? WHERE STREAM_ID=? AND created_branch_id=?",
                        new Object[]{branchId, path, storedBranchId}, false);
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setStream(String path, byte[] bytes) {
        if (!path.startsWith("/"))
            throw new RuntimeException("Path should start with / symbol.");
        long branchId = getActiveBranchId();
        if (branchId == 0l || !path.startsWith("/elements/")) {
            if (bytes == null) {
                if (deleteStreamBytes(path))
                    template.update("DELETE FROM " + prefix
                                    + "streams WHERE STREAM_ID=?",
                            new Object[]{path}, true);
                return;
            }

            writeStream(path, bytes);

            List<String> list = template.query("SELECT * FROM " + prefix
                    + "streams WHERE STREAM_ID=?", new RowMapper() {
                @Override
                public Object mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    return rs.getString("STREAM_ID");
                }
            }, new Object[]{path}, true);
            if (list.size() == 0) {
                template.update("INSERT INTO " + prefix
                                + "streams (STREAM_ID) VALUES (?)",
                        new Object[]{path}, true);
            }
        } else {
            List<Long> list = template
                    .query("SELECT * FROM "
                                    + prefix
                                    + "streams WHERE STREAM_ID=? AND created_branch_id IN "
                                    + "(SELECT MAX(created_branch_id) FROM "
                                    + prefix
                                    + "streams WHERE STREAM_ID=? AND created_branch_id <=? AND removed_branch_id>?",
                            new RowMapper() {

                                @Override
                                public Object mapRow(ResultSet rs, int rowNum)
                                        throws SQLException {
                                    return rs.getLong("created_branch_id");
                                }
                            }, new Object[]{path, path, branchId, branchId},
                            false);
            if (list.size() > 0) {
                long storedBranchId = list.get(0);
                if (storedBranchId == branchId) {
                    if (bytes == null) {
                        template.update(
                                "DELETE FROM "
                                        + prefix
                                        + "streams WHERE STREAM_ID=? AND created_branch_id=?",
                                new Object[]{path, branchId}, false);
                        deleteStreamBytes("/branches/" + branchId + path);
                    } else
                        writeStream("/branches/" + branchId + path, bytes);
                } else {
                    template.update(
                            "UPDATE "
                                    + prefix
                                    + "streams SET removed_branch_id=? WHERE STREAM_ID=? AND created_branch_id=?",
                            new Object[]{branchId, path, storedBranchId},
                            false);
                    if (bytes != null) {
                        template.update(
                                "INSERT INTO "
                                        + prefix
                                        + "streams (STREAM_ID, created_branch_id) VALUES (?, ?)",
                                new Object[]{path, branchId}, true);
                        writeStream("/branches/" + branchId + path, bytes);
                    }
                }
            } else {
                if (bytes != null) {
                    template.update(
                            "INSERT INTO "
                                    + prefix
                                    + "streams (STREAM_ID, created_branch_id) VALUES (?, ?)",
                            new Object[]{path, branchId}, true);
                    writeStream("/branches/" + branchId + path, bytes);
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public String[] getStreamNames() {
        List<String> list = template.query("SELECT * FROM " + prefix
                + "streams", new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("STREAM_ID");
            }
        });
        return list.toArray(new String[list.size()]);
    }

    public File getFileForPath(String path) {
        String r = getRealPath(path);
        if (r == null)
            return null;
        getStreamFromFile(r);
        File file = extractedFiles.get(r);
        if (file != null) {
            return file;
        }
        return null;
    }

    public static void copyStream(InputStream in, OutputStream out)
            throws IOException {
        copyStreamA(in, out);
        in.close();
        out.close();
    }

    private static void copyStreamA(InputStream in, OutputStream out)
            throws IOException {
        byte[] buff = new byte[1024 * 64];
        int r;
        while ((r = in.read(buff)) > 0) {
            out.write(buff, 0, r);
        }
    }

    public void close() throws IOException {
        if (zFile != null)
            zFile.close();
        if (clearSessionPath) {
            unlockSession();
            deleteRec(new File(tmpPath));
        } else {
            if (lock != null)
                lock.release();
        }
        zFile = null;
        fileTmpPath = null;
    }

    private void unlockSession() throws IOException {
        try {
            lock.release();
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
        oLock.close();
        fLock.delete();
    }

    public static void deleteRec(File dir) {
        File[] listFiles = dir.listFiles();
        if (listFiles == null)
            return;
        for (File file : listFiles) {
            if ((file.isDirectory()) && (!file.getName().equals(".."))
                    && (!file.getName().equals(".")))
                deleteRec(file);
            if (file.isFile())
                file.delete();
        }
        dir.delete();
    }

    public File getFile() {
        return file;
    }

    /**
     * @return the fileTmpPath
     */
    public String getTmpPath() {
        return tmpPath;
    }

    /**
     * @param clearSessionPath the clearSessionPath to set
     */
    public void setClearSessionPath(boolean clearSessionPath) {
        this.clearSessionPath = clearSessionPath;
    }

    /**
     * @return the clearSessionPath
     */
    public boolean isClearSessionPath() {
        return clearSessionPath;
    }

    public Connection getConnection() {
        return getTemplate().getConnection();
    }

}
