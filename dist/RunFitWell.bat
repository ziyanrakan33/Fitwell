@echo off
cd /d "%~dp0"
set LIB=..\lib
java -cp "FitWell.jar;%LIB%\commons-beanutils-1.9.4.jar;%LIB%\commons-collections-3.2.2.jar;%LIB%\commons-collections4-4.4.jar;%LIB%\commons-digester-2.1.jar;%LIB%\commons-lang3-3.8.1.jar;%LIB%\commons-logging-1.2.jar;%LIB%\hsqldb-2.5.0.jar;%LIB%\jackcess-3.0.1.jar;%LIB%\jasperreports-6.21.0.jar;%LIB%\jasperreports-fonts-6.21.0.jar;%LIB%\jasperreports-functions-6.21.0.jar;%LIB%\openpdf-1.3.32.jar;%LIB%\ucanaccess-5.0.1.jar" fitwell.App
pause
