Name "Ramus"

SetCompressor /SOLID lzma

# Defines
!define REGKEY "SOFTWARE\$(^Name)"
!define VERSION "2.0.2"
!define COMPANY "Ramus Soft Group"
!define URL http://www.ramussoftware.com/
!define JRE_VERSION "1.6"
!define JRE_URL "http://javadl.sun.com/webapps/download/GetFile/1.6.0_20-b02/windows-i586/jre-6u20-windows-i586.exe"

;!define JRE_URL "http://javadl.sun.com/webapps/download/GetFile/1.6.0_14-b08/windows-i586/jre-6u14-windows-i586.exe"
;!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=18714&/jre-6u5-windows-i586-p.exe"

# MUI defines
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKLM
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_REGISTRY_KEY ${REGKEY}
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME StartMenuGroup
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "Ramus"
!define MUI_FINISHPAGE_RUN_PARAMETERS install
!define MUI_UNFINISHPAGE_NOAUTOCLOSE

!define MUI_ICON "icon.ico"
!define MUI_UNICON "icon.ico"


;Description
!define DESC_SecAppFiles "Application files copy"
 
  ;Header

# Included files
!include Sections.nsh
!include MUI.nsh
!include x64.nsh

# Reserved Files

# Variables
Var StartMenuGroup

Var InstallJRE
Var JREPath
Var javawPath



# Installer pages
!insertmacro MUI_PAGE_WELCOME
!define MUI_LICENSEPAGE_CHECKBOX
# !insertmacro MUI_PAGE_LICENSE "license_en.rtf"
!insertmacro MUI_PAGE_LICENSE "$(MUILicense)"
  Page custom CheckInstalledJRE


!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup
!insertmacro MUI_PAGE_INSTFILES
  !define MUI_INSTFILESPAGE_FINISHHEADER_TEXT "Installation complete"
  !define MUI_PAGE_HEADER_TEXT "Installing"
  #!define MUI_PAGE_HEADER_SUBTEXT "Please wait while ${AppName} is being installed."
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------
;Modern UI Configuration
 
  !define MUI_ABORTWARNING

# Installer languages
!insertmacro MUI_LANGUAGE English
;!insertmacro MUI_LANGUAGE Russian
;!insertmacro MUI_LANGUAGE Ukrainian

; License Language
LicenseLangString MUILicense ${LANG_ENGLISH} "LICENSE"
; LicenseLangString MUILicense ${LANG_RUSSIAN} "license_ru.rtf"
; LicenseLangString MUILicense ${LANG_UKRAINIAN} "license_uk.rtf"
; LicenseData MUILicense

;Language Strings
 
  ReserveFile "jre.ini"
  !insertmacro MUI_RESERVEFILE_INSTALLOPTIONS




# Installer attributes
OutFile full/ramus-2.0.2-setup.exe
InstallDir "$PROGRAMFILES\Ramus"
CRCCheck on
XPStyle on
ShowInstDetails hide
VIProductVersion 2.0.1.0
VIAddVersionKey ProductName "Ramus"
VIAddVersionKey ProductVersion "${VERSION}"
VIAddVersionKey CompanyName "${COMPANY}"
VIAddVersionKey CompanyWebsite "${URL}"
VIAddVersionKey FileVersion "${VERSION}"
VIAddVersionKey FileDescription ""
VIAddVersionKey LegalCopyright ""
InstallDirRegKey HKLM "${REGKEY}" Path
ShowUninstDetails hide
RequestExecutionLevel admin

Section -installjre jre

SectionEnd
 
Section -Main SEC0000



  Push $0
  Push $1
 
;MessageBox MB_OK "${LANG_RUSSIAN}"
  Strcmp $InstallJRE "yes" InstallJRE JREPathStorage
  DetailPrint "Starting the JRE installation"
