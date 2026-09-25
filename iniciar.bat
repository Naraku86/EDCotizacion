@echo off
rem Inicia EDCotizacion.
rem   iniciar.bat [carpeta-de-datos] [puerto]
set DATOS=%~1
if "%DATOS%"=="" set DATOS=%USERPROFILE%\EDCotizacion
set PUERTO=%~2
if "%PUERTO%"=="" set PUERTO=8090
java -Dapp.home="%DATOS%" -jar "%~dp0target\edcotizacion.jar" --server.port=%PUERTO%
