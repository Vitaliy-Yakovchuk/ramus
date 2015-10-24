package com.ramussoft.gui.spell;

import java.io.IOException;
import java.util.zip.ZipFile;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.swing.JTextComponentSpellChecker;

public class Language implements Comparable<Language> {

    private ZipFile file;

    private String name;

    private SpellChecker spellChecker = null;

    Language(String name, ZipFile file) {
        this.name = name;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public String getLocalizedName() {
        return name;
    }

    @Override
    public String toString() {
        return getLocalizedName();
    }

    void close() throws IOException {
        file.close();
    }

    public JTextComponentSpellChecker createTextComponentSpellChecker() {
        return new JTextComponentSpellChecker(getSpellChecker());
    }

    public SpellChecker getSpellChecker() {
        if (spellChecker == null) {
            try {
                spellChecker = new SpellChecker(new OpenOfficeSpellDictionary(
                        file, false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return spellChecker;
    }

    @Override
    public int compareTo(Language o) {
        return name.compareTo(o.name);
    }

    public String getLanguage() {
        return file.getName().substring(0, 2).toLowerCase();
    }

    public String getCountry() {
        String name = file.getName();
        int i = name.indexOf('.');
        if (i == 2)
            return null;
        return name.substring(3, 5).toUpperCase();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Language))
            return false;
        Language other = (Language) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