InstallJRE:
StrCpy $2 "$TEMP\Java Runtime Environment.exe"
        nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
        Pop $R0 ;Get the return value
                StrCmp $R0 "success" +3
                MessageBox MB_OK "Download failed: $R0, please install Java Runtime Environment manually from http://java.com, and try to install Ramus again"
                Quit
ExecWait '"$TEMP\Java Runtime Environment.exe" /s /v\"/qn REBOOT=Suppress JAVAUPDATE=0 WEBSTARTICON=0\"' $0
Delete $2
SetRegView 32
ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
StrCpy $javawPath "$2\bin\javaw.exe"
Goto End
#				 

InstallVerif:
  DetailPrint "Seeking for Java Runtime Environment"
;  ;MessageBox MB_OK "Checking JRE outcome"
  Push "${JRE_VERSION}"
  Call DetectJRE  
  Pop $0	  ; DetectJRE's return value
  StrCmp $0 "0" Detect64 0
  StrCmp $0 "-1" Detect64 0
  Goto JavaExeVerif
  Push "The JRE setup failed"
  Goto ExitInstallJRE

Detect64:
  Push "${JRE_VERSION}"
  Call DetectJRE64  
  Pop $0	  ; DetectJRE's return value
  StrCmp $0 "0" ExitInstallJRE 0
  StrCmp $0 "-1" ExitInstallJRE 0
  Goto JavaExeVerif
  Push "The JRE setup failed"
  Goto ExitInstallJRE
 
JavaExeVerif:
  IfFileExists $0 JREPathStorage 0
  Push "The following file : $0, cannot be found."
  Goto ExitInstallJRE
 
JREPathStorage:
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jre.ini" "UserDefinedSection" "JREPath" $1
  StrCpy $JREPath $0
  
  Goto End
 
ExitInstallJRE:
  Pop $1
  MessageBox MB_OK "Java Runtime Environment not fount, install latest version of Java from http://java.com"
  Pop $1 	; Restore $1
  Pop $0 	; Restore $0
  Abort
End:
  Pop $1	; Restore $1
  Pop $0	; Restore $0


  IfFileExists "$INSTDIR\bin\icon.ico" CloseRamus DoNotCloseRamus
CloseRamus:
  ExecWait '"$javawPath" -jar "$INSTDIR\bin\ramus-startup.jar" \"--close\"' $0
DoNotCloseRamus:
  IfFileExists "$INSTDIR\bin\navigator.ico" CloseNavigator DoNotCloseNavigator
CloseNavigator:
  ExecWait '"$javawPath" -cp "$INSTDIR\bin\ramus-startup.jar" com.ramussoft.Startup com.ramussoft.navigator.ProjectNavigator "--close\"' $0
DoNotCloseNavigator:

RmDir /r $INSTDIR

    SetOutPath "$INSTDIR\bin"
    SetOverwrite on
    File /r full\bin\*
    
    SetOutPath "$INSTDIR\doc"
    SetOverwrite on
    File /r doc\*

    SetOutPath "$INSTDIR\lib"
    SetOverwrite on
    File /r full\lib\*


    #SetOutPath $TEMP\ramus-libs

  # ExecWait '"$javawPath" -classpath "$INSTDIR\bin\ramus-startup.jar" "com.ramussoft.pack200.Unpack" "$TEMP\ramus-libs" "$INSTDIR\lib"'
  # RmDir /r $TEMP\ramus-libs
  
    WriteRegStr HKLM "${REGKEY}\Components" Main 1

    WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    SetOutPath $INSTDIR
    WriteUninstaller $INSTDIR\uninstall.exe
    #!insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    #SetOutPath $SMPROGRAMS\$StartMenuGroup
    #CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk" $INSTDIR\uninstall.exe
    #!insertmacro MUI_STARTMENU_WRITE_END

    SetOutPath $SMPROGRAMS\$StartMenuGroup
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Ramus.lnk" "$javawPath" '-jar "$INSTDIR\bin\ramus-startup.jar" --close-startup' "$INSTDIR\bin\icon.ico" "0"
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Ramus Web Navigator.lnk" "$javawPath" '-cp "$INSTDIR\bin\ramus-startup.jar" com.ramussoft.Startup --close-startup com.ramussoft.navigator.ProjectNavigator' "$INSTDIR\bin\navigator.ico" "0"


