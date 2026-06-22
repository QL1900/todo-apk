@echo off
echo === TodoHub APK Push Script ===
echo.

REM Check for git
where git >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Git not found. Please install Git for Windows first.
    echo Download: https://git-scm.com/download/win
    pause
    exit /b 1
)

echo [1/3] Initializing git repository...
cd /d "C:\Users\QL\Documents\Codex\todo-apk"
if not exist .git (
    git init
    git add -A
    git commit -m "Initial commit: TodoHub Android app with widgets"
    echo Done.
) else (
    echo Already initialized.
)

echo [2/3] Setting up GitHub remote...
git remote remove origin 2>nul
git remote add origin https://github.com/QL1900/todo-apk.git
echo Remote set to: https://github.com/QL1900/todo-apk.git

echo [3/3] Pushing to GitHub...
git branch -M main
git push -u origin main

echo.
echo === Done! ===
echo Check your Actions at: https://github.com/QL1900/todo-apk/actions
echo.
pause
