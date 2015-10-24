package com.ramussoft.common.cached;

import java.util.Hashtable;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Qualifier;

public class CachedData {

    Object loadLock = new Object();

    public static class CacheHolder {
        Hashtable<Long, CachedElement> elements = new Hashtable<Long, CachedElement>();

        Hashtable<Long, CachedQualifier> qualifiers = new Hashtable<Long, CachedQualifier>();

        Hashtable<Long, CachedAttributeData> attributeData = new Hashtable<Long, CachedAttributeData>();

        Hashtable<String, Qualifier> systemQualifiers = new Hashtable<String, Qualifier>();

        Hashtable<String, Attribute> systemAttributes = new Hashtable<String, Attribute>();

        Hashtable<Long, Attribute> attributes = new Hashtable<Long, Attribute>();

        int count;
    }

    private Hashtable<Long, CacheHolder> branchHolders = new Hashtable<Long, CachedData.CacheHolder>();

    CacheHolder getCacheHolder(Long branchId) {
        CacheHolder holder = branchHolders.get(branchId);
        if (holder == null) {
            holder = new CacheHolder();
            branchHolders.put(branchId, holder);
        }
        holder.count++;

        return holder;
    }

    void removeCacheHolder(Long branchId) {
        CacheHolder holder = branchHolders.get(branchId);
        if (holder != null) {
            holder.count--;
            if (holder.count <= 0)
                branchHolders.remove(branchId);
        }
    }
}