;IntCmp $LANGUAGE ${LANG_UKRAINIAN} 0 nukr nukr
;    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Инструкция пользователя.lnk" '"$INSTDIR\doc\ru\Инструкция пользователя.pdf"'
;    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Технология JSSP.lnk" '"$INSTDIR\doc\ru\Технология JSSP.pdf"'
;nukr:
;IntCmp $LANGUAGE ${LANG_RUSSIAN} 0 nrus nrus
;    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Инструкция пользователя.lnk" '"$INSTDIR\doc\ru\Инструкция пользователя.pdf"'
;    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Технология JSSP.lnk" '"$INSTDIR\doc\ru\Технология JSSP.pdf"'
;nrus:


    WriteRegStr HKLM "Software\Classes\Ramusfile0" "" '"Ramus File"'
    WriteRegStr HKLM "Software\Classes\Ramusfile0\shell\open\command" "" 'javaw -jar "$INSTDIR\bin\ramus-startup.jar" --close-startup "%1"'
    WriteRegStr HKLM "Software\Classes\Ramusfile0\DefaultIcon" "" "$INSTDIR\bin\icon.ico"
    WriteRegStr HKLM "Software\Classes\.rsf" "" "RamusFile0"
    
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayVersion "${VERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" Publisher "${COMPANY}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" URLInfoAbout "${URL}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\uninstall.exe
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
 
SectionEnd
 
;--------------------------------
;Descriptions
 
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecAppFiles} $(DESC_SecAppFiles)
!insertmacro MUI_FUNCTION_DESCRIPTION_END
 
;--------------------------------
;Installer Functions
 
Function .onInit
 
  ;Extract InstallOptions INI Files
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "jre.ini"
  Call SetupSections
 
FunctionEnd
 
Function myPreInstfiles
 
  Call RestoreSections
  SetAutoClose true
 
FunctionEnd
 
Function CheckInstalledJRE
  ;MessageBox MB_OK "Checking Installed JRE Version"
  Push "${JRE_VERSION}"
  Call DetectJRE
  ;MessageBox MB_OK "Done checking JRE version"
  Exch $0	; Get return value from stack
  StrCmp $0 "0" DetectJRE64
  StrCmp $0 "-1" FoundOld
  Goto JREAlreadyInstalled
DetectJRE64:
  Push "${JRE_VERSION}"
  Call DetectJRE64
  ;MessageBox MB_OK "Done checking JRE version"
  Exch $0	; Get return value from stack
  StrCmp $0 "0" NoFound
  StrCmp $0 "-1" FoundOld
  Goto JREAlreadyInstalled
 
FoundOld:
  ;MessageBox MB_OK "Old JRE found"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jre.ini" "Field 1" "Text" "${AppName} requires a more recent version of the Java Runtime Environment than the one found on your computer."
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jre.ini" "Field 2" "Text" "The installation of JRE ${JRE_VERSION} will start. The installation will download 16 Mb."
  !insertmacro MUI_HEADER_TEXT "Java Runtime Environment" "Java Runtime Environment instalation."
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY_RETURN "jre.ini"
  Goto MustInstallJRE
 
NoFound:
  ;MessageBox MB_OK "JRE not found"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jre.ini" "Field 1" "Text" "Java Runtime Environment not fount."
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jre.ini" "Field 2" "Text" "The installation of JRE ${JRE_VERSION} will start. The installation will download 16 Mb."
  !insertmacro MUI_HEADER_TEXT "Java Runtime Environment" "Java Runtime Environment instalation."
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY_RETURN "jre.ini"
  Goto MustInstallJRE
 
