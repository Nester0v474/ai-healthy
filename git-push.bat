@echo off
cd /d "C:\Ai Healthy"
set GIT="C:\Program Files\Git\bin\git.exe"
%GIT% add -A
%GIT% commit -m "Update" 2>nul
%GIT% push -u origin main
if %ERRORLEVEL% neq 0 pause
pause
