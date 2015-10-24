package com.ramussoft.common;

import java.util.Hashtable;
import java.util.List;

import com.ramussoft.common.attribute.FindObject;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Transaction;

/**
 * Class for data storage and setup, etc.
 *
 * @author zdd
 */

public interface IEngine {

    /**
     * Creates new qualifier.
     */

    Qualifier createQualifier();

    /**
     * @return new system qualifier.
     */

    Qualifier createSystemQualifier();

    /**
     * Creates new element.
     *
     * @param qualifierId Qualifier id.
     * @return New element.
     */

    Element createElement(long qualifierId);

    /**
     * Creates element with specific id.
     *
     * @param qualifierId Qualifier id.
     * @param elementId   New element id, if == -1, some id>=0 will be set.
     * @return new Element.
     */

    Element createElement(long qualifierId, long elementId);

    /**
     * Method create new attribute.
     */

    Attribute createAttribute(AttributeType attributeType);

    /**
     * @param attributeType
     * @return new system attribute.
     */

    Attribute createSystemAttribute(AttributeType attributeType);

    /**
     * Method creates new attribute with some id, if attributeId==-1, method
     * creates automatically new id (>=0).
     */

    Attribute createAttribute(long attributeId, AttributeType attributeType);

    /**
     * Looking for the attribute by its id.
     */

    Attribute getAttribute(long attributeId);

    /**
     * Return all not system attributes.
     */

    List<Attribute> getAttributes();

    /**
     * Return all system attributes.
     */
    @Deprecated
    List<Attribute> getSystemAttributes();

    /**
     * @return list of system qualifiers.
     */

    @Deprecated
    List<Qualifier> getSystemQualifiers();

    /**
     * Looking for qualifier by its id, if qualifier not exists, method returns
     * <code>null</code>.
     */

    Qualifier getQualifier(long qualifierId);

    /**
     * Method updates qualifier fields.
     *
     * @param qualifier Qualifier which will be updated. To identify qualifier its id
     *                  uses.
     */

    void updateQualifier(Qualifier qualifier);

    /**
     * Method updates attribute fields.
     *
     * @param attribute Attribute which will be updated. To identify attribute its id
     *                  used.
     */

    void updateAttribute(Attribute attribute);

    /**
     * Method removes qualifier.
     *
     * @param id Qualifier id.
     */

    void deleteQualifier(long id);

    /**
     * Method removes attribute, this method deletes attribute from all
     * qualifiers too, which uses this attribute.
     *
     * @param id Attribute id.
     */

    void deleteAttribute(long id);

    /**
     * Method removes element. This method removes all attribute data too.
     *
     * @param id Element id.
     */

    void deleteElement(long id);

    /**
     * Return all elements of the Qualifier.
     *
     * @param qualifierId id of the qualifier. If qualifier with some id not exists,
     *                    method throws {@link QualifierNotExistsException} exception.
     * @return List of the elements, can not b null.
     */

    List<Element> getElements(long qualifierId);

    /**
     * Return array of available attribute types. This array can be different,
     * depends on plug-in install list.
     */

    AttributeType[] getAttributeTypes();

    /**
     * Return array of available system attribute types. This array can be
     * different, depends on plug-in install list. This array does not connect
     * with Qualifier system attribute and needs only not to show these
     * attributes for the and user.
     */

    AttributeType[] getSystemAttributeTypes();

    /**
     * Return list of qualifiers.
     */

    List<Qualifier> getQualifiers();

    /**
     * Return elements with its attributes in serialization format.
     *
     * @param qualifierId Qualifier id.
     * @param attributes  Attribute list.
     * @return Elements and its attributes.
     */

    Hashtable<Element, List<Persistent>[][]> getBinaryElements(
            long qualifierId, long[] attributeIds);

    /**
     * Return attribute in serialization format for some element.
     *
     * @param elementId Element id.
     * @param attribute Attribute.
     * @return Attribute data.
     */

    List<Persistent>[] getBinaryAttribute(long elementId, long attributeId);

    List<Persistent>[] getBinaryBranchAttribute(long elementId,
                                                long attributeId, long branchId);

    /**
     * Set some attribute in serialization format for element.
     *
     * @param elementId Element id.
     * @param attribute Attribute.
     * @param object    Data of some element.
     * @return true if this is a new value for a
     * branch (using for undo function in rebranch-data)
     */

    boolean setBinaryAttribute(long elementId, long attributeId,
                               Transaction transaction);

    /**
     * Method creates qualifier with some id.
     *
     * @param qualifierId Qualifier id, if == -1, then methods sets qualifierId by
     *                    itself;
     */

    Qualifier createQualifier(long qualifierId);