MustInstallJRE:
  Exch $0	; $0 now has the installoptions page return value
  ; Do something with return value here
  Pop $0	; Restore $0
  StrCpy $InstallJRE "yes"
  Return
 
JREAlreadyInstalled:
;  ;MessageBox MB_OK "No download: ${TEMP2}"
  ;MessageBox MB_OK "JRE already installed"
  StrCpy $InstallJRE "no"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jre.ini" "UserDefinedSection" "JREPath" $JREPATH
  Pop $0		; Restore $0
  Return
 
FunctionEnd
 
; Returns: 0 - JRE not found. -1 - JRE found but too old. Otherwise - Path to JAVA EXE
 
; DetectJRE. Version requested is on the stack.
; Returns (on stack)	"0" on failure (java too old or not installed), otherwise path to java interpreter
; Stack value will be overwritten!
 
Function DetectJRE64
  SetRegView 64

  Exch $0	; Get version requested  
		; Now the previous value of $0 is on the stack, and the asked for version of JDK is in $0
  Push $1	; $1 = Java version string (ie 1.5.0)
  Push $2	; $2 = Javahome
  Push $3	; $3 and $4 are used for checking the major/minor version of java
  Push $4
  ;MessageBox MB_OK "Detecting JRE"
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ;MessageBox MB_OK "Read : $1"
  StrCmp $1 "" DetectTry2
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  ;MessageBox MB_OK "Read 3: $2"
  StrCmp $2 "" DetectTry2
  Goto GetJRE
 
DetectTry2:
  SetRegView 64

  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  ;MessageBox MB_OK "Detect Read : $1"
  StrCmp $1 "" NoFound
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  ;MessageBox MB_OK "Detect Read 3: $2"
  StrCmp $2 "" NoFound
 
GetJRE:
; $0 = version requested. $1 = version found. $2 = javaHome
  ;MessageBox MB_OK "Getting JRE"
  IfFileExists "$2\bin\javaw.exe" 0 NoFound
  StrCpy $javawPath "$2\bin\javaw.exe"
  StrCpy $3 $0 1			; Get major version. Example: $1 = 1.5.0, now $3 = 1
  StrCpy $4 $1 1			; $3 = major version requested, $4 = major version found
  ;MessageBox MB_OK "Want $3 , found $4"
  IntCmp $4 $3 0 FoundOld FoundNew
  StrCpy $3 $0 1 2
  StrCpy $4 $1 1 2			; Same as above. $3 is minor version requested, $4 is minor version installed
  ;MessageBox MB_OK "Want $3 , found $4" 
  IntCmp $4 $3 FoundNew FoundOld FoundNew
 
NoFound:
  ;MessageBox MB_OK "JRE not found"
  Push "0"
  Goto DetectJREEnd
 
FoundOld:
  ;MessageBox MB_OK "JRE too old: $3 is older than $4"
;  Push ${TEMP2}
  Push "-1"
  Goto DetectJREEnd  
FoundNew:
  ;MessageBox MB_OK "JRE is new: $3 is newer than $4"
 
  Push "$2\bin\javaw.exe"
;  Push "OK"
;  Return
   Goto DetectJREEnd
DetectJREEnd:
	; Top of stack is return value, then r4,r3,r2,r1
	Exch	; => r4,rv,r3,r2,r1,r0
	Pop $4	; => rv,r3,r2,r1r,r0
	Exch	; => r3,rv,r2,r1,r0
	Pop $3	; => rv,r2,r1,r0
	Exch 	; => r2,rv,r1,r0
	Pop $2	; => rv,r1,r0
	Exch	; => r1,rv,r0
	Pop $1	; => rv,r0
	Exch	; => r0,rv
	Pop $0	; => rv 
FunctionEnd

