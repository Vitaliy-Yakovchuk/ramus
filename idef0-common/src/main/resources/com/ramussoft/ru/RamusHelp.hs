<?xml version='1.0' encoding='UTF-8' ?>
<!DOCTYPE helpset   
PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<helpset version="1.0">

  <!-- title -->
  <title>Справка: Ramus</title>

  <!-- maps -->
  <maps>
     <homeID>intro</homeID>
     <mapref location="Map.jhm"/>
  </maps>

  <!-- views -->
  <view mergetype="javax.help.UniteAppendMerge">
    <name>TOC</name>
    <label>Содержание</label>
    <type>javax.help.TOCView</type>
    <data>RamusTOC.xml</data>
  </view>

  <view mergetype="javax.help.SortMerge">
    <name>Index</name>
    <label>Индекс</label>
    <type>javax.help.IndexView</type>
    <data>RamusIndex.xml</data>
  </view>

  <view mergetype="javax.help.SortMerge">
    <name>Search</name>
    <label>Поиск</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch
    </data>
  </view>
  
</helpset>

