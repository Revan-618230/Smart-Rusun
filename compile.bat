@echo off
cd src
javac -cp ".;../lib/mssql-jdbc-13.2.1.jre11.jar" ^
entities/Pemilik.java ^
entities/PerangkatIoT.java ^
entities/UnitSarusun.java ^
ui/LoginPage.java ^
ui/MainMenu.java ^
ui/RoleSelectionPage.java ^
ui/dialogs/ManageIotDialog.java ^
ui/dialogs/ManageOwnersDialog.java ^
ui/dialogs/ViewActivityLogDialog.java ^
ui/dialogs/ViewUnitsDialog.java ^
database/DatabaseAccess.java ^
database/SecurityCheck.java ^
ui/SmartRusunSimulator.java 

if %errorlevel% equ 0 (
    echo Compilation successful!
    java -cp ".;../lib/mssql-jdbc-13.2.1.jre11.jar" ui.SmartRusunSimulator
) else (
    echo Compilation failed!
)
cd ..