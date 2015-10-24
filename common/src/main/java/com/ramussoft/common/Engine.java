package com.ramussoft.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.event.BranchListener;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.event.FormulaListener;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.event.StreamListener;

public interface Engine extends IEngine {

    /**
     * Return elements with its attributes.
     *
     * @param qualifierId Qualifier id.
     * @param attributes  Attribute list.
     * @return Elements and its attributes.
     */

    public Hashtable<Element, Object[]> getElements(Qualifier qualifier,
                                                    List<Attribute> attributes);

    /**
     * Return attribute for some element.
     *
     * @param element   Element for which we need attribute, if element =
     *                  <code>null</code>, then method returns property value for the
     *                  attribute.
     * @param attribute Attribute.
     * @return Attribute data.
     */

    public Object getAttribute(Element element, Attribute attribute);

    /**
     * Set some attribute for element.
     *
     * @param elementId Element id.
     * @param attribute Attribute.
     * @param object    Data of some element.
     */

    public void setAttribute(Element element, Attribute attribute, Object object);

    public void addAttributeListener(AttributeListener listener);

    public void addElementListener(Qualifier qualifier, ElementListener listener);

    public void addQualifierListener(QualifierListener listener);

    public void addElementAttributeListener(Qualifier qualifier,
                                            ElementAttributeListener listener);

    public void removeAttributeListener(AttributeListener listener);

    public void removeElementListener(Qualifier qualifier,
                                      ElementListener listener);

    public void removeQualifierListener(QualifierListener listener);

    public void removeElementAttributeListener(Qualifier qualifier,
                                               ElementAttributeListener listener);

    public AttributeListener[] getAttributeListeners();

    public ElementListener[] getElementListeners(Qualifier qualifier);

    public QualifierListener[] getQualifierListeners();

    public ElementAttributeListener[] getElementAttributeListeners(long id);

    public void replaceElements(Element[] oldElements, Element newElement);

    public List<Element> findElements(long qualifierId, Attribute attribute,
                                      Object object);

    /**
     * Set plug-in property.
     *
     * @param pluginName
     * @param key
     * @param value      can be <code>null</code>.
     */
    public void setPluginProperty(String pluginName, String key, Object value);

    public Object getPluginProperty(String pluginName, String key);

    public Properties getProperties(String path);

    public void setProperties(String path, Properties properties);

    /**
     * Return output stream, which can contain any binary data. Stream should be
     * closed after writing, or it can be not saved. Any changes in this stream
     * will not be stored in the journal.
     */

    OutputStream getOutputStream(String path);

    /**
     * Return input stream, which can contain any binary data. Stream should be
     * closed after reading. Return <code>null</code> if such file (stream) not
     * exists.
     */

    InputStream getInputStream(String path);

    /**
     * Return deligated engine is such is exists.
     */

    IEngine getDeligate();

    void addStreamListener(StreamListener listener);

    void removeStreamListener(StreamListener listener);

    StreamListener[] getStreamListeners();

    void addFormulaListener(FormulaListener listener);

    void removeFormulaListener(FormulaListener listener);

    FormulaListener[] getFormulaListeners();

    Object toUserValue(Attribute attribute, Element element, Object value);

    String[] getAllImplementationClasseNames();

    void setUndoableStream(String path, byte[] data);

    public void addBranchListener(BranchListener branchListener);

    public void removeBranchListener(BranchListener branchListener);

    public BranchListener[] getBranchListeners();

}