    /**
     * Return qualifierId for some elementId.
     *
     * @param elementId
     */

    long getQualifierIdForElement(long elementId);

    /**
     * Return id of this engine (as usual qualifier - 0, idef0 - 1).
     */

    int getId();

    /**
     * Method return persists lists arrays, which will be removed id element
     * will be removed. Array contain serialization transaction arrays with
     * attributes data, accord to the getAttributes, getSystemAttribues methods
     * of element qualifier.
     */

    Transaction[] getAttributeWhatWillBeDeleted(long elementId);

    Transaction[] getAttributesWhatWillBeDeleted(long elementId,
                                                 List<Attribute> attributes);

    Transaction getAttributePropertyWhatWillBeDeleted(long attributeId);

    /**
     * Method returns transactions of persistent objects which will be removed
     * if attribute will be remove from some qualifier.
     */

    Hashtable<Element, Transaction> getAttributeWhatWillBeDeleted(
            long qualifierId, long attributeId);

    /**
     * @param id Id of element.
     * @return Element with equals to id id.
     */

    Element getElement(long id);

    /**
     * Method updates name attribute in all elements.
     */

	/* void updateElements(List<Element> list); */

    /**
     * Return all saved in system stream names.
     */

    String[] getStreamNames();

    /**
     * Remove any existing stream.
     *
     * @param path Path of the stream.
     * @return <code>true</code> if stream where remove (and exists),
     * <code>false</code> is stream not exists.
     */

    boolean deleteStream(String path);

    /**
     * Return stream data.
     *
     * @param path Path to the stream.
     * @return Data.
     */

    byte[] getStream(String path);

    /**
     * Set stream data.
     */

    void setStream(String path, byte[] bytes);

    /**
     * Return element count for qualifier.
     */

    long getElementCountForQualifier(long qialifierId);

    /**
     * Return unique value for some sequence, if sequence not exists it will be
     * created.
     */

    long nextValue(String sequence);

    /**
     * Get system qualifier id by its name.
     */

    Qualifier getSystemQualifier(String qualifierName);

    /**
     * Set formula for some element, formula should be compiled.
     */

    void setCalculateInfo(CalculateInfo formula);

    /**
     * Return formula for some element.
     */

    CalculateInfo getCalculateInfo(long elementId, long attributeId);

    /**
     * Return element, that should be recalculated if some element will be
     * changed.
     *
     * @param autoRecalculate if is <code>true</code> return only autorecalculated elements,
     *                        if <code>false</code> return all elements.
     */

    List<CalculateInfo> getDependences(long elementId, long attributeId,
                                       boolean autoRecalculate);

    /**
     * Return formulas for regular expression for SQL Like operator.
     */
    public List<CalculateInfo> findCalculateInfos(String reg,
                                                  boolean autoRecalculate);

    /**
     * Return qualifier by its name.
     */

    Qualifier getQualifierByName(String qualifierName);

    /**
     * Return element by its name for some qualifier.
     */

    Element getElement(String elementName, long qualifierId);

    /**
     * Return attribute by its name.
     */

    Attribute getAttributeByName(String attributeName);

    /**
     * Return system attribute by its name.
     */

    Attribute getSystemAttribute(String attributeName);

    /**
     * Create system qualifier with specific id.
     */

    Qualifier createSystemQualifier(long qualifierId);

    /**
     * return element list for some find expression.
     */

    List<Element> getElements(long qualifierId, Attribute attribute,
                              FindObject[] findObjects);

    /**
     * Set new qualifier for existing element. Remove all attributes which are
     * not present in a new qualifier.
     */

    void setElementQualifier(long elementId, long qualifierId);

    /**
     * Creates new branch and activate it.
     *
     * @param parent Parent branch index, root branch is always 0;
     * @return Index of just create branch
     */

    public long createBranch(long parent, String reason, int type, String module);

    /**
     * Delete branch with equals index. If branch is active branch index, parent
     * branch to this branch will be activated. 0l branch cannot be deleted.
     *
     * @param branch Branch to delete. Throws runtime exception id branch is 0 or
     *               if branch no exists.
     */
    public void deleteBranch(long branch);

    /**
     * Return active branch.
     *
     * @return Active branch index.
     */

    public long getActiveBranch();

    /**
     * Change active branch.
     *
     * @param branchToActivate Branch what will be activated.
     */
    public void setActiveBranch(long branchToActivate);

    public Branch getRootBranch();

    /**
     * Update branch (only reason can be updated)
     */
    public void updateBranch(Branch branch);

    /**
     * Create new branch with passed branchId.
     */

    public void createBranch(long parentBranchId, long branchId, String reason,
                             int type, String module);

}