Function DetectJRE

  Exch $0	; Get version requested  
		; Now the previous value of $0 is on the stack, and the asked for version of JDK is in $0
  Push $1	; $1 = Java version string (ie 1.5.0)
  Push $2	; $2 = Javahome
  Push $3	; $3 and $4 are used for checking the major/minor version of java
  Push $4
  ;MessageBox MB_OK "Detecting JRE"
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ;MessageBox MB_OK "Read : $1"
  StrCmp $1 "" DetectTry2
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  ;MessageBox MB_OK "Read 3: $2"
  StrCmp $2 "" DetectTry2
  Goto GetJRE
 
DetectTry2:
  SetRegView 64

  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  ;MessageBox MB_OK "Detect Read : $1"
  StrCmp $1 "" NoFound
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  ;MessageBox MB_OK "Detect Read 3: $2"
  StrCmp $2 "" NoFound
 
GetJRE:
; $0 = version requested. $1 = version found. $2 = javaHome
  ;MessageBox MB_OK "Getting JRE"
  IfFileExists "$2\bin\javaw.exe" 0 NoFound
  StrCpy $javawPath "$2\bin\javaw.exe"
  StrCpy $3 $0 1			; Get major version. Example: $1 = 1.5.0, now $3 = 1
  StrCpy $4 $1 1			; $3 = major version requested, $4 = major version found
  ;MessageBox MB_OK "Want $3 , found $4"
  IntCmp $4 $3 0 FoundOld FoundNew
  StrCpy $3 $0 1 2
  StrCpy $4 $1 1 2			; Same as above. $3 is minor version requested, $4 is minor version installed
  ;MessageBox MB_OK "Want $3 , found $4" 
  IntCmp $4 $3 FoundNew FoundOld FoundNew
 
NoFound:
  ;MessageBox MB_OK "JRE not found"
  Push "0"
  Goto DetectJREEnd
 
FoundOld:
  ;MessageBox MB_OK "JRE too old: $3 is older than $4"
;  Push ${TEMP2}
  Push "-1"
  Goto DetectJREEnd  
FoundNew:
  ;MessageBox MB_OK "JRE is new: $3 is newer than $4"
 
  Push "$2\bin\javaw.exe"
;  Push "OK"
;  Return
   Goto DetectJREEnd
DetectJREEnd:
	; Top of stack is return value, then r4,r3,r2,r1
	Exch	; => r4,rv,r3,r2,r1,r0
	Pop $4	; => rv,r3,r2,r1r,r0
	Exch	; => r3,rv,r2,r1,r0
	Pop $3	; => rv,r2,r1,r0
	Exch 	; => r2,rv,r1,r0
	Pop $2	; => rv,r1,r0
	Exch	; => r1,rv,r0
	Pop $1	; => rv,r0
	Exch	; => r0,rv
	Pop $0	; => rv 
FunctionEnd
 
Function RestoreSections
  !insertmacro UnselectSection ${jre}
  !insertmacro SelectSection ${SecAppFiles}
  !insertmacro SelectSection ${SecCreateShortcut}
 
FunctionEnd
 
Function SetupSections
  !insertmacro SelectSection ${jre}
  !insertmacro UnselectSection ${SecAppFiles}
  !insertmacro UnselectSection ${SecCreateShortcut}
FunctionEnd
 
;--------------------------------
;Uninstaller Section
 
Section "Uninstall"
    ExecWait 'javaw -jar "$INSTDIR\bin\ramus-startup.jar" "--close\"' $0
    ExecWait 'javaw -cp "$INSTDIR\bin\ramus-startup.jar" com.ramussoft.Startup com.ramussoft.navigator.ProjectNavigator --close' $0
    RmDir /r /REBOOTOK $INSTDIR
    DeleteRegValue HKLM "${REGKEY}\Components" Main
    RmDir /r "$SMPROGRAMS\Ramus"
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
    #Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk"
    Delete /REBOOTOK $INSTDIR\uninstall.exe
    DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
    DeleteRegValue HKLM "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    DeleteRegKey HKLM "Software\Classes\RamusFile0"
    DeleteRegKey HKLM "Software\Classes\.rsf"
    #RmDir /r /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    RmDir /REBOOTOK $INSTDIR
 
SectionEnd
