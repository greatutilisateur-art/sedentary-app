@echo off
set PYTHONPATH=%~dp0
python -m unittest discover -s "%~dp0tests" -p "test_*.py" -v
pause
